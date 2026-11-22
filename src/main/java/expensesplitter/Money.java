package expensesplitter;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Exact money handling in integer cents. */
final class Money {

    private static final Pattern VALID = Pattern.compile("\\d+(\\.\\d{1,2})?");
    private static final Pattern TOO_PRECISE = Pattern.compile("\\d+\\.\\d{3,}");
    private static final Pattern PLAIN_NUMBER = Pattern.compile("\\d+(\\.\\d+)?");

    private Money() {}

    /**
     * Parses text such as {@code 30}, {@code 30.5} or {@code 30.50} into cents.
     *
     * @throws IllegalArgumentException whose message is the reason shown to the user
     */
    static long parse(String text) {
        if (VALID.matcher(text).matches()) {
            return new BigDecimal(text).movePointRight(2).longValueExact();
        }
        if (text.startsWith("-") && PLAIN_NUMBER.matcher(text.substring(1)).matches()) {
            throw new IllegalArgumentException("amount is negative");
        }
        if (TOO_PRECISE.matcher(text).matches()) {
            throw new IllegalArgumentException("amount has more than two decimal places");
        }
        throw new IllegalArgumentException("amount is not a number");
    }

    /** Formats cents with exactly two decimals, e.g. 5 becomes {@code 0.05}. */
    static String format(long cents) {
        long units = cents / 100;
        long rest = cents % 100;
        return units + "." + (rest < 10 ? "0" : "") + rest;
    }
}
