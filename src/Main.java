import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("server")) {
            GameServer server = new GameServer(Config.SERVER_PORT);
            server.start();
        } else {
            SwingUtilities.invokeLater(() -> {
                new GameLauncher();
            });
        }
    }
}
