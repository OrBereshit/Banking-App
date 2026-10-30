import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
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
 * Screen for new user registration and account creation.
 */
public class RegisterFrame extends JFrame {

    private final LoginFrame parentLoginFrame;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backToLoginButton;

    public RegisterFrame(LoginFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;

        setTitle("Banking Management System - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);

        // When window is closed, restore login frame
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (parentLoginFrame != null) {
                    parentLoginFrame.setVisible(true);
                }
            }
        });

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Open an Account", SwingConstants.CENTER);
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Create a new bank account in seconds", SwingConstants.CENTER);
        subtitleLabel.setFont(UIHelper.FONT_REGULAR);
        subtitleLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form Card Panel
        JPanel cardPanel = UIHelper.createCardPanel();
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // 1. Full Name
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(UIHelper.FONT_BOLD);
        nameLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);
        nameField = UIHelper.createTextField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        cardPanel.add(nameLabel, gbc);
        gbc.gridy = 1;
        cardPanel.add(nameField, gbc);

        // 2. Email Address
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(UIHelper.FONT_BOLD);
        emailLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);
        emailField = UIHelper.createTextField(20);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(emailLabel, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(emailField, gbc);

        // 3. Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIHelper.FONT_BOLD);
        passwordLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);
        passwordField = UIHelper.createPasswordField(20);

        gbc.gridy = 4;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(passwordLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(passwordField, gbc);

        // 4. Confirm Password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setFont(UIHelper.FONT_BOLD);
        confirmPasswordLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);
        confirmPasswordField = UIHelper.createPasswordField(20);

        gbc.gridy = 6;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(confirmPasswordLabel, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(confirmPasswordField, gbc);

        // Register Button
        registerButton = UIHelper.createPrimaryButton("Register & Open Account");
        registerButton.addActionListener(e -> performRegistration());

        gbc.gridy = 8;
        gbc.insets = new Insets(16, 4, 4, 4);
        cardPanel.add(registerButton, gbc);

        // Footer Link
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(UIHelper.COLOR_BG);
        footerPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel hasAccLabel = new JLabel("Already have an account?");
        hasAccLabel.setFont(UIHelper.FONT_REGULAR);
        hasAccLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        backToLoginButton = new JButton("Login here");
        backToLoginButton.setFont(UIHelper.FONT_BOLD);
        backToLoginButton.setForeground(UIHelper.COLOR_ACCENT);
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToLoginButton.addActionListener(e -> {
            RegisterFrame.this.dispose();
            if (parentLoginFrame != null) {
                parentLoginFrame.setVisible(true);
            }
        });

        footerPanel.add(hasAccLabel);
        footerPanel.add(backToLoginButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        // 1. Validate fields
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your full name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        // Basic email syntax validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            emailField.requestFocus();
            return;
        }

        if (password.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        if (password.length < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        if (!Arrays.equals(password, confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Creating Account...");

        // Execute registration on background thread
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return Database.registerUser(name, email, password);
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                registerButton.setText("Register & Open Account");
                Arrays.fill(password, '\0');
                Arrays.fill(confirmPassword, '\0');

                try {
                    String accountNumber = get();
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                            "Registration successful!\n\n"
                                    + "Name: " + name + "\n"
                                    + "Email: " + email + "\n"
                                    + "Account Number: " + accountNumber + "\n"
                                    + "Initial Balance: ₹ 0.00\n"
                                    + "Status: ACTIVE\n\n"
                                    + "Please log in using your email and password.",
                            "Account Created Successfully",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Return to login frame
                    RegisterFrame.this.dispose();
                    if (parentLoginFrame != null) {
                        parentLoginFrame.setVisible(true);
                    }
                } catch (Exception ex) {
                    String errorMsg = ex.getMessage();
                    if (errorMsg != null && errorMsg.contains("already exists")) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                                "An account with this email address already exists. Please login or use a different email.",
                                "Registration Error",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                                "Failed to create account: " + errorMsg,
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        worker.execute();
    }
}
