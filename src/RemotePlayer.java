import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class RemotePlayer {
    private final int id;
    private double x, y;
    private double angle;
    private Animation walkAnimation;
    private Animation attackAnimation;
    private boolean isAttacking;
    private boolean isMoving;
    private double bobOffset;
    private int skinId;
    private String name;
    private long lastSeenTime;
    private int health = Config.MAX_HEALTH;
    private int killCount = 0;
    
    private double targetX, targetY, targetAngle;
    private double moveSpeed = 0.15;
    private long attackEndTime = 0;
    private long attackRecoveryDelay = 300;
    private double knockbackVelX = 0;
    private double knockbackVelY = 0;
    private long knockbackStartTime = 0;
    public ParticleManager particleManager;
    private Clip damagedClip;
    private Clip deathClip;
    private boolean isDashing = false;
    private double dashVelX = 0;
    private double dashVelY = 0;
    private long dashStartTime = 0;
    private long lastDashTime = 0;
    private long deathTime = 0;
    
    public RemotePlayer(int id, double x, double y, int skinId, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.isAttacking = false;
        this.isMoving = false;
        this.skinId = skinId;
        this.name = name;
        this.lastSeenTime = System.currentTimeMillis();
        this.targetX = x;
        this.targetY = y;
        this.targetAngle = 0;
        
        loadAnimations();
    }
    
    private void loadAnimations() {
        try {
            String skinPath = skinId == 1 ? "assets/player/skin_1/" : "assets/player/skin_2/";
            
            BufferedImage[] walkFrames = new BufferedImage[1];
            walkFrames[0] = ImageIO.read(new File(skinPath + "skins-sheet2-0.png"));
            walkAnimation = new Animation(walkFrames, Config.WALK_ANIMATION_DELAY);
            
            BufferedImage[] attackFrames;
            if (skinId == 1) {
                attackFrames = new BufferedImage[6];
                attackFrames[0] = ImageIO.read(new File(skinPath + "skins-sheet2-0.png"));
                attackFrames[1] = ImageIO.read(new File(skinPath + "skins-sheet2-1.png"));
                attackFrames[2] = ImageIO.read(new File(skinPath + "skins-sheet2-2.png"));
                attackFrames[3] = ImageIO.read(new File(skinPath + "skins-sheet2-3.png"));
                attackFrames[4] = ImageIO.read(new File(skinPath + "skins-sheet2-3.png"));
                attackFrames[5] = ImageIO.read(new File(skinPath + "skins-sheet2-3.png"));
            } else {
                attackFrames = new BufferedImage[11];
                attackFrames[0] = ImageIO.read(new File(skinPath + "skins-sheet2-0.png"));
                attackFrames[1] = ImageIO.read(new File(skinPath + "skins-sheet2-1.png"));
                attackFrames[2] = ImageIO.read(new File(skinPath + "skins-sheet2-2.png"));
                attackFrames[3] = ImageIO.read(new File(skinPath + "skins-sheet2-3.png"));
                attackFrames[4] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
                attackFrames[5] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
                attackFrames[6] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
                attackFrames[7] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
                attackFrames[8] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
                attackFrames[9] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
                attackFrames[10] = ImageIO.read(new File(skinPath + "skins-sheet2-4.png"));
            }
            
            attackAnimation = new Animation(attackFrames, Config.ATTACK_ANIMATION_DELAY);
            
            particleManager = new ParticleManager();
            
            try {
                AudioInputStream damagedStream = AudioSystem.getAudioInputStream(new File("assets/sfx/damaged.wav"));
                damagedClip = AudioSystem.getClip();
                damagedClip.open(damagedStream);
                
                AudioInputStream deathStream = AudioSystem.getAudioInputStream(new File("assets/sfx/death.wav"));
                deathClip = AudioSystem.getClip();
                deathClip.open(deathStream);
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void update(double newX, double newY, double newAngle, boolean isAttacking, boolean isDashing) {
        targetX = newX;
        targetY = newY;
        targetAngle = newAngle;
        this.isAttacking = isAttacking;
        this.lastSeenTime = System.currentTimeMillis();
        
        if (isAttacking && !this.isAttacking) {
            attackAnimation.reset();
        }
        
 
        if (isDashing && !this.isDashing) {
            this.isDashing = true;
            this.dashStartTime = System.currentTimeMillis();
            this.lastDashTime = System.currentTimeMillis();
          
            this.dashVelX = Math.cos(this.angle) * Config.DASH_DISTANCE;
            this.dashVelY = Math.sin(this.angle) * Config.DASH_DISTANCE;
        }
        
        double distance = Math.sqrt((targetX - x) * (targetX - x) + (targetY - y) * (targetY - y));
        long currentTime = System.currentTimeMillis();
        boolean inRecovery = (currentTime - attackEndTime) < attackRecoveryDelay;
        this.isMoving = distance > Config.MOVEMENT_THRESHOLD && !isAttacking && !inRecovery;
        
        if (isMoving) {
            bobOffset += Config.BOB_SPEED;
        }
        
        if (this.isAttacking) {
            attackAnimation.update();
            if (attackAnimation.isFinished()) {
                attackEndTime = System.currentTimeMillis();
            }
        }
    }
    
    public void interpolateMove() {
        applyKnockback();
        applyDash();
        
        if (!isAttacking && !isDashing) {
            double lerpX = x + (targetX - x) * moveSpeed;
            double lerpY = y + (targetY - y) * moveSpeed;
            this.angle += (targetAngle - this.angle) * moveSpeed;
            
            this.x = lerpX;
            this.y = lerpY;
        } else {
            this.x = targetX;
            this.y = targetY;
            this.angle = targetAngle;
        }
    }
    
    private void applyKnockback() {
        if (knockbackVelX != 0 || knockbackVelY != 0) {
            long currentTime = System.currentTimeMillis();
            long knockbackElapsed = currentTime - knockbackStartTime;
            
            if (knockbackElapsed >= 100) {
                double newX = x + knockbackVelX;
                double newY = y + knockbackVelY;
                
                newX = Math.max(Config.PLAYER_SIZE/2, Math.min(Config.MAP_WIDTH - Config.PLAYER_SIZE/2, newX));
                newY = Math.max(Config.PLAYER_SIZE/2, Math.min(Config.MAP_HEIGHT - Config.PLAYER_SIZE/2, newY));
                
                this.x = newX;
                this.y = newY;
            }
            
            knockbackVelX *= 0.7;
            knockbackVelY *= 0.7;
            
            if (Math.abs(knockbackVelX) < 0.1) knockbackVelX = 0;
            if (Math.abs(knockbackVelY) < 0.1) knockbackVelY = 0;
        }
    }
    
    public void applyKnockbackFrom(double attackerX, double attackerY, double force) {
        double dxToAttacker = attackerX - x;
        double dyToAttacker = attackerY - y;
        double angleToAttacker = Math.atan2(dyToAttacker, dxToAttacker);
        
        knockbackVelX = -Math.cos(angleToAttacker) * force;
        knockbackVelY = -Math.sin(angleToAttacker) * force;
        knockbackStartTime = System.currentTimeMillis();
    }
    
    public void render(Graphics2D g2d) {
        if (health <= 0) {
            renderPlayerInfo(g2d, 0);
            return;
        }
        
        BufferedImage currentFrame;
        
        if (isAttacking) {
            currentFrame = attackAnimation.getCurrentFrame();
        } else if (isMoving) {
            currentFrame = walkAnimation.getCurrentFrame();
        } else {
            currentFrame = walkAnimation.getFrame(0);
        }
        
        if (currentFrame != null) {
            int scaledWidth = Config.PLAYER_SIZE;
            int scaledHeight = Config.PLAYER_SIZE;
            
            double bobY = 0;
            if (isMoving) {
                bobY = Math.sin(bobOffset) * Config.BOB_AMPLITUDE;
            }
            
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y + bobY);
            g2dCopy.rotate(angle);
            g2dCopy.translate(-scaledWidth / 2, -scaledHeight / 2);
            g2dCopy.drawImage(currentFrame, 0, 0, scaledWidth, scaledHeight, null);
            g2dCopy.dispose();
            
            renderPlayerInfo(g2d, bobY);
        }
    }
    
    public int getId() {
        return id;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public boolean isTimedOut() {
        return System.currentTimeMillis() - lastSeenTime > 10000;
    }
    
    private void renderPlayerInfo(Graphics2D g2d, double bobY) {
        if (health <= 0) {
            long deathCurrentTime = System.currentTimeMillis();
            long timeSinceDeath = deathCurrentTime - deathTime;
            long remainingTime = Config.RESPAWN_TIME - timeSinceDeath;
            
            if (remainingTime > 0) {
                int respawnSeconds = (int) ((remainingTime + 999) / 1000);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                g2d.setColor(Color.RED);
                String respawnText = "Respawn: " + respawnSeconds;
                FontMetrics deathFm = g2d.getFontMetrics();
                int textWidth = deathFm.stringWidth(respawnText);
                int textX = (int)x - textWidth / 2;
                int textY = (int)(y + bobY) + Config.PLAYER_SIZE / 2 + 30;
                g2d.drawString(respawnText, textX, textY);
            }
            return;
        }
        
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        
        int nameWidth = fm.stringWidth(name);
        int nameX = (int)x - nameWidth / 2;
        int nameY = (int)(y + bobY) - Config.PLAYER_SIZE / 2 - 35;
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, nameX + 1, nameY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(name, nameX, nameY);
        
        int barWidth = 80;
        int barHeight = 8;
        int healthBarX = (int)x - barWidth / 2;
        int healthBarY = (int)(y + bobY) - Config.PLAYER_SIZE / 2 - 25;
        
        double healthPercent = (double)health / Config.MAX_HEALTH;
        
        g2d.setColor(Color.RED);
        g2d.fillRect(healthBarX, healthBarY, barWidth, barHeight);
        
        g2d.setColor(Color.GREEN);
        g2d.fillRect(healthBarX, healthBarY, (int)(barWidth * healthPercent), barHeight);
        
        g2d.setColor(Color.WHITE);
        g2d.drawRect(healthBarX, healthBarY, barWidth, barHeight);
        
     
        long currentTime = System.currentTimeMillis();
        long timeSinceLastDash = currentTime - lastDashTime;
        long remainingCooldown = Config.DASH_COOLDOWN - timeSinceLastDash;
        
        if (remainingCooldown > 0) {
            int dashBarY = healthBarY + barHeight + 3;
            int dashBarHeight = 6;
            
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(healthBarX, dashBarY, barWidth, dashBarHeight);
            
            double dashCooldownPercent = 1.0 - ((double)remainingCooldown / Config.DASH_COOLDOWN);
            g2d.setColor(Color.RED);
            g2d.fillRect(healthBarX, dashBarY, (int)(barWidth * dashCooldownPercent), dashBarHeight);
            
            g2d.setColor(Color.WHITE);
            g2d.drawRect(healthBarX, dashBarY, barWidth, dashBarHeight);
        }
    }
    
    
    public void takeDamage(int damage) {
        int oldHealth = health;
        health -= damage;
        if (health < 0) health = 0;
        
        if (damagedClip != null && health > 0) {
            damagedClip.setFramePosition(0);
            damagedClip.start();
        }
        
        if (deathClip != null && health <= 0 && oldHealth > 0) {
            deathClip.setFramePosition(0);
            deathClip.start();
        }
    }
    
    
    public int getHealth() {
        return health;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public boolean isAttacking() {
        return isAttacking;
    }
    
    public double getAngle() {
        return angle;
    }
    
    public int getSkinId() {
        return skinId;
    }
    
    public void setHealth(int health) {
        if (health == 0 && this.health > 0) {
            deathTime = System.currentTimeMillis();
        }
        this.health = health;
    }
    
    public void revive() {
        this.health = Config.MAX_HEALTH;
        this.deathTime = 0;
        this.x = Config.MAP_WIDTH / 2;
        this.y = Config.MAP_HEIGHT / 2;
    }
    
    public int getKillCount() {
        return killCount;
    }
    
    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }
    
    public String getName() {
        return name;
    }
    
    private void applyDash() {
        if (isDashing) {
            long currentTime = System.currentTimeMillis();
            long dashElapsed = currentTime - dashStartTime;
            
            if (dashElapsed >= 150) { 
                double newX = x + dashVelX;
                double newY = y + dashVelY;
         
                newX = Math.max(Config.PLAYER_SIZE/2, Math.min(Config.MAP_WIDTH - Config.PLAYER_SIZE/2, newX));
                newY = Math.max(Config.PLAYER_SIZE/2, Math.min(Config.MAP_HEIGHT - Config.PLAYER_SIZE/2, newY));
                
                x = newX;
                y = newY;
                
                isDashing = false;
                dashVelX = 0;
                dashVelY = 0;
            }
        }
    }
}
