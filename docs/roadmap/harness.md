---
roadmap-id: harness
status: active
last-updated: 2026-04-27
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

- status: in-progress
- related-issues: []
- depends-on: [RM-HARNESS-001]
- 설명: 활성 로드맵 + 현재 상태 문서를 읽어 다음 작업 후보를 제시하는 `/issue-suggest` 커맨드. 로드맵 부재 시 명시적 되묻기. 새 로드맵/항목 추가용 `/roadmap-add`. 폐기 항목 정리용 `/roadmap-cleanup`.

### RM-HARNESS-003: 워크플로우 검증 / dogfooding

- status: planned
- depends-on: [RM-HARNESS-001, RM-HARNESS-002]
- 설명: 실제 이슈 1~2개를 `/issue-suggest` → `/issue-start` → 구현 → `/issue-pr` 흐름으로 한 번 돌려 보고, 발견된 갭(상태 문서 누락 항목, 후보 추출 정확도, 비용)을 수정.

### RM-HARNESS-004: 비용/품질 메트릭

- status: planned
- 설명: 부분 스캔이 실제로 토큰 사용량을 줄이는지 측정 가능한 형태로 기록. 자동 이슈 후보의 적중률(승인/거절 비율) 추적. 측정 방식 자체는 추후 정의.
