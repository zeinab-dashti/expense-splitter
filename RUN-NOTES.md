# Run notes

Spec Kit v1.3.2, Claude Code in VS Code, branch article-1-first-spec.

## Step 1: constitution 

Command I typed:
/speckit-constitution Plain Java 21 with no application framework. Every rule in the spec has an automated test. Runs from the command line.

Result: .specify/memory/constitution.md, 55 lines, version 1.0.0. I gave 21 words.
Everything I typed is in the file, as three principles.

What it added that I did not ask for:
- Money rule: amounts must use exact arithmetic (BigDecimal or integer cents), never float or double. I gave no money rule. It probably came from the project name.
- Test-only libraries such as JUnit are allowed. I only said no application framework.
- One command must build the artifact and run all tests. This forces a build tool into the plan.
- Business logic must live outside the command-line entry point.
- Every spec rule must be traceable to its tests in the plan or tasks.
- A rule without a test counts as unimplemented. Tests run unattended with no network.
- Rationale lines, a Governance section with semantic versioning and a ratification date. These come from the template.

What the constitution leaves open: who absorbs the leftover cent in an uneven split, whether names are case-sensitive, how settlement picks payments and the input format. The spec has to settle those.

Decision: I kept the file exactly as generated, because a beginner gets the same file.

Questions it asked me: none  
Errors: no errors  
Surprises:
- The agent's reply listed four additions I did not ask for: the money rule, one build command for the full test suite, traceability from each spec rule to its tests and all tests passing before a change is merged. The file holds more. JUnit being allowed, business logic outside the entry point and tests running with no network were not in its list.
- It reordered my sentences (Java, command line, tests instead of Java, tests, command line).
- It labeled the test principle NON-NEGOTIABLE, which I did not write.
- An HTML comment sits at the top of the file. It is a Sync Impact Report, and the command's own instructions ask for it. I expected it to be gone. It is the only comment left, because the template's example comments were all removed. I left it.

## Step 2: specify

Command I typed:
/speckit-specify Build a Java command-line tool that splits shared expenses. I give it who paid what and who shared each expense, and it prints who owes whom

Result: specs/001-expense-splitter/spec.md and checklists/requirements.md. No new git branch. The agent said no extension hooks were registered, so none ran. The spec header only records the branch I was already on.
Size: 3 user stories, 13 functional requirements, 5 success criteria, 9 edge cases and 7 assumptions, from two short sentences of mine.
Questions it asked me: none.
Its own checklist: all 16 items ticked on the first pass, with no clarification markers. The same session that wrote the spec graded it.

What its reply told me:
- It listed three assumptions: one currency with equal splits, a fixed order for leftover cents and plain text input with the format left for planning. The spec's Assumptions section has seven.
- The decisions that change behavior most sit in the requirements and edge cases, not in that list. The reply names the capitalization of names, zero amounts and negative amounts as edge cases covered and does not say which way it decided. It does not mention that amounts with more than two decimals are rejected, or that invalid input prints no settlement.
- It described the second story as "settle with the fewest payments (at most N-1)". It does not mention that the spec says the mathematical minimum is not required.
- It ended with "ready for /speckit-clarify or /speckit-plan" and made no commit.

Beyond my sentences, it added two stories: fewest payments to settle up and clear feedback on bad input.

Decisions it made without asking:
- Names are case-insensitive and surrounding whitespace is ignored (FR-008).
- Amounts with more than two decimals are rejected (FR-009).
- A zero amount is accepted and a negative amount is rejected.
- Invalid input prints no settlement at all and signals failure (FR-010).
- Empty input prints a "nothing to settle" message and succeeds (FR-011).
- A payer who is not a sharer is owed the full amount.

Left for the plan: the input format and which sharer absorbs the leftover cent. For the cent the spec only says "for example, in the order they are listed".

Settlement: the story is titled "Fewest payments to settle up". The requirement says at most N-1 lines (FR-006) and the assumptions say the mathematical minimum is not required. The title promises more than the requirement.

Compared with the chat-only attempt:
- Names: the chat-only code treated alice and Alice as two people. The spec makes them one.
- Decimals: the chat-only code accepted 10.005 and rounded it to 10.01 without a warning. The spec rejects it.
- Zero amounts: the chat-only code rejected them. The spec accepts them.
- Settlement: the chat-only greedy code gave 4 payments where 3 suffice. That still meets "at most N-1", so this spec would accept it.

