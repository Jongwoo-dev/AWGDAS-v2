# /roadmap-add — Add a new roadmap or roadmap item

Argument (optional): a hint — either a roadmap file name (`harness`) to scope adds to that roadmap, or a short description of the new item.

This command **modifies `docs/roadmap/`** and **never modifies code**. It is interactive — it asks the user before writing.

## Procedure

### 1. Read the index

```
docs/roadmap/INDEX.md
```

Capture the list of active roadmap files.

### 2. Decide target — existing roadmap or new

Ask the user (skip if obvious from the argument):

```
어디에 추가할까요?

  활성 로드맵:
  1. harness.md (자동 개발 하네스 자체 발전)
  ...

  N. (새 로드맵 만들기)
```

- If user picks an existing roadmap → go to step 4.
- If user picks "new" → go to step 3.

### 3. Create a new roadmap file (only if "new" was chosen)

Ask the user for:
- Roadmap filename (kebab, no extension — e.g., `backend`)
- One-line summary for the INDEX
- ROADMAP code for IDs (UPPER, default = filename uppercased)

Validate:
- File must not already exist in `docs/roadmap/` or `docs/roadmap/archive/`
- ROADMAP code must not be reused by an existing roadmap

Create `docs/roadmap/{filename}.md` with this skeleton:

```markdown
---
roadmap-id: {filename}
status: active
last-updated: {today}
---

# {filename} — {one-line summary}

## 항목

(첫 항목은 아래 step 5에서 추가됩니다)
```

Update `docs/roadmap/INDEX.md` — append a row to the "활성 로드맵" table.

Then proceed to step 4 with the new file as the target.

### 4. Determine the next RM ID

Read the target roadmap file. Find every header matching `### RM-{ROADMAP}-(\d+):`. The next ID is `RM-{ROADMAP}-{MAX+1, zero-padded to 3 digits}`. If no items exist yet, start at `001`.

**Never reuse an ID that already appeared, even for `deprecated` items.** Always go higher.

### 5. Gather item details from the user

Ask:
- 제목 (한 줄)
- 설명 (한두 문장 — 무엇을, 왜)
- 의존성 (선택): 기존 RM ID 목록, 쉼표 구분
- 부모 epic (선택): 자식 항목으로 추가할 경우 부모 RM ID
- 초기 상태 (기본 `planned`. `blocked`도 허용. `in-progress`/`done`/`deprecated`는 여기서 설정 금지 — 작업 흐름에서 자연스럽게 전이)

Validate every dependency ID exists in some active roadmap. If any is missing, warn and ask whether to remove it or proceed. Validate the parent ID (if given) exists and is currently `epic: true`.

### 5.5. Abstractness check

If the user marked the item explicitly as a child (parent given), or already declared it as an epic, skip this step.

Otherwise, judge whether the item looks too abstract for a single PR. Heuristics (any 1+ matches → suspect):

- Title or description mentions multiple distinct domains (e.g., "auth + DB + UI", "전반", "전체", "시스템")
- Description contains and-of-concerns ("가입과 로그인과 탈퇴", "X와 Y를 모두")
- Estimated affected state docs span 3+ files
- Title/description contains broad words: "시스템 구축", "전체 개편", "통합", "리팩토링 전반"

If none match, proceed to step 6.

If suspected abstract, **stop and ask the user** explicitly:

```
이 항목이 한 PR/이슈로 끝내기엔 좀 큰 것 같습니다.
근거: {위 휴리스틱 중 어떤 게 걸렸는지 한 줄}

어떻게 할까요?

1. 더 좁혀서 다시 입력 — 새 제목/설명을 받습니다.
2. 분해 제안 보기 — LLM이 자식 항목들로 쪼갤 안을 제시합니다 (사용자 승인 후 epic + 자식들 함께 등록).
3. epic으로 그대로 등록 — 이 항목은 `epic: true`가 되고 /issue-suggest 후보에서 제외됩니다. 자식 항목은 나중에 `/roadmap-add`로 별도 추가합니다.
```

- **Option 1** → loop back to step 5 with fresh input.
- **Option 2** → produce 2~5 child item proposals (each: 제목 + 1줄 설명 + 추정 의존성). Show them and ask for approval. On approval, treat current item as the epic and the proposed children as separate items to register in the same run. Each child gets its own RM ID (next available, sequential), `parent: RM-{epic-id}`, `status: planned`.
- **Option 3** → mark current item with `epic: true`, proceed to step 6 with no children registered. User can add children later.

Whichever option, never silently auto-decide — always confirm with the user.

### 6. Show the diff and confirm

Display what will be appended to the target roadmap file. Examples:

**Single item (non-epic):**
```markdown
### RM-HARNESS-005: {제목}

- status: planned
- depends-on: [RM-HARNESS-003]
- 설명: {설명}
```

**Epic only (option 3):**
```markdown
### RM-HARNESS-005: {제목}

- status: planned
- epic: true
- 설명: {설명}
```

**Epic + children (option 2):**
```markdown
### RM-HARNESS-005: {제목}

- status: planned
- epic: true
- 설명: {설명}

### RM-HARNESS-006: {자식 1 제목}

- status: planned
- parent: RM-HARNESS-005
- 설명: {자식 1 설명}

### RM-HARNESS-007: {자식 2 제목}

- status: planned
- parent: RM-HARNESS-005
- depends-on: [RM-HARNESS-006]
- 설명: {자식 2 설명}
```

**Child only (parent specified in step 5):**
```markdown
### RM-HARNESS-005: {제목}

- status: planned
- parent: RM-HARNESS-002
- 설명: {설명}
```

Ask: "이대로 추가할까요? (yes/no)"

### 7. On approval

- Append the new item to the target roadmap file (preserve existing content; do not reformat unrelated lines).
- Update the target roadmap's frontmatter `last-updated` to today.
- If a new roadmap was created in step 3, INDEX.md is already updated; otherwise leave INDEX untouched (the row's one-line summary doesn't need to change for a single new item).

### 8. Report

Output:
- The new RM ID
- The file path that was edited
- Suggested next step: `/issue-suggest {roadmap}` to register an issue from this item

## Failure modes to avoid

- **Never overwrite an existing roadmap file.** If the user-supplied filename collides, refuse.
- **Never reuse an RM ID** even after deprecation/cleanup.
- **Never silently change** unrelated items in the file. Only append.
- **Don't create commits.** Leave the working tree dirty for the user to review and commit.
- **Never auto-decompose without approval.** Step 5.5 option 2 always requires the user to approve the child list before writing.
- **Don't nest epics.** If the user tries to add a child whose parent is itself an epic-of-an-epic, refuse and ask to flatten.
