package expensesplitter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SettlerTest {

    private static Expense expense(String payer, long cents, String... sharers) {
        return new Expense(payer, cents, List.of(sharers), 1);
    }

    private static long sum(Map<String, Long> balances) {
        return balances.values().stream().mapToLong(Long::longValue).sum();
    }

    // ---- balances (US1) ----

    @Test
    void evenSplitAmongThree() {
        Map<String, Long> b = Settler.balances(List.of(expense("Alice", 3000, "Alice", "Bob", "Carol")));
        assertEquals(-1000, b.get("bob"));
        assertEquals(-1000, b.get("carol"));
        assertEquals(2000, b.get("alice"));
        assertEquals(0, sum(b));
    }

    @Test
    void unevenSplitPayerAbsorbsLeftoverCents() {
        Map<String, Long> b = Settler.balances(List.of(expense("Alice", 1000, "Alice", "Bob", "Carol")));
        assertEquals(-333, b.get("bob"));
        assertEquals(-333, b.get("carol"));
        assertEquals(666, b.get("alice"));
        assertEquals(0, sum(b));
    }

    @Test
    void payerNotASharer() {
        Map<String, Long> b = Settler.balances(List.of(expense("Alice", 1000, "Bob", "Carol", "Dave")));
        assertEquals(-333, b.get("bob"));
        assertEquals(-333, b.get("carol"));
        assertEquals(-333, b.get("dave"));
        assertEquals(999, b.get("alice"));
        assertEquals(0, sum(b));
    }

    @Test
    void payerIsOnlySharer() {
        Map<String, Long> b = Settler.balances(List.of(expense("Alice", 1000, "Alice")));
        assertEquals(0, b.get("alice"));
    }

    @Test
    void zeroAmountChangesNothing() {
        Map<String, Long> b = Settler.balances(List.of(expense("Alice", 0, "Alice", "Bob")));
        assertEquals(0, b.get("alice"));
        assertEquals(0, b.get("bob"));
    }

    @Test
    void mutualExpensesCancel() {
        Map<String, Long> b = Settler.balances(List.of(
                expense("Alice", 2000, "Alice", "Bob"),
                expense("Bob", 2000, "Alice", "Bob")));
        assertEquals(0, b.get("alice"));
        assertEquals(0, b.get("bob"));
    }

    @Test
    void caseAndWhitespaceVariantsAreOnePerson() {
        List<Expense> expenses = List.of(
                expense("Alice", 1000, "Alice", "Bob"),
                expense(" ALICE ", 1000, "alice", "BOB"));
        Map<String, Long> b = Settler.balances(expenses);
        assertEquals(2, b.size());
        assertEquals(1000, b.get("alice"));
        assertEquals(-1000, b.get("bob"));
    }

    @Test
    void firstSeenSpellingIsDisplayed() {
        List<Expense> expenses = List.of(
                expense("Alice", 1000, "Bob"),
                expense("ALICE", 1000, "bob"));
        Settlement s = Settler.settle(expenses).get(0);
        assertEquals("Bob", s.debtor());
        assertEquals("Alice", s.creditor());
        assertEquals(2000, s.amountCents());
    }

    @Test
    void settleSimpleCase() {
        List<Settlement> result = Settler.settle(List.of(expense("Alice", 3000, "Alice", "Bob", "Carol")));
        assertEquals(List.of(new Settlement("Bob", "Alice", 1000), new Settlement("Carol", "Alice", 1000)), result);
    }

    // ---- settlement (US2) ----

    @Test
    void chainCollapsesToOneSettlement() {
        List<Settlement> result = Settler.settle(List.of(
                expense("Bob", 1000, "Alice"),
                expense("Carol", 1000, "Bob")));
        assertEquals(List.of(new Settlement("Alice", "Carol", 1000)), result);
    }

    @Test
    void greedyUsesFewerLinesThanNameOrderMatching() {
        // Balances: A -600, B -400, C +400, D +600.
        // Name-order two-pointer needs 3 lines (A>C 400, A>D 200, B>D 400); greedy needs 2.
        List<Expense> expenses = List.of(
                expense("D", 600, "A"),
                expense("C", 400, "B"));
        List<Settlement> result = Settler.settle(expenses);
        assertEquals(List.of(new Settlement("A", "D", 600), new Settlement("B", "C", 400)), result);
    }

    @Test
    void tiesAreBrokenByNameDeterministically() {
        // Bob and Carol each owe 20.00; Alice and Dave are each owed 20.00.
        List<Expense> expenses = List.of(
                expense("Dave", 2000, "Bob", "Carol"),
                expense("Alice", 2000, "Bob", "Carol"));
        List<Settlement> expected = List.of(
                new Settlement("Bob", "Alice", 2000),
                new Settlement("Carol", "Dave", 2000));
        assertEquals(expected, Settler.settle(expenses));
        assertEquals(expected, Settler.settle(expenses));
    }

    @Test
    void outputIsOrderedByDebtorThenCreditorIgnoringCase() {
        List<Settlement> result = Settler.settle(List.of(expense("alice", 3000, "Zed", "bob", "alice")));
        assertEquals("bob", result.get(0).debtor());
        assertEquals("Zed", result.get(1).debtor());
    }

    @Test
    void randomizedProperties() {
        Random random = new Random(42);
        for (int iteration = 0; iteration < 300; iteration++) {
            int people = 2 + random.nextInt(9);
            int count = 1 + random.nextInt(50);
            List<Expense> expenses = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String payer = "P" + random.nextInt(people);
                List<String> sharers = new ArrayList<>();
                for (int p = 0; p < people; p++) {
                    if (random.nextBoolean()) {
                        sharers.add("P" + p);
                    }
                }
                if (sharers.isEmpty()) {
                    sharers.add("P" + random.nextInt(people));
                }
                expenses.add(new Expense(payer, random.nextInt(10001), sharers, i + 1));
            }
            Map<String, Long> balances = Settler.balances(expenses);
            assertEquals(0, sum(balances));
            List<Settlement> settlements = Settler.settle(expenses);
            Map<String, Long> after = new HashMap<>(balances);
            for (Settlement s : settlements) {
                after.merge(Expense.nameKey(s.debtor()), s.amountCents(), Long::sum);
                after.merge(Expense.nameKey(s.creditor()), -s.amountCents(), Long::sum);
                assertTrue(s.amountCents() > 0);
            }
            after.values().forEach(v -> assertEquals(0L, v));
            long nonZero = balances.values().stream().filter(v -> v != 0).count();
            assertTrue(settlements.size() <= Math.max(0, nonZero - 1), "more than N-1 settlements");
            long owed = balances.values().stream().filter(v -> v > 0).mapToLong(Long::longValue).sum();
            assertEquals(owed, settlements.stream().mapToLong(Settlement::amountCents).sum());
        }
    }
}
