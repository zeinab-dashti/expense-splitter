<!--
Sync Impact Report
- Version change: (blank template) → 1.0.0
- Modified principles: none renamed (initial adoption)
- Added principles: I. Plain Java 21, No Framework; II. Command-Line Interface;
  III. Every Rule Has an Automated Test (NON-NEGOTIABLE)
- Added sections: Technology & Build Constraints; Development Workflow
- Removed sections: none (unused template Principle 4 and 5 slots dropped)
- Follow-up TODOs: none
-->
# Expense Splitter Constitution

## Core Principles

### I. Plain Java 21, No Framework
The application MUST be written in plain Java 21 and MUST NOT depend on any application
framework (e.g., Spring, Quarkus, Micronaut, Jakarta EE containers). Runtime dependencies MUST
be limited to the JDK standard library. Test-only libraries (e.g., JUnit) are permitted.
Rationale: a small tool stays easy to read, build, and run when it carries no framework weight.

### II. Command-Line Interface
The application MUST run from the command line. Input MUST come from arguments or stdin, normal
output MUST go to stdout, and errors MUST go to stderr with a non-zero exit code on failure.
Business logic MUST live outside the CLI entry point so it can be tested without launching a
process.
Rationale: a predictable text protocol makes the tool scriptable and easy to test.

### III. Every Rule Has an Automated Test (NON-NEGOTIABLE)
Every rule stated in a feature spec MUST be covered by at least one automated test that fails
when the rule is broken. A rule without a test is considered unimplemented. Tests MUST run
unattended from the build command and MUST NOT require manual steps or network access.
Rationale: the spec is the source of truth, and tests are how that truth is enforced.

## Technology & Build Constraints

- Language level: Java 21 (source and target).
- Build MUST produce a runnable artifact and run the full test suite with a single command.
- Money amounts MUST use exact arithmetic (e.g., `BigDecimal` or integer minor units), never
  `float` or `double`.

## Development Workflow

- Each spec rule MUST be traceable to its test(s) in the plan or tasks.
- All tests MUST pass before a change is merged.
- Reviews MUST verify compliance with this constitution; any deviation MUST be justified in
  the plan's complexity notes.

## Governance

This constitution supersedes other practices. Amendments MUST be made by editing this file,
with the version bumped per semantic versioning: MAJOR for removed or redefined principles,
MINOR for added principles or materially expanded guidance, PATCH for clarifications. Every
plan and review MUST check compliance with the principles above.

**Version**: 1.0.0 | **Ratified**: 2026-11-16 | **Last Amended**: 2026-11-16
