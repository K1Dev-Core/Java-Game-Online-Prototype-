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
    private double bobOffset = 0;
    private Clip footstepsClip;
    private Clip weaponClip;
    private boolean wasMoving = false;
    private int skinId;
    
    public long getLastAttackTime() {
        return lastAttackTime;
    }
    
    public Player(double x, double y, int skinId) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.skinId = skinId;
        
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
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void update(InputHandler inputHandler, ParticleManager particleManager) {
        targetX = inputHandler.getMouseX();
        targetY = inputHandler.getMouseY();
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime >= Config.ATTACK_COOLDOWN) {
            canAttack = true;
        }
        
        if (inputHandler.isMouseClicked() && canAttack) {
            isAttacking = true;
            attackAnimation.reset();
            lastAttackTime = currentTime;
            canAttack = false;
            particleManager.spawnParticle(x, y, angle, skinId);
            
            if (weaponClip != null) {
                weaponClip.setFramePosition(0);
                weaponClip.start();
            }
            
            inputHandler.setMouseClicked(false);
        }
        
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > Config.MOVEMENT_THRESHOLD) {
            isMoving = true;
            angle = Math.atan2(dy, dx);
            
            double newX = x + Math.cos(angle) * speed;
            double newY = y + Math.sin(angle) * speed;
            
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
            if (isMoving && !isAttacking) {
                bobY = Math.sin(bobOffset) * Config.BOB_AMPLITUDE;
            }
            
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.translate(x, y + bobY);
            g2dCopy.rotate(angle);
            g2dCopy.translate(-scaledWidth / 2, -scaledHeight / 2);
            g2dCopy.drawImage(currentFrame, 0, 0, scaledWidth, scaledHeight, null);
            g2dCopy.dispose();
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
}
