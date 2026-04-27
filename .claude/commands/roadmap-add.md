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
- 초기 상태 (기본 `planned`. `blocked`도 허용. `in-progress`/`done`/`deprecated`는 여기서 설정 금지 — 작업 흐름에서 자연스럽게 전이)

Validate every dependency ID exists in some active roadmap. If any is missing, warn and ask whether to remove it or proceed.

### 6. Show the diff and confirm

Display what will be appended to the target roadmap file:

```markdown
### RM-HARNESS-005: {제목}

- status: planned
- depends-on: [RM-HARNESS-003]
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
