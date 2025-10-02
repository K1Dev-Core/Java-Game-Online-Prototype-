import java.io.*;
import java.net.*;
import java.util.*;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected;
    private ClientListener listener;
    private int playerId;
    
    public interface ClientListener {
        void onPlayerUpdate(String playerData);
        void onPlayersReceived(List<GameServer.PlayerData> players);
        void onAttackEvent(String attackData);
        void onConnectionLost();
    }
    
    public NetworkClient(ClientListener listener) {
        this.listener = listener;
        this.connected = false;
    }
    
    public boolean connect(String serverIP, int port, int skinId, String playerName) {
        try {
            socket = new Socket(serverIP, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            out.println(skinId + ":1:" + playerName);
            
            String playerIdStr = in.readLine();
            if (playerIdStr != null && !playerIdStr.startsWith("PLAYERS:")) {
                if ("DUPLICATE".equals(playerIdStr)) {
                    System.out.println("Duplicate connection rejected");
                    return false;
                }
                playerId = Integer.parseInt(playerIdStr);
                connected = true;
                new Thread(this::listenForMessages).start();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void listenForMessages() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null && connected) {
                if (inputLine.startsWith("PLAYERS:")) {
                    String playersData = inputLine.substring(8);
                    List<GameServer.PlayerData> players = parsePlayersData(playersData);
                    if (listener != null) {
                        listener.onPlayersReceived(players);
                    }
                } else if (inputLine.startsWith("ATTACK:")) {
                    if (listener != null) {
                        listener.onAttackEvent(inputLine);
                    }
                } else {
                    if (listener != null) {
                        listener.onPlayerUpdate(inputLine);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        connected = false;
        if (listener != null) {
            listener.onConnectionLost();
        }
    }
    
    private List<GameServer.PlayerData> parsePlayersData(String data) {
        List<GameServer.PlayerData> players = new ArrayList<>();
        if (data.isEmpty()) return players;
        
        String[] playerStrings = data.split(";");
        for (String playerStr : playerStrings) {
            try {
                String[] parts = playerStr.split(":");
                if (parts.length >= 11) {
                    int id = Integer.parseInt(parts[0]);
                    int skinId = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    double angle = Double.parseDouble(parts[4]);
                    boolean isAttacking = "1".equals(parts[5]);
                    long lastAttackTime = Long.parseLong(parts[6]);
                    String name = parts[7];
                    int health = Integer.parseInt(parts[8]);
                    int killCount = Integer.parseInt(parts[9]);
                    boolean isDashing = "1".equals(parts[10]);
                    
                    GameServer.PlayerData playerData = new GameServer.PlayerData(id, x, y, skinId, name);
                    playerData.angle = angle;
                    playerData.isAttacking = isAttacking;
                    playerData.lastAttackTime = lastAttackTime;
                    playerData.health = health;
                    playerData.killCount = killCount;
                    playerData.isDashing = isDashing;
                    
                    players.add(playerData);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return players;
    }
    
    public void sendPlayerData(double x, double y, double angle, boolean isAttacking, long lastAttackTime, int health, int killCount, boolean isDashing) {
        if (connected && out != null) {
            out.println(String.format("%.1f:%.1f:%.1f:%s:%d:%d:%d:%s", 
                x, y, angle, isAttacking ? "1" : "0", lastAttackTime, health, killCount, isDashing ? "1" : "0"));
        }
    }
    
    public void sendAttackEvent(double x, double y, double angle, int skinId) {
        if (connected && out != null) {
            out.println(String.format("ATTACK:%.1f:%.1f:%.1f:%d", x, y, angle, skinId));
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getPlayerId() {
        return playerId;
    }
}
