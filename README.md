# Banking Management System (Java Swing + SQLite + JDBC)

A clean, beginner-friendly desktop Banking Management System built entirely in pure Java using **Java Swing** for the graphical user interface, **JDBC** for database operations, and **SQLite** for serverless, zero-configuration local database storage.

---

## 1. Project Overview

This project implements a complete banking mini-project without any external build frameworks (No Maven, No Gradle, No Spring, No Hibernate, No JavaFX). It requires only standard Java (Java 8 or higher) and the single bundled `sqlite-jdbc.jar` library.

### Key Capabilities & Rules:
- **Serverless Database:** Uses a local SQLite database (`banking.db`) automatically created upon application startup.
- **Foreign Key Enforcement:** Foreign keys are explicitly enabled on every connection (`PRAGMA foreign_keys = ON;`).
- **Cryptographic Security:** Standard PBKDF2 with HMAC-SHA256 password hashing (salt + 65,536 iterations), strictly avoiding plaintext storage or homebrewed hashing algorithms.
- **Financial Exactness:** Money is stored and computed strictly as integer cents (paise) to prevent floating-point rounding errors (`float`/`double` are never used for currency calculations).
- **Atomic Transactions:** Deposits, withdrawals, and inter-account transfers use `Connection.setAutoCommit(false)`, `commit()`, and `rollback()` to guarantee ACID compliance.
- **Responsive UI:** Database operations are handled using `SwingWorker` background tasks to keep the Swing Event Dispatch Thread (EDT) fast and responsive.

---

## 2. Project Directory Structure

```text
BankingApp/
├── src/
│   ├── Main.java                    # Application entry point & initialization
│   ├── Database.java                # Central JDBC access layer, schema & transactions
│   ├── LoginFrame.java              # User authentication screen
│   ├── RegisterFrame.java           # Account opening & registration screen
│   ├── DashboardFrame.java          # Main banking dashboard with recent transactions
│   ├── DepositFrame.java            # Fund deposit window
│   ├── WithdrawFrame.java           # Fund withdrawal window
│   ├── TransferFrame.java           # Inter-account fund transfer window
│   ├── TransactionHistoryFrame.java # Complete transaction history with JTable
│   ├── ProfileFrame.java            # User profile viewer & name updater
│   ├── PasswordUtil.java            # Standard PBKDF2 hashing & verification
│   ├── MoneyUtil.java               # Exact integer cent conversion & INR formatting
│   ├── Session.java                 # In-memory user session state
│   └── UIHelper.java                # Consistent Swing styling & component factory
├── lib/
│   └── sqlite-jdbc.jar              # SQLite JDBC driver (bundled)
├── bin/                             # Compiled bytecode classes
├── README.md                        # Documentation & setup guide
└── banking.db                       # SQLite database file (created automatically on startup)
```

---

## 3. Database Schema & Tables

When the application runs for the first time, `Database.initializeDatabase()` executes the following SQL statements to initialize `banking.db`:

### Users Table
```sql
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Accounts Table
```sql
CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_number TEXT NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    balance INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```
*Note: Each registered user receives an initial savings account with an initial balance of ₹0.00 (`0` cents) and an `ACTIVE` status.*

### Transactions Table
```sql
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id TEXT NOT NULL,
    account_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    amount INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);
