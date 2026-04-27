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
     → Approved: human merges (`agent-pr-created` stays as-is on closed issue)
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

Argument (optional): GitHub issue number. If omitted, selects the oldest `agent-ready` issue.

1. **Resolve issue**: find or validate issue number, confirm `agent-ready` label
2. **Sync main**: pull remote, push any unpushed local commits first
3. **Create branch**: `issue/{N}-{kebab-title}` from latest `main`
4. **Draft plan (Explore sub-agent)**: spawn a sub-agent to analyze the issue against the codebase and project rules, returning a structured implementation plan
5. **Review plan (Review sub-agent)**: spawn a separate sub-agent with fresh context to check completeness, convention compliance, missing concerns, and scope accuracy
6. **Refine plan**: incorporate review feedback, compile final plan
7. **Present to user**: output final plan with review findings addressed
8. **Wait for user approval**: do NOT implement or change labels before approval
9. **On approval**: `gh issue edit {N} --add-label "agent-working" --remove-label "agent-ready"`, then start implementation
10. **On rejection**: delete branch, no label changes

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

## Dogfooding 회고

각 dogfooding 사이클에서 발견된 갭과 결정은 `docs/harness/dogfooding-NNN-YYYY-MM.md`로 기록한다.

- [Cycle 001 (2026-04)](dogfooding-001-2026-04.md) — 첫 dogfooding, 8개 갭 발견 (RM-HARNESS-005/006/007 분리)
