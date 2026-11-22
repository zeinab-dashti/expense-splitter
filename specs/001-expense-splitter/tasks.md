# Tasks: Expense Splitter

**Input**: Design documents from `/specs/001-expense-splitter/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/cli.md, quickstart.md
**Tests**: REQUIRED. Constitution Principle III: every spec rule needs an automated test that fails when the rule is broken. Tests are written before implementation within each story.
**Organization**: Grouped by user story. All paths are relative to the repository root. Package: `expensesplitter`. Main sources: `src/main/java/expensesplitter/`; tests: `src/test/java/expensesplitter/`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on incomplete tasks)
- **[Story]**: US1, US2, US3 (user story phases only)

---

## Phase 1: Setup

**Purpose**: Maven project that builds, tests and packages with `mvn verify`

- [X] T001 Create `pom.xml` at the repo root: `groupId expensesplitter`, `artifactId expense-splitter`, `maven.compiler.release=21`, UTF-8 encoding, test-scope dependency `org.junit.jupiter:junit-jupiter` 5.x, `maven-surefire-plugin` (a version that supports JUnit 5), and `maven-jar-plugin` with `finalName` `expense-splitter` and manifest `Main-Class: expensesplitter.Main`. No runtime dependencies (Constitution I).
- [X] T002 [P] Create the directories `src/main/java/expensesplitter/` and `src/test/java/expensesplitter/`, plus a `.gitignore` at the repo root containing `target/`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared types and money handling used by every story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 [P] Create `src/main/java/expensesplitter/InputException.java`: a checked exception holding `int lineNumber` and `String reason`, with `getLineNumber()` and `getReason()`. `getMessage()` returns `Line <n>: <reason>` (contracts/cli.md).
- [X] T004 [P] Write `src/test/java/expensesplitter/MoneyTest.java` covering: `30`→3000, `30.5`→3050, `30.50`→3050, `0`→0, `0.00`→0; `abc`, empty and `1.2.3` → reason `amount is not a number`; `-5` and `-0.50` → `amount is negative`; `1.234` → `amount has more than two decimal places`; formatting 1250→`12.50`, 5→`0.05`, 0→`0.00`, 100000→`1000.00`. Write it so it fails until T005 exists.
- [X] T005 Create `src/main/java/expensesplitter/Money.java` (depends on T003, T004): `static long parse(String text)` that validates with `BigDecimal` against `\d+(\.\d{1,2})?` (a leading `-` is reported as negative; more than 2 decimals is rejected) and throws an exception carrying the reason strings listed in T004 (use a package-private unchecked or checked helper that the parser converts to `InputException` with a line number); `static String format(long cents)` using integer division and modulo to always print two decimals. No `double`/`float` anywhere (Constitution, Build constraints).
- [X] T006 [P] Create `src/main/java/expensesplitter/Expense.java`: `record Expense(String payer, long amountCents, List<String> sharers, int lineNumber)`. Data-model constraints: `payer` "Required, non-blank after trim"; `amountCents` "≥ 0"; `sharers` "≥ 1 entry; no two entries equal under the name key (trim + lower-case)".
- [X] T007 [P] Create `src/main/java/expensesplitter/Settlement.java`: `record Settlement(String debtor, String creditor, long amountCents)` where `amountCents` is "> 0".

**Checkpoint**: `mvn test -Dtest=MoneyTest` passes; foundation ready.

---

## Phase 3: User Story 1 - See who owes whom (Priority: P1) 🎯 MVP

**Goal**: Read well-formed expenses, split equally in cents (payer absorbs leftover), net balances, print settlement lines.

**Independent Test**: `printf 'Alice, 30.00, Alice, Bob, Carol\n' | java -jar target/expense-splitter.jar` prints `Bob owes Alice 10.00` and `Carol owes Alice 10.00`, exit 0.

### Tests for User Story 1 (write first, confirm they fail)

- [X] T008 [P] [US1] Create `src/test/java/expensesplitter/SettlerTest.java` (balances part) covering FR-002/003/007: 30.00 among Alice (payer), Bob, Carol → Bob −1000, Carol −1000, Alice +2000; 10.00 among Alice (payer), Bob, Carol → Bob −333, Carol −333, Alice +666; payer not a sharer (10.00 paid by Alice for Bob, Carol) → Bob −333, Carol −333, Alice +666 and balances sum to 0; payer is the only sharer → all balances 0; zero-amount expense → all balances 0; two mutually paid 20.00 expenses between Alice and Bob → all balances 0; case/whitespace variants (`alice`, ` ALICE `) are one person and the first-seen spelling is displayed (FR-008); balances always sum to exactly 0.
- [X] T009 [P] [US1] Create `src/test/java/expensesplitter/ExpenseParserTest.java` (valid-input part) covering: line `Alice, 30.00, Alice, Bob, Carol` → payer `Alice`, 3000 cents, sharers `[Alice, Bob, Carol]`; fields are trimmed; blank lines and lines starting with `#` are skipped while physical 1-based line numbers are kept (a skipped first line makes the next expense `lineNumber` 2); amount without decimals (`30`) parses.
- [X] T010 [P] [US1] Create `src/test/java/expensesplitter/MainTest.java` (success part) calling `Main.run(String[] args, InputStream in, PrintStream out, PrintStream err)` with in-memory streams: quickstart scenarios 1, 2 (`Nobody owes anything.`), 4 and 6 (empty input → `Nobody owes anything.`, exit 0); file-argument input read from a temp file via `@TempDir`; `-` argument reads stdin; stderr is empty and exit code is 0 in all of these.

