# expense-splitter

A small Java command-line tool that splits shared expenses. It reads one expense per line, splits each equally (in exact cents), and prints the fewest-ish payments needed to settle up.

## Build and test

Requires JDK 21+ and Maven 3.9+.

```bash
mvn verify
```

This runs all tests and produces `target/expense-splitter.jar`.

## Run

```bash
java -jar target/expense-splitter.jar [FILE | -]
```

- No argument or `-`: read expenses from standard input.
- One `FILE`: read expenses from that file (UTF-8).

## Input format

One expense per line:

```text
payer, amount, sharer1, sharer2, ...
```

- Fields are separated by commas; surrounding whitespace is trimmed.
- `amount` is a non-negative decimal with at most two decimal places (`30`, `30.5`, `30.50`).
- At least one sharer is required; the payer may or may not be listed.
- Blank lines and lines starting with `#` are ignored.
- Names are case-insensitive; the first spelling seen is displayed.
- Uneven splits are rounded down per sharer and the payer absorbs the leftover cents.

## Output

Settlement lines on stdout, sorted by debtor then creditor (case-insensitive):

```text
Bob owes Alice 10.00
Carol owes Alice 10.00
```

If nothing needs settling: `Nobody owes anything.`

## Errors and exit codes

| Exit | Meaning | stderr |
|------|---------|--------|
| 0 | Success (including "Nobody owes anything.") | empty |
| 1 | Invalid expense line(s); nothing on stdout | `Line <n>: <reason>` per invalid line |
| 2 | Wrong number of arguments, or file unreadable | `Usage: expense-splitter [FILE \| -]` or `Cannot read file: <path>` |

Reasons: `missing payer`, `missing amount`, `amount is not a number`, `amount is negative`, `amount has more than two decimal places`, `no sharers`, `duplicate sharer "<name>"`.
