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

### 4. Update project-state docs (mandatory)

The implementation has changed the project state. Reflect those changes in `docs/project-state/` so future `/issue-start` runs see the new reality.

1. **Identify affected docs.** Use the list produced in `/issue-start` step 10 ("State docs to update at PR time"). If absent, derive from the diff:
   - Code under `controller/`, `service/`, `domain/`, `repository/`, `templates/` → corresponding `docs/project-state/domains/*.md`
   - Code under `config/` (security) → `docs/project-state/infra/security.md`
   - New migrations or entities → `docs/project-state/infra/database.md`
   - New test patterns → `docs/project-state/infra/testing.md`
2. **Update each affected doc:**
   - Move completed items from `## 미구현 / TODO` to `## 구현됨` (with file paths)
   - Add new TODOs surfaced during implementation
   - Update frontmatter: `last-updated` (today's date), append `#{issue}` to `related-issues`, adjust `status` if changed
3. **Update `docs/project-state/INDEX.md`** — refresh the affected row's status and one-line summary if it materially changed. Do NOT touch unrelated rows.
4. **If a new domain/infra area emerged** that doesn't exist yet, create the file under `domains/` or `infra/` and add an INDEX row.
5. **Commit the doc updates** as a separate commit:
   ```
   git add docs/project-state/
   git commit -m "docs(state): update project-state for #{issue}"
   ```

If no state docs need updating (rare — usually only pure refactors with no behavior change), explicitly note that and skip. Do not invent updates.

### 5. Check for database migrations
Check if any new files exist in `src/main/resources/db/migration/`.
If yes, the `Database Migrations` section in the PR body is **mandatory** — list each migration file with a brief description.

### 6. Read issue for PR context
```
gh issue view $ARGUMENTS --json title,body,labels
```

### 7. Push branch
```
git push -u origin HEAD
```

### 8. Create PR
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

## State Docs Updated
{list updated docs/project-state files, or "None"}

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
- [ ] Project state docs updated (`docs/project-state/`)
EOF
)" \
  --base main
```

Fill in the checklist accurately — check items that were verified, leave unchecked items that don't apply or weren't verified.

### 9. Update labels
```
gh issue edit $ARGUMENTS --add-label "agent-pr-created" --remove-label "agent-working"
```

### 10. Report
Output the PR URL and a brief summary of what was implemented.
