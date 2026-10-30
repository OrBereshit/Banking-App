import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
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
 * Screen displaying the complete transaction history for the user's account.
 */
public class TransactionHistoryFrame extends JFrame {

    private final DashboardFrame parentDashboard;

    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JLabel statusInfoLabel;
    private JButton refreshButton;
    private JButton backButton;

    public TransactionHistoryFrame(DashboardFrame parentDashboard) {
        this.parentDashboard = parentDashboard;

        setTitle("Banking Management System - Transaction History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 560);
        setLocationRelativeTo(null);

        initComponents();
        loadTransactions();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UIHelper.COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Header & Action Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIHelper.COLOR_BG);

        JPanel titleContainer = new JPanel(new BorderLayout(0, 4));
        titleContainer.setBackground(UIHelper.COLOR_BG);

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(UIHelper.FONT_TITLE);
        titleLabel.setForeground(UIHelper.COLOR_PRIMARY);

        statusInfoLabel = new JLabel("Account: " + Session.getAccountNumber() + " | Loading transactions...");
        statusInfoLabel.setFont(UIHelper.FONT_REGULAR);
        statusInfoLabel.setForeground(UIHelper.COLOR_TEXT_MUTED);

        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(statusInfoLabel, BorderLayout.SOUTH);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBar.setBackground(UIHelper.COLOR_BG);

        refreshButton = UIHelper.createSecondaryButton("⟳ Refresh");
        refreshButton.addActionListener(e -> loadTransactions());

        backButton = UIHelper.createPrimaryButton("← Back to Dashboard");
        backButton.addActionListener(e -> {
            TransactionHistoryFrame.this.dispose();
            if (parentDashboard != null) {
                parentDashboard.refreshAccountData();
            }
        });

        buttonBar.add(refreshButton);
        buttonBar.add(backButton);

        topPanel.add(titleContainer, BorderLayout.WEST);
        topPanel.add(buttonBar, BorderLayout.EAST);

        // Table Model & Styling
        String[] columns = {"Reference ID", "Transaction Type", "Amount", "Description", "Date & Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionsTable = new JTable(tableModel);
        transactionsTable.setFont(UIHelper.FONT_REGULAR);
        transactionsTable.setRowHeight(32);
        transactionsTable.getTableHeader().setFont(UIHelper.FONT_BOLD);
        transactionsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        transactionsTable.getTableHeader().setForeground(UIHelper.COLOR_TEXT_MAIN);
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(UIHelper.COLOR_BORDER);

        // Alignments
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        transactionsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        transactionsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        transactionsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Set column widths
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        // Colored amount renderer
        transactionsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
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

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        scrollPane.setBorder(new LineBorder(UIHelper.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void loadTransactions() {
        int accountId = Session.getAccountId();
        refreshButton.setEnabled(false);
        statusInfoLabel.setText("Account: " + Session.getAccountNumber() + " | Fetching transactions from database...");

        SwingWorker<List<Database.TransactionRecord>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Database.TransactionRecord> doInBackground() throws Exception {
                return Database.getAllTransactions(accountId);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    List<Database.TransactionRecord> records = get();
                    tableModel.setRowCount(0);

                    if (records.isEmpty()) {
                        statusInfoLabel.setText("Account: " + Session.getAccountNumber() + " | No transactions recorded yet.");
                        tableModel.addRow(new Object[]{"-", "-", "₹ 0.00", "No transactions found for this account.", "-"});
                    } else {
                        statusInfoLabel.setText("Account: " + Session.getAccountNumber() + " | Total " + records.size() + " transaction(s) found (newest first).");

                        for (Database.TransactionRecord txn : records) {
                            boolean isCredit;
                            String displayType;

                            if ("DEPOSIT".equalsIgnoreCase(txn.type)) {
                                isCredit = true;
                                displayType = "DEPOSIT";
                            } else if ("WITHDRAWAL".equalsIgnoreCase(txn.type)) {
                                isCredit = false;
                                displayType = "WITHDRAWAL";
                            } else {
                                // TRANSFER
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
                } catch (Exception ex) {
                    statusInfoLabel.setText("Error loading transactions.");
                    JOptionPane.showMessageDialog(TransactionHistoryFrame.this,
                            "Failed to load transaction history: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