```

---

## 4. Class Explanation

| Class | Responsibility |
|---|---|
| `Main.java` | Sets native system Look & Feel, initializes the SQLite database schema, and launches `LoginFrame` on the Swing Event Dispatch Thread (EDT). |
| `Database.java` | Encapsulates all JDBC logic with `PreparedStatement`, transaction management (`setAutoCommit(false)` / `commit()` / `rollback()`), foreign key enforcement, registration, login, deposits, withdrawals, transfers, and queries. |
| `LoginFrame.java` | Authenticates users against stored PBKDF2 password hashes, stores session state, and transitions to `DashboardFrame`. |
| `RegisterFrame.java` | Validates user input (email format, matching passwords, minimum length), hashes the password, generates a unique 10-digit account number, and creates the user and account records atomically. |
| `DashboardFrame.java` | Displays real-time SQLite balance, account number, status badge, quick action buttons, and a table showing the 5 most recent transactions. |
| `DepositFrame.java` | Allows cash deposits (> ₹0.00, max 2 decimals), updates the balance in SQLite, and writes a deposit transaction record in a single transaction. |
| `WithdrawFrame.java` | Verifies balance sufficiency, prevents negative balances, debits funds, and logs a withdrawal transaction record atomically. |
| `TransferFrame.java` | Validates recipient account, verifies sender balance, debits sender, credits recipient, and writes dual matched transaction records under a shared transfer reference (`TXN-TRF-...`) within a single SQL transaction. |
| `TransactionHistoryFrame.java` | Displays the complete transaction ledger in a Swing `JTable` (deposits, withdrawals, incoming/outgoing transfers) sorted newest first, with signed INR formatting and refresh capability. |
| `ProfileFrame.java` | Allows the user to view account details and update their full name in SQLite while locking account number and balance as read-only. |
| `PasswordUtil.java` | Generates cryptographically secure random salts and PBKDF2-HMAC-SHA256 hashes, with constant-time equality checking to mitigate timing attacks. |
| `MoneyUtil.java` | Converts user input strings directly into integer cents (paise) without float/double calculations, and formats cents to Indian Rupee strings (`₹ 1,500.00`). |
| `Session.java` | Stores the active authenticated user's ID, name, email, account ID, and account number in memory. |
| `UIHelper.java` | Provides a unified color palette, typography, styled buttons, padded text fields, and card panels for a modern Swing interface. |

---

## 5. Adding `sqlite-jdbc.jar` to Classpath in IDEs

The only dependency is `lib/sqlite-jdbc.jar`. Here is how to configure it in popular IDEs:

### IntelliJ IDEA
1. Open IntelliJ IDEA and open the `BankingApp` folder (`File` → `Open...`).
2. Go to **File** → **Project Structure** (or press `Ctrl+Alt+Shift+S` / `Cmd+;`).
3. Under **Project Settings**, select **Modules** (or **Libraries**).
4. Click the **+** (Add) button → select **JARs or Directories...**.
5. Navigate to `BankingApp/lib/sqlite-jdbc.jar` and click **OK**.
6. Ensure the `src` folder is marked as **Sources Root** (Right-click `src` → **Mark Directory as** → **Sources Root**).
7. Click **Apply** and **OK**.
8. Right-click `Main.java` and select **Run 'Main.main()'**.

### Eclipse IDE
1. Open Eclipse and choose **File** → **New** → **Java Project**.
2. Uncheck "Use default location" and browse to select the `BankingApp` folder.
3. Right-click the project in Project Explorer → **Build Path** → **Configure Build Path...**.
4. Go to the **Libraries** tab, select **Classpath** (if visible), and click **Add JARs...** (or **Add External JARs...**).
5. Browse to `BankingApp/lib/sqlite-jdbc.jar` and click **Open**.
6. Click **Apply and Close**.
7. Right-click `Main.java` → **Run As** → **Java Application**.

### Apache NetBeans
1. Open NetBeans and choose **File** → **Open Project...** or create a **Java Application with Existing Sources**.
2. Right-click the project in the Projects panel → **Properties**.
3. Select **Libraries** from Categories.
4. Under the **Compile** tab, click **Add JAR/Folder**.
5. Select `lib/sqlite-jdbc.jar` and click **Open**.
6. Click **OK**.
7. Right-click `Main.java` → **Run File** (or `Shift+F6`).

---

## 6. How to Compile and Run from the Terminal

Ensure you have Java JDK (Java 8 or later) installed on your system.

### On Linux / macOS:
```bash
# Navigate to the project root
cd BankingApp

