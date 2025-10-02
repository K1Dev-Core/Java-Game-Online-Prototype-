import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;

public class InputHandler {
    private int mouseX, mouseY;
    private boolean mouseClicked;
    private final JPanel gamePanel;
    
    public InputHandler(JPanel gamePanel) {
        this.gamePanel = gamePanel;
        this.mouseClicked = false;
    }
    
    public void addListeners() {
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                mouseClicked = true;
            }
        });
        
        gamePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
    }
    
    public int getMouseX() {
        return mouseX;
    }
    
    public int getMouseY() {
        return mouseY;
    }
    
    public boolean isMouseClicked() {
        return mouseClicked;
    }
    
    public void setMouseClicked(boolean clicked) {
        this.mouseClicked = clicked;
    }
}