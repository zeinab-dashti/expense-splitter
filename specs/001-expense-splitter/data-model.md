# Data Model: Expense Splitter

All types are in-memory only; nothing is persisted.

## Expense (record)

| Field | Type | Rules |
|-------|------|-------|
| `payer` | String (display name) | Required, non-blank after trim |
| `amountCents` | long | ≥ 0; parsed from `\d+(\.\d{1,2})?`; negative, non-numeric, missing, or >2 decimals rejected |
| `sharers` | List<String> | ≥ 1 entry; no two entries equal under the name key (trim + lower-case) |
| `lineNumber` | int | 1-based source line, used only for error messages |

## Person

Not a class: identified by name key (trimmed, lower-cased, `Locale.ROOT`). A `Map<String key, String displayName>` records the first-seen spelling.

## Balance

`Map<String key, Long cents>`: positive = is owed, negative = owes. Invariant: the sum over all people is exactly 0.

Per expense with payer P, amount A, sharers S (n = |S|), share = A / n (floor):
- For each sharer s ≠ P: balance[s] −= share; balance[P] += share.
- Leftover cents (A − share·n) and the payer's own share therefore stay with the payer and never create a debt.

Example: A = 10.00, S = {Alice(P), Bob, Carol}: share = 3.33 → Bob −3.33, Carol −3.33, Alice +6.66. (Alice's own 3.34 = share + 1 leftover cent.)

When the payer is not a sharer: every sharer −share, payer +share·(n); the payer absorbs A − share·n.

## Settlement (record)

| Field | Type | Rules |
|-------|------|-------|
| `debtor` | String (display name) | Balance was negative |
| `creditor` | String (display name) | Balance was positive |
| `amountCents` | long | > 0 |

Invariants: applying all settlements zeroes every balance; count ≤ (people with non-zero balance) − 1; list sorted by debtor then creditor, case-insensitive.

## InputException

`lineNumber` (int) + `reason` (String). Rendered as `Line <n>: <reason>` on stderr.

## State transitions

Linear pipeline with no stored state: lines → `List<Expense>` (or `InputException`) → balances → `List<Settlement>` → formatted text.
