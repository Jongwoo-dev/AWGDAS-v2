# /issue-suggest — Suggest next feat issue from roadmap + state

Argument (optional): roadmap filter — a roadmap file name or RM prefix (e.g., `/issue-suggest harness` or `/issue-suggest RM-HARNESS`). If empty, considers all active roadmaps.

Output: a short list of candidate issues. **No issue is created without explicit user approval.**

## Procedure

### 1. Read roadmap

```
docs/roadmap/INDEX.md
```

Then load each **active** roadmap file listed in the index (`status: active` in frontmatter). If `$ARGUMENTS` is provided, restrict to roadmaps whose filename or RM prefix matches.

### 2. Handle the no-roadmap case

If `docs/roadmap/INDEX.md` does not exist, has no active roadmaps, or all matching roadmaps have zero `planned` items:

**Stop and ask the user explicitly. Do NOT auto-infer candidates.**

Present these three options:

```
활성 로드맵에서 시작 가능한 항목을 찾지 못했습니다. 어떻게 할까요?

1. 새 로드맵/항목을 추가하고 다시 시도 → `/roadmap-add` 사용을 안내
2. 로드맵 없이 현재 상태 문서(docs/project-state/)만 보고 후보를 추론 (정확도 낮음)
3. 취소
```

- Option 1 → end this command, instruct the user to run `/roadmap-add` first.
- Option 2 → skip steps 3–4, jump to step 5 with a single "best-guess" candidate derived from `docs/project-state/`'s `미구현 / TODO` sections; clearly mark it as inferred (no roadmap-ref).
- Option 3 → stop.

### 3. Check roadmap status reliability

Before extracting candidates, scan loaded roadmap items for potential mismatches between the `status` field and reality signals. Goal: surface stale `in-progress` markers and prematurely-marked items so the user can fix the roadmap before `/issue-suggest` proposes new work on top of an inconsistent state. Origin: Finding 1 of `docs/harness/dogfooding-001-2026-04.md` (`RM-HARNESS-002` was `in-progress` while all its described commands already existed).

**Scope.** Only check items with `status ∈ {planned, in-progress}`. Skip `done` / `deprecated` / `blocked` (out of scope for v1).

**Signal A — command file existence.** For each in-scope item, extract `/command-name` patterns from the description (regex: ``\`/[a-z][a-z0-9-]*\``, matching kebab-case slugs in backticks). For each match:

- Check whether `.claude/commands/{command-name}.md` exists.
- Apply the **modification heuristic**: if the description text within ~30 chars before or after the match contains modification verbs (`보강`, `수정`, `개선`, `강화`), label that match as `(수정 대상으로 추정)`. Do NOT suppress — just label, so the user sees it but isn't shamed by a false-positive warning.

An item flags Signal A when **all** extracted command files exist AND **at least one** match is not labeled as 수정 대상.

**Signal B — related-issues PR merge state.** For each in-scope item where `related-issues` is non-empty:

- If the list has **more than 3 entries**, skip Signal B for that item and append a one-line note in the warning (`Signal B skipped: >3 related-issues — cost guard`).
- Otherwise call `gh pr view {N} --json merged --jq .merged` for each entry. Read-only call — apply the lookup retry policy from `docs/harness/github-api-retry.md` (5s wait + 1 retry on transient failure).
- If **all** related-issues are merged, the item flags Signal B.

**Aggregate and present.** Collect all flagged items into a single batched warning. If the list is empty, proceed silently to step 4.

If any flags exist, output:

```
[ 로드맵 status 신뢰성 경고 ]
다음 항목에서 status와 산출물 신호가 불일치할 수 있습니다.

- RM-HARNESS-002 (in-progress): 명시된 명령이 모두 존재 → done 가능성
  · /issue-suggest ✓, /roadmap-add ✓, /roadmap-cleanup ✓
- RM-HARNESS-005 (in-progress): 명시된 명령이 수정 대상으로 보임 (보강/수정 키워드)
  · /issue-suggest (수정 대상으로 추정)
- RM-HARNESS-007 (in-progress): related-issues 모두 머지됨 → done 가능성
  · #7 (merged), #8 (merged)

자동 갱신은 하지 않습니다. 계속 후보 추출을 진행할까요? (yes / no)
```

**User decision.**
- `yes` → proceed to step 4 (extract candidates).
- `no` → stop the entire flow. The user updates the roadmap manually and re-runs `/issue-suggest`.

**알려진 한계 (v1).**
- 설명에 명령이 명시되지 않고 PR 제목/본문에만 기록된 경우는 미검출 (참조 문서 존재 검사는 v2로 연기).
- `/issue-start` step 12가 status를 미리 `in-progress`로 flip한 직후 자기참조 false positive 가능 — 수정 휴리스틱이 부분 완화하지만 완전히 막진 못함. 사용자가 `yes`로 패스 가능.
- "수정 대상으로 추정" 라벨은 보조 정보일 뿐 — 최종 판단은 사용자.

### 4. Extract candidates

For each loaded roadmap file, find items where:
- `status: planned`
- **Not `epic: true`** — epics are abstract; only their children become candidates
- All IDs listed in `depends-on` have `status: done` in any active roadmap
- If the item has a `parent: RM-{ID}`, the parent must not be `archived`/`deprecated`
- Not `blocked`

**역참조 그래프 구축.** 우선순위 산정 전, 활성 로드맵의 모든 항목을 훑어 양방향 의존 그래프를 만든다:

- 항목 X의 `depends-on: [A, B]`는 A→X, B→X 엣지로 본다 (A가 X 앞에 와야 함).
- 항목 X의 `blocks: [C, D]`는 X→C, X→D 엣지로 본다 (X가 C, D 앞에 와야 함).
- 두 표현은 동일한 관계의 두 방향 — 합쳐서 단일 그래프로 처리. 중복 엣지는 한 번만 카운트.

