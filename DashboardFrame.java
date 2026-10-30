import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Main application dashboard displaying account details and recent transactions.
 */
public class DashboardFrame extends JFrame {

    private JLabel welcomeLabel;
    private JLabel accountNumberLabel;
    private JLabel balanceLabel;
    private JLabel statusLabel;
    private JTable recentTransactionsTable;
    private DefaultTableModel tableModel;

    public DashboardFrame() {
        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Session expired. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        setTitle("Banking Management System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(840, 660);
        setLocationRelativeTo(null);

        initComponents();
        refreshAccountData();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Navigation Bar
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(UIHelper.COLOR_BG);

        JPanel leftNav = new JPanel(new GridLayout(2, 1, 0, 4));
        leftNav.setBackground(UIHelper.COLOR_BG);

        welcomeLabel = new JLabel("Welcome, " + Session.getUserName());
        welcomeLabel.setFont(UIHelper.FONT_TITLE);
        welcomeLabel.setForeground(UIHelper.COLOR_PRIMARY);

        accountNumberLabel = new JLabel("A/C No: " + Session.getAccountNumber() + " | Email: " + Session.getUserEmail());
        accountNumberLabel.setFont(UIHelper.FONT_REGULAR);
        accountNumberLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        leftNav.add(welcomeLabel);
        leftNav.add(accountNumberLabel);

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightNav.setBackground(UIHelper.COLOR_BG);

        JButton profileBtn = UIHelper.createSecondaryButton("Profile");
        profileBtn.addActionListener(e -> new ProfileFrame(this).setVisible(true));

        JButton logoutBtn = UIHelper.createDangerButton("Logout");
        logoutBtn.addActionListener(e -> performLogout());

        rightNav.add(profileBtn);
        rightNav.add(logoutBtn);

        navBar.add(leftNav, BorderLayout.WEST);
        navBar.add(rightNav, BorderLayout.EAST);

        // Account Summary Card
        JPanel summaryCard = UIHelper.createCardPanel();
        summaryCard.setLayout(new BorderLayout(16, 16));

        JPanel balancePanel = new JPanel(new GridLayout(3, 1, 0, 6));
        balancePanel.setBackground(UIHelper.COLOR_CARD);

        JLabel balanceTitle = new JLabel("AVAILABLE BALANCE");
        balanceTitle.setFont(UIHelper.FONT_BOLD);
        balanceTitle.setForeground(UIHelper.COLOR_TEXT_MUTED);

        balanceLabel = new JLabel("₹ 0.00");
        balanceLabel.setFont(UIHelper.FONT_BALANCE);
        balanceLabel.setForeground(UIHelper.COLOR_PRIMARY);

        statusLabel = new JLabel("Status: ACTIVE");
        statusLabel.setFont(UIHelper.FONT_BOLD);
        statusLabel.setForeground(UIHelper.COLOR_SUCCESS);

        balancePanel.add(balanceTitle);
        balancePanel.add(balanceLabel);
        balancePanel.add(statusLabel);

        // Action Buttons Grid
        JPanel actionPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        actionPanel.setBackground(UIHelper.COLOR_CARD);

        JButton depositBtn = UIHelper.createPrimaryButton("+ Deposit");
        depositBtn.addActionListener(e -> new DepositFrame(this).setVisible(true));

        JButton withdrawBtn = UIHelper.createPrimaryButton("- Withdraw");
        withdrawBtn.addActionListener(e -> new WithdrawFrame(this).setVisible(true));

        JButton transferBtn = UIHelper.createPrimaryButton("⇄ Transfer");
        transferBtn.addActionListener(e -> new TransferFrame(this).setVisible(true));

        JButton historyBtn = UIHelper.createSecondaryButton("⟳ History");
        historyBtn.addActionListener(e -> new TransactionHistoryFrame(this).setVisible(true));

        actionPanel.add(depositBtn);
        actionPanel.add(withdrawBtn);
        actionPanel.add(transferBtn);
        actionPanel.add(historyBtn);

        summaryCard.add(balancePanel, BorderLayout.CENTER);
        summaryCard.add(actionPanel, BorderLayout.SOUTH);

        // Recent Transactions Section
        JPanel recentTxnPanel = new JPanel(new BorderLayout(0, 10));
        recentTxnPanel.setBackground(UIHelper.COLOR_BG);

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(UIHelper.COLOR_BG);

        JLabel recentTxnLabel = new JLabel("Recent Transactions (Last 5)");
        recentTxnLabel.setFont(UIHelper.FONT_SUBTITLE);
        recentTxnLabel.setForeground(UIHelper.COLOR_TEXT_MAIN);

        JButton viewAllBtn = new JButton("View All History →");
        viewAllBtn.setFont(UIHelper.FONT_BOLD);
        viewAllBtn.setForeground(UIHelper.COLOR_ACCENT);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> new TransactionHistoryFrame(this).setVisible(true));

        tableHeaderPanel.add(recentTxnLabel, BorderLayout.WEST);
        tableHeaderPanel.add(viewAllBtn, BorderLayout.EAST);

        // Table Model & Styling
        String[] columns = {"Ref ID", "Type", "Amount", "Description", "Date & Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Non-editable cells
            }
        };

