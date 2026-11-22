# Implementation Plan: Expense Splitter

**Branch**: `001-expense-splitter` (git branch `article-1-first-spec`) | **Date**: 2026-11-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-expense-splitter/spec.md`

## Summary

A plain-Java 21 command-line tool that reads one expense per line (file argument or stdin), splits each expense equally in integer cents (payer absorbs leftover cents), nets every person's balance, and prints at most N-1 settlement lines using a greedy largest-debtor/largest-creditor matching. Invalid input produces a line-numbered error on stderr, no settlement, and a non-zero exit code. All logic lives in a testable core library; `Main` is a thin I/O wrapper.

## Technical Context

**Language/Version**: Java 21 (source and target)

**Primary Dependencies**: None at runtime (JDK only). Test-only: JUnit Jupiter 5.x

**Storage**: N/A (stateless, single run)

**Testing**: JUnit 5 via Maven Surefire; unit tests for the core plus end-to-end tests of the CLI entry point with in-memory streams

**Target Platform**: Any OS with a JDK 21+ runtime

**Project Type**: CLI

**Performance Goals**: Instant for the stated scale (10 people, 50 expenses); no special optimisation

**Constraints**: Exact arithmetic (`long` cents, no floating point); deterministic output; stdout for results, stderr for errors

**Scale/Scope**: Up to ~10 people and ~50 expenses per run; code must still behave correctly for larger inputs

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Plain Java 21, No Framework | JDK-only runtime; JUnit is test-scope only | PASS |
| II. Command-Line Interface | Args/stdin in, stdout results, stderr errors, non-zero exit on failure; logic outside `Main` | PASS |
| III. Every Rule Has an Automated Test | Traceability table below maps each FR/edge case to tests; tests run via `mvn verify`, offline once dependencies are cached | PASS |
| Build constraint: single command builds + tests | `mvn verify` produces the jar and runs all tests | PASS |
| Build constraint: exact money arithmetic | Amounts parsed into `long` cents via `BigDecimal`; no `double`/`float` | PASS |

**Post-design re-check (after Phase 1)**: PASS. No violations; Complexity Tracking is empty.

## Project Structure

### Documentation (this feature)

```text
specs/001-expense-splitter/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── cli.md
└── tasks.md             # created later by /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml
src/
├── main/java/expensesplitter/
│   ├── Main.java               # CLI entry: read args/stdin, call core, print, exit code
│   ├── ExpenseParser.java      # text lines -> Expense list; throws InputException(line, reason)
│   ├── InputException.java     # carries line number + message
│   ├── Expense.java            # record: payer, amountCents, sharers
│   ├── Settler.java            # balances + greedy settlement + ordering
│   ├── Settlement.java         # record: debtor, creditor, amountCents
│   └── Money.java              # parse "12.50" <-> cents; format cents as 2dp
└── test/java/expensesplitter/
    ├── MoneyTest.java
    ├── ExpenseParserTest.java
    ├── SettlerTest.java
    └── MainTest.java           # end-to-end: stdout/stderr/exit code
```

**Structure Decision**: Single Maven project, one package. Parsing, calculation and formatting are separate classes so each is unit-testable without launching a process (Principle II).

### Spec-to-test traceability (Principle III)

| Rule | Test class |
|------|-----------|
| FR-001, FR-009 (missing payer/amount, non-numeric, negative, >2 decimals, no sharers, duplicate sharer) | ExpenseParserTest |
| FR-002, FR-003, FR-007 (equal split, leftover cents to payer, payer not a sharer, payer sole sharer, zero amount) | SettlerTest |
| FR-004, FR-012 (format, deterministic ordering by debtor then creditor, case-insensitive) | SettlerTest, MoneyTest |
| FR-005, FR-006, SC-002 (zero balances after settling, ≤ N-1 lines, owed = received) | SettlerTest (incl. randomized property test with fixed seed) |
| FR-008 (case/whitespace, first spelling displayed) | ExpenseParserTest, SettlerTest |
| FR-010, FR-011, FR-013 (no output + failure on bad input; "nobody owes anything"; stdout/stderr split; empty input) | MainTest |
| SC-005 (identical repeat output) | MainTest |
| User Story 1-3 acceptance scenarios | SettlerTest / MainTest |

## Complexity Tracking

No constitution violations; nothing to justify.
