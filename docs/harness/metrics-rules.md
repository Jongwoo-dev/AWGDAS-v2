# Harness Metrics Rules

자동 개발 하네스의 비용/품질 메트릭을 어떻게 캡처하고 어디에 기록할지 규정한다. RM-HARNESS-004의 산출물이며, 이슈 #13에서 설계되었다.

## 측정 대상 (v1)

두 종류의 메트릭만 캡처한다. 그 외 신호(완료/머지율, changes-requested 횟수 등)는 v1 범위 밖이며 별도 RM 항목으로 추적한다.

1. **`/issue-suggest` 후보 승인 메트릭** — 후보가 실제로 이슈로 등록되었는가? (사용자가 `none`으로 거절했으면 라인 자체를 남기지 않는다.)
2. **`/issue-start` 부분 스캔 프록시 메트릭** — Explore 서브 에이전트가 실제로 어떤 상태 문서를 읽었는가? broad fallback이 발생했는가?

### 토큰 수가 아니라 프록시인 이유

Claude Code는 명령 실행 중 turn 단위 토큰 카운트를 노출하지 않는다. 따라서 "부분 스캔이 토큰을 줄였는지"는 직접 측정할 수 없고, **읽은 상태 문서 수 / 가용 상태 문서 수** 또는 **broad fallback 발생 여부**로 근사한다. 향후 API 노출이 가능해지면 실제 토큰 카운트로 교체한다.

## 기록 위치 / 포맷

`docs/harness/metrics/` 하위에 두 개의 append-only JSONL 파일을 둔다. 한 줄당 한 JSON 객체.

```
docs/harness/metrics/
├── issue-suggest-candidates.jsonl
└── issue-start-partial-scans.jsonl
```

### JSONL을 선택한 이유

- **Append-only** — 각 명령 실행 끝에 한 줄만 추가. 동시성 충돌 가능성 낮음 (단일 사용자 하네스).
- **자동화 친화적** — `jq -s '...'`, `cat ... | jq -c 'select(...)'` 같은 사후 분석 쉬움.
- **Markdown 표 대안 거부** — 표는 행 수정/리포맷이 잦으면 머지 충돌 ↑, 자동화 어려움. 본 메트릭은 거의 읽기 전용 누적이라 JSONL이 적합.
- **레포 컨벤션** — 본 레포는 `.md`/`.yaml`/`.sql`/`.java` 위주이며 JSONL 선례가 없다. 본 문서가 그 선례를 명시적으로 도입한다.

### 필드명은 영어, 본문은 한국어

JSONL 필드명은 모두 영어 snake_case. 한국어 키(`"날짜"`, `"채택"`)는 자동화 도구 친화성이 낮아 금지한다. 본 가이드 문서 본문은 다른 하네스 룰 문서(`github-api-retry.md`, `database-rules.md`)와 동일하게 한국어를 유지한다.

## 스키마

### `issue-suggest-candidates.jsonl`

`/issue-suggest`가 사용자 승인을 받아 **이슈를 실제로 생성한 직후 1줄 append**.

| 필드 | 타입 | 설명 |
|------|------|------|
| `date` | string (ISO 8601) | 이슈 생성 직후의 UTC 타임스탬프 |
| `roadmap_filter` | string \| null | `/issue-suggest`에 전달된 인자 (예: `"harness"`), 없으면 `null` |
| `candidates_presented` | integer | step 6에서 사용자에게 제시된 후보 수 (1~5) |
| `candidate_summaries` | string[] | 제시된 각 후보의 한 줄 요약 (`"RM-HARNESS-004: 비용/품질 메트릭"` 형식). 길이는 `candidates_presented`와 같다 |
| `accepted_index` | integer (1-based) | 사용자가 채택한 후보 번호 |
| `issue_created` | integer | 생성된 GitHub 이슈 번호 |
| `roadmap_ref` | string \| null | 채택된 항목의 RM-ID. `inferred-from` 분기(option 2)면 `null` |

**예시:**

```jsonl
{"date":"2026-04-28T05:30:00Z","roadmap_filter":null,"candidates_presented":1,"candidate_summaries":["RM-HARNESS-004: 비용/품질 메트릭"],"accepted_index":1,"issue_created":13,"roadmap_ref":"RM-HARNESS-004"}
```

**라인을 남기지 않는 경우:**

- 사용자가 `none`으로 거절 (step 7 종료 시점).
- step 2의 no-roadmap 분기에서 사용자가 옵션 3(취소) 선택.
- API 일시 실패로 이슈가 끝까지 생성되지 못한 경우 (재시도 정책: `github-api-retry.md`).

후보 거절도 신호이긴 하지만 v1에서는 captured selection만 기록한다. 거절율이 필요하면 v2 RM 항목에서 재논의.

### `issue-start-partial-scans.jsonl`

`/issue-start` step 7에서 **Explore 서브 에이전트가 plan을 반환한 직후 1줄 append**. step 12(approval)가 아니라 **step 7 종료 시점**임에 주의 — 사용자가 step 11에서 reject해도 line은 남는다 (스캔 자체는 발생했으므로 비용 기록 의미가 있음).

