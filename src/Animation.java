import java.awt.image.BufferedImage;

public class Animation {
    private final BufferedImage[] frames;
    private int currentFrame;
    private long lastTime;
    private final long delay;
    private boolean finished;
    
    public Animation(BufferedImage[] frames, long delay) {
        this.frames = frames;
        this.delay = delay;
        this.currentFrame = 0;
        this.lastTime = System.currentTimeMillis();
        this.finished = false;
    }
    
    public void update() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastTime >= delay) {
            currentFrame++;
            if (currentFrame >= frames.length) {
                currentFrame = 0;
                finished = true;
            }
            lastTime = currentTime;
        }
    }
    
    public BufferedImage getCurrentFrame() {
        if (frames.length > 0) {
            return frames[currentFrame];
        }
        return null;
    }
    
    public BufferedImage getFrame(int index) {
        if (index >= 0 && index < frames.length) {
            return frames[index];
        }
        return null;
    }
    
    public void reset() {
        currentFrame = 0;
        finished = false;
        lastTime = System.currentTimeMillis();
    }
    
    public boolean isFinished() {
        return finished;
    }
}
