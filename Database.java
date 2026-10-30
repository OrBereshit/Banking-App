import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Central Database Access Layer for SQLite.
 * Manages connections, schema initialization, and transactional financial operations.
 */
public class Database {

    private static final String DB_FILE = "banking.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    // Load SQLite JDBC Driver class
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    /**
     * DTO representing an account's state in SQLite.
     */
    public static class AccountInfo {
        public final int accountId;
        public final String accountNumber;
        public final int userId;
        public final long balanceCents;
        public final String status;

        public AccountInfo(int accountId, String accountNumber, int userId, long balanceCents, String status) {
            this.accountId = accountId;
            this.accountNumber = accountNumber;
            this.userId = userId;
            this.balanceCents = balanceCents;
            this.status = status;
        }
    }

    /**
     * DTO representing a transaction log entry.
     */
    public static class TransactionRecord {
        public final String transactionId;
        public final String type;
        public final long amountCents;
        public final String description;
        public final String createdAt;

        public TransactionRecord(String transactionId, String type, long amountCents, String description, String createdAt) {
            this.transactionId = transactionId;
            this.type = type;
            this.amountCents = amountCents;
            this.description = description;
            this.createdAt = createdAt;
        }
    }

    /**
     * Obtains a new SQLite connection with Foreign Key enforcement enabled.
     *
     * @return Connection instance
     * @throws SQLException on database connection error
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            // Enable SQLite foreign key constraint enforcement on every connection
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Automatically creates the database schema if tables do not exist.
     */
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "email TEXT NOT NULL UNIQUE, "
                    + "password TEXT NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");");

            // 2. Accounts Table
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "account_number TEXT NOT NULL UNIQUE, "
                    + "user_id INTEGER NOT NULL, "
                    + "balance INTEGER NOT NULL DEFAULT 0, "
                    + "status TEXT NOT NULL DEFAULT 'ACTIVE', "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ");");

            // 3. Transactions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "transaction_id TEXT NOT NULL, "
                    + "account_id INTEGER NOT NULL, "
                    + "type TEXT NOT NULL, "
                    + "amount INTEGER NOT NULL, "
                    + "description TEXT, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE"
                    + ");");
        }
    }

    /**
     * Generates a unique 10-digit account number.
     */
    private static String generateUniqueAccountNumber(Connection conn) throws SQLException {
        Random random = new Random();
        while (true) {
            // 10-digit number starting with 1-9
            long num = 1000000000L + (long) (random.nextDouble() * 8999999999L);
            String candidate = String.valueOf(num);

            String checkSql = "SELECT id FROM accounts WHERE account_number = ?;";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setString(1, candidate);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        return candidate;
                    }
                }
            }
        }
    }

    /**
     * Registers a new user and creates their primary savings account atomically.
     *
     * @param name User full name
     * @param email User email address
     * @param password Plaintext password characters to hash
     * @return The newly assigned unique account number
     * @throws Exception if registration fails (e.g., duplicate email)
     */
    public static String registerUser(String name, String email, char[] password) throws Exception {
        String hashedPassword = PasswordUtil.hashPassword(password);
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Check duplicate email
            String emailCheckSql = "SELECT id FROM users WHERE email = ?;";
            try (PreparedStatement checkStmt = conn.prepareStatement(emailCheckSql)) {
                checkStmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("An account with this email address already exists.");
                    }
                }
            }

            // Insert User
            String insertUserSql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?);";
            int userId = -1;
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, name.trim());
                userStmt.setString(2, email.trim().toLowerCase());
                userStmt.setString(3, hashedPassword);
                userStmt.executeUpdate();

                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated user ID.");
                    }
                }
            }

            // Generate unique account number and create account
            String accountNumber = generateUniqueAccountNumber(conn);
            String insertAccSql = "INSERT INTO accounts (account_number, user_id, balance, status) VALUES (?, ?, 0, 'ACTIVE');";
            try (PreparedStatement accStmt = conn.prepareStatement(insertAccSql)) {
                accStmt.setString(1, accountNumber);
                accStmt.setInt(2, userId);
                accStmt.executeUpdate();
            }

            conn.commit();
            return accountNumber;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param email Email
     * @param password Password characters
     * @return User account information if login succeeds, null otherwise
     * @throws Exception on database error
     */
    public static AccountInfo authenticateUser(String email, char[] password) throws Exception {
        String query = "SELECT u.id AS user_id, u.name, u.email, u.password, "
                + "a.id AS account_id, a.account_number, a.balance, a.status "
                + "FROM users u "
                + "JOIN accounts a ON u.id = a.user_id "
                + "WHERE u.email = ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verifyPassword(password, storedHash)) {
                        int userId = rs.getInt("user_id");
                        String name = rs.getString("name");
                        int accountId = rs.getInt("account_id");
                        String accNum = rs.getString("account_number");
                        long balance = rs.getLong("balance");
                        String status = rs.getString("status");

                        Session.setSession(userId, name, email.trim().toLowerCase(), accountId, accNum);
                        return new AccountInfo(accountId, accNum, userId, balance, status);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the latest account data directly from SQLite.
     *
     * @param accountId Account ID
     * @return AccountInfo
     * @throws SQLException on error or account not found
     */
    public static AccountInfo getAccountDetails(int accountId) throws SQLException {
        String query = "SELECT id, account_number, user_id, balance, status FROM accounts WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new AccountInfo(
                            rs.getInt("id"),
                            rs.getString("account_number"),
                            rs.getInt("user_id"),
                            rs.getLong("balance"),
                            rs.getString("status")
                    );
                } else {
                    throw new SQLException("Account not found.");
                }
            }
        }
    }

    /**
     * Executes an atomic cash deposit into an account.
     *
     * @param accountId Target account ID
     * @param amountCents Deposit amount in cents
     * @param description Optional note
     * @return Updated account balance in cents
     * @throws Exception on error
     */
    public static long deposit(int accountId, long amountCents, String description) throws Exception {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Verify account status
            String statusQuery = "SELECT balance, status FROM accounts WHERE id = ?;";
            long currentBalance;
            String status;
            try (PreparedStatement statusStmt = conn.prepareStatement(statusQuery)) {
                statusStmt.setInt(1, accountId);
                try (ResultSet rs = statusStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Account not found.");
                    }
                    currentBalance = rs.getLong("balance");
                    status = rs.getString("status");
                }
            }

            if (!"ACTIVE".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Account is currently " + status + ". Deposits are not allowed.");
            }

            long newBalance = currentBalance + amountCents;

            // Update balance
            String updateSql = "UPDATE accounts SET balance = ? WHERE id = ?;";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setLong(1, newBalance);
                updateStmt.setInt(2, accountId);
                updateStmt.executeUpdate();
            }

            // Insert transaction
            String txnId = "TXN-DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String desc = (description == null || description.trim().isEmpty()) ? "Cash Deposit" : description.trim();
            String insertTxnSql = "INSERT INTO transactions (transaction_id, account_id, type, amount, description) VALUES (?, ?, 'DEPOSIT', ?, ?);";
            try (PreparedStatement txnStmt = conn.prepareStatement(insertTxnSql)) {
                txnStmt.setString(1, txnId);
                txnStmt.setInt(2, accountId);
                txnStmt.setLong(3, amountCents);
                txnStmt.setString(4, desc);
                txnStmt.executeUpdate();
            }

            conn.commit();
            return newBalance;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Executes an atomic cash withdrawal from an account.
     *
     * @param accountId Target account ID
     * @param amountCents Withdrawal amount in cents
     * @param description Optional note
     * @return Updated account balance in cents
     * @throws Exception on error or insufficient funds
     */
    public static long withdraw(int accountId, long amountCents, String description) throws Exception {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Verify account status and balance
            String statusQuery = "SELECT balance, status FROM accounts WHERE id = ?;";
            long currentBalance;
            String status;
            try (PreparedStatement statusStmt = conn.prepareStatement(statusQuery)) {
                statusStmt.setInt(1, accountId);
                try (ResultSet rs = statusStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Account not found.");
                    }
                    currentBalance = rs.getLong("balance");
                    status = rs.getString("status");
                }
            }

            if (!"ACTIVE".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Account is currently " + status + ". Withdrawals are not allowed.");
            }

            if (currentBalance < amountCents) {
                throw new IllegalStateException("Insufficient funds. Available balance: " + MoneyUtil.formatCentsToInr(currentBalance));
            }

            long newBalance = currentBalance - amountCents;

            // Update balance
            String updateSql = "UPDATE accounts SET balance = ? WHERE id = ?;";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setLong(1, newBalance);
                updateStmt.setInt(2, accountId);
                updateStmt.executeUpdate();
            }

            // Insert transaction
            String txnId = "TXN-WTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String desc = (description == null || description.trim().isEmpty()) ? "Cash Withdrawal" : description.trim();
            String insertTxnSql = "INSERT INTO transactions (transaction_id, account_id, type, amount, description) VALUES (?, ?, 'WITHDRAWAL', ?, ?);";
            try (PreparedStatement txnStmt = conn.prepareStatement(insertTxnSql)) {
                txnStmt.setString(1, txnId);
                txnStmt.setInt(2, accountId);
                txnStmt.setLong(3, amountCents);
                txnStmt.setString(4, desc);
                txnStmt.executeUpdate();
            }

            conn.commit();
            return newBalance;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Executes an atomic transfer of funds between two accounts.
     * Updates both balances and writes matched transaction records under a shared reference.
     *
     * @param senderAccountId Sender account ID
     * @param recipientAccountNumber Target account number
     * @param amountCents Amount to transfer in cents
     * @param description Optional user description
     * @return Sender's updated account balance in cents
     * @throws Exception on error or validation failure
     */
    public static long transfer(int senderAccountId, String recipientAccountNumber, long amountCents, String description) throws Exception {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }
        if (recipientAccountNumber == null || recipientAccountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient account number is required.");
        }

        String targetAcc = recipientAccountNumber.trim();
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Fetch sender info
            String senderQuery = "SELECT id, account_number, balance, status FROM accounts WHERE id = ?;";
            long senderBalance;
            String senderAccNum;
            String senderStatus;
            try (PreparedStatement sStmt = conn.prepareStatement(senderQuery)) {
                sStmt.setInt(1, senderAccountId);
                try (ResultSet rs = sStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Sender account not found.");
                    }
                    senderAccNum = rs.getString("account_number");
                    senderBalance = rs.getLong("balance");
                    senderStatus = rs.getString("status");
                }
            }

            if (senderAccNum.equalsIgnoreCase(targetAcc)) {
                throw new IllegalArgumentException("Sender and recipient accounts must be different.");
            }

            if (!"ACTIVE".equalsIgnoreCase(senderStatus)) {
                throw new IllegalStateException("Your account is " + senderStatus + ". Transfers are not allowed.");
            }

            if (senderBalance < amountCents) {
                throw new IllegalStateException("Insufficient funds. Available balance: " + MoneyUtil.formatCentsToInr(senderBalance));
            }

            // 2. Fetch recipient info
            String recipientQuery = "SELECT id, account_number, balance, status FROM accounts WHERE account_number = ?;";
            int recipientAccountId;
            long recipientBalance;
            String recipientStatus;
            try (PreparedStatement rStmt = conn.prepareStatement(recipientQuery)) {
                rStmt.setString(1, targetAcc);
                try (ResultSet rs = rStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Recipient account '" + targetAcc + "' does not exist.");
                    }
                    recipientAccountId = rs.getInt("id");
                    recipientBalance = rs.getLong("balance");
                    recipientStatus = rs.getString("status");
                }
            }

            if (!"ACTIVE".equalsIgnoreCase(recipientStatus)) {
                throw new IllegalStateException("Recipient account is currently " + recipientStatus + ". Transfer cannot proceed.");
            }

            long newSenderBalance = senderBalance - amountCents;
            long newRecipientBalance = recipientBalance + amountCents;

            // 3. Deduct from sender
            String deductSql = "UPDATE accounts SET balance = ? WHERE id = ?;";
            try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                deductStmt.setLong(1, newSenderBalance);
                deductStmt.setInt(2, senderAccountId);
                deductStmt.executeUpdate();
            }

            // 4. Credit to recipient
            String creditSql = "UPDATE accounts SET balance = ? WHERE id = ?;";
            try (PreparedStatement creditStmt = conn.prepareStatement(creditSql)) {
                creditStmt.setLong(1, newRecipientBalance);
                creditStmt.setInt(2, recipientAccountId);
                creditStmt.executeUpdate();
            }

            // 5. Insert shared transfer transaction records
            String sharedTxnId = "TXN-TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String userDescSuffix = (description != null && !description.trim().isEmpty()) ? " - " + description.trim() : "";

            String senderDesc = "Transfer to " + targetAcc + userDescSuffix;
            String recipientDesc = "Transfer from " + senderAccNum + userDescSuffix;

            String insertTxnSql = "INSERT INTO transactions (transaction_id, account_id, type, amount, description) VALUES (?, ?, 'TRANSFER', ?, ?);";
            try (PreparedStatement txnStmt = conn.prepareStatement(insertTxnSql)) {
                // Sender record
                txnStmt.setString(1, sharedTxnId);
                txnStmt.setInt(2, senderAccountId);
                txnStmt.setLong(3, amountCents);
                txnStmt.setString(4, senderDesc);
                txnStmt.executeUpdate();

                // Recipient record
                txnStmt.setString(1, sharedTxnId);
                txnStmt.setInt(2, recipientAccountId);
                txnStmt.setLong(3, amountCents);
                txnStmt.setString(4, recipientDesc);
                txnStmt.executeUpdate();
            }

            conn.commit();
            return newSenderBalance;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Retrieves recent transactions for an account.
     *
     * @param accountId Account ID
     * @param limit Maximum number of records to return
     * @return List of TransactionRecords (newest first)
     * @throws SQLException on error
     */
    public static List<TransactionRecord> getRecentTransactions(int accountId, int limit) throws SQLException {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = "SELECT transaction_id, type, amount, description, created_at "
                + "FROM transactions WHERE account_id = ? "
                + "ORDER BY id DESC LIMIT ?;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactionRecord(
                            rs.getString("transaction_id"),
                            rs.getString("type"),
                            rs.getLong("amount"),
                            rs.getString("description"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all transactions for an account (newest first).
     *
     * @param accountId Account ID
     * @return List of TransactionRecords
     * @throws SQLException on error
     */
    public static List<TransactionRecord> getAllTransactions(int accountId) throws SQLException {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = "SELECT transaction_id, type, amount, description, created_at "
                + "FROM transactions WHERE account_id = ? "
                + "ORDER BY id DESC;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactionRecord(
                            rs.getString("transaction_id"),
                            rs.getString("type"),
                            rs.getLong("amount"),
                            rs.getString("description"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Updates a user's display name.
     *
     * @param userId User ID
     * @param newName New full name
     * @throws SQLException on error
     */
    public static void updateUserName(int userId, String newName) throws SQLException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        String sql = "UPDATE users SET name = ? WHERE id = ?;";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName.trim());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }
}
