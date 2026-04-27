---
roadmap-id: harness
status: active
last-updated: 2026-04-28
---

# harness — 자동 개발 하네스 로드맵

자동 개발 하네스(AWGDAS-v2의 진짜 핵심)의 발전 항목. 일반 기능 개발과 별개로 관리.

## 항목

### RM-HARNESS-001: 부분 스캔 인프라

- status: done
- completed-at: 2026-04-27
- related-issues: []
- related-state-docs: [`docs/project-state/INDEX.md`]
- 설명: 프로젝트 상태를 도메인/인프라 단위로 분할(`docs/project-state/`), `/issue-start`가 INDEX → 관련 문서만 부분 스캔. `/issue-pr`이 머지 전 상태 문서 갱신 의무화.

### RM-HARNESS-002: 자동 이슈 생성

- status: done
- completed-at: 2026-04-28
- related-issues: []
- depends-on: [RM-HARNESS-001]
- 설명: 활성 로드맵 + 현재 상태 문서를 읽어 다음 작업 후보를 제시하는 `/issue-suggest` 커맨드. 로드맵 부재 시 명시적 되묻기. 새 로드맵/항목 추가용 `/roadmap-add`. 폐기 항목 정리용 `/roadmap-cleanup`.

### RM-HARNESS-003: 워크플로우 검증 / dogfooding

- status: done
- completed-at: 2026-04-28
- related-issues: [#5]
- depends-on: [RM-HARNESS-001, RM-HARNESS-002]
- 설명: 실제 이슈 1~2개를 `/issue-suggest` → `/issue-start` → 구현 → `/issue-pr` 흐름으로 한 번 돌려 보고, 발견된 갭(상태 문서 누락 항목, 후보 추출 정확도, 비용)을 수정.

### RM-HARNESS-004: 비용/품질 메트릭

- status: planned
- 설명: 부분 스캔이 실제로 토큰 사용량을 줄이는지 측정 가능한 형태로 기록. 자동 이슈 후보의 적중률(승인/거절 비율) 추적. 측정 방식 자체는 추후 정의.

### RM-HARNESS-005: /issue-suggest 상태 신뢰성

- status: done
- completed-at: 2026-04-28
- related-issues: [#9]
- depends-on: [RM-HARNESS-002]
- 설명: 로드맵 항목의 `status` 필드와 실제 산출물(예: 명령 파일 존재, 관련 PR 머지 등) 사이 불일치를 `/issue-suggest`가 감지·경고하도록 보강. RM-HARNESS-003 dogfooding cycle 001에서 RM-002가 `in-progress`로 남아 있다 수동 갱신된 사례(Finding 1, `docs/harness/dogfooding-001-2026-04.md` 참고).

### RM-HARNESS-006: /issue-suggest 후보 랭킹 정교화

- status: planned
- related-issues: [#11]
- depends-on: [RM-HARNESS-002]
- 설명: "depends-on이 비어 있는 항목 우선" 규칙이 foundational vs. dependent 관계를 잡지 못함. dogfooding cycle 001에서 RM-004가 RM-003 위에 랭크된 사례(Finding 2). 알고리즘 재설계 — 어떤 신호를 추가할지부터 토론 필요.

### RM-HARNESS-007: /issue-suggest GitHub API 재시도

- status: done
- completed-at: 2026-04-28
- related-issues: [#7]
- depends-on: [RM-HARNESS-002]
- 설명: `gh issue create` 등 GitHub API 호출이 일시 실패(예: HTTP 504)할 경우의 재시도 정책을 `/issue-suggest`/`/issue-start`에 명시. 멱등성 확인(목록 조회 후 재시도) 포함. dogfooding cycle 001 Finding 5.
