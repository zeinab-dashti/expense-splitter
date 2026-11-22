package expensesplitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Command-line entry point: a thin wrapper around the core classes. */
public final class Main {

    private Main() {}

    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, System.err));
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        if (args.length > 1) {
            err.println("Usage: expense-splitter [FILE | -]");
            return 2;
        }
        List<String> lines;
        try {
            lines = readLines(args, in);
        } catch (IOException e) {
            err.println("Cannot read file: " + args[0]);
            return 2;
        }
        ExpenseParser.ParseResult parsed = ExpenseParser.parseAll(lines);
        if (!parsed.errors().isEmpty()) {
            parsed.errors().forEach(e -> err.println(e.getMessage()));
            return 1;
        }
        List<Settlement> settlements = Settler.settle(parsed.expenses());
        if (settlements.isEmpty()) {
            out.println("Nobody owes anything.");
        }
        for (Settlement s : settlements) {
            out.println(s.debtor() + " owes " + s.creditor() + " " + Money.format(s.amountCents()));
        }
        return 0;
    }

    private static List<String> readLines(String[] args, InputStream in) throws IOException {
        if (args.length == 0 || args[0].equals("-")) {
            Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            return new BufferedReader(reader).lines().toList();
        }
        return Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);
    }
}
