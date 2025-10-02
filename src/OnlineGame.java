import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class OnlineGame extends JPanel implements Runnable, NetworkClient.ClientListener {
    private final Player localPlayer;
    private final InputHandler inputHandler;
    private final javax.swing.Timer gameTimer;
    private final ParticleManager particleManager;
    private NetworkClient networkClient;
    private Map<Integer, RemotePlayer> remotePlayers;
    private BufferedImage mapBackground;
    private String playerName;
    private javax.swing.Timer networkTimer;
    
    public OnlineGame(int skinId, String playerName) {
        this.playerName = playerName;
        setPreferredSize(new Dimension(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        setFocusable(true);
        
        try {
            mapBackground = ImageIO.read(new File("assets/bg/map_bg.png"));
        } catch (IOException e) {
            e.printStackTrace();
            setBackground(Color.DARK_GRAY);
        }
        
        localPlayer = new Player(Config.WINDOW_WIDTH / 2, Config.WINDOW_HEIGHT / 2, skinId, playerName, 
            (x, y, angle, attackSkinId) -> {
                if (networkClient != null && networkClient.isConnected()) {
                    networkClient.sendAttackEvent(x, y, angle, attackSkinId);
                }
            });
        inputHandler = new InputHandler(this);
        particleManager = new ParticleManager();
        remotePlayers = new HashMap<>();
        
        inputHandler.addListeners();
        
        gameTimer = new javax.swing.Timer(1000 / Config.FPS, e -> {
            update();
            repaint();
        });
        
        
        startNetworkClient(skinId);
        new Thread(this).start();
    }
    
    private void startNetworkClient(int skinId) {
        networkClient = new NetworkClient(this);
        
        String serverIP = Config.SERVER_IP;
        if (connectToServer(serverIP, Config.SERVER_PORT, skinId)) {
            networkTimer = new javax.swing.Timer(1000 / Config.NETWORK_UPDATE_RATE, e -> {
                if (networkClient.isConnected()) {
                    networkClient.sendPlayerData(
                        localPlayer.getX(),
                        localPlayer.getY(),
                        localPlayer.getAngle(),
                        localPlayer.isAttacking(),
                        localPlayer.getLastAttackTime(),
                        localPlayer.getHealth(),
                        localPlayer.getKillCount(),
                        localPlayer.isDashing()
                    );
                }
            });
            networkTimer.start();
        }
    }
    
    private boolean connectToServer(String serverIP, int port, int skinId) {
        for (int attempts = 0; attempts < 3; attempts++) {
            if (networkClient.connect(serverIP, port, skinId, playerName)) {
                return true;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Cannot connect to server. Please try again.");
        });
        return false;
    }
    
    @Override
    public void run() {
        gameTimer.start();
    }
    
    private void update() {
        if (localPlayer.isDead()) {
            respawnPlayer();
        } else {
            localPlayer.update(inputHandler, particleManager);
        }
        
        particleManager.update();
        checkCombat();
        
        remotePlayers.values().removeIf(RemotePlayer::isTimedOut);
    }
    
    private void respawnPlayer() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - localPlayer.getDeathTime() >= Config.RESPAWN_TIME) {
            localPlayer.revive();
            localPlayer.setPosition(
                Config.MAP_WIDTH / 2,
                Config.MAP_HEIGHT / 2
            );
        }
    }
    
    
    
    private void checkCombat() {
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            if (remotePlayer.isAttacking()) {
                remotePlayer.particleManager.spawnParticle(remotePlayer.getX(), remotePlayer.getY(), remotePlayer.getAngle(), remotePlayer.getSkinId());
            }
        }
    }
    
    private void applyKnockbackToLocal() {
        RemotePlayer attacker = findClosestAttacker();
        if (attacker != null) {
            localPlayer.applyKnockbackFrom(attacker.getX(), attacker.getY(), Config.KNOCKBACK_FORCE);
        }
    }
    
    private RemotePlayer findClosestAttacker() {
        RemotePlayer closestAttacker = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (RemotePlayer player : remotePlayers.values()) {
            if (player.isAttacking()) {
                double distance = Math.sqrt(
                    Math.pow(player.getX() - localPlayer.getX(), 2) +
                    Math.pow(player.getY() - localPlayer.getY(), 2)
                );
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestAttacker = player;
                }
            }
        }
        
        return closestAttacker;
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (mapBackground != null) {
            g2d.drawImage(mapBackground, 0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, null);
        }
        
        localPlayer.render(g2d);
        
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            remotePlayer.interpolateMove();
            remotePlayer.render(g2d);
            if (remotePlayer.particleManager != null && remotePlayer.getHealth() > 0) {
                remotePlayer.particleManager.update();
                remotePlayer.particleManager.render(g2d);
            }
        }
        
        particleManager.render(g2d);
        
        if (!localPlayer.isDead()) {
            renderCooldownBar(g2d);
            renderScoreboard(g2d);
        }
        
        localPlayer.renderDeathOverlay(g2d);
    }
    
    private void renderCooldownBar(Graphics2D g2d) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttack = currentTime - localPlayer.getLastAttackTime();
        
        if (timeSinceLastAttack >= Config.ATTACK_COOLDOWN) {
            return;
        }
        
        double cooldownProgress = Math.min(1.0, (double) timeSinceLastAttack / Config.ATTACK_COOLDOWN);
        
        g2d.setColor(Color.BLACK);
        g2d.fillRect(Config.COOLDOWN_BAR_X, Config.COOLDOWN_BAR_Y, Config.COOLDOWN_BAR_WIDTH, Config.COOLDOWN_BAR_HEIGHT);
        
        g2d.setColor(Color.WHITE);
        int fillWidth = (int) (Config.COOLDOWN_BAR_WIDTH * (1.0 - cooldownProgress));
        g2d.fillRect(Config.COOLDOWN_BAR_X, Config.COOLDOWN_BAR_Y, fillWidth, Config.COOLDOWN_BAR_HEIGHT);
    }
    
    private void renderScoreboard(Graphics2D g2d) {
       
        
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("SCOREBOARD", 20, 35);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        
        int yPos = 60;
        g2d.drawString("Players", 20, yPos);
        yPos += 20;
        
        List<Map.Entry<String, Integer>> allPlayers = new ArrayList<>();
        allPlayers.add(new AbstractMap.SimpleEntry<>(localPlayer.getPlayerName(), localPlayer.getKillCount()));
        
        for (RemotePlayer player : remotePlayers.values()) {
            allPlayers.add(new AbstractMap.SimpleEntry<>(player.getName(), player.getKillCount()));
        }
        
        allPlayers.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        for (Map.Entry<String, Integer> entry : allPlayers) {
            if (yPos > 180) break;
            if (entry.getKey().equals(localPlayer.getPlayerName())) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.drawString(String.format("%-3d - %s", entry.getValue(), entry.getKey()), 25, yPos);
            yPos += 15;
        }
        
        if (!networkClient.isConnected()) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("DISCONNECTED", 20, 190);
        }
    }
    
    @Override
    public void onPlayerUpdate(String playerData) {
        
    }
    
    @Override
    public void onAttackEvent(String attackData) {
        String[] parts = attackData.split(":");
        if (parts.length >= 5) {
            try {
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double angle = Double.parseDouble(parts[3]);
                int skinId = Integer.parseInt(parts[4]);
                
                particleManager.spawnParticle(x, y, angle, skinId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onPlayersReceived(java.util.List<GameServer.PlayerData> players) {
        Set<Integer> activePlayerIds = new HashSet<>();
        for (GameServer.PlayerData playerData : players) {
            activePlayerIds.add(playerData.id);
            if (playerData.id != networkClient.getPlayerId()) {
                RemotePlayer remotePlayer = remotePlayers.get(playerData.id);
                if (remotePlayer == null) {
                    remotePlayer = new RemotePlayer(playerData.id, playerData.x, playerData.y, playerData.skinId, playerData.name);
                    remotePlayers.put(playerData.id, remotePlayer);
                } else {
                    remotePlayer.update(playerData.x, playerData.y, playerData.angle, playerData.isAttacking, playerData.isDashing);
                    remotePlayer.setHealth(playerData.health);
                    remotePlayer.setKillCount(playerData.killCount);
                    
                    if (remotePlayer.isDead() && playerData.health > 0) {
                        remotePlayer.revive();
                    }
                }
            } else {
                int oldHealth = localPlayer.getHealth();
                int oldKillCount = localPlayer.getKillCount();
                
                localPlayer.heal(0);
                int currentHealth = localPlayer.getHealth();
                
                if (playerData.health != currentHealth && playerData.health < oldHealth) {
                    if (playerData.health == 0 && oldHealth > 0) {
                        localPlayer.takeDamage(oldHealth);
                        applyKnockbackToLocal();
                    } else if (playerData.health < oldHealth) {
                        int damage = oldHealth - playerData.health;
                        localPlayer.takeDamage(damage);
                        applyKnockbackToLocal();
                    }
                }
                
                if (playerData.killCount > oldKillCount) {
                    localPlayer.addKill();
                }
            }
        }
        
        remotePlayers.entrySet().removeIf(entry -> !activePlayerIds.contains(entry.getKey()));
    }
    
    @Override
    public void onConnectionLost() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Connection lost");
        });
    }
    
    public void dispose() {
        if (networkClient != null) {
            networkClient.disconnect();
        }
        if (networkTimer != null) {
            networkTimer.stop();
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
}
