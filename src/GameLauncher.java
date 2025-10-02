import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GameLauncher {
    private JFrame frame;
    
    public GameLauncher() {
        frame = new JFrame("QuickClaw Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        CharacterSelection selection = new CharacterSelection(this::onCharacterSelected);
        frame.add(selection);
        frame.pack();
        frame.setLocationRelativeTo(null);
        try {
            Image cursorImage = ImageIO.read(new File("./assets/ui/ic_cursor.png"));
            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "hand");
            frame.setCursor(customCursor);
        } catch (IOException e) {
        }
        frame.setVisible(true);


    }
    
    private void onCharacterSelected(int skinId, String playerName) {
        frame.getContentPane().removeAll();
        
        OnlineGame onlineGame = new OnlineGame(skinId, playerName);
        frame.add(onlineGame);
        
        frame.revalidate();
        frame.repaint();
        Component[] components = frame.getContentPane().getComponents();
        if (components.length > 0) {
            components[0].requestFocus();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameLauncher();
        });
    }
}