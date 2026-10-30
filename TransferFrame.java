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
 * Screen for transferring funds to another registered account.
 */
public class TransferFrame extends JFrame {

    private final DashboardFrame parentDashboard;

    private JTextField senderAccountField;
    private JTextField availableBalanceField;
    private JTextField recipientAccountField;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton transferButton;
    private JButton backButton;

    private long currentBalanceCents = 0;

    public TransferFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;

        setTitle("Banking Management System - Transfer Funds");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 580);
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

        JLabel titleLabel = new JLabel("Transfer Money", SwingConstants.CENTER);
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Instant and secure fund transfer between bank accounts", SwingConstants.CENTER);
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

        // 1. Sender Account Number (read-only)
        JLabel senderLabel = new JLabel("From Account (Sender)");
        senderLabel.setFont(UIHelper.FONT_BOLD);
        senderLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        senderAccountField = UIHelper.createTextField(20);
        senderAccountField.setText(Session.getAccountNumber());
        senderAccountField.setEditable(false);
        senderAccountField.setBackground(new java.awt.Color(241, 245, 249));

        gbc.gridx = 0;
        gbc.gridy = 0;
        cardPanel.add(senderLabel, gbc);
        gbc.gridy = 1;
        cardPanel.add(senderAccountField, gbc);

        // 2. Available Balance
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

        // 3. Recipient Account Number
        JLabel recipientLabel = new JLabel("To Account (Recipient Account Number)");
        recipientLabel.setFont(UIHelper.FONT_BOLD);
        recipientLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        recipientAccountField = UIHelper.createTextField(20);

        gbc.gridy = 4;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(recipientLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(recipientAccountField, gbc);

        // 4. Transfer Amount Field
        JLabel amountLabel = new JLabel("Transfer Amount (in INR ₹)");
        amountLabel.setFont(UIHelper.FONT_BOLD);
        amountLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        amountField = UIHelper.createTextField(20);

        gbc.gridy = 6;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(amountLabel, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(amountField, gbc);

        // 5. Optional Description
        JLabel descLabel = new JLabel("Remarks / Description (Optional)");
        descLabel.setFont(UIHelper.FONT_BOLD);
        descLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        descriptionField = UIHelper.createTextField(20);

        gbc.gridy = 8;
        gbc.insets = new Insets(8, 4, 4, 4);
        cardPanel.add(descLabel, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(descriptionField, gbc);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIHelper.COLOR_CARD);

        backButton = UIHelper.createSecondaryButton("Back");
        backButton.addActionListener(e -> {
            TransferFrame.this.dispose();
            if (parentDashboard != null) {
                parentDashboard.refreshAccountData();
            }
        });

        transferButton = UIHelper.createPrimaryButton("Confirm Transfer");
        transferButton.addActionListener(e -> performTransfer());

        buttonPanel.add(backButton);
        buttonPanel.add(transferButton);

        gbc.gridy = 10;
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

    private void performTransfer() {
        String recipientAcc = recipientAccountField.getText().trim();
        String rawAmount = amountField.getText().trim();
        String description = descriptionField.getText().trim();

        if (recipientAcc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the recipient's account number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            recipientAccountField.requestFocus();
            return;
        }

        if (recipientAcc.equalsIgnoreCase(Session.getAccountNumber())) {
            JOptionPane.showMessageDialog(this, "You cannot transfer money to your own account.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            recipientAccountField.requestFocus();
            return;
        }

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
                            + "Transfer Amount: " + MoneyUtil.formatCentsToInr(amountCents) + "\n"
                            + "Available Balance: " + MoneyUtil.formatCentsToInr(currentBalanceCents),
                    "Transfer Error",
                    JOptionPane.WARNING_MESSAGE);
            amountField.requestFocus();
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm transfer of " + MoneyUtil.formatCentsToInr(amountCents)
                        + " to Account " + recipientAcc + "?",
                "Confirm Fund Transfer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        transferButton.setEnabled(false);
        transferButton.setText("Transferring...");
        backButton.setEnabled(false);

        int senderAccountId = Session.getAccountId();

        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                return Database.transfer(senderAccountId, recipientAcc, amountCents, description);
            }

            @Override
            protected void done() {
                transferButton.setEnabled(true);
                transferButton.setText("Confirm Transfer");
                backButton.setEnabled(true);

                try {
                    long remainingBalanceCents = get();
                    currentBalanceCents = remainingBalanceCents;
                    availableBalanceField.setText(MoneyUtil.formatCentsToInr(remainingBalanceCents));

                    JOptionPane.showMessageDialog(TransferFrame.this,
                            "Transfer Successful!\n\n"
                                    + "Transferred: " + MoneyUtil.formatCentsToInr(amountCents) + "\n"
                                    + "To Account: " + recipientAcc + "\n"
                                    + "Remaining Balance: " + MoneyUtil.formatCentsToInr(remainingBalanceCents),
                            "Transfer Confirmation",
                            JOptionPane.INFORMATION_MESSAGE);

                    recipientAccountField.setText("");
                    amountField.setText("");
                    descriptionField.setText("");

                    if (parentDashboard != null) {
                        parentDashboard.refreshAccountData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TransferFrame.this,
                            "Transfer failed: " + ex.getMessage(),
                            "Transfer Error",
                            JOptionPane.ERROR_MESSAGE);
                    loadAvailableBalance();
                }
            }
        };

        worker.execute();
    }
}
