import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application launcher for the Banking Management System.
 */
public class Main {

    public static void main(String[] args) {
        // Set native system look and feel for a clean, modern desktop appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing Look and Feel
        }

        // Initialize SQLite Database schema before opening GUI
        try {
            Database.initializeDatabase();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize database: " + e.getMessage() + "\n\n"
                            + "Please ensure sqlite-jdbc.jar is present on the classpath.",
                    "Fatal Startup Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
            return;
        }

        // Start the application GUI on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
