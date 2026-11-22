package expensesplitter;

/** A payment of {@code amountCents} from {@code debtor} to {@code creditor}. */
public record Settlement(String debtor, String creditor, long amountCents) {

    public Settlement {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("settlement amount must be positive");
        }
    }
}