| 필드 | 타입 | 설명 |
|------|------|------|
| `date` | string (ISO 8601) | step 7 종료 직후 UTC 타임스탬프 |
| `issue` | integer | `/issue-start`의 인자 이슈 번호 |
| `state_docs_read` | string[] | Explore가 실제로 read한 `docs/project-state/*` 또는 `docs/harness/*` 경로 목록 |
| `state_docs_available` | integer | `docs/project-state/INDEX.md`가 가리키는 활성 도메인/인프라 문서의 총 수 (체크 시점 기준) |
| `broadened_to_codebase` | boolean | Explore가 `src/` 전반에 Glob/Grep을 돌렸는지 여부 |
| `notes` | string | (선택) "메타 이슈, 도메인 매칭 없음" 등 짧은 메모. 빈 문자열 가능 |

**예시:**

```jsonl
{"date":"2026-04-28T05:35:00Z","issue":13,"state_docs_read":["docs/project-state/INDEX.md"],"state_docs_available":5,"broadened_to_codebase":false,"notes":"meta issue, no domain docs matched"}
{"date":"2026-04-28T08:00:00Z","issue":14,"state_docs_read":["docs/project-state/INDEX.md","docs/project-state/domains/auth.md","docs/project-state/infra/security.md"],"state_docs_available":5,"broadened_to_codebase":false,"notes":""}
```

#### Explore 서브 에이전트의 반환 의무

부모 명령이 위 필드를 채울 수 있도록, `/issue-start` step 7의 Explore 호출은 plan 본문 외에 다음 블록을 명시적으로 반환하도록 지시한다.

```
state_docs_analysis:
- read: [경로 목록]
- broadened: true|false
- notes: 한 줄 메모 (없으면 빈 문자열)
```

부모 명령은 이 블록을 파싱해 jsonl 라인을 만든다. **Explore가 이 블록을 누락한 경우** 부모 명령은:

- `state_docs_read`를 `[]`로, `broadened_to_codebase`를 `false`로, `notes`를 `"state_docs_analysis missing in Explore output"`로 채워 라인을 남긴다.
- 사용자에게 한 줄 경고 surface (`Explore 반환에 state_docs_analysis 블록이 없어 메트릭이 부분 데이터로 기록되었습니다.`).

라인을 통째로 누락하는 것보다 결함을 함께 기록하는 편이 후속 분석에 도움된다.

`state_docs_available`는 부모 명령이 별도로 계산한다 — `docs/project-state/INDEX.md`의 도메인 표 + 인프라 표 행 수를 세면 된다. INDEX 자체는 카운트에 포함하지 않는다 (항상 read되므로 변별력 없음).

## 트리거 위치 (요약)

| 메트릭 | 명령 | 정확한 트리거 |
|--------|------|---------------|
| issue-suggest-candidates | `/issue-suggest` | step 8에서 `gh issue create`가 성공 응답을 반환한 직후, 후보 단위로 1줄 |
| issue-start-partial-scans | `/issue-start` | step 7 종료 직후 (Explore 반환 직후), step 11 승인 여부와 무관하게 1줄 |

## 분석 예시

`jq` 한 줄로 자주 쓸 만한 질의:

```bash
# 후보 적중률: 제시된 후보 중 첫 번째가 채택된 비율
cat docs/harness/metrics/issue-suggest-candidates.jsonl \
  | jq -s 'map(.accepted_index == 1) | (map(select(.)) | length) / length'

# 부분 스캔 효율 프록시: 평균적으로 활성 상태 문서의 몇 %를 read했는가
cat docs/harness/metrics/issue-start-partial-scans.jsonl \
  | jq -s 'map((.state_docs_read | length) / .state_docs_available) | add / length'

# broad fallback 발생율
cat docs/harness/metrics/issue-start-partial-scans.jsonl \
  | jq -s 'map(select(.broadened_to_codebase)) | length'
```

이 질의들을 정기적으로 돌려 dogfooding 회고에 첨부한다 (수동, v1에서는 자동화 없음).

## 한계 및 향후 작업

- **토큰 카운트 부재.** 위 "토큰 수가 아니라 프록시인 이유" 참고. API 노출이 추가되면 실제 카운트 병기.
- **무한 누적.** JSONL은 회전되지 않는다. 파일이 ~10MB를 넘으면 사용자가 수동으로 `docs/harness/metrics/archive/{filename}.{YYYY-Q}.jsonl`로 이동. v1 자동화 없음.
- **완료 신호 부재.** `/issue-pr` 머지 여부, changes-requested 발생 여부 등 후행 품질 신호는 캡처하지 않는다. 별도 RM 항목으로 분리.
- **소급 미적용.** 본 정책 도입 시점(이슈 #13 머지) 이전 실행은 메트릭 라인이 없다. 과거 dogfooding 사이클은 `docs/harness/dogfooding-*.md`만 참고.
- **동시 실행.** 단일 사용자 하네스를 가정. 두 `/issue-suggest`가 동시에 라인을 쓰면 한 줄이 손상될 수 있으나, v1 사용 패턴에서는 발생하지 않는다고 본다.

## 적용 범위

본 정책은 다음 명령에 적용된다:

- `.claude/commands/issue-suggest.md` — step 8
- `.claude/commands/issue-start.md` — step 7

다른 명령(`/issue-pr`, `/roadmap-cleanup` 등)으로의 확장은 후속 RM 항목에서 결정한다.
