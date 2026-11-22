# CLI Contract: expense-splitter

## Invocation

```text
java -jar target/expense-splitter.jar [FILE | -]
```

- No argument or `-`: read expenses from standard input.
- One `FILE`: read expenses from that file (UTF-8).
- Any other argument count: usage error.

## Input format

One expense per line:

```text
payer, amount, sharer1, sharer2, ...
```

- Fields separated by commas; surrounding whitespace trimmed.
- `amount`: non-negative decimal with at most two decimal places (`30`, `30.5`, `30.50`).
- At least one sharer; the payer may or may not be listed.
- Blank lines and lines starting with `#` are ignored.
- Names are case-insensitive; the first spelling seen is displayed.

## Output (stdout, exit 0)

Settlement lines, one per line, sorted by debtor then creditor (case-insensitive):

```text
<debtor> owes <creditor> <amount>
```

`<amount>` always has exactly two decimals. If there is nothing to settle (no expenses or all balances zero):

```text
Nobody owes anything.
```

## Errors (stderr, nothing on stdout)

| Condition | Message form | Exit |
|-----------|--------------|------|
| Invalid expense line | `Line <n>: <reason>` (reasons: missing payer, missing amount, amount is not a number, amount is negative, amount has more than two decimal places, no sharers, duplicate sharer "<name>") | 1 |
| Wrong number of arguments | `Usage: expense-splitter [FILE | -]` | 2 |
| File cannot be read | `Cannot read file: <path>` | 2 |

All invalid lines may be reported (one message each); at minimum the first is reported. No settlement is ever printed alongside an error.

## Examples

Input:
```text
Alice, 30.00, Alice, Bob, Carol
```
Output:
```text
Bob owes Alice 10.00
Carol owes Alice 10.00
```

Input:
```text
Alice, abc, Bob
```
stderr: `Line 1: amount is not a number`, exit 1.
