import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private int port;
    private boolean running;
    private ServerSocket serverSocket;
    private HashMap<Integer, PlayerData> players;
    private HashMap<Integer, ClientHandler> connectedClients;
    private HashMap<String, Long> recentConnections;
    private int nextPlayerId = 1;
    
    public static class PlayerData {
        public int id;
        public double x, y;
        public double angle;
        public boolean isAttacking;
        public boolean isDashing;
        public int skinId;
        public String name;
        public int health;
        public int killCount;
        
        public PlayerData(int id, double x, double y, int skinId, String name) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.angle = 0;
            this.isAttacking = false;
            this.isDashing = false;
            this.skinId = skinId;
            this.name = name;
            this.health = Config.MAX_HEALTH;
            this.killCount = 0;
        }
        
        @Override
        public String toString() {
            System.out.println("debug: " + String.format("%d:%d:%.1f:%.1f:%.1f:%s:%d:%s:%d:%d:%s", 
                id, skinId, x, y, angle, isAttacking ? "1" : "0", lastAttackTime, name, health, killCount, isDashing ? "1" : "0"));
            return String.format("%d:%d:%.1f:%.1f:%.1f:%s:%d:%s:%d:%d:%s", 
                id, skinId, x, y, angle, isAttacking ? "1" : "0", lastAttackTime, name, health, killCount, isDashing ? "1" : "0");
        }
        
        public long lastAttackTime = 0;
    }
    
    public GameServer(int port) {
        this.port = port;
        this.players = new HashMap<>();
        this.connectedClients = new HashMap<>();
        this.recentConnections = new HashMap<>();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Server started on port " + port);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                connectedClients.put(nextPlayerId, handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private PlayerData playerData;
        private int playerId;
        private long lastBroadcastTime = 0;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        
        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                String playerInfo = in.readLine();
                String[] parts = playerInfo.split(":");
                if (parts.length >= 3) {
                    int skinId = Integer.parseInt(parts[0]);
                    String playerName = parts.length > 2 ? parts[2] : "Player";
                    
                    String clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + playerName;
                    long currentTime = System.currentTimeMillis();
                    
                    synchronized(recentConnections) {
                        if (recentConnections.containsKey(clientKey)) {
                            long lastConnection = recentConnections.get(clientKey);
                            if (currentTime - lastConnection < 5000) {
                                System.out.println("Rejecting duplicate connection from " + clientKey);
                                out.println("DUPLICATE");
                                return;
                            }
                        }
                        recentConnections.put(clientKey, currentTime);
                    }
                    
                    playerId = nextPlayerId++;
                    playerData = new PlayerData(playerId, 
                        Config.MAP_WIDTH / 2,
                        Config.MAP_HEIGHT / 2,
                        skinId, playerName);
                    
                    synchronized(players) {
                        players.put(playerData.id, playerData);
                        connectedClients.put(playerId, this);
                        
                        out.println(playerData.id);
                        broadcastPlayers();
                    }
                }
                
                String inputLine;
                while ((inputLine = in.readLine()) != null && !clientSocket.isClosed()) {
                    handleClientMessage(inputLine);
                }
                
            } catch (IOException e) {
                System.out.println("Client disconnected: " + (playerData != null ? playerData.name : "Unknown"));
            } finally {
                synchronized(players) {
                    if (playerData != null) {
                        System.out.println("Removing player " + playerData.name + " (ID: " + playerData.id + ") from game");
                        players.remove(playerData.id);
                        broadcastPlayers();
                        System.out.println("Players remaining: " + players.size());
                    }
                }
                synchronized(connectedClients) {
                    connectedClients.remove(playerId);
                }
                try {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        
        private void handleClientMessage(String message) {
            if (message.startsWith("ATTACK:")) {
                handleAttackEvent(message);
            } else {
                String[] parts = message.split(":");
                if (parts.length >= 7 && playerData != null) {
                    try {
                        playerData.x = Double.parseDouble(parts[0]);
                        playerData.y = Double.parseDouble(parts[1]);
                        playerData.angle = Double.parseDouble(parts[2]);
                        playerData.isAttacking = "1".equals(parts[3]);
                        playerData.lastAttackTime = Long.parseLong(parts[4]);
                        playerData.health = Integer.parseInt(parts[5]);
                        
                        if (parts.length >= 7) {
                            playerData.killCount = Integer.parseInt(parts[6]);
                        }
                        
                        if (parts.length >= 8) {
                            playerData.isDashing = "1".equals(parts[7]);
                        }
                        
                        checkCombatAndUpdateKills();
                        
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastBroadcastTime > 50) {
                            broadcastPlayers();
                            lastBroadcastTime = currentTime;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        private void handleAttackEvent(String message) {
            String[] parts = message.split(":");
            if (parts.length >= 5) {
                try {
                    String attackData = String.format("ATTACK:%s:%s:%s:%s", parts[1], parts[2], parts[3], parts[4]);
                    broadcastAttackEvent(attackData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void broadcastAttackEvent(String attackData) {
            for (java.util.Map.Entry<Integer, GameServer.ClientHandler> entry : connectedClients.entrySet()) {
                GameServer.ClientHandler handler = entry.getValue();
                try {
                    if (handler != null && handler.out != null) {
                        handler.out.println(attackData);
                    }
                } catch (Exception e) {
                }
            }
        }
        
        private void checkCombatAndUpdateKills() {
            synchronized(players) {
                long currentTime = System.currentTimeMillis();
                boolean hasChange = false;
                
                for (PlayerData attacker : players.values()) {
                    if (attacker.isAttacking && currentTime - attacker.lastAttackTime < Config.ATTACK_COOLDOWN) {
                        double attackX = attacker.x + Math.cos(attacker.angle) * Config.ATTACK_RANGE;
                        double attackY = attacker.y + Math.sin(attacker.angle) * Config.ATTACK_RANGE;
                        
                        for (PlayerData target : players.values()) {
                            if (attacker.id != target.id && target.health > 0) {
                                double distance = Math.sqrt((attackX - target.x) * (attackX - target.x) + 
                                                           (attackY - target.y) * (attackY - target.y));
                                
                                if (distance < Config.PLAYER_SIZE) {
                                    target.health -= Config.ATTACK_DAMAGE;
                                    if (target.health <= 0) {
                                        target.health = 0;
                                        attacker.killCount++;
                                        attacker.health = Config.MAX_HEALTH;
                                    }
                                    hasChange = true;
                                }
                            }
                        }
                    }
                }
                
            }
        }
        
        private void broadcastPlayers() {
            synchronized(players) {
                StringBuilder playerList = new StringBuilder();
                for (PlayerData player : players.values()) {
                    if (playerList.length() > 0) {
                        playerList.append(";");
                    }
                    playerList.append(player.toString());
                }
                
                String playerData = "PLAYERS:" + playerList.toString();
                
                for (java.util.Map.Entry<Integer, GameServer.ClientHandler> entry : connectedClients.entrySet()) {
                    GameServer.ClientHandler handler = entry.getValue();
                    try {
                        if (handler != null && handler.out != null) {
                            handler.out.println(playerData);
                        }
                    } catch (Exception e) {
                        }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer(Config.SERVER_PORT);
        server.start();
    }
}
