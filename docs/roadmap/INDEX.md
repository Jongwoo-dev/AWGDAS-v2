# Roadmap Index

이 디렉토리는 AWGDAS-v2의 **목표/로드맵을 영역별로 분할** 보관한다. 자동 이슈 제안 (`/issue-suggest`)이 활성 로드맵을 읽고 다음 작업 후보를 추출한다.

## 사용 규칙

- 로드맵은 **여러 개 공존 가능** (예: 하네스 / 백엔드 / 게임 콘텐츠).
- 본 INDEX는 항상 로드되는 진입점 — 짧게 유지.
- 로드맵 파일 추가/제거 시 이 INDEX의 표를 갱신.
- 항목은 **삭제 금지**, `status: deprecated`로 마킹만. 정리는 `/roadmap-cleanup`이 별도로 처리.

## 활성 로드맵

| 파일 | status | 한 줄 요약 |
|------|--------|-----------|
| [harness.md](harness.md) | active | 자동 개발 하네스 자체 발전 (부분 스캔, 자동 이슈, 자동 검증 등) |

## ID 부여 규칙

- 형식: `RM-{ROADMAP}-{NNN}`
  - ROADMAP: 로드맵 파일명을 대문자로 (예: `harness.md` → `HARNESS`)
  - NNN: 3자리 일련번호, 로드맵 파일별로 독립적으로 증가
- ID는 **불변** — 항목이 deprecated 되어도 ID는 보존 (이슈/상태 문서가 참조하기 때문)
- 새 항목 추가 시 해당 로드맵의 최대 번호 + 1

## 항목 상태값

- `planned` — 시작 전, 의존성 충족 시 후보가 됨
- `in-progress` — 진행 중
- `done` — 완료
- `deprecated` — 폐기, 참조는 유지 (cleanup 시점까지)
- `blocked` — 외부 차단 요인, 후보에서 제외

## 신규 로드맵 추가 시

1. `docs/roadmap/{name}.md` 생성 (frontmatter 포함)
2. 본 INDEX의 "활성 로드맵" 표에 1줄 추가
3. 첫 항목은 `RM-{NAME}-001`부터
