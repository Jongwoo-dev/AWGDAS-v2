# Project State Index

이 디렉토리는 AWGDAS-v2의 **현재 상태를 도메인/인프라 단위로 분할**해 기록한다. 자동 개발 하네스가 이슈 작업 시 **전체 코드 스캔 대신 관련 문서만 부분 스캔**하는 진입점이다.

## 사용 규칙

- 작업 시작 전: 이 INDEX를 먼저 읽고, 이슈와 관련된 도메인/인프라 문서만 추가로 읽는다.
- 작업 종료 시 (`/issue-pr`): 변경된 영역의 상태 문서를 함께 업데이트한다.
- 새 도메인 추가 시: `domains/{name}.md` 생성 + 이 INDEX에 1줄 추가.
- 본 INDEX는 항상 로드된다 — 짧게 유지한다.

## 도메인

| 파일 | status | 한 줄 요약 |
|------|--------|-----------|
| [domains/auth.md](domains/auth.md) | in-progress | dev-login만 구현 (인메모리 dev/dev), 실제 OAuth/회원가입 미구현 |
| [domains/home.md](domains/home.md) | stable | 인증 불필요 홈 페이지, 로직 없음 |

## 인프라

| 파일 | status | 한 줄 요약 |
|------|--------|-----------|
| [infra/database.md](infra/database.md) | not-started | Flyway 활성화, 마이그레이션 0개, 엔티티 0개 |
| [infra/security.md](infra/security.md) | in-progress | Spring Security formLogin, 인메모리 사용자 |
| [infra/testing.md](infra/testing.md) | in-progress | WebMvcTest + SpringBootTest 패턴, 통합/단위 분리 |

## 컨벤션

| 파일 | 한 줄 요약 |
|------|-----------|
| [conventions.md](conventions.md) | CLAUDE.md에 없는 추가 규칙 (현재 비어있음) |

## 상태값 정의

- `not-started` — 디렉토리/파일 골격조차 없음
- `in-progress` — 일부 구현됨, 미완 항목 있음
- `stable` — 현 단계 목표상 완성, 추가 작업 예정 없음

## 로드맵 연결

각 상태 문서의 frontmatter `roadmap-refs` 필드는 G2(`docs/roadmap/`)가 추가된 후 채워진다. 현재 단계(G1)에서는 비워둔다.
