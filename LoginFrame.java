import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * Screen for user authentication.
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("Banking Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Sign in to your Banking Account", SwingConstants.CENTER);
        subtitleLabel.setFont(UIHelper.FONT_REGULAR);
        subtitleLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form Card Panel
        JPanel cardPanel = UIHelper.createCardPanel();
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Email Label & Field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(UIHelper.FONT_BOLD);
        emailLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        emailField = UIHelper.createTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        cardPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        cardPanel.add(emailField, gbc);

        // Password Label & Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIHelper.FONT_BOLD);
        passwordLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        passwordField = UIHelper.createPasswordField(20);

        gbc.gridy = 2;
        gbc.insets = new Insets(12, 4, 6, 4);
        cardPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(6, 4, 16, 4);
        cardPanel.add(passwordField, gbc);

        // Login Button
        loginButton = UIHelper.createPrimaryButton("Login");
        loginButton.addActionListener(e -> performLogin());

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 4, 6, 4);
        cardPanel.add(loginButton, gbc);

        // Bottom Navigation (Register Link)
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(UIHelper.COLOR_BG);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel noAccLabel = new JLabel("Don't have an account?");
        noAccLabel.setFont(UIHelper.FONT_REGULAR);
        noAccLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        registerButton = new JButton("Register Now");
        registerButton.setFont(UIHelper.FONT_BOLD);
        registerButton.setForeground(UIHelper.COLOR_ACCENT);
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            RegisterFrame registerFrame = new RegisterFrame(this);
            registerFrame.setVisible(true);
            this.setVisible(false);
        });

        footerPanel.add(noAccLabel);
        footerPanel.add(registerButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Allow pressing Enter in password field to login
        passwordField.addActionListener(e -> performLogin());

        setContentPane(mainPanel);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Execute authentication on background thread to keep EDT responsive
        SwingWorker<Database.AccountInfo, Void> worker = new SwingWorker<>() {
            @Override
            protected Database.AccountInfo doInBackground() throws Exception {
                return Database.authenticateUser(email, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                // Clear password array in memory
                java.util.Arrays.fill(password, '\0');

                try {
                    Database.AccountInfo accountInfo = get();
                    if (accountInfo != null) {
                        // Open dashboard
                        DashboardFrame dashboard = new DashboardFrame();
                        dashboard.setVisible(true);
                        LoginFrame.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Invalid email address or password.",
                                "Authentication Failed",
                                JOptionPane.ERROR_MESSAGE);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Database error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
