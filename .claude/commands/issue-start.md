# /issue-start — Analyze issue, create branch, plan implementation

Argument (optional): GitHub issue number (e.g., `/issue-start 5`)

If no argument is given, automatically select the oldest open `agent-ready` issue.

This command handles **new `agent-ready` issues only**. For `changes-requested` rework, use manual instruction (Phase 1) or `/issue-revise` (Phase 2).

## Procedure

### 1. Resolve issue number
- If `$ARGUMENTS` is provided, use that issue number.
- If `$ARGUMENTS` is empty, find the oldest `agent-ready` issue:
  ```
  gh issue list --label "agent-ready" --state open --json number,title,createdAt --limit 10
  ```
  - If no `agent-ready` issues exist, **stop and inform the user**: "No agent-ready issues found."
  - If multiple exist, pick the oldest (first created). Inform the user which issue was selected.

> **API 일시 실패 처리.** `gh issue list`/`gh issue view` 같은 읽기 호출은 일시 실패 시 5초 대기 후 1회 재시도(idempotency check 불필요). 정책: [`docs/harness/github-api-retry.md`](../../docs/harness/github-api-retry.md).

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

### 7. Draft implementation plan (Explore sub-agent)
Spawn an **Explore sub-agent** to analyze the issue against the current codebase. Provide the sub-agent with:
- The full issue body
- Instructions to read `CLAUDE.md`, `docs/harness/database-rules.md`, `docs/harness/testing-rules.md`
- **Partial-scan instructions (mandatory order):**
  1. Read `docs/project-state/INDEX.md` first.
  2. From the INDEX, identify domain/infra documents related to the issue and read **only those**. Example: an auth-related issue reads `domains/auth.md` + `infra/security.md`; a DB schema issue reads `infra/database.md` + relevant `domains/*.md`.
  3. Use the `구현됨` / `미구현` / `관련 파일 경로` sections of those state docs to locate concrete code paths.
  4. Only fall back to broad codebase exploration (Glob/Grep across `src/`) if the state documents are insufficient — and report which doc was missing context so the human can update it later.

**메타/프로세스 이슈 적응.** 이슈가 하네스 명령, 워크플로우, 회고 문서 등 메타 영역에 속해 도메인/인프라 코드와 무관할 수 있다. 그 경우 아래 결과 항목 중 다수가 적용되지 않는다(Flyway 마이그레이션 없음, JPA 계층 없음, 컨트롤러 없음, 테스트 파일 없음 등). 부모 에이전트 브리핑이 메타 이슈임을 명시했거나, 매칭되는 도메인/인프라 상태 문서가 없으면, 표준 결과 형식을 강요하지 말고 실제로 작업이 발생하는 영역(예: `.claude/commands/*.md`, `docs/harness/*.md`, `docs/roadmap/*.md`)만 보고한다. plan 템플릿이 안 맞는 사실 자체를 보고에 포함해도 좋다.

The sub-agent should return:
- What needs to be built
- Which layers are affected (domain, repository, service, controller, migration, template, config) — *메타 이슈는 해당 없음으로 명시*
- Dependencies on existing code
- Files to create/modify (full paths)
- Flyway migration(s) needed (with proposed filename following `V{yyyyMMddHHmmss}__{desc}.sql`) — *메타 이슈는 생략*
- Test files to create — *메타 이슈는 생략 가능*
- Implementation order: migrations → domain → repository → service → DTO → controller → templates — *메타 이슈는 자유 순서*
- Estimated scope: small (1-2 files) / medium (3-5 files) / large (6+ files)
- **`state_docs_analysis` block** — required for partial-scan metrics (see `docs/harness/metrics-rules.md`). Format:
  ```
  state_docs_analysis:
  - read: [docs/project-state/INDEX.md, docs/project-state/domains/auth.md, ...]
  - broadened: false
  - notes: 빈 문자열 또는 한 줄 메모 (예: "no domain doc matched, meta issue")
  ```
  `read`는 Explore가 실제로 read한 `docs/project-state/*` / `docs/harness/*` 경로. `broadened`는 `src/` 전반에 Glob/Grep을 돌렸는지. 메타 이슈여서 도메인 문서가 매칭되지 않았더라도 INDEX는 read 했을 것이므로 빈 배열은 거의 발생하지 않는다.

