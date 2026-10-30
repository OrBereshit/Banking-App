/**
 * Utility for monetary calculations and formatting in Indian Rupees (INR).
 * All balances and amounts are stored and calculated as integer cents (paise)
 * to avoid floating-point rounding errors.
 */
public class MoneyUtil {

    /**
     * Parses a user-inputted INR string (e.g., "150", "150.5", "150.50") into integer cents (paise).
     * Strictly disallows negative values and more than 2 decimal places.
     * Does NOT use float or double arithmetic.
     *
     * @param input Raw amount string from text field
     * @return Amount in cents (paise)
     * @throws IllegalArgumentException if format is invalid or amount is <= 0
     */
    public static long parseInrToCents(String input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }

        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty.");
        }

        // Must be positive digits with optional up to 2 decimal places
        if (!trimmed.matches("^[0-9]+(\\.[0-9]{1,2})?$")) {
            throw new IllegalArgumentException("Please enter a valid positive amount with up to 2 decimal places (e.g. 500 or 500.50).");
        }

        String[] parts = trimmed.split("\\.");
        long rupees;
        try {
            rupees = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount is too large.");
        }

        long paise = 0;
        if (parts.length > 1) {
            String decimalPart = parts[1];
            if (decimalPart.length() == 1) {
                paise = Long.parseLong(decimalPart) * 10;
            } else if (decimalPart.length() == 2) {
                paise = Long.parseLong(decimalPart);
            }
        }

        // Prevent overflow
        if (rupees > (Long.MAX_VALUE - paise) / 100) {
            throw new IllegalArgumentException("Amount exceeds maximum supported limit.");
        }

        long totalCents = rupees * 100 + paise;
        if (totalCents <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        return totalCents;
    }

    /**
     * Formats integer cents into a standard INR currency string (e.g., ₹1,250.50).
     *
     * @param cents Amount in cents (paise)
     * @return Formatted currency string
     */
    public static String formatCentsToInr(long cents) {
        boolean negative = cents < 0;
        long absCents = Math.abs(cents);
        long rupees = absCents / 100;
        long paise = absCents % 100;

        String formatted = String.format("₹ %,d.%02d", rupees, paise);
        return negative ? "-" + formatted : formatted;
    }

    /**
     * Formats integer cents with an explicit + or - sign prefix for transaction logs.
     *
     * @param cents Amount in cents (paise)
     * @param isCredit true for incoming funds (+), false for outgoing funds (-)
     * @return Formatted signed currency string
     */
    public static String formatSignedCents(long cents, boolean isCredit) {
        long absCents = Math.abs(cents);
        long rupees = absCents / 100;
        long paise = absCents % 100;
        String sign = isCredit ? "+ " : "- ";
        return sign + String.format("₹ %,d.%02d", rupees, paise);
    }
}
