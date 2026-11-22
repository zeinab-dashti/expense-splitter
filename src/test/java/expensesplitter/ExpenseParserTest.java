package expensesplitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExpenseParserTest {

    @Test
    void parsesValidLine() throws Exception {
        Expense e = ExpenseParser.parse(List.of("Alice, 30.00, Alice, Bob, Carol")).get(0);
        assertEquals("Alice", e.payer());
        assertEquals(3000, e.amountCents());
        assertEquals(List.of("Alice", "Bob", "Carol"), e.sharers());
        assertEquals(1, e.lineNumber());
    }

    @Test
    void trimsFields() throws Exception {
        Expense e = ExpenseParser.parse(List.of("  Alice ,  5 ,  Bob  ")).get(0);
        assertEquals("Alice", e.payer());
        assertEquals(List.of("Bob"), e.sharers());
    }

    @Test
    void skipsBlankAndCommentLinesKeepingPhysicalLineNumbers() throws Exception {
        List<Expense> list = ExpenseParser.parse(List.of("# header", "", "   ", "Alice, 10, Bob"));
        assertEquals(1, list.size());
        assertEquals(4, list.get(0).lineNumber());
        assertEquals(2, ExpenseParser.parse(List.of("# c", "Alice, 10, Bob")).get(0).lineNumber());
    }

    @Test
    void amountWithoutDecimalsParses() throws Exception {
        assertEquals(3000, ExpenseParser.parse(List.of("Alice, 30, Bob")).get(0).amountCents());
    }

    // ---- invalid input (US3) ----

    private static void assertError(int line, String reason, String... lines) {
        InputException e = assertThrows(InputException.class, () -> ExpenseParser.parse(List.of(lines)));
        assertEquals(line, e.getLineNumber());
        assertEquals(reason, e.getReason());
    }

    @Test
    void missingPayer() {
        assertError(1, "missing payer", ", 10, Bob");
    }

    @Test
    void missingAmount() {
        assertError(1, "missing amount", "Alice");
        assertError(1, "missing amount", "Alice, , Bob");
    }

    @Test
    void nonNumericAmount() {
        assertError(1, "amount is not a number", "Alice, abc, Bob");
    }

    @Test
    void negativeAmount() {
        assertError(1, "amount is negative", "Alice, -5, Bob");
    }

    @Test
    void tooManyDecimals() {
        assertError(1, "amount has more than two decimal places", "Alice, 1.234, Bob");
    }

    @Test
    void noSharers() {
        assertError(1, "no sharers", "Alice, 10");
        assertError(1, "no sharers", "Alice, 10, ,");
    }

    @Test
    void duplicateSharerIgnoringCaseAndWhitespace() {
        assertError(1, "duplicate sharer \"bob\"", "Alice, 10, Bob, bob");
        assertError(1, "duplicate sharer \"BOB\"", "Alice, 10, Bob,  BOB ");
    }

    @Test
    void errorReportsPhysicalLineNumber() {
        assertError(3, "amount is not a number", "# comment", "", "Alice, x, Bob");
    }

    @Test
    void zeroAmountIsAccepted() throws Exception {
        assertEquals(0, ExpenseParser.parse(List.of("Alice, 0, Bob")).get(0).amountCents());
    }

    @Test
    void parseAllCollectsEveryInvalidLineInOrder() {
        ExpenseParser.ParseResult r = ExpenseParser.parseAll(List.of(
                "Alice, 10, Bob", "Alice, abc, Bob", "# c", ", 5, Bob", "Bob, 2, Alice"));
        assertEquals(2, r.expenses().size());
        assertEquals(2, r.errors().size());
        assertEquals(2, r.errors().get(0).getLineNumber());
        assertEquals(4, r.errors().get(1).getLineNumber());
    }
}
