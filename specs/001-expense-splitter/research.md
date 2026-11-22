# Research: Expense Splitter

No NEEDS CLARIFICATION items remained in the Technical Context; the decisions below close the open points the spec deferred to planning.

## 1. Input format

- **Decision**: One expense per line: `payer, amount, sharer1, sharer2, ...` (comma-separated, fields trimmed). Blank lines and lines starting with `#` are ignored. Line numbers in errors are 1-based physical line numbers.
- **Rationale**: Trivial to type, easy to parse with no dependencies, human-readable, and the first two fields map directly to payer and amount.
- **Alternatives considered**: `Alice paid 30 for Bob Carol` (natural language: ambiguous parsing, names with spaces break); JSON/CSV with header (heavier than a 50-line hand-typed input needs); interactive prompts (not scriptable, violates the stdin protocol).

## 2. Input source

- **Decision**: Zero args or `-` reads stdin; one arg is a file path. More than one arg is a usage error.
- **Rationale**: Satisfies the constitution (args or stdin) and spec (file, pipe, or typed input).
- **Alternatives considered**: Flags like `--file`: unnecessary for one input.

## 3. Money representation

- **Decision**: `long` cents. Parse with `BigDecimal` to validate: must match `\d+(\.\d{1,2})?`; more than two decimals, signs other than a leading `-` (reported as negative), or non-numeric text are rejected. Format via integer division to always show two decimals.
- **Rationale**: Exact, and the "rounded down, payer absorbs remainder" rule is plain integer division and modulo.
- **Alternatives considered**: `BigDecimal` everywhere (more ceremony, same result); `double` (forbidden by constitution).

## 4. Split rule

- **Decision**: `share = amountCents / n` (floor); each sharer other than the payer owes `share`; the payer's balance gains `amountCents - share * (number of non-payer sharers)`, i.e. the payer absorbs all remainder cents. Net per expense: payer +amount, each non-payer sharer −share; payer's own share is implicitly the remainder.
- **Rationale**: Matches FR-007 and the 10.00/3 example (Bob −3.33, Carol −3.33, Alice +6.67 net, her share 3.34). Works when the payer is not a sharer (payer is owed the sum of shares, absorbing remainder) and when the payer is the sole sharer (no balance change).
- **Alternatives considered**: Distribute leftover cents round-robin (rejected by clarification).

## 5. Settlement algorithm

- **Decision**: Compute balances; repeatedly match the largest creditor with the largest debtor (ties broken by name, case-insensitive), transfer `min(credit, debt)`, drop whoever reaches zero. Finally sort lines by debtor then creditor (case-insensitive).
- **Rationale**: Each step zeroes at least one person and the last step zeroes two, so lines ≤ N−1 (FR-006). Deterministic (FR-012, SC-005). The spec does not require the mathematically minimum count, which is NP-hard in general.
- **Alternatives considered**: Subset-sum partitioning for the true minimum (unnecessary complexity, explicitly out of scope).

## 6. Name handling

- **Decision**: Key = trimmed, lower-cased name (`Locale.ROOT`); display name = first spelling seen, scanning input top to bottom, payer before sharers within a line. Empty names after trimming count as missing.
- **Rationale**: FR-008. Duplicate-sharer detection (FR-009) uses the same key.

## 7. Build and test tooling

- **Decision**: Maven, `maven.compiler.release=21`, JUnit Jupiter 5, Surefire; `maven-jar-plugin` sets `Main-Class` so `java -jar target/expense-splitter.jar` works. `mvn verify` is the single build+test command.
- **Rationale**: Ubiquitous, one file, single command per the constitution.
- **Alternatives considered**: Gradle (equivalent, needs wrapper files); javac + manual JUnit console launcher (no standard single command).

## 8. Exit codes and output

- **Decision**: 0 success (including "nobody owes anything"); 1 invalid input (error on stderr, nothing on stdout); 2 usage error or unreadable file. Core is `static int run(args, InputStream, PrintStream out, PrintStream err)` so tests need no process.
- **Rationale**: FR-010, FR-013, Principle II.
