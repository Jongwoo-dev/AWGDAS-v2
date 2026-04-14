# /issue-start — Analyze issue, create branch, plan implementation

Argument (optional): GitHub issue number (e.g., `/issue-start 5`)

If no argument is given, automatically select the oldest open `agent-ready` issue.

This command handles **new `agent-ready` issues only**. For `changes-requested` rework, use manual instruction (Phase 1) or `/issue-revise` (Phase 2).

## Procedure

### 1. Resolve issue number
- If `$ARGUMENTS` is provided, use that issue number.
- If `$ARGUMENTS` is empty, find the oldest `agent-ready` issue:
  ```
  gh issue list --label "agent-ready" --state open --sort created --json number,title --limit 1
  ```
  - If no `agent-ready` issues exist, **stop and inform the user**: "No agent-ready issues found."
  - If multiple exist, pick the oldest (first created). Inform the user which issue was selected.

### 2. Read the issue
```
gh issue view {number} --json number,title,body,labels,assignees,comments
```

### 3. Verify label
Confirm the issue has `agent-ready` label. If not, **stop and inform the user**. Do NOT proceed with `changes-requested`, `blocked`, or any other state.

### 4. Check for existing branch
```
git branch -a | grep "issue/{number}"
```
If a branch exists, ask the user: continue existing work or start fresh?

### 5. Sync main before branching
```
git checkout main
git pull origin main
```
Check if local main is ahead of remote:
```
git log origin/main..main --oneline
```
- If there are unpushed commits, **push them first**: `git push origin main`
- This prevents unpushed commits from leaking into the feature branch PR diff.

### 6. Create branch
```
git checkout -b issue/{number}-{kebab-title}
```
Truncate the title portion to ~40 characters.

### 7. Analyze the issue
Read the issue body carefully. Identify:
- **What** needs to be built
- **Which layers** are affected (domain, repository, service, controller, migration, template, config)
- **Dependencies** on existing code
- **Ambiguities** or missing information — if found, flag them clearly

### 8. Output implementation plan
Present a structured plan:
- Files to create/modify (full paths)
- Flyway migration(s) needed (with proposed filename following `V{yyyyMMddHHmmss}__{desc}.sql`)
- Test files to create
- Implementation order: migrations → domain → repository → service → DTO → controller → templates
- Estimated scope: small (1-2 files) / medium (3-5 files) / large (6+ files)

### 9. Wait for user approval
**Do NOT implement anything or change any labels before explicit user approval.**

### 10. On approval
```
gh issue edit $ARGUMENTS --add-label "agent-working" --remove-label "agent-ready"
```
Begin implementation following the approved plan.

### 11. On rejection
```
git checkout main
git branch -D issue/{number}-{kebab-title}
```
No label changes. The issue stays `agent-ready` for future rework of the plan.