### Implementation for User Story 1

- [X] T011 [US1] Create `src/main/java/expensesplitter/ExpenseParser.java`: `static List<Expense> parse(List<String> lines) throws InputException`. Split each non-blank, non-`#` line on `,`, trim fields; field 0 = payer, field 1 = amount via `Money.parse`, remaining fields = sharers; record the 1-based physical line number. (Validation of bad input is added in US3.)
- [X] T012 [US1] Create `src/main/java/expensesplitter/Settler.java` with `static Map<String, Long> balances(List<Expense>)` (keys are name keys = trim + lower-case with `Locale.ROOT`; also keep a first-seen display-name map, scanning top to bottom, payer before sharers within a line, per research §6) implementing the data-model rule: share = `amountCents / n` (floor); for each sharer ≠ payer: `balance[s] −= share; balance[payer] += share`. Payer is never given a debt for leftover cents. Expose the display-name lookup needed by the settle step.
- [X] T013 [US1] In `src/main/java/expensesplitter/Settler.java` add `static List<Settlement> settle(List<Expense>)` returning settlements that zero every balance, using a simple two-pointer match of debtors to creditors in name order (the greedy largest-first strategy replaces this in US2). Result is sorted by debtor then creditor, case-insensitive (FR-012). Uses display names in the output.
- [X] T014 [US1] Create `src/main/java/expensesplitter/Main.java`: `public static void main` calls `System.exit(run(args, System.in, System.out, System.err))`; `static int run(String[] args, InputStream in, PrintStream out, PrintStream err)` reads UTF-8 lines from stdin (no args or `-`) or from the single file argument, calls `ExpenseParser.parse` then `Settler.settle`, prints one `<debtor> owes <creditor> <amount>` line per settlement using `Money.format`, or `Nobody owes anything.` when there are no settlements, to `out`; returns 0. (Error and usage handling is added in US3.)
- [X] T015 [US1] Run `mvn verify`; make T008–T010 pass. Run quickstart scenarios 1, 2, 4 against `target/expense-splitter.jar`.

**Checkpoint**: Story 1 works end to end and is demoable (MVP).

---

## Phase 4: User Story 2 - Fewest payments to settle up (Priority: P2)

**Goal**: Greedy largest-debtor/largest-creditor matching, giving at most N-1 settlement lines.

**Independent Test**: `printf 'Bob, 10, Alice\nCarol, 10, Bob\n' | java -jar target/expense-splitter.jar` prints the single line `Alice owes Carol 10.00`.

### Tests for User Story 2 (write first, confirm they fail)

