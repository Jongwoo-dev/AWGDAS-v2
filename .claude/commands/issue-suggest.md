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
- Option 2 → skip step 3, jump to step 4 with a single "best-guess" candidate derived from `docs/project-state/`'s `미구현 / TODO` sections; clearly mark it as inferred (no roadmap-ref).
- Option 3 → stop.

### 3. Extract candidates

For each loaded roadmap file, find items where:
- `status: planned`
- All IDs listed in `depends-on` have `status: done` in any active roadmap
- Not `blocked`

Build a candidate list with up to **5 entries**, prioritized by:
1. Items with no `depends-on` (true entry points)
2. Items whose dependencies were most recently completed
3. Order within the roadmap file (earlier items first)

For each candidate, derive the **affected state docs** by reading `docs/project-state/INDEX.md` and matching the item's description against domain/infra one-line summaries. If unsure, list the candidates and let the user confirm scope.

### 4. Read state context for each candidate

For each candidate, open the state docs identified above and capture:
- Existing implementations to reuse
- Known constraints
- Implicit prerequisites surfaced in `미구현 / TODO`

This becomes the issue body's "관련 상태" section.

### 5. Present candidates to the user

Output for review (one block per candidate):

```
[후보 1] RM-HARNESS-002: 자동 이슈 생성
  설명: ...
  관련 상태 문서: docs/project-state/...
  의존성 충족: RM-HARNESS-001 (done)
  추정 범위: small | medium | large
```

Then ask: "이 중 어느 항목을 이슈로 등록할까요? (번호 또는 'none')"

### 6. Wait for user approval

**Do NOT call `gh issue create` before explicit user approval.** If user says "none", stop without changes.

### 7. Create the issue(s)

For each approved candidate:

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

### 8. Update the roadmap item

In the source roadmap file, append the new issue number to the item's `related-issues` list:

```markdown
### RM-HARNESS-002: 자동 이슈 생성
- status: in-progress    # planned → in-progress
- related-issues: [#7]
```

Update `last-updated` in the roadmap frontmatter.

Do NOT change status here if multiple candidates were registered for the same item — keep `planned` until the actual work begins (typically `/issue-start` will flip it).

### 9. Report

Output:
- Created issue URLs
- Updated roadmap file path
- Suggested next step: `/issue-start {N}`

## Failure modes to avoid

- **Never auto-create an issue without approval.** Even if the candidate looks obvious.
- **Never delete or rename existing RM IDs** when updating roadmap files. Append-only.
- **Never propose a `done` or `deprecated` item.** They are filtered out in step 3.
- **If two candidates collide** (same affected state docs), warn the user before registering both.
