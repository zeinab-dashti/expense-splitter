# Prompt

Build a Java command-line tool that splits shared expenses. I give it who paid what and who shared each expense, and it prints who owes whom

# Final answer

I added a single-file Java tool, `ExpenseSplitter.java`, plus a sample input, `sample-expenses.txt`.

**Input format** (from a file argument or stdin), one expense per line:

```
payer amount participant1,participant2,...   # optional comment
```

**Run:**

```
java ExpenseSplitter.java sample-expenses.txt
```

**Sample output** (for the included sample file):

```
Bob owes Carol 29.75
Dave owes Carol 25.00
Alice owes Carol 0.25
```

**How it works**
- Amounts are stored as integer cents, so there are no floating-point errors.
- Each expense is split evenly among its participants. Any leftover cents go to the first participants, so totals always balance exactly.
- The payer doesn't have to be a participant.
- It computes each person's net balance, then repeatedly pairs the largest debtor with the largest creditor. This keeps the number of payments small.
- Bad input (wrong field count, invalid or non-positive amount, no participants) prints the line number and exits with status 1.

Limitations: only equal splits are supported (no custom shares or percentages), and names can't contain spaces or commas.

Nothing is committed or pushed. The work is on the new branch `chat-only-attempt`, created from `main`, and `main` is untouched.

## Probe runs (deliberate)

I chose these three probes on purpose, to see how the tool behaves in specific situations. No code was changed. The README has no build or run instructions, so I compiled with `javac -d <outdir> ExpenseSplitter.java` and ran `java -cp <outdir> ExpenseSplitter probes/<name>.txt`.

### 1. ten-three-ways (`probes/ten-three-ways.txt`)

Input:
```
Alice 10.00 Alice,Bob,Carol
```
Output:
```
Bob owes Alice 3.33
Carol owes Alice 3.33
```

### 2. settle (`probes/settle.txt`)

Input:
```
A 4 C
A 2 D
B 5 E
```
Output:
```
E owes A 5.00
C owes B 4.00
D owes A 1.00
D owes B 1.00
```

### 3. case (`probes/case.txt`)

Input:
```
Alice 10 Alice,Bob
alice 4 Bob
```
Output:
```
Bob owes Alice 5.00
Bob owes alice 4.00
```
