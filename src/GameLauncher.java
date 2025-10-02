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
        frame.setVisible(true);
    }
    
    private void onCharacterSelected(int skinId) {
        frame.getContentPane().removeAll();
        Game game = new Game(skinId);
        frame.add(game);
        frame.revalidate();
        frame.repaint();
        game.requestFocus();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameLauncher();
        });
    }
}