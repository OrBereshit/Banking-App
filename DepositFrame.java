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
 * Screen for depositing funds into the user's account.
 */
public class DepositFrame extends JFrame {

    private final DashboardFrame parentDashboard;

    private JTextField accountNumberField;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton depositButton;
    private JButton backButton;

    public DepositFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;

        setTitle("Banking Management System - Deposit Money");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(480, 500);
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
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.COLOR_BG);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Deposit Money", SwingConstants.CENTER);
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        JLabel subtitleLabel = new JLabel("Add funds directly to your savings account", SwingConstants.CENTER);
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

        // 1. Current Account Number (read-only)
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

        // 2. Deposit Amount (INR)
        JLabel amountLabel = new JLabel("Deposit Amount (in INR ₹)");
        amountLabel.setFont(UIHelper.FONT_BOLD);
        amountLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        amountField = UIHelper.createTextField(20);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 4, 4, 4);
        cardPanel.add(amountLabel, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(amountField, gbc);

        // 3. Optional Description
        JLabel descLabel = new JLabel("Description (Optional)");
        descLabel.setFont(UIHelper.FONT_BOLD);
        descLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        descriptionField = UIHelper.createTextField(20);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 4, 4, 4);
        cardPanel.add(descLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 4, 4, 4);
        cardPanel.add(descriptionField, gbc);

        // Buttons Panel (Deposit & Back)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIHelper.COLOR_CARD);

        backButton = UIHelper.createSecondaryButton("Back");
        backButton.addActionListener(e -> {
            DepositFrame.this.dispose();
            if (parentDashboard != null) {
                parentDashboard.refreshAccountData();
            }
        });

        depositButton = UIHelper.createSuccessButton("Confirm Deposit");
        depositButton.addActionListener(e -> performDeposit());

        buttonPanel.add(backButton);
        buttonPanel.add(depositButton);

        gbc.gridy = 6;
        gbc.insets = new Insets(18, 4, 4, 4);
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void performDeposit() {
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

        depositButton.setEnabled(false);
        depositButton.setText("Processing...");
        backButton.setEnabled(false);

        int accountId = Session.getAccountId();

        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() throws Exception {
                return Database.deposit(accountId, amountCents, description);
            }

            @Override
            protected void done() {
                depositButton.setEnabled(true);
                depositButton.setText("Confirm Deposit");
                backButton.setEnabled(true);

                try {
                    long newBalanceCents = get();
                    JOptionPane.showMessageDialog(DepositFrame.this,
                            "Deposit of " + MoneyUtil.formatCentsToInr(amountCents) + " was successful!\n\n"
                                    + "Updated Balance: " + MoneyUtil.formatCentsToInr(newBalanceCents),
                            "Deposit Successful",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Clear fields and refresh dashboard
                    amountField.setText("");
                    descriptionField.setText("");

                    if (parentDashboard != null) {
                        parentDashboard.refreshAccountData();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DepositFrame.this,
                            "Failed to process deposit: " + ex.getMessage(),
                            "Deposit Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