# Compile all source files into the bin/ directory
mkdir -p bin
javac -cp "lib/sqlite-jdbc.jar" -d bin src/*.java

# Run the application
java -cp "bin:lib/sqlite-jdbc.jar" Main
```

### On Windows (Command Prompt / PowerShell):
```cmd
:: Navigate to the project root
cd BankingApp

:: Compile all source files into the bin directory
if not exist bin mkdir bin
javac -cp "lib\sqlite-jdbc.jar" -d bin src\*.java

:: Run the application (semicolon path separator on Windows)
java -cp "bin;lib\sqlite-jdbc.jar" Main
```

---

## 7. Step-by-Step Testing Instructions

Follow this test scenario to verify all core features:

### Step 1: User Registration
1. Launch the application. The **Login** screen appears.
2. Click **Register Now** at the bottom.
3. Fill in the form:
   - **Full Name:** `Alice Smith`
   - **Email Address:** `alice@example.com`
   - **Password:** `secret123`
   - **Confirm Password:** `secret123`
4. Click **Register & Open Account**.
5. A confirmation dialog will display:
   - Success message
   - Your newly generated 10-digit Account Number (e.g. `7631264934`)
   - Initial Balance: `₹ 0.00`
   - Status: `ACTIVE`
6. Click **OK**. You will be returned to the Login screen.

### Step 2: Login and Dashboard
1. On the Login screen, enter:
   - **Email:** `alice@example.com`
   - **Password:** `secret123`
2. Click **Login**.
3. The **Dashboard** opens showing:
   - Welcome message: `Welcome, Alice Smith`
   - Account Number: `A/C No: 7631264934`
   - Available Balance: `₹ 0.00`
   - Status: `Status: ACTIVE`

### Step 3: Test Cash Deposit
1. From the Dashboard, click **+ Deposit**.
2. In the Deposit screen, enter:
   - **Deposit Amount:** `5000.00`
   - **Description:** `Initial Salary Deposit`
3. Click **Confirm Deposit**.
4. A success dialog displays: `Deposit of ₹ 5,000.00 was successful! Updated Balance: ₹ 5,000.00`.
5. Click **Back** to return to the Dashboard. Notice the Dashboard balance is immediately updated to `₹ 5,000.00` and the transaction appears under Recent Transactions.

### Step 4: Test Cash Withdrawal & Insufficient Funds Check
1. From the Dashboard, click **- Withdraw**.
2. Test overdraft prevention:
   - Enter **Withdrawal Amount:** `10000.00`
   - Click **Confirm Withdrawal**.
   - Notice the error dialog: `Insufficient funds. Requested: ₹ 10,000.00 Available: ₹ 5,000.00`.
3. Test valid withdrawal:
   - Enter **Withdrawal Amount:** `1500.00`
   - Description: `ATM Withdrawal`
   - Click **Confirm Withdrawal**.
4. A success dialog displays: `Withdrawal of ₹ 1,500.00 was successful! Remaining Balance: ₹ 3,500.00`.
5. Click **Back**. The Dashboard balance updates to `₹ 3,500.00`.

### Step 5: Test Inter-Account Fund Transfer
1. Click **Logout** from the Dashboard and confirm logout.
2. Click **Register Now** to register a second user:
   - **Full Name:** `Bob Johnson`
   - **Email:** `bob@example.com`
   - **Password:** `password456`
   - **Confirm Password:** `password456`
3. Click **Register & Open Account**. Note Bob's Account Number from the dialog (e.g., `8688926155`).
4. Log back in as Alice (`alice@example.com` / `secret123`).
5. From the Dashboard, click **⇄ Transfer**.
6. Enter:
   - **To Account:** Bob's account number (e.g., `8688926155`)
   - **Transfer Amount:** `1000.50`
   - **Remarks:** `Dinner Split`
7. Click **Confirm Transfer** and click **Yes** in the confirmation popup.
8. Success dialog displays: `Transferred: ₹ 1,000.50 To Account: 8688926155 Remaining Balance: ₹ 2,499.50`.
9. Click **Back**. Alice's dashboard shows balance `₹ 2,499.50`.
10. Log out and log in as Bob (`bob@example.com` / `password456`). Bob's balance reflects `₹ 1,000.50` and the incoming transfer appears in his recent transactions.

### Step 6: Test Transaction History
1. Click **⟳ History** (or **View All History →**).
2. The complete ledger is displayed in a clean `JTable`:
   - Every transaction shows its unique Reference ID, Type, Amount (in green `+` or red `-`), Description, and Date/Time.
   - Click **⟳ Refresh** to reload records dynamically from SQLite.

### Step 7: Test Profile Name Update
1. Click **Profile**.
2. Update **Full Name** from `Bob Johnson` to `Robert Johnson`.
3. Click **Save Changes**.
4. Return to the Dashboard; the header displays `Welcome, Robert Johnson`.
5. Close the application and restart it: all balances, users, and transactions persist in `banking.db`.
