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