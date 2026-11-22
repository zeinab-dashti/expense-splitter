package expensesplitter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Computes balances and settlements from expenses. */
final class Settler {

    private Settler() {}

    /** First-seen display spelling per name key, scanning top to bottom, payer before sharers. */
    static Map<String, String> displayNames(List<Expense> expenses) {
        Map<String, String> names = new LinkedHashMap<>();
        for (Expense e : expenses) {
            names.putIfAbsent(Expense.nameKey(e.payer()), e.payer().trim());
            for (String s : e.sharers()) {
                names.putIfAbsent(Expense.nameKey(s), s.trim());
            }
        }
        return names;
    }

    /** Net cents per person key: positive = is owed, negative = owes. Always sums to 0. */
    static Map<String, Long> balances(List<Expense> expenses) {
        Map<String, Long> balances = new LinkedHashMap<>();
        for (String key : displayNames(expenses).keySet()) {
            balances.put(key, 0L);
        }
        for (Expense e : expenses) {
            String payer = Expense.nameKey(e.payer());
            long share = e.amountCents() / e.sharers().size();
            for (String sharer : e.sharers()) {
                String key = Expense.nameKey(sharer);
                if (!key.equals(payer)) {
                    balances.merge(key, -share, Long::sum);
                    balances.merge(payer, share, Long::sum);
                }
            }
        }
        return balances;
    }

    /**
     * Greedy matching: repeatedly pair the largest creditor with the largest debtor
     * (ties broken by name key), transfer the smaller amount, and drop whoever reaches zero.
     * Every step zeroes at least one person, so there are at most N-1 settlements.
     */
    static List<Settlement> settle(List<Expense> expenses) {
        Map<String, String> names = displayNames(expenses);
        Map<String, Long> remaining = new TreeMap<>();
        balances(expenses).forEach((key, cents) -> {
            if (cents != 0) {
                remaining.put(key, cents);
            }
        });
        List<Settlement> result = new ArrayList<>();
        while (!remaining.isEmpty()) {
            String debtor = null;
            String creditor = null;
            for (Map.Entry<String, Long> entry : remaining.entrySet()) { // TreeMap: name order
                long cents = entry.getValue();
                if (cents < 0 && (debtor == null || cents < remaining.get(debtor))) {
                    debtor = entry.getKey();
                } else if (cents > 0 && (creditor == null || cents > remaining.get(creditor))) {
                    creditor = entry.getKey();
                }
            }
            long amount = Math.min(-remaining.get(debtor), remaining.get(creditor));
            result.add(new Settlement(names.get(debtor), names.get(creditor), amount));
            adjust(remaining, debtor, amount);
            adjust(remaining, creditor, -amount);
        }
        result.sort(Comparator
                .comparing((Settlement s) -> Expense.nameKey(s.debtor()))
                .thenComparing(s -> Expense.nameKey(s.creditor())));
        return result;
    }

    private static void adjust(Map<String, Long> remaining, String key, long delta) {
        long updated = remaining.get(key) + delta;
        if (updated == 0) {
            remaining.remove(key);
        } else {
            remaining.put(key, updated);
        }
    }
}
