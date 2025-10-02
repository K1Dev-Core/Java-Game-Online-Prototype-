import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;

public class InputHandler {
    private int mouseX, mouseY;
    private boolean mouseClicked;
    private boolean dashPressed;
    private final JPanel gamePanel;
    
    public InputHandler(JPanel gamePanel) {
        this.gamePanel = gamePanel;
        this.mouseClicked = false;
        this.dashPressed = false;
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
        
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    dashPressed = true;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    dashPressed = false;
                }
            }
        });
        
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
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
    
    public boolean isDashPressed() {
        return dashPressed;
    }
    
    public void setDashPressed(boolean pressed) {
        this.dashPressed = pressed;
    }
}