- [X] T016 [P] [US2] Extend `src/test/java/expensesplitter/SettlerTest.java` with settlement tests: chain (Bob pays 10 for Alice, Carol pays 10 for Bob) → exactly one settlement `Alice → Carol 1000`; at most N−1 lines for a 4-person example where the two-pointer approach would give more; tie-breaking by name (case-insensitive) is deterministic when two debtors/creditors have equal amounts; output order is by debtor then creditor, case-insensitive (FR-012).
- [X] T017 [P] [US2] Extend `src/test/java/expensesplitter/SettlerTest.java` with a randomized property test using `new Random(42)` (fixed seed), ≥ 200 iterations of random valid expenses (2–10 people, 1–50 expenses, amounts 0–10000 cents): applying all settlements zeroes every balance (FR-005), settlement count ≤ (people with non-zero balance) − 1 (FR-006), total owed equals total received (SC-002), every settlement amount is > 0.
- [X] T018 [P] [US2] Extend `src/test/java/expensesplitter/MainTest.java`: quickstart scenario 3 (chain collapses to `Alice owes Carol 10.00`); running the same input twice yields byte-identical stdout (SC-005).

### Implementation for User Story 2

- [X] T019 [US2] In `src/main/java/expensesplitter/Settler.java` replace the two-pointer matching in `settle` with the greedy algorithm from research §5: repeatedly pair the largest creditor with the largest debtor (ties broken by name key, case-insensitive), transfer `min(credit, debt)`, remove whoever reaches zero; finally sort by debtor then creditor, case-insensitive.
- [X] T020 [US2] Run `mvn verify`; make T016–T018 pass and confirm quickstart scenario 3.

**Checkpoint**: Stories 1 and 2 both work; output is minimal-ish (≤ N−1) and deterministic.

---

## Phase 5: User Story 3 - Clear feedback on bad input (Priority: P3)

**Goal**: Line-numbered errors on stderr, nothing on stdout, non-zero exit code.

**Independent Test**: `printf 'Alice, abc, Bob\n' | java -jar target/expense-splitter.jar; echo "exit=$?"` prints `Line 1: amount is not a number` on stderr, nothing on stdout, `exit=1`.

### Tests for User Story 3 (write first, confirm they fail)

- [X] T021 [P] [US3] Extend `src/test/java/expensesplitter/ExpenseParserTest.java` with one test per FR-009 case, each asserting line number and exact reason: missing payer (`, 10, Bob`) → `missing payer`; missing amount (`Alice`) → `missing amount`; non-numeric (`Alice, abc, Bob`) → `amount is not a number`; negative (`Alice, -5, Bob`) → `amount is negative`; `Alice, 1.234, Bob` → `amount has more than two decimal places`; no sharers (`Alice, 10`) → `no sharers`; duplicate sharer including case/whitespace variants (`Alice, 10, Bob, bob`) → `duplicate sharer "bob"`; an error on physical line 3 after a comment and a blank line reports line 3. Also: zero amount (`Alice, 0, Bob`) is accepted.
- [X] T022 [P] [US3] Extend `src/test/java/expensesplitter/MainTest.java`: invalid input → exit 1, stdout empty, stderr contains `Line <n>: <reason>` (quickstart scenario 5); a valid line plus an invalid line → still no stdout and exit 1 (no partial settlement, FR-010); multiple invalid lines → one stderr message per invalid line, in line order; two arguments → exit 2, stderr `Usage: expense-splitter [FILE | -]`, stdout empty; non-existent file → exit 2, stderr `Cannot read file: <path>`, stdout empty.

### Implementation for User Story 3

