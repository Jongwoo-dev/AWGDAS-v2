# /issue-pr — Create pull request for completed issue

Argument: GitHub issue number (e.g., `/issue-pr 5`)

## Prerequisites
- Implementation must be complete
- Must be on the correct `issue/{N}-*` branch
- Working tree must be clean (all changes committed)

## Procedure

### 1. Verify branch and state
Confirm current branch matches `issue/$ARGUMENTS-*`. If not, stop.
Confirm working tree is clean (`git status`). If not, stop.

### 2. Final build
```
./gradlew clean build
```
Must pass. If it fails, diagnose and fix before continuing.

### 3. Diff summary
```
git diff main...HEAD --stat
```
Output the change summary for the user to review.

### 4. Check for database migrations
Check if any new files exist in `src/main/resources/db/migration/`.
If yes, the `Database Migrations` section in the PR body is **mandatory** — list each migration file with a brief description.

### 5. Read issue for PR context
```
gh issue view $ARGUMENTS --json title,body,labels
```

### 6. Push branch
```
git push -u origin HEAD
```

### 7. Create PR
```
gh pr create \
  --title "{concise title}" \
  --body "$(cat <<'EOF'
## Summary
- {bullet 1}
- {bullet 2}

## Changes
{list key files added/modified}

## Database Migrations
{list migration files with description, or "None"}

## Testing
{summary of tests added}

## Issue
Closes #{N}

## Checklist
- [x] Build passes (`./gradlew build`)
- [x] All tests pass
- [ ] Migration naming follows convention (`V{yyyyMMddHHmmss}__{desc}.sql`)
- [ ] No TODO comments in committed code
- [ ] No debug artifacts (System.out.println, commented-out code)
- [ ] DTOs used at controller boundary (entities not exposed)
- [ ] Constructor injection only (no field `@Autowired`)
EOF
)" \
  --base main
```

Fill in the checklist accurately — check items that were verified, leave unchecked items that don't apply or weren't verified.

### 8. Update labels
```
gh issue edit $ARGUMENTS --add-label "agent-pr-created" --remove-label "agent-working"
```

### 9. Report
Output the PR URL and a brief summary of what was implemented.
