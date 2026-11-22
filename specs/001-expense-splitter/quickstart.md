# Quickstart: Validating the Expense Splitter

Prerequisites: JDK 21+, Maven 3.9+. Formats and messages: see [contracts/cli.md](contracts/cli.md); rules: see [data-model.md](data-model.md).

## Build and run all tests (single command)

```bash
mvn verify
```

Expected: build succeeds, all tests pass, `target/expense-splitter.jar` exists.

## Scenarios

Run each from the repository root.

1. **Basic split (Story 1)**
   ```bash
   printf 'Alice, 30.00, Alice, Bob, Carol\n' | java -jar target/expense-splitter.jar
   ```
   Expect `Bob owes Alice 10.00` and `Carol owes Alice 10.00`, exit 0.

2. **Everyone even**
   ```bash
   printf 'Alice, 20, Alice, Bob\nBob, 20, Alice, Bob\n' | java -jar target/expense-splitter.jar
   ```
   Expect `Nobody owes anything.`, exit 0.

3. **Chain collapses (Story 2)**
   ```bash
   printf 'Bob, 10, Alice\nCarol, 10, Bob\n' | java -jar target/expense-splitter.jar
   ```
   Expect the single line `Alice owes Carol 10.00`.

4. **Uneven split, payer absorbs cents**
   ```bash
   printf 'Alice, 10.00, Alice, Bob, Carol\n' | java -jar target/expense-splitter.jar
   ```
   Expect `Bob owes Alice 3.33` and `Carol owes Alice 3.33`.

5. **Bad input (Story 3)**
   ```bash
   printf 'Alice, abc, Bob\n' | java -jar target/expense-splitter.jar; echo "exit=$?"
   ```
   Expect `Line 1: amount is not a number` on stderr, nothing on stdout, `exit=1`.

6. **File argument and empty input**
   ```bash
   java -jar target/expense-splitter.jar expenses.txt
   printf '' | java -jar target/expense-splitter.jar
   ```
   Expect settlement lines for the file; `Nobody owes anything.` for empty input; exit 0.

7. **Determinism (SC-005)**: run any scenario twice and compare output; it must be identical.