        recentTransactionsTable = new JTable(tableModel);
        recentTransactionsTable.setFont(UIHelper.FONT_REGULAR);
        recentTransactionsTable.setRowHeight(32);
        recentTransactionsTable.getTableHeader().setFont(UIHelper.FONT_BOLD);
        recentTransactionsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        recentTransactionsTable.getTableHeader().setForeground(UIHelper.COLOR_TEXT_MAIN);
        recentTransactionsTable.setShowGrid(true);
        recentTransactionsTable.setGridColor(UIHelper.COLOR_BORDER);

        // Center align reference, type, and date columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        recentTransactionsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        recentTransactionsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        recentTransactionsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Custom renderer for Amount with color (Green for credit, Red for debit)
        recentTransactionsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("+")) {
                        c.setForeground(UIHelper.COLOR_SUCCESS);
                    } else if (str.startsWith("-")) {
                        c.setForeground(UIHelper.COLOR_DANGER);
                    } else {
                        c.setForeground(UIHelper.COLOR_TEXT_MAIN);
                    }
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        scrollPane.setBorder(new LineBorder(UIHelper.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        recentTxnPanel.add(tableHeaderPanel, BorderLayout.NORTH);
        recentTxnPanel.add(scrollPane, BorderLayout.CENTER);

        // Assemble Content
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setBackground(UIHelper.COLOR_BG);
        centerContainer.add(summaryCard, BorderLayout.NORTH);
        centerContainer.add(recentTxnPanel, BorderLayout.CENTER);

        mainPanel.add(navBar, BorderLayout.NORTH);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    /**
     * Re-queries SQLite for current balance, account status, and 5 latest transactions.
     */
    public void refreshAccountData() {
        int accountId = Session.getAccountId();
        if (accountId <= 0) return;

        welcomeLabel.setText("Welcome, " + Session.getUserName());

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                Database.AccountInfo info = Database.getAccountDetails(accountId);
                List<Database.TransactionRecord> txns = Database.getRecentTransactions(accountId, 5);
                return new DashboardData(info, txns);
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    // Update Balance
                    balanceLabel.setText(MoneyUtil.formatCentsToInr(data.accountInfo.balanceCents));

                    // Update Status
                    String status = data.accountInfo.status;
                    statusLabel.setText("Status: " + status);
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        statusLabel.setForeground(UIHelper.COLOR_SUCCESS);
                    } else {
                        statusLabel.setForeground(UIHelper.COLOR_DANGER);
                    }

                    // Populate Recent Transactions Table
                    tableModel.setRowCount(0);
                    if (data.transactions.isEmpty()) {
                        tableModel.addRow(new Object[]{"-", "-", "₹ 0.00", "No recent transactions found", "-"});
                    } else {
                        for (Database.TransactionRecord txn : data.transactions) {
                            boolean isCredit;
                            String displayType;

                            if ("DEPOSIT".equalsIgnoreCase(txn.type)) {
                                isCredit = true;
                                displayType = "DEPOSIT";
                            } else if ("WITHDRAWAL".equalsIgnoreCase(txn.type)) {
                                isCredit = false;
                                displayType = "WITHDRAWAL";
                            } else {
                                // TRANSFER: Check if incoming or outgoing based on description
                                if (txn.description != null && txn.description.toLowerCase().contains("from")) {
                                    isCredit = true;
                                    displayType = "TRANSFER (IN)";
                                } else {
                                    isCredit = false;
                                    displayType = "TRANSFER (OUT)";
                                }
                            }

                            String formattedAmount = MoneyUtil.formatSignedCents(txn.amountCents, isCredit);
                            tableModel.addRow(new Object[]{
                                    txn.transactionId,
                                    displayType,
                                    formattedAmount,
                                    txn.description,
                                    txn.createdAt
                            });
                        }
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Failed to refresh account data: " + e.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Session.clear();
            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }

    private static class DashboardData {
        final Database.AccountInfo accountInfo;
        final List<Database.TransactionRecord> transactions;

        DashboardData(Database.AccountInfo accountInfo, List<Database.TransactionRecord> transactions) {
            this.accountInfo = accountInfo;
            this.transactions = transactions;
        }
    }
}
