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

**언어 정책.** PR 제목과 본문은 **한국어**로 작성한다. 단, conventional commit prefix(`feat(scope):`, `fix:`, `refactor:` 등)는 그대로 유지하고 그 뒤의 설명만 한국어로 적는다. 예: `feat(harness): /issue-suggest 상태 신뢰성 체크 (#9)`. 코드 식별자, 파일 경로, 명령어, conventional prefix는 번역하지 않는다.

```
gh pr create \
  --title "{conventional prefix}: {간결한 한국어 설명}" \
  --body "$(cat <<'EOF'
## 요약
- {불릿 1}
- {불릿 2}

## 변경 사항
{추가/수정된 주요 파일 목록}

## 데이터베이스 마이그레이션
{마이그레이션 파일과 간단한 설명, 없으면 "없음"}

## 상태 문서 갱신
{갱신된 docs/project-state 파일 목록, 없으면 "없음"}

## 테스트
{추가한 테스트 요약}

## 이슈
Closes #{N}

## 체크리스트
- [x] 빌드 통과 (`./gradlew build`)
- [x] 모든 테스트 통과
- [ ] 마이그레이션 파일명 규칙 준수 (`V{yyyyMMddHHmmss}__{desc}.sql`)
- [ ] 커밋된 코드에 TODO 주석 없음
- [ ] 디버그 산출물 없음 (System.out.println, 주석 처리된 코드)
- [ ] 컨트롤러 경계에서 DTO 사용 (엔티티 미노출)
- [ ] 생성자 주입만 사용 (필드 `@Autowired` 금지)
- [ ] 프로젝트 상태 문서 갱신 (`docs/project-state/`)
EOF
)" \
  --base main
```

체크리스트는 정확하게 채운다 — 검증된 항목만 체크하고, 적용 불가하거나 검증되지 않은 항목은 비워둔다. 적용 불가 항목은 항목 끝에 ` — N/A, 사유` 를 덧붙여도 된다.

### 9. Update labels
```
gh issue edit $ARGUMENTS --add-label "agent-pr-created" --remove-label "agent-working"
```

### 10. Report
Output the PR URL and a brief summary of what was implemented.
