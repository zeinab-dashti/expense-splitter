# Spec Kit setup notes

## Version installed
- specify-cli **1.3.2** (tag `v1.3.2`, commit `55264c2f2f2d1146f02abc543cb913dcdf1f9342`)
- git 2.43.0, uv 0.11.32

## Commands run
1. `git checkout -b article-1-first-spec main`
2. `git --version`; `uv --version`
3. `gh release view --repo github/spec-kit --json tagName,publishedAt`
4. `mcp add_repo github/spec-kit` (denied by the permission classifier; not used)
5. `git ls-remote --tags --refs https://github.com/github/spec-kit.git | sed 's#.*refs/tags/##' | sort -V | tail -5`
6. `curl -sSL -o /dev/null -w '%{url_effective} %{http_code}\n' https://github.com/github/spec-kit/releases/latest`
7. `uv tool install specify-cli --force --from git+https://github.com/github/spec-kit.git@v1.3.2`
8. `specify self check`
9. `specify init --help`
10. `specify init --here --integration claude </dev/null` (aborted, see errors)
11. Trial in a scratch copy of the repo: `specify init --here --force --non-interactive --integration claude`, then checked README.md, LICENSE and .gitignore by sha256. All unchanged.
12. `specify init --here --force --non-interactive --integration claude` (real run, exit 0; README.md, LICENSE and .gitignore verified unchanged by sha256)

## Errors, warnings, surprises (verbatim)

**Release lookup.** The latest tag could NOT be confirmed as a "release":
```
HTTP 403: GitHub access to this repository is not enabled for this session. Use add_repo to request access. ... (https://api.github.com/repos/github/spec-kit/releases/latest)
```
`curl https://github.com/github/spec-kit/releases/latest` returned `403`. I therefore used the highest version tag from `git ls-remote`. The five highest were: v1.2.5, v1.2.6, v1.3.0, v1.3.1, v1.3.2. Please check that v1.3.2 is the latest published release.

**specify self check:**
```
Installed: 1.3.2
Could not check latest release: rate limited (configure ~/.specify/auth.json 
with a GitHub token)
```

**First init attempt (without --force):**
```
Warning: Current directory is not empty (4 items)
Template files will be merged with existing content and may overwrite existing files. Do you want to continue? [y/N]: Error: Current directory is not empty and no confirmation input is available. 
Re-run with --force to merge into it.
```
Surprise: `--force --non-interactive` was needed to proceed. The real run printed:
```
Warning: Current directory is not empty (4 items)
--force supplied: skipping confirmation and proceeding with merge
```
Nothing was overwritten.

## Folder tree created by init
```
.claude/skills/{speckit-analyze,speckit-checklist,speckit-clarify,speckit-constitution,
                speckit-converge,speckit-implement,speckit-plan,speckit-specify,speckit-tasks}/
.specify/integrations/
.specify/memory/
.specify/scripts/bash/
.specify/templates/
.specify/workflows/speckit/
```

## Command names for Claude Code (hyphen style)
`/speckit-constitution`, `/speckit-specify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-implement`, `/speckit-converge`
Optional: `/speckit-clarify`, `/speckit-analyze`, `/speckit-checklist`

(Not the dotted `/speckit.specify` style. No slash commands were run.)
