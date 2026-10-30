import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * Screen displaying user profile details and allowing name updates.
 * Disallows manual modification of account number or balance.
 */
public class ProfileFrame extends JFrame {

    private final DashboardFrame parentDashboard;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField accountNumberField;
    private JTextField balanceField;
    private JButton saveButton;
    private JButton backButton;

    public ProfileFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;

        setTitle("Banking Management System - User Profile");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("User Profile", SwingConstants.CENTER);
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("View and update your personal banking profile", SwingConstants.CENTER);
        subtitleLabel.setFont(UIHelper.FONT_REGULAR);
        subtitleLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form Card Panel
        JPanel cardPanel = UIHelper.createCardPanel();
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        // 1. Full Name (editable)
        JLabel nameLabel = new JLabel("Full Name (Editable)");
        nameLabel.setFont(UIHelper.FONT_BOLD);
        nameLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        nameField = UIHelper.createTextField(20);
        nameField.setText(Session.getUserName());

        gbc.gridx = 0;
        gbc.gridy = 0;
        cardPanel.add(nameLabel, gbc);
        gbc.gridy = 1;
        cardPanel.add(nameField, gbc);

        // 2. Email Address (read-only)
        JLabel emailLabel = new JLabel("Email Address (Registered)");
        emailLabel.setFont(UIHelper.FONT_BOLD);
        emailLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        emailField = UIHelper.createTextField(20);
        emailField.setText(Session.getUserEmail());
        emailField.setEditable(false);
        emailField.setBackground(new java.awt.Color(241, 245, 249));

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(emailLabel, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(emailField, gbc);

        // 3. Account Number (read-only)
        JLabel accLabel = new JLabel("Account Number (Non-editable)");
        accLabel.setFont(UIHelper.FONT_BOLD);
        accLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        accountNumberField = UIHelper.createTextField(20);
        accountNumberField.setText(Session.getAccountNumber());
        accountNumberField.setEditable(false);
        accountNumberField.setBackground(new java.awt.Color(241, 245, 249));

        gbc.gridy = 4;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(accLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(accountNumberField, gbc);

        // 4. Current Balance (read-only)
        JLabel balanceLabel = new JLabel("Current Balance (Non-editable)");
        balanceLabel.setFont(UIHelper.FONT_BOLD);
        balanceLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        balanceField = UIHelper.createTextField(20);
        balanceField.setText("Loading...");
        balanceField.setEditable(false);
        balanceField.setBackground(new java.awt.Color(241, 245, 249));
        balanceField.setFont(UIHelper.FONT_BOLD);
        balanceField.setForeground(UIHelper.COLOR_PRIMARY);

        gbc.gridy = 6;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(balanceLabel, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(balanceField, gbc);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIHelper.COLOR_CARD);

        backButton = UIHelper.createSecondaryButton("Back");
        backButton.addActionListener(e -> {
            ProfileFrame.this.dispose();
            if (parentDashboard != null) {
                parentDashboard.refreshAccountData();
            }
        });

        saveButton = UIHelper.createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> saveProfileChanges());

        buttonPanel.add(backButton);
        buttonPanel.add(saveButton);

        gbc.gridy = 8;
        gbc.insets = new Insets(18, 4, 4, 4);
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void loadProfileData() {
        int accountId = Session.getAccountId();
        SwingWorker<Database.AccountInfo, Void> worker = new SwingWorker<>() {
            @Override
            protected Database.AccountInfo doInBackground() throws Exception {
                return Database.getAccountDetails(accountId);
            }

            @Override
            protected void done() {
                try {
                    Database.AccountInfo info = get();
                    balanceField.setText(MoneyUtil.formatCentsToInr(info.balanceCents));
                } catch (Exception e) {
                    balanceField.setText("Error loading balance");
                }
            }
        };
        worker.execute();
    }

    private void saveProfileChanges() {
        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");
        backButton.setEnabled(false);

        int userId = Session.getUserId();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Database.updateUserName(userId, newName);
                return null;
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");
                backButton.setEnabled(true);

                try {
                    get();
                    Session.setUserName(newName);
                    JOptionPane.showMessageDialog(ProfileFrame.this,
                            "Profile updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    if (parentDashboard != null) {
                        parentDashboard.refreshAccountData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ProfileFrame.this,
                            "Failed to update profile: " + ex.getMessage(),
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
