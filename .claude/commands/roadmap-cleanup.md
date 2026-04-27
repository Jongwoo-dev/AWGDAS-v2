# /roadmap-cleanup — Move unreferenced deprecated items to archive

Argument (optional): roadmap filter (e.g., `harness`). If empty, scans all active roadmaps.

This command **never deletes**. It moves `deprecated` items that are no longer referenced into `docs/roadmap/archive/{filename}.md`. Always interactive — user confirms before any move.

## Procedure

### 1. Read active roadmaps

```
docs/roadmap/INDEX.md
```

Load each active roadmap file (or only the one matching the argument).

### 2. Find deprecated items

For each loaded roadmap, extract every item header `### RM-{ROADMAP}-{NNN}:` whose body contains `status: deprecated`.

### 2.5. Find epics whose children are all deprecated/archived

Additionally, scan every `epic: true` item in the loaded roadmaps. For each epic:

1. Collect children (items with `parent: RM-{epic-id}`).
2. If the epic has **at least one child** and **every child is either `deprecated` (in the active file) or already `archived` (i.e., not present in the active file but found in `docs/roadmap/archive/`)**, treat the epic as an archive candidate.
3. Mark such epics as a "bundle" — when archiving, move the epic together with any of its still-`deprecated` children in one step.

If neither step 2 nor 2.5 found anything → output "정리 대상 없음." and stop.

### 3. Check references for each candidate

For each deprecated item, search for its RM ID in:

**a) GitHub issues (open + closed):**
```
gh issue list --state all --search "{RM-ID}" --json number,state,title
```

**b) Project state docs:**
- Frontmatter `roadmap-refs` arrays in `docs/project-state/**/*.md`
- Body text mentioning the RM ID

**c) Other roadmap files** (a deprecated item could be a `depends-on` of another):
- Search `depends-on:` lines across `docs/roadmap/*.md` for the RM ID

Categorize each item:
- **Safe to archive**: zero references everywhere, OR only references are in *closed* issues with no open downstream work
- **Has live references**: at least one open issue, or referenced by a non-deprecated item's `depends-on`, or actively referenced in a state doc — **do NOT move**
- **Mixed**: has only closed/historical references — let user decide

### 4. Present the report

Output a table grouping epic bundles together:

```
정리 후보:

  RM-HARNESS-099 — Safe to archive
    references: (none)

  RM-HARNESS-098 — Has live references — SKIP
    references:
      - issue #42 (open)
      - docs/project-state/domains/auth.md (roadmap-refs)

  RM-HARNESS-097 — Mixed — needs decision
    references:
      - issue #15 (closed)

  [bundle] RM-HARNESS-010 (epic) + children
    RM-HARNESS-011 — Safe to archive
    RM-HARNESS-012 — Safe to archive (already in archive/)
    epic itself: references (none)
    → 묶음으로 archive 가능
```

For each "Safe" or "Mixed" individual item, ask the user whether to archive it. For each epic bundle, ask once for the whole bundle (yes archives epic + remaining deprecated children together; no skips the bundle entirely).

### 5. On approval per item

For each approved item:

1. **Append** the full item block (header + body) to `docs/roadmap/archive/{original-filename}.md`. Create the archive file if it doesn't exist with this header:
   ```markdown
   ---
   archive-of: {original-filename}
   ---

   # archive of {original-filename}

   Items moved here are no longer in the active roadmap. RM IDs remain reserved.
   ```
2. **Remove** the item block from the source roadmap file (only the lines belonging to that item — leave everything else untouched).
3. Update the source roadmap's frontmatter `last-updated` to today.

**Do NOT touch state docs or issues.** Cleanup only moves roadmap items; references in other documents stay as-is and become "historical pointers" that resolve via `archive/`.

### 6. Report

Output:
- Items moved (RM IDs and target archive file)
- Items skipped (with reasons)
- Suggested next step: review the diff (`git diff docs/roadmap/`) and commit if desired

## Failure modes to avoid

- **Never delete an item.** Always move to archive.
- **Never reuse an archived RM ID** — even after archive, the ID is permanently retired.
- **Never archive an item with open issue references.** Always SKIP and report.
- **Don't auto-run.** This command is manual-only — never schedule it as a hook or cron.
- **Don't modify items that aren't `status: deprecated`.** Only deprecated is eligible.
