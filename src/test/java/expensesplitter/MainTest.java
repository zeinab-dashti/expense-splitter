package expensesplitter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private record Result(int exit, String out, String err) {}

    private static Result run(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exit = Main.run(
                args,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
        return new Result(exit, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    private static final String NL = System.lineSeparator();

    // ---- success (US1) ----

    @Test
    void basicSplit() {
        Result r = run("Alice, 30.00, Alice, Bob, Carol\n");
        assertEquals(0, r.exit);
        assertEquals("Bob owes Alice 10.00" + NL + "Carol owes Alice 10.00" + NL, r.out);
        assertEquals("", r.err);
    }

    @Test
    void everyoneEven() {
        Result r = run("Alice, 20, Alice, Bob\nBob, 20, Alice, Bob\n");
        assertEquals(0, r.exit);
        assertEquals("Nobody owes anything." + NL, r.out);
        assertEquals("", r.err);
    }

    @Test
    void unevenSplitPayerAbsorbsCents() {
        Result r = run("Alice, 10.00, Alice, Bob, Carol\n");
        assertEquals(0, r.exit);
        assertEquals("Bob owes Alice 3.33" + NL + "Carol owes Alice 3.33" + NL, r.out);
    }

    @Test
    void emptyInput() {
        Result r = run("");
        assertEquals(0, r.exit);
        assertEquals("Nobody owes anything." + NL, r.out);
        assertEquals("", r.err);
    }

    @Test
    void readsFileArgument(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("expenses.txt");
        Files.writeString(file, "Alice, 30.00, Alice, Bob, Carol\n", StandardCharsets.UTF_8);
        Result r = run("", file.toString());
        assertEquals(0, r.exit);
        assertEquals("Bob owes Alice 10.00" + NL + "Carol owes Alice 10.00" + NL, r.out);
        assertEquals("", r.err);
    }

    @Test
    void dashReadsStdin() {
        Result r = run("Alice, 30.00, Alice, Bob, Carol\n", "-");
        assertEquals(0, r.exit);
        assertEquals("Bob owes Alice 10.00" + NL + "Carol owes Alice 10.00" + NL, r.out);
    }

    @Test
    void chainCollapses() {
        Result r = run("Bob, 10, Alice\nCarol, 10, Bob\n");
        assertEquals(0, r.exit);
        assertEquals("Alice owes Carol 10.00" + NL, r.out);
    }

    @Test
    void sameInputTwiceGivesIdenticalOutput() {
        String input = "Alice, 10.00, Alice, Bob, Carol\nBob, 7.30, Alice, Dave\nDave, 3, Carol\n";
        assertEquals(run(input).out, run(input).out);
    }

    // ---- failures (US3) ----

    @Test
    void invalidInputGoesToStderrWithExit1() {
        Result r = run("Alice, abc, Bob\n");
        assertEquals(1, r.exit);
        assertEquals("", r.out);
        assertEquals("Line 1: amount is not a number" + NL, r.err);
    }

    @Test
    void validPlusInvalidLinePrintsNoSettlement() {
        Result r = run("Alice, 30, Alice, Bob\nBob, -1, Alice\n");
        assertEquals(1, r.exit);
        assertEquals("", r.out);
        assertEquals("Line 2: amount is negative" + NL, r.err);
    }

    @Test
    void everyInvalidLineIsReportedInOrder() {
        Result r = run("Alice, x, Bob\nAlice, 5, Bob\n, 5, Bob\nAlice\n");
        assertEquals(1, r.exit);
        assertEquals("", r.out);
        assertEquals("Line 1: amount is not a number" + NL
                + "Line 3: missing payer" + NL
                + "Line 4: missing amount" + NL, r.err);
    }

    @Test
    void twoArgumentsIsUsageError() {
        Result r = run("", "a.txt", "b.txt");
        assertEquals(2, r.exit);
        assertEquals("", r.out);
        assertEquals("Usage: expense-splitter [FILE | -]" + NL, r.err);
    }

    @Test
    void missingFileIsReported(@TempDir Path dir) {
        String missing = dir.resolve("nope.txt").toString();
        Result r = run("", missing);
        assertEquals(2, r.exit);
        assertEquals("", r.out);
        assertEquals("Cannot read file: " + missing + NL, r.err);
    }
}