Two things I noticed when reading:
- SC-003 asks for a person to finish in under 2 minutes. An automated test cannot check a person's time. The constitution says every rule in a spec needs an automated test, and the spec's Assumptions says every rule above has one. If a success criterion counts as a rule, SC-003 breaks that.
- N is defined two ways. FR-006 counts people with non-zero balances. Story 2 and the Assumptions count people. FR-006 is stricter, so nothing breaks, but a test could use either.

Decision: I kept the spec exactly as generated.  
Errors or surprises in the session: no errors. The surprises are the ones listed above.

## Step 3: clarify

Command I typed:
/speckit-clarify

Result: a new Clarifications section in spec.md plus edits in five other places. Nothing else changed. It asked 3 questions and stopped, although the limit is 5. Its reason: the remaining gaps were low-impact.

Questions, its recommendation and my answer:
1. Leftover cents. It recommended A (extra cents to sharers in listed order). I answered C: the payer absorbs the leftover cents.
2. The same person listed twice as a sharer. It recommended A (count them once). I answered C: reject the line.
3. Order of the settlement lines. It recommended A (largest debtor with largest creditor first). I answered B: alphabetical by debtor, then by creditor.
It recommended option A all three times. I overrode all three.

How I answered: I talked through each question with Claude in a separate chat before replying. Its suggestions were C, C and B, and those were my answers.

Sections touched: Clarifications (new, one line per answer), Edge Cases (the leftover-cent line rewritten with a worked example, plus a new duplicate-sharer line), FR-007, FR-009, FR-012 and Assumptions. The Clarifications section records only my short answers and not the reasons.
It added "case-insensitive" to the sort rule. I had not said that.

Checklist: 16 of 16 still pass. It left the file alone, although the checklist's note says the cent ordering is left to planning. The agent called that note out of date itself. It left it because the command's instructions allow editing only the checkboxes.

What it did not catch:
- The Edge Cases line "the payer is not among the sharers: the payer is owed the full amount by the sharers" was not updated. It is false for an uneven split. Example: Dave pays 10.00 for Alice, Bob and Carol. Each sharer owes 3.33 and Dave absorbs 0.01, so Dave is owed 9.99. FR-007 says the payer absorbs the cents even when the payer is not a sharer. Story 1's third scenario ("paid minus own share") has the same problem.
- The two issues I noted in Step 2 (SC-003 and the two meanings of N) are untouched.

What it left open: how debtors are matched with creditors. In its words, the spec only says the output is sorted and the plan should choose a deterministic pairing. So the plan decides which payments the user sees, while Story 2 still promises "fewest payments". The input format is also left to planning.

Surprises:
- A question about print order carried a decision about the algorithm, because option A described the pairing method.
- Its reason for recommending A on question 1 was that it matched its own earlier example. Its reason on question 2 was "the result users most likely expect", which is a guess about users.
- One silent decision in the spec (names are case-insensitive) produced a later question (duplicates that differ by case).

## Step 4: plan

Command I typed:
/speckit-plan

Result: five new files under specs/001-expense-splitter/ (plan.md, research.md, data-model.md, contracts/cli.md and quickstart.md), 313 lines in total from a 123-line spec. It asked me no questions. spec.md and the checklist were not touched.

Decisions it made for the points the spec left to planning:
- Input format: one line per expense, comma-separated: payer, amount, sharer1, sharer2 and so on. A file argument or stdin.
- Build: Maven with JUnit 5. mvn verify is the single command.
- Settlement pairing: greedy, largest creditor with largest debtor. It says the true minimum is NP-hard in general and out of scope.
- Exit codes 0, 1 and 2 and the error message forms. The spec only said "signal failure".
- Structure: 7 source classes, 4 test classes and a pom.xml. The chat-only version was one 96-line file.

The Dave contradiction: the plan follows FR-007 for a payer who is not a sharer (the payer absorbs the cents) and never mentions that the spec's Edge Cases line says the payer is owed the full amount. The Constitution Check passes without a note.

Errors inside the plan documents:
- research.md says Alice nets +6.67 in the 10.00 split between three. data-model.md says +6.66. +6.66 is right (10.00 minus her own 3.34). With +6.67 the balances would sum to 0.01. research.md also says its numbers match the example.

