package expensesplitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void parsesValidAmounts() {
        assertEquals(3000, Money.parse("30"));
        assertEquals(3050, Money.parse("30.5"));
        assertEquals(3050, Money.parse("30.50"));
        assertEquals(0, Money.parse("0"));
        assertEquals(0, Money.parse("0.00"));
    }

    @Test
    void rejectsNonNumbers() {
        for (String text : new String[] {"abc", "", "1.2.3"}) {
            assertReason("amount is not a number", text);
        }
    }

    @Test
    void rejectsNegatives() {
        assertReason("amount is negative", "-5");
        assertReason("amount is negative", "-0.50");
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertReason("amount has more than two decimal places", "1.234");
    }

    @Test
    void formatsWithTwoDecimals() {
        assertEquals("12.50", Money.format(1250));
        assertEquals("0.05", Money.format(5));
        assertEquals("0.00", Money.format(0));
        assertEquals("1000.00", Money.format(100000));
    }

    private static void assertReason(String reason, String text) {
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> Money.parse(text));
        assertEquals(reason, e.getMessage());
    }
}
