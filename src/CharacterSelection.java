import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CharacterSelection extends JPanel {
    private int selectedSkin = 1;
    private BufferedImage[] skinImages;
    private CharacterCallback callback;
    
    public interface CharacterCallback {
        void onCharacterSelected(int skinId);
    }
    
    public CharacterSelection(CharacterCallback callback) {
        this.callback = callback;
        setLayout(null);
        setPreferredSize(new Dimension(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        setBackground(Color.GRAY);
        
        loadImages();
        setupUI();
    }
    
    private void loadImages() {
        try {
            skinImages = new BufferedImage[2];
            skinImages[0] = ImageIO.read(new File("assets/player/skin_1/skins-sheet2-0.png"));
            skinImages[1] = ImageIO.read(new File("assets/player/skin_2/skins-sheet2-0.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setupUI() {
        JLabel titleLabel = new JLabel("QUICKCLAW");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(Config.WINDOW_WIDTH/2 - 140, 80, 280, 60);
        
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(Config.WINDOW_WIDTH/2 - 80, 180, 160, 30);
        
        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.BOLD, 16));
        nameField.setBounds(Config.WINDOW_WIDTH/2 - 150, 210, 300, 35);
        
        JButton prevButton = new JButton("<");
        prevButton.setFont(new Font("Arial", Font.BOLD, 32));
        prevButton.setBackground(Color.GRAY);
        prevButton.setForeground(Color.WHITE);
        prevButton.setBounds(400, 320, 60, 60);
        
        JButton nextButton = new JButton(">");
        nextButton.setFont(new Font("Arial", Font.BOLD, 32));
        nextButton.setBackground(Color.GRAY);
        nextButton.setForeground(Color.WHITE);
        nextButton.setBounds(840, 320, 60, 60);
        
        JButton joinButton = new JButton("PLAY GAME");
        joinButton.setFont(new Font("Arial", Font.BOLD, 24));
        nextButton.setBackground(Color.GRAY);
        nextButton.setForeground(Color.WHITE);
        joinButton.setBounds(Config.WINDOW_WIDTH/2 - 100, 480, 200, 50);
        
        prevButton.addActionListener(e -> {
            selectedSkin = (selectedSkin == 1) ? 2 : 1;
            updateCharacterDisplay();
        });
        
        nextButton.addActionListener(e -> {
            selectedSkin = (selectedSkin == 1) ? 2 : 1;
            updateCharacterDisplay();
        });
        
        joinButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                playerName = "Player" + selectedSkin;
            }
            callback.onCharacterSelected(selectedSkin);
        });
        
        add(titleLabel);
        add(nameLabel);
        add(nameField);
        add(prevButton);
        add(nextButton);
        add(joinButton);
        
        updateCharacterDisplay();
    }
    
    private void updateCharacterDisplay() {
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (skinImages != null && skinImages[selectedSkin - 1] != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            int centerX = Config.WINDOW_WIDTH / 2;
            int centerY = Config.WINDOW_HEIGHT / 2;
            
            BufferedImage currentSkin = skinImages[selectedSkin - 1];
            int scaledWidth = 150;
            int scaledHeight = 150;
            
      
            g2d.fillRect(centerX - scaledWidth/2 - 10, centerY - scaledHeight/2 - 10, 
                        scaledWidth + 20, scaledHeight + 20);
            
            g2d.drawImage(currentSkin, centerX - scaledWidth/2, centerY - scaledHeight/2, 
                         scaledWidth, scaledHeight, null);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String skinText = "Skin " + selectedSkin;
            int textWidth = fm.stringWidth(skinText);
            g2d.drawString(skinText, centerX - textWidth/2, centerY + scaledHeight/2 + 40);
        }
    }
}
