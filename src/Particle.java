import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Particle {
    private double x, y;
    private double angle;
    private Animation animation;
    private boolean active = false;
    private long spawnTime;
    private double moveOffset = 0;
    private int skinId = 1;
    
    public Particle() {
        loadAnimation(1);
    }
    
    private void loadAnimation(int skinId) {
        try {
            this.skinId = skinId;
            BufferedImage[] frames;
            
            if (skinId == 1) {
                frames = new BufferedImage[4];
                frames[0] = ImageIO.read(new File("assets/player/skin_1/ranged_particles-sheet0-0.png"));
                frames[1] = ImageIO.read(new File("assets/player/skin_1/ranged_particles-sheet0-1.png"));
                frames[2] = ImageIO.read(new File("assets/player/skin_1/ranged_particles-sheet0-2.png"));
                frames[3] = ImageIO.read(new File("assets/player/skin_1/ranged_particles-sheet0-3.png"));
            } else {
                frames = new BufferedImage[3];
                frames[0] = ImageIO.read(new File("assets/player/skin_2/ranged_particles-sheet0-0.png"));
                frames[1] = ImageIO.read(new File("assets/player/skin_2/ranged_particles-sheet0-1.png"));
                frames[2] = ImageIO.read(new File("assets/player/skin_2/ranged_particles-sheet0-2.png"));
            }
            animation = new Animation(frames, Config.PARTICLE_ANIMATION_DELAY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void spawn(double x, double y, double angle, int skinId) { 
        loadAnimation(skinId);
        this.angle = angle;
        this.x = x + Math.cos(angle) * Config.PARTICLE_SPAWN_DISTANCE;
        this.y = y + Math.sin(angle) * Config.PARTICLE_SPAWN_DISTANCE;
        this.moveOffset = 0;
        this.active = true;
        this.spawnTime = System.currentTimeMillis();
        animation.reset();
    }
    
    public void update() {
        if (!active) return;
        
        animation.update();
        
        moveOffset += 3.0;
        
        if (moveOffset > Config.ATTACK_RANGE) {
            active = false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - spawnTime >= Config.PARTICLE_DURATION) {
            active = false;
        }
    }
    
    public void render(java.awt.Graphics2D g2d) {
        if (!active) return;
        
        BufferedImage frame = animation.getCurrentFrame();
        if (frame != null) {
            int scaledSize = 80;
            double renderX = x + Math.cos(angle) * moveOffset;
            double renderY = y + Math.sin(angle) * moveOffset;
            int drawX = (int) renderX - scaledSize / 2;
            int drawY = (int) renderY - scaledSize / 2;
            g2d.drawImage(frame, drawX, drawY, scaledSize, scaledSize, null);
        }
    }
    
    public boolean isActive() {
        return active;
    }
}
