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
    private boolean isConnecting = false;
    private BufferedImage backgroundImage;
    private BufferedImage prevButtonImage;
    private BufferedImage nextButtonImage;
    private BufferedImage playButtonImage;
    
    public interface CharacterCallback {
        void onCharacterSelected(int skinId, String playerName);
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
            
            backgroundImage = ImageIO.read(new File("assets/ui/window.png"));
            prevButtonImage = ImageIO.read(new File("assets/ui/buttonarrow-sheet0.png"));
            nextButtonImage = ImageIO.read(new File("assets/ui/buttonarrow-sheet1.png"));
            playButtonImage = ImageIO.read(new File("assets/ui/Play.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load UI images: " + e.getMessage());
        }
    }
    
    private void setupUI() {
        JLabel titleLabel = new JLabel("QUICKCLAW");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(Config.WINDOW_WIDTH/2 - 100, 80, 280, 60);
        
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(Config.WINDOW_WIDTH/2 - 80, 180, 160, 30);

        JTextField nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.BOLD, 16));
        nameField.setBounds(Config.WINDOW_WIDTH/2 - 150, 210, 300, 35);
        
        JLabel instructionsLabel = new JLabel("HOW TO PLAY:");
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        instructionsLabel.setForeground(Color.WHITE);
        instructionsLabel.setBounds(Config.WINDOW_WIDTH - 180, Config.WINDOW_HEIGHT - 120, 160, 20);
        
        JButton prevButton = createImageButton(prevButtonImage, 400, 320, 60, 60);
        
        JButton nextButton = createImageButton(nextButtonImage, 840, 320, 60, 60);
        
        JButton joinGameButton = createImageButton(playButtonImage, Config.WINDOW_WIDTH/2 - 100, 520, 200, 60);
        final String[] originalText = {"Play"};
        joinGameButton.setText(originalText[0]);
        
        prevButton.addActionListener(e -> {
            selectedSkin = (selectedSkin == 1) ? 2 : 1;
            updateCharacterDisplay();
            SoundManager.playButtonSound();
        });
        
        nextButton.addActionListener(e -> {
            selectedSkin = (selectedSkin == 1) ? 2 : 1;
            updateCharacterDisplay();
            SoundManager.playButtonSound();
        });
        
        joinGameButton.addActionListener(e -> {
            if (isConnecting) {
                return;
            }
            
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a player name");
                return;
            }
            
            isConnecting = true;
            joinGameButton.setEnabled(false);
            joinGameButton.setText("Connecting...");
            SoundManager.playButtonSound();
            
            new Thread(() -> {
                try {
                    callback.onCharacterSelected(selectedSkin, playerName);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        isConnecting = false;
                        joinGameButton.setEnabled(true);
                        joinGameButton.setText(originalText[0]);
                        JOptionPane.showMessageDialog(this, "Failed to connect to server");
                    });
                }
            }).start();
        });
        
        
        add(titleLabel);
        add(nameLabel);
        add(nameField);
        add(instructionsLabel);
        
        
        JLabel attackLabel = new JLabel("Attack: Left Click");
        attackLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        attackLabel.setForeground(Color.WHITE);
        attackLabel.setBounds(Config.WINDOW_WIDTH - 180, Config.WINDOW_HEIGHT - 85, 160, 15);
        add(attackLabel);
        
        JLabel dashLabel = new JLabel("Dash: Shift Key");
        dashLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        dashLabel.setForeground(Color.WHITE);
        dashLabel.setBounds(Config.WINDOW_WIDTH - 180, Config.WINDOW_HEIGHT - 70, 160, 15);
        add(dashLabel);
        
        add(prevButton);
        add(nextButton);
        add(joinGameButton);
        
        updateCharacterDisplay();
    }
    
    private void updateCharacterDisplay() {
        repaint();
    }
    
    
    private JButton createImageButton(BufferedImage image, int x, int y, int width, int height) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (image != null) {
                    g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                
                g2d.dispose();
            }
        };
        
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBounds(x, y, width, height);
        
        final int originalWidth = width;
        final int originalHeight = height;
        final int originalX = x;
        final int originalY = y;
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                int newWidth = (int)(originalWidth * 1.1);
                int newHeight = (int)(originalHeight * 1.1);
                int newX = originalX - (newWidth - originalWidth) / 2;
                int newY = originalY - (newHeight - originalHeight) / 2;
                button.setBounds(newX, newY, newWidth, newHeight);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBounds(originalX, originalY, originalWidth, originalHeight);
            }
        });
        
        return button;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        paintSkinPreview(g2d);
    }
    
    private void paintSkinPreview(Graphics2D g2d) {
        if (skinImages != null && skinImages[selectedSkin - 1] != null) {
            BufferedImage currentSkin = skinImages[selectedSkin - 1];
            int centerX = Config.WINDOW_WIDTH / 2;
            int centerY = 360;
            int frameWidth = 200;
            int frameHeight = 200;
            int skinWidth = 150;
            int skinHeight = 150;
            
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, centerX - frameWidth/2, centerY - frameHeight/2, 
                             frameWidth, frameHeight, null);
            }
            
            g2d.drawImage(currentSkin, centerX - skinWidth/2, centerY - skinHeight/2, 
                         skinWidth, skinHeight, null);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            String skinText = "Skin " + selectedSkin;
            int textWidth = fm.stringWidth(skinText);
            g2d.drawString(skinText, centerX - textWidth/2, centerY + frameHeight/2 + 40);
        }
    }
}
