---
cycle: 001
date: 2026-04-28
target-issue: "#5"
target-roadmap-item: RM-HARNESS-003
---

# Harness Dogfooding Cycle 001 (2026-04)

자동 개발 하네스를 실제 이슈에 처음 끝까지 돌려 본 사이클의 회고. 8개의 갭을 관찰했고, 그중 3개는 즉시 수정, 3개는 후속 RM 항목으로 분리, 2개는 기록만 했다.

## 사이클 개요

- **대상 이슈**: [#5](https://github.com/Jongwoo-dev/AWGDAS-v2/issues/5) — `RM-HARNESS-003: 워크플로우 검증 / dogfooding`
- **흐름**: `/issue-suggest` → `/issue-start` → 본 회고 작성 + 부분 수정 → `/issue-pr`
- **시작 상태**: 활성 로드맵 1개(`harness.md`), 항목 4개 (001 done, 002 in-progress, 003/004 planned)

### 메타 회귀

이번 사이클의 검증 주체(이슈 #5)와 검증 도구(`/issue-suggest`, `/issue-start`)가 동일 도메인(harness)에서 나왔다. 이슈 자신을 실행하면서 자신을 평가하는 구조 — 이것 자체가 흥미로운 발견이지만, 다양한 갭 노출에는 한계가 있을 수 있다. **다음 사이클은 다른 도메인(예: auth, database)의 이슈를 대상으로 돌리는 편이 더 풍부한 갭을 드러낼 것**이라 본다.

## 발견 정리

각 항목은 관찰(Observation) — 결정(Decision) — 후속 변경(Follow-up) 형식.

### Finding 1 — 로드맵 status 필드 무비판 신뢰

**관찰.** `RM-HARNESS-002: 자동 이슈 생성`이 `status: in-progress`로 표시돼 있었다. 그러나 그 설명에 언급된 모든 명령(`/issue-suggest`, `/roadmap-add`, `/roadmap-cleanup`)은 이미 `.claude/commands/`에 존재했다. `/issue-suggest`는 status 필드만 보고 002를 후보에서 제외했고, 그 결과 002에 의존하는 RM-HARNESS-003도 후보로 떠오르지 않았다. 사용자가 "002는 사실상 done인 것 같다"고 짚어줘서야 갱신이 이뤄졌다.

**결정.** `RM-HARNESS-005`로 분리.

**후속.** 명령 존재 여부 등 산출물 신호와 status 필드 사이 불일치를 `/issue-suggest`가 감지·경고하도록 보강하는 작업은 별도 RM 항목.

### Finding 2 — 우선순위 규칙이 foundational/dependent 관계 미반영

**관찰.** `/issue-suggest` step 3의 우선순위 규칙은 "depends-on이 비어 있는 항목 우선"이다. 이번 사이클에서 RM-HARNESS-004(메트릭, depends-on 비어있음)가 RM-HARNESS-003(dogfooding, 002에 의존) 위에 랭크됐다. 하지만 004(무엇을 측정할지)는 003(어떤 갭이 측정할 가치가 있는지)이 먼저 풀려야 의미 있는 설계가 가능한 foundational 관계였다. 사용자/에이전트 모두 003이 먼저라는 데 동의했다.

**결정.** `RM-HARNESS-006`으로 분리.

**후속.** 후보 랭킹 알고리즘 정교화는 별도 RM. 현재 단계에서는 사람이 우선순위를 뒤집을 수 있는 여지를 명령이 이미 제공하므로 즉시 차단되진 않는다.

### Finding 3 — 이슈 본문 템플릿이 메타 항목에 부정합

**관찰.** `/issue-suggest` step 7의 issue body 템플릿은 `## 작업 범위 (추정)`에 "파일/영역 — 상태 문서에서 도출"을, `## 관련 상태 문서`에 "docs/project-state/{...}"를 가정한다. RM-HARNESS-003은 도메인/인프라 코드와 무관한 메타 항목이라 두 섹션 모두 자연스럽게 채울 내용이 없었다. 이번 사이클에서는 "(없음 — 하네스 메타 항목)"이라 명시해 우회했다.

**결정.** **즉시 수정 (in-PR).**

**후속.** `.claude/commands/issue-suggest.md` step 7 본문 템플릿에 메타 항목 분기 표현 추가. 이번 PR에 포함.

### Finding 4 — 부분 스캔의 메타 폴백 부재

**관찰.** `/issue-start` step 7은 Explore 에이전트에게 "`docs/project-state/INDEX.md`를 먼저 읽고 도메인/인프라 문서로 좁혀라"고 지시한다. 메타 이슈는 매핑되는 도메인 문서가 없어, 이 절차가 비어버린다. 이번 사이클에서는 부모 에이전트(나)가 "메타 이슈"임을 명시적으로 Explore 브리핑에 적었기 때문에 graceful하게 작동했다.

**결정.** 기록만, 즉시 수정 없음.

**후속.** Explore 에이전트가 매칭되는 상태 문서가 없을 때 "(없음)"을 정직하게 보고하고 진행하는 것은 현재도 가능하다. 추가 명령 변경은 불필요. Finding 7과 일부 겹치지만 그쪽은 plan 템플릿(Flyway/계층) 부정합이라 별개.

### Finding 5 — GitHub API 일시 실패 시 재시도 가이드 없음

**관찰.** `gh issue create` 실행 중 한 차례 `HTTP 504 Gateway Timeout`이 발생했다. 이슈 목록 조회로 미생성 확인 후 재실행하니 성공. 이 흐름은 부모 에이전트가 직접 판단한 결과이며, `/issue-suggest` 명령에는 일시 실패 처리에 대한 가이드가 없다.

**결정.** `RM-HARNESS-007`로 분리.

**후속.** API 호출 일시 실패에 대한 재시도 정책(예: 1회 재시도, 멱등성 확인 후 재시도)을 명령 문서에 명시하는 작업은 별도 RM.

### Finding 6 — `/issue-suggest`의 긍정 행동들

**관찰.** 다음 동작들은 의도대로 작동했고 보존할 가치가 있다.

- 사용자 승인 없이 `gh issue create`를 호출하지 않았다.
- 후보별로 우선순위 근거(어떤 규칙이 적용됐는지)를 출력했다.
- `done`/`deprecated` 항목은 후보에서 제외됐다.
- 의존성이 충족되지 않은 항목(예: RM-003)은 진단 없이 자동 제안되지 않았다 — 사용자가 002를 done으로 마킹한 후에야 합류했다.

**결정.** 액션 없음.

### Finding 7 — `/issue-start` plan 템플릿이 메타 이슈에 부정합

**관찰.** `/issue-start` step 7의 Explore 브리핑 결과 형식은 다음을 가정한다.

- Flyway 마이그레이션 파일명(`V{yyyyMMddHHmmss}__{desc}.sql`)
- domain/repository/service/DTO/controller 계층
- 테스트 파일
- 구현 순서: migrations → domain → repository → service → DTO → controller

이슈 #5(메타 이슈)는 위 어느 항목도 적용되지 않았다. 회고 문서 작성과 명령 마크다운 편집이 전부였다. 부모 에이전트가 Explore 브리핑에 "메타 이슈 — 표준 plan 템플릿이 안 맞을 수 있음. 그 부정합 자체를 dogfooding finding으로 보고하라"고 명시했기에 graceful했다.

**결정.** **즉시 수정 (in-PR).**

**후속.** `.claude/commands/issue-start.md` step 7 Explore 지시문에 "메타/프로세스 이슈는 도메인/계층/Flyway/테스트가 적용되지 않을 수 있다 — 적응해 보고할 것" 한 줄 추가. 이번 PR에 포함.

### Finding 8 — `/issue-start`가 로드맵 항목 status를 갱신하지 않음

**관찰.** Review 에이전트가 plan 검토 중 발견. `/issue-suggest` step 8 마지막 줄에 "여러 후보가 등록되면 status는 `planned` 유지 — 보통 `/issue-start`가 flip한다"고 명시돼 있다. 하지만 `/issue-start.md`에는 status flip 단계가 없다. 결과적으로 RM-HARNESS-003은 작업이 진행 중인데도 로드맵엔 `planned`인 채로 남아 있게 된다 — 다음 `/issue-suggest` 호출이 같은 항목을 다시 후보로 제시할 위험.

**결정.** **즉시 수정 (in-PR).**

**후속.** `.claude/commands/issue-start.md` step 12("On approval")에 로드맵 status 갱신 단계 추가. 이슈 본문에서 `roadmap-ref: RM-{ID}`를 추출해 해당 로드맵 파일에서 `planned` → `in-progress`로 flip하고 `last-updated`를 갱신. 메타 RM이거나 `roadmap-ref`가 없으면 skip.

## 요약

### 이번 PR에 포함되는 변경 (4건)

1. 본 회고 문서 신규 작성 (`docs/harness/dogfooding-001-2026-04.md`)
2. `.claude/commands/issue-suggest.md` step 7 — 메타 이슈 분기 표현 (Finding 3)
3. `.claude/commands/issue-start.md` step 7 — Explore 메타 적응 노트 (Finding 7)
4. `.claude/commands/issue-start.md` step 12 — 로드맵 status 자동 갱신 (Finding 8)
5. `docs/harness/issue-workflow.md` — 본 회고 발견 경로 링크
6. `docs/roadmap/harness.md` — RM-003 in-progress 수동 갱신 + RM-005/006/007 신규

### 후속 RM 항목 (3건)

| ID | 출처 Finding | 한 줄 요약 |
|----|------------|-----------|
| RM-HARNESS-005 | F1 | `/issue-suggest`가 산출물(명령 존재 등)과 status 필드 불일치를 감지 |
| RM-HARNESS-006 | F2 | `/issue-suggest` 후보 랭킹에 foundational/dependent 관계 반영 |
| RM-HARNESS-007 | F5 | `/issue-suggest`/`/issue-start`에 GitHub API 일시 실패 재시도 정책 명시 |

### 비용 관찰

부분 스캔은 의도대로 작동했다. Explore 에이전트는 `docs/harness/*.md`, `.claude/commands/*.md`, 그리고 명시적으로 지정된 몇 개 문서만 읽었다. `src/` 전체로 번지는 일은 없었다. 다만 메타 이슈라 도메인 코드를 읽을 필요 자체가 없었던 점도 기여 — 다음 사이클(코드 도메인 이슈)에서 비용 추정이 더 의미 있을 것이다.

### 다음 사이클을 위한 권장

- 메타가 아닌 실제 도메인 이슈(예: auth 회원가입, DB 스키마)로 사이클을 한 번 더 돌릴 것.
- 위 RM-005/006/007 중 하나를 수행하기 전에 `/issue-suggest`를 다시 돌려, 본 사이클의 in-PR fix(특히 Finding 8 status flip)가 실제로 작동하는지 확인할 것.
