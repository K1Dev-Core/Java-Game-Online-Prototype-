import java.util.ArrayList;

public class ParticleManager {
    private ArrayList<Particle> particleList;
    private final int MAX_PARTICLES = 10;
    
    public ParticleManager() {
        particleList = new ArrayList<>();
        for (int i = 0; i < MAX_PARTICLES; i++) {
            particleList.add(new Particle());
        }
    }
    
    public void spawnParticle(double startX, double startY, double angle, int skinId) {
        for (Particle particle : particleList) {
            if (!particle.isActive()) {
                particle.spawn(startX, startY, angle, skinId);
                break;
            }
        }
    }
    
    public void update() {
        for (Particle particle : particleList) {
            particle.update();
        }
    }
    
    public void render(java.awt.Graphics2D g2d) {
        for (Particle particle : particleList) {
            particle.render(g2d);
        }
    }
}
