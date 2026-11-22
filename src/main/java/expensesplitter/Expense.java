package expensesplitter;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** One expense: who paid, how much (in cents) and who shares it. */
public record Expense(String payer, long amountCents, List<String> sharers, int lineNumber) {

    public Expense {
        if (payer == null || payer.isBlank()) {
            throw new IllegalArgumentException("payer must not be blank");
        }
        if (amountCents < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        if (sharers == null || sharers.isEmpty()) {
            throw new IllegalArgumentException("at least one sharer is required");
        }
        Set<String> keys = new HashSet<>();
        for (String sharer : sharers) {
            if (!keys.add(nameKey(sharer))) {
                throw new IllegalArgumentException("duplicate sharer: " + sharer);
            }
        }
        sharers = List.copyOf(sharers);
    }

    /** The identity of a person: trimmed and lower-cased. */
    static String nameKey(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
