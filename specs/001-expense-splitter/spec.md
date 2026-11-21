# Feature Specification: Expense Splitter

**Feature Branch**: `article-1-first-spec`

**Created**: 2026-11-21

**Status**: Draft

**Input**: User description: "Build a Java command-line tool that splits shared expenses. I give it who paid what and who shared each expense, and it prints who owes whom"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - See who owes whom (Priority: P1)

A person who has shared costs with friends (a trip, a dinner, a household) lists each expense: who paid, how much, and who shared it. They run the tool and get a short list of settlement lines such as "Bob owes Alice 12.50", so everyone knows exactly what to pay back.

**Why this priority**: This is the whole purpose of the tool. Without it there is no product.

**Independent Test**: Provide a small set of expenses with known totals and confirm the printed settlement lines match hand-calculated results.

**Acceptance Scenarios**:

1. **Given** Alice paid 30.00 for an expense shared equally by Alice, Bob and Carol, **When** the tool runs, **Then** it prints "Bob owes Alice 10.00" and "Carol owes Alice 10.00".
2. **Given** Alice paid 20.00 for an expense shared by Alice and Bob, and Bob paid 20.00 for a different expense shared by Alice and Bob, **When** the tool runs, **Then** it reports that nobody owes anything.
3. **Given** several expenses paid by different people with different sharers, **When** the tool runs, **Then** each person's net position (paid minus own share) is reflected, so that the total owed equals the total to be received.

---

### User Story 2 - Fewest payments to settle up (Priority: P2)

Rather than listing every pairwise debt, the tool nets everyone's balances and prints the smallest practical set of payments, so the group makes as few transfers as possible.

**Why this priority**: It makes the output genuinely useful, but a correct (non-minimal) result from Story 1 is already usable.

**Independent Test**: Provide a chain of debts (A owes B, B owes C) and confirm the output collapses them into fewer payments than the raw debts.

**Acceptance Scenarios**:

1. **Given** Alice owes Bob 10.00 and Bob owes Carol 10.00 through separate expenses, **When** the tool runs, **Then** it prints a single line "Alice owes Carol 10.00".
2. **Given** any set of expenses with N people, **When** the tool runs, **Then** it prints at most N-1 settlement lines.

---

### User Story 3 - Clear feedback on bad input (Priority: P3)

When the input is malformed or inconsistent (missing amount, negative amount, expense with no sharers), the tool says what is wrong and which input line caused it, instead of printing a misleading result.

**Why this priority**: Protects trust in the numbers, but only matters once the core calculation works.

**Independent Test**: Feed each kind of invalid input and confirm an error message is shown, no settlement is printed, and the tool signals failure.

**Acceptance Scenarios**:

1. **Given** an expense with a non-numeric amount, **When** the tool runs, **Then** it reports an error naming the offending line and prints no settlement.
2. **Given** an expense with no sharers, **When** the tool runs, **Then** it reports an error naming the offending line.

---

### Edge Cases

- Amount does not divide evenly (e.g., 10.00 among 3 people): shares are calculated in whole cents and the leftover cents are assigned deterministically so the shares always add up exactly to the amount paid.
- The payer is not among the sharers (paid for others only): the payer is owed the full amount by the sharers.
- The payer is the only sharer: the expense has no effect on anyone's balance.
- Zero-amount expense: accepted, no effect on balances.
- Negative amount: rejected as invalid.
- Empty input (no expenses): prints a message that there is nothing to settle and succeeds.
- Same person's name written with different capitalization or surrounding spaces: treated as the same person.
- Everyone already even: prints that nobody owes anything.
- Amounts with more than two decimal places: rejected as invalid.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The tool MUST accept a list of expenses, each stating the payer, the amount paid, and the people who shared it.
- **FR-002**: The tool MUST split each expense equally among its sharers.
- **FR-003**: The tool MUST compute each person's net balance as total paid minus total of their shares across all expenses.
- **FR-004**: The tool MUST print settlement lines of the form "<debtor> owes <creditor> <amount>", with amounts shown to exactly two decimal places.
- **FR-005**: The tool MUST produce a settlement set in which following every line leaves every person with a zero balance.
- **FR-006**: The tool MUST produce no more than N-1 settlement lines for N people with non-zero balances.
- **FR-007**: The tool MUST use exact cent-level arithmetic; split shares MUST always sum exactly to the expense amount, with any leftover cents assigned in a deterministic way (the same input always gives the same output).
- **FR-008**: The tool MUST treat names as case-insensitive and ignore surrounding whitespace, displaying each person under the spelling first used.
- **FR-009**: The tool MUST reject, with an error message naming the offending input line, any expense that has a missing or non-numeric amount, a negative amount, more than two decimal places, a missing payer, or no sharers.
- **FR-010**: When input is invalid, the tool MUST print no settlement lines and MUST signal failure to the caller.
- **FR-011**: When there is nothing to settle (no expenses, or all balances zero), the tool MUST print a clear "nobody owes anything" message and signal success.
- **FR-012**: The tool MUST print the same settlement lines in the same order for the same input (stable ordering).
- **FR-013**: Normal output MUST go to standard output; error messages MUST go to standard error.

### Key Entities

- **Person**: A participant identified by name; may be a payer, a sharer, or both.
- **Expense**: A single shared cost with one payer, an amount in money, and one or more sharers.
- **Balance**: A person's net position across all expenses; positive means they are owed money, negative means they owe.
- **Settlement**: One payment instruction: a debtor, a creditor, and an amount.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For every worked example in this spec, the printed result matches the hand-calculated answer exactly.
- **SC-002**: For any valid input, the total amount owed in the output equals the total amount to be received, to the cent.
- **SC-003**: A user can enter a group's expenses and read the final settlement in under 2 minutes for a group of up to 10 people and 50 expenses.
- **SC-004**: 100% of invalid inputs in the edge-case list produce an error message that identifies the offending line, and none produce a settlement.
- **SC-005**: Running the tool twice on the same input produces identical output.

## Assumptions

- All expenses are in a single currency; no currency symbols or conversion are handled.
- Every expense is split equally among its sharers; unequal splits (percentages or fixed amounts) are out of scope for this version.
- Input is provided as plain text, one expense per line (via a file, piped input, or typed input), with a simple human-readable format to be defined during planning.
- Leftover cents from uneven splits are assigned in a fixed, predictable order (for example, to sharers in the order they are listed).
- The tool is single-run and stateless: nothing is saved between runs, and there are no user accounts.
- Settlement minimization targets a small number of payments (at most N-1); finding the mathematically optimal minimum is not required.
- Per the project constitution, the tool is a command-line program and every rule above is covered by an automated test.
