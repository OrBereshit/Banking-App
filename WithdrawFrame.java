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
 * Screen for withdrawing funds from the user's account.
 */
public class WithdrawFrame extends JFrame {

    private final DashboardFrame parentDashboard;

    private JTextField accountNumberField;
    private JTextField availableBalanceField;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton withdrawButton;
    private JButton backButton;

    private long currentBalanceCents = 0;

    public WithdrawFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;

        setTitle("Banking Management System - Withdraw Money");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 540);
        setLocationRelativeTo(null);
        setResizable(false);

        // Notify parent on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (parentDashboard != null) {
                    parentDashboard.refreshAccountData();
                }
            }
        });

        initComponents();
        loadAvailableBalance();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Withdraw Money", SwingConstants.CENTER);
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Debit funds from your savings account", SwingConstants.CENTER);
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

        // 1. Account Number (read-only)
        JLabel accLabel = new JLabel("Account Number");
        accLabel.setFont(UIHelper.FONT_BOLD);
        accLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        accountNumberField = UIHelper.createTextField(20);
        accountNumberField.setText(Session.getAccountNumber());
        accountNumberField.setEditable(false);
        accountNumberField.setBackground(new java.awt.Color(241, 245, 249));

        gbc.gridx = 0;
        gbc.gridy = 0;
        cardPanel.add(accLabel, gbc);
        gbc.gridy = 1;
        cardPanel.add(accountNumberField, gbc);

        // 2. Available Balance (read-only)
        JLabel balanceLabel = new JLabel("Available Balance");
        balanceLabel.setFont(UIHelper.FONT_BOLD);
        balanceLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        availableBalanceField = UIHelper.createTextField(20);
        availableBalanceField.setText("Loading...");
        availableBalanceField.setEditable(false);
        availableBalanceField.setBackground(new java.awt.Color(241, 245, 249));
        availableBalanceField.setForeground(UIHelper.COLOR_PRIMARY);
        availableBalanceField.setFont(UIHelper.FONT_BOLD);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(balanceLabel, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(availableBalanceField, gbc);

        // 3. Amount Field
        JLabel amountLabel = new JLabel("Withdrawal Amount (in INR ₹)");
        amountLabel.setFont(UIHelper.FONT_BOLD);
        amountLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        amountField = UIHelper.createTextField(20);

        gbc.gridy = 4;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(amountLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(amountField, gbc);

        // 4. Optional Description
        JLabel descLabel = new JLabel("Description (Optional)");
        descLabel.setFont(UIHelper.FONT_BOLD);
        descLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        descriptionField = UIHelper.createTextField(20);

        gbc.gridy = 6;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(descLabel, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(descriptionField, gbc);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIHelper.COLOR_CARD);

        backButton = UIHelper.createSecondaryButton("Back");
        backButton.addActionListener(e -> {
            WithdrawFrame.this.dispose();
            if (parentDashboard != null) {
                parentDashboard.refreshAccountData();
            }
        });

        withdrawButton = UIHelper.createPrimaryButton("Confirm Withdrawal");
        withdrawButton.addActionListener(e -> performWithdrawal());

        buttonPanel.add(backButton);
        buttonPanel.add(withdrawButton);

        gbc.gridy = 8;
        gbc.insets = new Insets(18, 4, 4, 4);
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void loadAvailableBalance() {
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
                    currentBalanceCents = info.balanceCents;
                    availableBalanceField.setText(MoneyUtil.formatCentsToInr(currentBalanceCents));
                } catch (Exception e) {
                    availableBalanceField.setText("Error fetching balance");
                }
            }
        };
        worker.execute();
    }

    private void performWithdrawal() {
        String rawAmount = amountField.getText().trim();
        String description = descriptionField.getText().trim();

        final long amountCents;
        try {
            amountCents = MoneyUtil.parseInrToCents(rawAmount);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            amountField.requestFocus();
            return;
        }

        if (amountCents > currentBalanceCents) {
            JOptionPane.showMessageDialog(this,
                    "Insufficient funds.\n\n"
                            + "Requested: " + MoneyUtil.formatCentsToInr(amountCents) + "\n"
                            + "Available: " + MoneyUtil.formatCentsToInr(currentBalanceCents),
                    "Withdrawal Error",
                    JOptionPane.WARNING_MESSAGE);
            amountField.requestFocus();
            return;
        }

        withdrawButton.setEnabled(false);
        withdrawButton.setText("Processing...");
        backButton.setEnabled(false);

        int accountId = Session.getAccountId();

        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                return Database.withdraw(accountId, amountCents, description);
            }

            @Override
            protected void done() {
                withdrawButton.setEnabled(true);
                withdrawButton.setText("Confirm Withdrawal");
                backButton.setEnabled(true);

                try {
                    long remainingBalanceCents = get();
                    currentBalanceCents = remainingBalanceCents;
                    availableBalanceField.setText(MoneyUtil.formatCentsToInr(remainingBalanceCents));

                    JOptionPane.showMessageDialog(WithdrawFrame.this,
                            "Withdrawal of " + MoneyUtil.formatCentsToInr(amountCents) + " was successful!\n\n"
                                    + "Remaining Balance: " + MoneyUtil.formatCentsToInr(remainingBalanceCents),
                            "Withdrawal Successful",
                            JOptionPane.INFORMATION_MESSAGE);

                    amountField.setText("");
                    descriptionField.setText("");

                    if (parentDashboard != null) {
                        parentDashboard.refreshAccountData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WithdrawFrame.this,
                            "Failed to process withdrawal: " + ex.getMessage(),
                            "Withdrawal Error",
                            JOptionPane.ERROR_MESSAGE);
                    // Reload fresh balance in case of conflict
                    loadAvailableBalance();
                }
            }
        };

        worker.execute();
    }
}
