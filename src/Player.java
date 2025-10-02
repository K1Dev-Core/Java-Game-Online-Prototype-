import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class Player {
    private double x, y;
    private double targetX, targetY;
    private double speed = Config.PLAYER_SPEED;
    private double angle = 0;
    private Animation walkAnimation;
    private Animation attackAnimation;
    private boolean isAttacking = false;
    private boolean isMoving = false;
    private long lastAttackTime = 0;
    private boolean canAttack = true;
    private long attackEndTime = 0;
    private long attackRecoveryDelay = 300;
    private double knockbackVelX = 0;
    private double knockbackVelY = 0;
    private long knockbackStartTime = 0;
    private double bobOffset = 0;
    private Clip footstepsClip;
    private Clip weaponClip;
    private boolean wasMoving = false;
    private int skinId;
    private int health = Config.MAX_HEALTH;
    private String playerName = "Player";
    private PlayerAttackCallback attackCallback;
    private int killCount = 0;
    private Clip damagedClip;
    private Clip deathClip;
    private Clip buttonClip;
    private boolean isDashing = false;
    private long lastDashTime = 0;
    private double dashVelX = 0;
    private double dashVelY = 0;
    private long dashStartTime = 0;
    private long deathTime = 0;
    
    public interface PlayerAttackCallback {
        void onPlayerAttack(double x, double y, double angle, int skinId);
    }
    
    public long getLastAttackTime() {
        return lastAttackTime;
    }
    
    public Player(double x, double y, int skinId, String playerName, PlayerAttackCallback attackCallback) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.skinId = skinId;
        this.playerName = playerName;
        this.attackCallback = attackCallback;
        
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
            
            try {
                AudioInputStream footstepsStream = AudioSystem.getAudioInputStream(new File("assets/sfx/footsteps.wav"));
                footstepsClip = AudioSystem.getClip();
                footstepsClip.open(footstepsStream);
                
                AudioInputStream weaponStream = AudioSystem.getAudioInputStream(new File("assets/sfx/weapon.wav"));
                weaponClip = AudioSystem.getClip();
                weaponClip.open(weaponStream);
                
                AudioInputStream damagedStream = AudioSystem.getAudioInputStream(new File("assets/sfx/damaged.wav"));
                damagedClip = AudioSystem.getClip();
                damagedClip.open(damagedStream);
                
                AudioInputStream deathStream = AudioSystem.getAudioInputStream(new File("assets/sfx/death.wav"));
                deathClip = AudioSystem.getClip();
                deathClip.open(deathStream);
                
                AudioInputStream buttonStream = AudioSystem.getAudioInputStream(new File("assets/sfx/button.wav"));
                buttonClip = AudioSystem.getClip();
                buttonClip.open(buttonStream);
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void update(InputHandler inputHandler, ParticleManager particleManager) {
        applyKnockback();
        applyDash();
        
        targetX = inputHandler.getMouseX();
        targetY = inputHandler.getMouseY();
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime >= Config.ATTACK_COOLDOWN) {
            canAttack = true;
        }
      
        if (inputHandler.isDashPressed() && currentTime - lastDashTime >= Config.DASH_COOLDOWN && !isDead()) {
            dashVelX = Math.cos(angle) * Config.DASH_DISTANCE;
            dashVelY = Math.sin(angle) * Config.DASH_DISTANCE;
            isDashing = true;
            lastDashTime = currentTime;
            dashStartTime = currentTime;
            SoundManager.playTeleportSound();
            inputHandler.setDashPressed(false);
        }
        
        if (inputHandler.isMouseClicked() && canAttack && !isDead()) {
            isAttacking = true;
            attackAnimation.reset();
            lastAttackTime = currentTime;
            canAttack = false;
            particleManager.spawnParticle(x, y, angle, skinId);
            
            if (attackCallback != null) {
                attackCallback.onPlayerAttack(x, y, angle, skinId);
            }
            
            if (weaponClip != null) {
                weaponClip.setFramePosition(0);
                weaponClip.start();
            }
            
            inputHandler.setMouseClicked(false);
        }
        
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        long recoveryTime = System.currentTimeMillis();
        boolean inRecovery = (recoveryTime - attackEndTime) < attackRecoveryDelay;
        
        if (distance > Config.MOVEMENT_THRESHOLD && !isAttacking && !inRecovery) {
            isMoving = true;
            double moveAngle = Math.atan2(dy, dx);
            angle = moveAngle;
            
            double newX = x + Math.cos(moveAngle) * speed;
            double newY = y + Math.sin(moveAngle) * speed;
            
            if (newX >= Config.PLAYER_SIZE/2 && newX <= Config.MAP_WIDTH - Config.PLAYER_SIZE/2) {
                x = newX;
            }
            if (newY >= Config.PLAYER_SIZE/2 && newY <= Config.MAP_HEIGHT - Config.PLAYER_SIZE/2) {
                y = newY;
            }
        } else {
            isMoving = false;
        }
        
        if (isAttacking) {
            attackAnimation.update();
            if (attackAnimation.isFinished()) {
                isAttacking = false;
                attackEndTime = System.currentTimeMillis();
            }
        } else if (isMoving) {
            bobOffset += Config.BOB_SPEED;
        }
        
        if (isMoving && footstepsClip != null) {
            if (!footstepsClip.isRunning()) {
                footstepsClip.setFramePosition(0);
                footstepsClip.start();
            }
        }
        if (!isMoving && footstepsClip != null) {
            footstepsClip.stop();
            footstepsClip.setFramePosition(0);
        }
    }
    
    public void render(Graphics2D g2d) {
        if (isDead()) {
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
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    
    public boolean canAttack() {
        long currentTime = System.currentTimeMillis();
        if (!canAttack) {
            canAttack = currentTime >= lastAttackTime + Config.ATTACK_COOLDOWN;
        }
        return canAttack;
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            deathTime = System.currentTimeMillis();
        }
        
        if (damagedClip != null && health > 0) {
            damagedClip.setFramePosition(0);
            damagedClip.start();
        }
        
        if (deathClip != null && health <= 0) {
            deathClip.setFramePosition(0);
            deathClip.start();
        }
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public int getHealth() {
        return health;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void heal(int amount) {
        health += amount;
        if (health > Config.MAX_HEALTH) health = Config.MAX_HEALTH;
    }
    
    public void revive() {
        health = Config.MAX_HEALTH;
        deathTime = 0;
    }
    
    public void addKill() {
        killCount++;
        health = Config.MAX_HEALTH;
    }
    
    public int getKillCount() {
        return killCount;
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
                
                x = newX;
                y = newY;
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
    
    public boolean isAttacking() {
        return isAttacking;
    }
    
    public boolean isDashing() {
        return isDashing;
    }
    
    public long getDeathTime() {
        return deathTime;
    }
    
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public int getSkinId() {
        return skinId;
    }
    
    public void playButtonSound() {
        if (buttonClip != null) {
            buttonClip.setFramePosition(0);
            buttonClip.start();
        }
    }
    
    public void renderDeathOverlay(Graphics2D g2d) {
        if (isDead() && deathTime > 0) {
            long deathCurrentTime = System.currentTimeMillis();
            long timeSinceDeath = deathCurrentTime - deathTime;
            long remainingTime = Config.RESPAWN_TIME - timeSinceDeath;
            
            if (remainingTime > 0) {
          
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
                
              
                int respawnSeconds = (int) ((remainingTime + 999) / 1000);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.setColor(Color.RED);
                String respawnText = "YOU DIED";
                FontMetrics fm = g2d.getFontMetrics();
                int titleWidth = fm.stringWidth(respawnText);
                int titleX = Config.WINDOW_WIDTH / 2 - titleWidth / 2;
                int titleY = Config.WINDOW_HEIGHT / 2 - 50;
                g2d.drawString(respawnText, titleX, titleY);
                
          
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                g2d.setColor(Color.WHITE);
                String countdownText = "Respawning in " + respawnSeconds + " seconds";
                FontMetrics countFm = g2d.getFontMetrics();
                int countWidth = countFm.stringWidth(countdownText);
                int countX = Config.WINDOW_WIDTH / 2 - countWidth / 2;
                int countY = Config.WINDOW_HEIGHT / 2 + 20;
                g2d.drawString(countdownText, countX, countY);
            }
        }
    }
    
    private void renderPlayerInfo(Graphics2D g2d, double bobY) {
        if (isDead()) {
            return;
        }
        
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        
        int nameWidth = fm.stringWidth(playerName);
        int nameX = (int)x - nameWidth / 2;
        int nameY = (int)(y + bobY) - Config.PLAYER_SIZE / 2 - 35;
        
        g2d.setColor(Color.BLACK);
        g2d.drawString(playerName, nameX + 1, nameY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(playerName, nameX, nameY);
        
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