각 후보 항목 Y에 대해 **`refCount(Y)` = Y → ? 엣지의 수, 단 도착점이 `planned` 또는 `in-progress` 상태인 경우만 카운트**. 즉 "Y가 done 되어야 풀리는, 아직 살아 있는 항목 수". `done`/`deprecated`/`blocked`/`archived` 도착점은 무시 (foundational 신호로 무의미).

스키마 배경: `docs/roadmap/INDEX.md`의 "의존 관계 선언" 섹션 참고.

Build a candidate list with up to **5 entries**, prioritized by:
1. **`refCount` 내림차순** — foundational(다른 항목의 토대) 항목 우선. dogfooding cycle 001 Finding 2의 RM-004/RM-003 미스랭크를 직접 해결.
2. Items whose dependencies were most recently completed (deps의 `completed-at` 최댓값으로 비교; deps 없으면 `epoch`로 간주해 가장 낮음)
3. Children whose parent epic has the most progress (e.g., 2 of 3 children already done) — finishing an epic is preferred over starting a new one
4. Order within the roadmap file (earlier items first)

> ⚠️ 이전 규칙 "Items with no `depends-on`"(빈 deps 우선)는 제거됨. 빈 deps는 foundational의 신호가 아니라 단순 "선언 누락" 가능성이 더 큼. dogfooding cycle 001 Finding 2 참고.

For each candidate, derive the **affected state docs** by reading `docs/project-state/INDEX.md` and matching the item's description against domain/infra one-line summaries. If unsure, list the candidates and let the user confirm scope.

### 5. Read state context for each candidate

For each candidate, open the state docs identified above and capture:
- Existing implementations to reuse
- Known constraints
- Implicit prerequisites surfaced in `미구현 / TODO`

This becomes the issue body's "관련 상태" section.

### 6. Present candidates to the user

Output for review (one block per candidate):

```
[후보 1] RM-HARNESS-002: 자동 이슈 생성
  설명: ...
  관련 상태 문서: docs/project-state/...
  의존성 충족: RM-HARNESS-001 (done)
  refCount: 2 (RM-HARNESS-003, RM-HARNESS-004 가 이 항목을 가리킴)
  추정 범위: small | medium | large
```

`refCount`가 0인데 다른 후보가 1+이면, 양쪽 모두 표시해 사용자가 ranking 근거를 볼 수 있도록 한다.

Then ask: "이 중 어느 항목을 이슈로 등록할까요? (번호 또는 'none')"

### 7. Wait for user approval

**Do NOT call `gh issue create` before explicit user approval.** If user says "none", stop without changes.

### 8. Create the issue(s)

For each approved candidate:

> **API 일시 실패 처리.** `gh issue create`가 HTTP 504/타임아웃 등으로 실패하면 멱등성 확인 후 1회 재시도. 정책: [`docs/harness/github-api-retry.md`](../../docs/harness/github-api-retry.md).

```
gh issue create \
  --title "{RM-ID}: {short title}" \
  --label "agent-ready" \
  --body "$(cat <<'EOF'
## 목적
{설명}

## 작업 범위 (추정)
- {파일/영역 — 상태 문서에서 도출}

## 관련 상태 문서
- docs/project-state/{...}

## 메타
- roadmap-ref: {RM-ID}
- depends-on: [{만족된 의존성 ID들}]
EOF
)"
```

If option 2 path was taken (no roadmap), omit `roadmap-ref` and add `inferred-from: docs/project-state` instead.

**메타 항목 분기.** 후보가 하네스/프로세스 같은 메타 항목이라 도메인/인프라 코드와 직접 무관하면, body 템플릿의 두 섹션을 다음과 같이 채운다:

- `## 작업 범위 (추정)` 항목을 `(메타 항목 — 파일 단위 범위 미정의)`로 적거나, 알려진 명령/문서 단위로 한 줄씩 적는다.
- `## 관련 상태 문서`는 `(없음 — 하네스/메타 항목)`으로 적는다.

이 분기는 후보의 description이 도메인/인프라 키워드와 매칭되지 않거나, step 5의 상태 컨텍스트 수집 결과가 비어있을 때 적용한다.

### 9. Update the roadmap item

In the source roadmap file, append the new issue number to the item's `related-issues` list:

```markdown
### RM-HARNESS-002: 자동 이슈 생성
- status: in-progress    # planned → in-progress
- related-issues: [#7]
```

Update `last-updated` in the roadmap frontmatter.

Do NOT change status here if multiple candidates were registered for the same item — keep `planned` until the actual work begins (typically `/issue-start` will flip it).

### 10. Roll up epic completion

After updating items in step 9, scan all `epic: true` items across the loaded roadmaps. For each epic:

1. Collect all items whose `parent: RM-{epic-id}` matches.
2. If **every child has `status: done` or `status: archived`**, and at least one child exists, set the epic's `status` to `done` and add a `completed-at: {today}` line.
3. If no children exist yet, do nothing — empty epic stays as-is.
4. If any child is still `planned` / `in-progress` / `blocked`, do nothing.

Output a one-line summary for each epic that flipped to done so the user sees the implicit roll-up.

### 11. Report

Output:
- Created issue URLs
- Updated roadmap file path
- Suggested next step: `/issue-start {N}`

## Failure modes to avoid

- **Never auto-create an issue without approval.** Even if the candidate looks obvious.
- **Never delete or rename existing RM IDs** when updating roadmap files. Append-only.
- **Never propose a `done` or `deprecated` item.** They are filtered out in step 4.
- **If two candidates collide** (same affected state docs), warn the user before registering both.
- **Never auto-update a roadmap status from the reliability check (step 3).** Warning only — user must update manually.
