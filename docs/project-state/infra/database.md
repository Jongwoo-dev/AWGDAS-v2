---
area: database
status: not-started
last-updated: 2026-04-27
related-issues: []
roadmap-refs: []
---

# database — 데이터베이스/마이그레이션

## 구현됨

- `application.yaml`: `spring.jpa.hibernate.ddl-auto=validate`, `spring.flyway.enabled=true`, `spring.jpa.open-in-view=false`
- `application-local.yaml`: H2 드라이버 지정, h2-console 활성화 (`/h2-console`), 접속 정보는 `config/secrets-local.yaml`에서 주입
- `application-test.yaml`: H2 드라이버, 접속 정보는 `config/secrets-test.yaml`에서 주입

## 미구현 / TODO

- **마이그레이션 파일 0개** — `src/main/resources/db/migration/` 디렉토리 자체가 아직 존재하지 않음
- **JPA 엔티티 0개** — `domain/` 패키지 비어있음
- **Repository 0개** — `repository/` 패키지 비어있음
- 운영용 PostgreSQL 프로파일 미정의

## 알려진 제약

- `ddl-auto=validate`이므로 첫 엔티티 추가 시 반드시 Flyway 마이그레이션이 함께 와야 부팅 가능. (자세한 규칙: `docs/harness/database-rules.md`)
- 마이그레이션 파일명 형식: `V{yyyyMMddHHmmss}__{desc}.sql`
- secrets 파일은 git에 포함되지 않음 — `config/secrets-local.yaml` 등은 로컬에 별도 작성 필요.

## 관련 파일 경로

- `src/main/resources/application.yaml`
- `src/main/resources/application-local.yaml`
- `src/main/resources/application-test.yaml`
- `docs/harness/database-rules.md` (규칙)