Constitution Check:
- Principle III passes with the qualifier "offline once dependencies are cached". The constitution says tests must not need network access, and Maven downloads dependencies on the first run.
- The check says the traceability table maps each FR and edge case. SC-001, SC-003 and SC-004 are not mentioned by name anywhere in the plan files. The 2-minute entry-time criterion in SC-003 became "Performance Goals: Instant for the stated scale", so a person's time turned into machine speed and no test covers it.

Smaller items:
- The plan header says Branch 001-expense-splitter (git branch article-1-first-spec). No git branch has that name. The spec header says the feature branch is article-1-first-spec.
- The CLI contract says all invalid lines may be reported and at minimum the first. The spec does not say which, so a test cannot pin it.
- Quickstart scenario 6 reads expenses.txt, and no step creates that file.

Decision: I kept everything exactly as generated.
Maven installed on my machine: no. I never mentioned Maven. It came from the constitution's one-command build rule, which came from my sentence about tests. The plan then listed Maven 3.9 or later as a prerequisite, so the implementation step needs it installed first.  
Errors or surprises in the session: no errors. The surprises are the ones listed above.  

## Step 5: Tasks

Command I typed: `/speckit-tasks`

Result: `tasks.md`, 166 lines, 30 tasks in six phases: setup (2), foundational (5), one phase per user story (8, 5 and 6) and polish (4). Every task has an ID and a file path. Tests come before implementation inside each story.

What it added that I did not ask for:
- A foundational phase (Money, InputException, Expense, Settlement) that no user story owns.
- A fixed-seed property test (`new Random(42)`, at least 200 iterations) for the settlement rules.
- Polish tasks: a README, a traceability check against the plan, and a grep for `float` and `double` in the main code.
- Parallel markers on tasks and a parallel example.

Decisions made without asking me:
- Story 1 uses a simple two-pointer settlement that Story 2 replaces with the greedy one. The reply flagged this as a choice I could change.
- Stories 2 and 3 edit the same files as Story 1, so they run in order.
- Exact error strings for every kind of invalid input.

What it caught that the plan step did not: research.md says Alice nets +6.67 in the 10.00 example and data-model.md says +6.66. The tasks Notes section flags the mismatch and follows data-model.md (+666). I had not mentioned it anywhere.

What it did not catch:
- The edge-case line in spec.md that says the payer is owed the full amount when the payer is not a sharer. T008 tests Alice +666 for 10.00, which follows FR-007 and contradicts that line. The task list does not mention the conflict.
- SC-003 (under 2 minutes) has no task and no test.
- Story 2 is titled "Fewest payments", but the property test only checks at most N-1 lines. The greedy method meets that limit and does not guarantee the fewest.

Surprises: T015, T020, T026 and T030 all run `mvn verify`. I do not have Maven installed, so I need it before the implement step.

Errors or surprises in the session: no errors. The surprises are the ones listed above.  
Decision: I kept tasks.md exactly as generated.

## Step 6: Analyze

Command I typed: `/speckit-analyze`, in a fresh conversation.

Result: a read-only report with 13 findings: 0 critical, 1 high, 7 medium and 5 low. It changed no files. Coverage was 17 of 18 requirements.

What it caught that earlier steps missed:
- The edge case in spec.md that says the payer is owed the full amount. FR-007 and the data model say otherwise for uneven splits (I1, high).
- The +6.67 error in research.md (I2).
- SC-003 has no task and no test (G2).

What it found that I had not noticed:
- Blank sharer fields such as `Bob,,Carol` would be silently accepted, and no test covers them (U1).
- The tests for money and the parser are written against an exception type and a parser API that later tasks change (U2, I3).
- None of the spec's worked examples are pinned in a test (G1).
- The plan's `Expense` record has no `lineNumber`, while the data model and tasks do (I4).
- The contract says "at minimum the first" error is reported, but the tasks report all of them (U3).

What it said about the pipeline's own rules: the plan's "offline once dependencies are cached" may conflict with the constitution's no-network rule for tests. It called this arguably compliant (K1).

Decisions made without asking me: none. It offered fixes and said it would not apply any without my approval.

Surprises: the report found problems that the earlier steps had produced and passed, including the two spec and research contradictions. The tasks step had caught one of them (the +6.67 error) in its notes and still left research.md unfixed.

Errors or surprises in the session: no errors. The surprises are the ones listed above.  
Decision: I changed nothing. I left every finding in place before implementing so I can see whether they reach the code.