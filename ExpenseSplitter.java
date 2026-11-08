import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Splits shared expenses and prints who owes whom.
 *
 * Input (file argument or stdin), one expense per line:
 *     payer amount participant1,participant2,...   [# comment]
 * Example:
 *     Alice 60 Alice,Bob,Carol
 *     Bob 30.50 Alice,Bob
 *
 * Run: java ExpenseSplitter.java expenses.txt
 */
public class ExpenseSplitter {

    public static void main(String[] args) throws IOException {
        try (BufferedReader in = args.length > 0
                ? new BufferedReader(new FileReader(args[0]))
                : new BufferedReader(new InputStreamReader(System.in))) {
            Map<String, Long> balance = new TreeMap<>(); // cents; >0 is owed money
            String line;
            int n = 0;
            while ((line = in.readLine()) != null) {
                n++;
                int hash = line.indexOf('#');
                if (hash >= 0) line = line.substring(0, hash);
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    addExpense(line, balance);
                } catch (IllegalArgumentException e) {
                    System.err.println("Line " + n + ": " + e.getMessage());
                    System.exit(1);
                }
            }
            printSettlement(balance);
        }
    }

    static void addExpense(String line, Map<String, Long> balance) {
        String[] parts = line.split("\\s+");
        if (parts.length != 3)
            throw new IllegalArgumentException("expected: payer amount participant1,participant2,...");
        String payer = parts[0];
        long cents;
        try {
            cents = new BigDecimal(parts[1]).setScale(2, RoundingMode.HALF_UP)
                    .movePointRight(2).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException("invalid amount '" + parts[1] + "'");
        }
        if (cents <= 0) throw new IllegalArgumentException("amount must be positive");

        // de-duplicate participants, keep order
        List<String> people = new ArrayList<>(new LinkedHashSet<>(Arrays.asList(parts[2].split(","))));
        people.removeIf(String::isEmpty);
        if (people.isEmpty()) throw new IllegalArgumentException("no participants");

        // Split evenly; leftover cents go to the first participants so totals stay exact.
        long share = cents / people.size();
        long extra = cents % people.size();
        balance.merge(payer, cents, Long::sum);
        for (int i = 0; i < people.size(); i++) {
            balance.merge(people.get(i), -(share + (i < extra ? 1 : 0)), Long::sum);
        }
    }

    static void printSettlement(Map<String, Long> balance) {
        // Greedy matching of largest debtor with largest creditor => few transactions.
        PriorityQueue<Map.Entry<String, Long>> creditors = new PriorityQueue<>((a, b) -> Long.compare(b.getValue(), a.getValue()));
        PriorityQueue<Map.Entry<String, Long>> debtors = new PriorityQueue<>((a, b) -> Long.compare(b.getValue(), a.getValue()));
        for (Map.Entry<String, Long> e : balance.entrySet()) {
            if (e.getValue() > 0) creditors.add(new AbstractMap.SimpleEntry<>(e));
            else if (e.getValue() < 0) debtors.add(new AbstractMap.SimpleEntry<>(e.getKey(), -e.getValue()));
        }
        if (creditors.isEmpty()) {
            System.out.println("Everyone is settled up.");
            return;
        }
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<String, Long> c = creditors.poll();
            Map.Entry<String, Long> d = debtors.poll();
            long pay = Math.min(c.getValue(), d.getValue());
            System.out.printf("%s owes %s %s%n", d.getKey(), c.getKey(), money(pay));
            if (c.getValue() > pay) { c.setValue(c.getValue() - pay); creditors.add(c); }
            if (d.getValue() > pay) { d.setValue(d.getValue() - pay); debtors.add(d); }
        }
    }

    static String money(long cents) {
        return String.format("%d.%02d", cents / 100, cents % 100);
    }
}
