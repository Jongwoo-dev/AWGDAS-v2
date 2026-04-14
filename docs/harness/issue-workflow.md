# Issue-Based Development Workflow

## Work Targets

The agent works on:
- **New work**: open issues with `agent-ready` label → use `/issue-start {N}`
- **Rework**: issues with `changes-requested` label → manual instruction (Phase 1) / `/issue-revise {N}` (Phase 2)

## Label Lifecycle

```
[New Work]
Human creates issue
  → Adds `feature` or `bug`
  → Adds `agent-ready` when specification is complete

  → Agent runs /issue-start: analyze + output plan (NO label change)
     → User approves: remove `agent-ready`, add `agent-working`, start implementation
     → User rejects: delete branch, no label change (agent-ready stays)

  → During implementation, if issue is ambiguous:
     remove `agent-working`, add `blocked`, stop work
     → Human resolves: remove `blocked`, re-add `agent-ready`

  → Agent runs /issue-pr: remove `agent-working`, add `agent-pr-created`

[Review]
  → Human reviews PR
     → Approved: human merges, remove `agent-pr-created`
     → Changes needed: human adds `changes-requested`, removes `agent-pr-created`

[Rework] (Phase 1: manual instruction / Phase 2: /issue-revise)
  → Agent picks up `changes-requested` issue
     → Prerequisite: existing PR + `issue/{N}-*` branch must exist
     → If PR or branch not found → add `blocked`, stop
     → Checkout existing `issue/{N}-*` branch (do NOT create new branch)
     → Remove `changes-requested`, add `agent-working`
     → After fixes, run /issue-pr: remove `agent-working`, add `agent-pr-created`
```

## /issue-start Procedure

Argument: GitHub issue number (e.g., `/issue-start 5`)

1. **Read issue**: `gh issue view {N} --json title,body,labels,assignees,comments`
2. **Verify label**: confirm `agent-ready` is present. If not, stop. This command does NOT handle `changes-requested` or other states.
3. **Create branch**: from latest `main`
   ```
   git checkout main && git pull origin main
   git checkout -b issue/{N}-{kebab-title}
   ```
   Truncate title to ~40 characters for the branch name.
4. **Analyze issue**: identify
   - What needs to be built
   - Affected layers (entity, repository, service, controller, migration, template)
   - Dependencies on existing code
   - Ambiguities or missing information
5. **Output plan**: structured implementation plan with
   - Files to create/modify (full paths)
   - Flyway migration(s) needed (with proposed filename)
   - Test files to create
   - Implementation order (migrations → domain → repository → service → controller → templates)
6. **Wait for user approval**: do NOT implement or change labels before approval
7. **On approval**: `gh issue edit {N} --add-label "agent-working" --remove-label "agent-ready"`, then start implementation
8. **On rejection**: `git checkout main && git branch -D issue/{N}-*`, no label changes

## /issue-pr Procedure

Argument: GitHub issue number (e.g., `/issue-pr 5`)

1. **Verify state**: confirm on `issue/{N}-*` branch, working tree is clean
2. **Final build**: `./gradlew clean build` — must pass
3. **Diff summary**: `git diff main...HEAD --stat` — output change summary
4. **Check for migrations**: if any files exist in `src/main/resources/db/migration/`, include `Database Migrations` section in PR body
5. **Push**: `git push -u origin HEAD`
6. **Create PR**: `gh pr create` with template-based body including:
   - Summary (2-3 bullets)
   - Changes (key files)
   - Database Migrations (list files, or "None")
   - Testing (tests added)
   - Issue reference (`Closes #{N}`)
   - Checklist
7. **Update labels**: `gh issue edit {N} --add-label "agent-pr-created" --remove-label "agent-working"`
8. **Report**: output the PR URL

## Blocked Handling

When the agent encounters ambiguity or missing information during implementation:

1. Stop implementation immediately
2. `gh issue edit {N} --add-label "blocked" --remove-label "agent-working"`
3. Comment on the issue explaining what is unclear: `gh issue comment {N} --body "..."`
4. Inform the user

Resolution (by human):
1. Answer the question in the issue comments
2. `gh issue edit {N} --remove-label "blocked" --add-label "agent-ready"`
3. Agent can then re-enter via `/issue-start {N}`

## Rules

- Never start implementation without user approval of the plan
- If an issue is ambiguous, ask — do not guess
- `git diff main...HEAD` summary must be output before PR creation
- If DB migration files are present, `Database Migrations` section in PR body is mandatory
- One logical change per commit; prefer separate commits for migrations