- [X] T023 [US3] In `src/main/java/expensesplitter/ExpenseParser.java` add validation, converting `Money` failures into `InputException(lineNumber, reason)`: payer blank after trim → `missing payer`; fewer than 2 fields or blank amount → `missing amount`; zero sharers (after trimming, ignoring blank fields) → `no sharers`; two sharers with the same name key → `duplicate sharer "<name>"` (name as written on the later occurrence). Reason strings must match contracts/cli.md exactly.
- [X] T024 [US3] In `src/main/java/expensesplitter/ExpenseParser.java` make parsing collect every invalid line instead of stopping at the first: provide `static ParseResult parseAll(List<String> lines)` (or throw an aggregate exception exposing `List<InputException>`) so `Main` can report all of them in line order.
- [X] T025 [US3] In `src/main/java/expensesplitter/Main.java` handle failures: invalid lines → print `Line <n>: <reason>` for each to `err`, print nothing to `out`, return 1; argument count > 1 → print `Usage: expense-splitter [FILE | -]` to `err`, return 2; `IOException` reading a file → print `Cannot read file: <path>` to `err`, return 2. Normal output stays on `out` only (FR-013).
- [X] T026 [US3] Run `mvn verify`; make T021–T022 pass and confirm quickstart scenario 5.

**Checkpoint**: All three stories are independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T027 [P] Add `README.md` at the repo root with build (`mvn verify`), run (`java -jar target/expense-splitter.jar [FILE | -]`), input format and exit codes, copied from contracts/cli.md.
- [X] T028 Verify Constitution III traceability: for every row of the "Spec-to-test traceability" table in plan.md and every edge case in spec.md, confirm a test exists; add any missing test (e.g. FR-008 first-spelling display, FR-011, SC-001 worked examples from the spec).
- [X] T029 Confirm there is no `float`/`double` usage in `src/main` (`grep -rnE "\b(double|float)\b" src/main` returns nothing) and that `pom.xml` has no runtime dependencies.
- [X] T030 Run the full quickstart.md (all 7 scenarios) against the built jar, then run `mvn verify` from a clean checkout state (`mvn clean verify`) to confirm the single-command build.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (1)** → **Foundational (2)** → user stories → **Polish (6)**.
- **US1 (P1)**: after Phase 2; no dependency on other stories.
- **US2 (P2)**: after US1 (it replaces `Settler.settle` internals and extends the same test files).
- **US3 (P3)**: after US1 (extends `ExpenseParser` and `Main`); independent of US2 in behaviour, but it edits the same files, so run sequentially unless the work is split carefully.

### Within a story

Tests first (they must fail) → parser/model → `Settler` → `Main` → full `mvn verify`.

### Parallel Opportunities

- Phase 1: T002 alongside T001.
- Phase 2: T003, T004, T006, T007 together (T005 needs T003 and T004).
- US1 tests: T008, T009, T010 together (separate files).
- US2 tests: T016, T017, T018 touch two files; T016/T017 both edit `SettlerTest.java`, so do T016 and T017 in one pass or sequentially, and T018 in parallel with them.
- US3 tests: T021 and T022 together (different files).
- Different stories can be worked in parallel only if the shared files (`Settler.java`, `ExpenseParser.java`, `Main.java`) are merged carefully.

### Parallel Example: User Story 1 tests

```text
Task: "Balances tests in src/test/java/expensesplitter/SettlerTest.java"
Task: "Parser tests in src/test/java/expensesplitter/ExpenseParserTest.java"
Task: "End-to-end tests in src/test/java/expensesplitter/MainTest.java"
```

---

## Implementation Strategy

### MVP First

1. Phase 1 → Phase 2 → Phase 3 (US1). Stop and validate with quickstart scenarios 1, 2, 4, 6.

### Incremental Delivery

1. Add US2 (greedy settlement) → validate scenario 3 and the property test.
2. Add US3 (error handling) → validate scenario 5 and exit codes.
3. Polish.

---

## Notes

- Spec-to-test mapping: FR-001/009 → ExpenseParserTest (T009, T021); FR-002/003/007/008 → SettlerTest (T008); FR-004/012 → MoneyTest, SettlerTest, MainTest (T004, T016, T010); FR-005/006/SC-002 → SettlerTest (T017); FR-010/011/013 → MainTest (T010, T022); SC-005 → MainTest (T018).
- Known inconsistency in `research.md` §4: it says the payer's balance net is "+6.67" in the 10.00 example, but the data-model rule (payer gains `share` per non-payer sharer, so +6.66) is the one consistent with the "balances sum to 0" invariant. Tasks follow `data-model.md`; the tasks and tests above use +666.
- Commit after each task or logical group.
