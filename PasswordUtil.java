import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Standard Java password hashing utility using PBKDF2 with HMAC-SHA256.
 * Adheres to standard Java cryptography standards without third-party dependencies.
 */
public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Hashes a plaintext password with a random cryptographically secure salt.
     *
     * @param password Password characters
     * @return Formatted string containing salt:hash (Base64 encoded)
     */
    public static String hashPassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with PBKDF2: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies a candidate password against the stored salt:hash string.
     * Uses constant-time comparison to prevent timing attacks.
     *
     * @param password Candidate password characters
     * @param storedHash Stored salt:hash string
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(char[] password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] computedHash = skf.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(computedHash, expectedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
