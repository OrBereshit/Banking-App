/**
 * Application session state manager.
 * Holds credentials and account metadata for the currently authenticated user.
 */
public class Session {

    private static int userId = -1;
    private static String userName = "";
    private static String userEmail = "";
    private static int accountId = -1;
    private static String accountNumber = "";

    public static synchronized void setSession(int uId, String name, String email, int accId, String accNumber) {
        userId = uId;
        userName = name;
        userEmail = email;
        accountId = accId;
        accountNumber = accNumber;
    }

    public static synchronized boolean isLoggedIn() {
        return userId > 0 && accountId > 0;
    }

    public static synchronized int getUserId() {
        return userId;
    }

    public static synchronized String getUserName() {
        return userName;
    }

    public static synchronized void setUserName(String name) {
        userName = name;
    }

    public static synchronized String getUserEmail() {
        return userEmail;
    }

    public static synchronized int getAccountId() {
        return accountId;
    }

    public static synchronized String getAccountNumber() {
        return accountNumber;
    }

    public static synchronized void clear() {
        userId = -1;
        userName = "";
        userEmail = "";
        accountId = -1;
        accountNumber = "";
    }
}