### 7-bis. Append partial-scan metric line

Right after the Explore sub-agent returns (regardless of whether the user later approves or rejects the plan in step 11), append one JSONL line to `docs/harness/metrics/issue-start-partial-scans.jsonl`.

```jsonl
{"date":"<UTC ISO 8601>","issue":<N>,"state_docs_read":[...],"state_docs_available":<count>,"broadened_to_codebase":<bool>,"notes":"<string>"}
```

- `state_docs_available` = number of rows in the 도메인 + 인프라 tables of `docs/project-state/INDEX.md` (excluding INDEX.md itself).
- If the Explore output is missing the `state_docs_analysis` block, fill `state_docs_read=[]`, `broadened_to_codebase=false`, `notes="state_docs_analysis missing in Explore output"`, and surface a one-line warning to the user.

Schema and rationale: [`docs/harness/metrics-rules.md`](../../docs/harness/metrics-rules.md).

### 8. Review plan (Review sub-agent)
Spawn a **separate Review sub-agent** with a fresh context. Provide it with:
- The original issue body
- The draft plan from step 7
- Instructions to read `CLAUDE.md`, `docs/harness/database-rules.md`, `docs/harness/testing-rules.md`
- Instructions to read `docs/project-state/INDEX.md` and the same domain/infra state docs the Explore agent identified (for cross-checking against current state)

The review sub-agent must check:
- **Completeness**: Are all required layers covered? Are tests included for every new feature?
- **Convention compliance**: Does the plan follow ddl-auto=validate, constructor injection, DTO boundaries, Flyway naming?
- **Missing concerns**: Profile configuration, security implications, existing test compatibility, dependency conflicts?
- **Scope accuracy**: Is the estimated scope realistic?
- **State doc impact**: Which state docs (`docs/project-state/domains/*.md`, `docs/project-state/infra/*.md`) will need updating at PR time? List them.

The review sub-agent returns a list of issues found (may be empty).

### 9. Refine plan
Incorporate the review sub-agent's feedback into the plan. If significant issues were found, resolve them. Then compile the **final implementation plan**.

### 10. Present final plan to user
Output the final plan with:
- Files to create/modify (full paths)
- Flyway migration(s) needed
- Test files to create
- Implementation order
- Estimated scope
- Review findings that were addressed (if any)
- **State docs to update at PR time** (e.g., `docs/project-state/domains/auth.md`)

### 11. Wait for user approval
**Do NOT implement anything or change any labels before explicit user approval.**

### 12. On approval

```
gh issue edit {number} --add-label "agent-working" --remove-label "agent-ready"
```

> **API 일시 실패 처리.** `gh issue edit`은 mutation이므로 일시 실패 시 라벨 상태를 먼저 조회해 이미 적용/해제되었는지 확인 후 재시도. 정책: [`docs/harness/github-api-retry.md`](../../docs/harness/github-api-retry.md).

**로드맵 status 갱신.** 이슈 본문에서 `roadmap-ref: RM-{ID}` 줄을 추출. 활성 로드맵 파일들을 훑어 해당 ID의 항목을 찾고:

- 항목 status가 `planned`면 `in-progress`로 flip.
- 이미 `in-progress`/`done`/`deprecated`/`blocked`이면 그대로 둔다.
- 로드맵 파일의 `last-updated`를 오늘 날짜로 갱신.
- 이슈에 `roadmap-ref`가 없거나 (예: `inferred-from`만 있는 경우) 매칭되는 RM 항목이 없으면 skip.

이 갱신은 별도 커밋으로 묶지 않아도 된다 — 구현 PR에 포함되어 머지된다.

Begin implementation following the approved plan.

### 13. Implementation complete
After all code is committed and `./gradlew clean build` passes, ask the user:

> "구현 완료. `/issue-pr {number}`을 호출할까요?"

- If yes: invoke `/issue-pr {number}` (handles push, PR creation, and label transition to `agent-pr-created`)
- If no: stop here. The user will handle PR creation manually or later.

### 14. On rejection
```
git checkout main
git branch -D issue/{number}-{kebab-title}
```
No label changes. The issue stays `agent-ready` for future rework of the plan.
