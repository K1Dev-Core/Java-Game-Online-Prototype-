import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Game extends JPanel implements Runnable {
    private final Player player;
    private final InputHandler inputHandler;
    private final Timer gameTimer;
    private final ParticleManager particleManager;
    private BufferedImage mapBackground;
    
    public Game(int skinId) {
        setPreferredSize(new Dimension(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        setFocusable(true);
        
        try {
            mapBackground = ImageIO.read(new File("assets/bg/map_bg.png"));
        } catch (IOException e) {
            e.printStackTrace();
            setBackground(Color.DARK_GRAY);
        }
        
        player = new Player(Config.WINDOW_WIDTH / 2, Config.WINDOW_HEIGHT / 2, skinId);
        inputHandler = new InputHandler(this);
        particleManager = new ParticleManager();
        
        inputHandler.addListeners();
        
        gameTimer = new Timer(1000 / Config.FPS, e -> {
            update();
            repaint();
        });
        
        new Thread(this).start();
    }
    
    @Override
    public void run() {
        gameTimer.start();
    }
    
    private void update() {
        player.update(inputHandler, particleManager);
        particleManager.update();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (mapBackground != null) {
            g2d.drawImage(mapBackground, 0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT, null);
        }
        
        player.render(g2d);
        particleManager.render(g2d);
        renderCooldownBar(g2d);
    }
    
    private void renderCooldownBar(Graphics2D g2d) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttack = currentTime - player.getLastAttackTime();
        
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameLauncher.main(args);
        });
    }
}