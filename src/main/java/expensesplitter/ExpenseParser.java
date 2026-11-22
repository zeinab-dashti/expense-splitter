package expensesplitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Turns text lines into {@link Expense} objects, validating each line. */
final class ExpenseParser {

    /** Everything that was parsed, plus every invalid line in line order. */
    record ParseResult(List<Expense> expenses, List<InputException> errors) {}

    private ExpenseParser() {}

    /** Parses all lines; throws the first invalid line's error. */
    static List<Expense> parse(List<String> lines) throws InputException {
        ParseResult result = parseAll(lines);
        if (!result.errors().isEmpty()) {
            throw result.errors().get(0);
        }
        return result.expenses();
    }

    /** Parses all lines, collecting every invalid line instead of stopping at the first. */
    static ParseResult parseAll(List<String> lines) {
        List<Expense> expenses = new ArrayList<>();
        List<InputException> errors = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.trim().startsWith("#")) {
                continue;
            }
            try {
                expenses.add(parseLine(line, i + 1));
            } catch (InputException e) {
                errors.add(e);
            }
        }
        return new ParseResult(expenses, errors);
    }

    private static Expense parseLine(String line, int lineNumber) throws InputException {
        String[] fields = line.split(",", -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        String payer = fields[0];
        if (payer.isEmpty()) {
            throw new InputException(lineNumber, "missing payer");
        }
        if (fields.length < 2 || fields[1].isEmpty()) {
            throw new InputException(lineNumber, "missing amount");
        }
        long cents;
        try {
            cents = Money.parse(fields[1]);
        } catch (IllegalArgumentException e) {
            throw new InputException(lineNumber, e.getMessage());
        }
        List<String> sharers = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 2; i < fields.length; i++) {
            String sharer = fields[i];
            if (sharer.isEmpty()) {
                continue;
            }
            if (!seen.add(Expense.nameKey(sharer))) {
                throw new InputException(lineNumber, "duplicate sharer \"" + sharer + "\"");
            }
            sharers.add(sharer);
        }
        if (sharers.isEmpty()) {
            throw new InputException(lineNumber, "no sharers");
        }
        return new Expense(payer, cents, sharers, lineNumber);
    }
}
