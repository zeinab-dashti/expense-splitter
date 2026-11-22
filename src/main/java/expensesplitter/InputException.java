package expensesplitter;

/** A problem with one line of input. Rendered as {@code Line <n>: <reason>}. */
public class InputException extends Exception {

    private final int lineNumber;
    private final String reason;

    public InputException(int lineNumber, String reason) {
        super("Line " + lineNumber + ": " + reason);
        this.lineNumber = lineNumber;
        this.reason = reason;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getReason() {
        return reason;
    }
}
