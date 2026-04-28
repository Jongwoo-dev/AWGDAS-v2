---
area: database
status: in-progress
last-updated: 2026-04-29
related-issues: [#15, #17]
roadmap-refs: [RM-PRODUCT-019, RM-PRODUCT-023]
---

# database — 데이터베이스/마이그레이션

## 구현됨

- `application.yaml`: `spring.jpa.hibernate.ddl-auto=validate`, `spring.flyway.enabled=true`, `spring.jpa.open-in-view=false`
- `application-local.yaml`: H2 드라이버 지정, h2-console 활성화 (`/h2-console`), 접속 정보는 `config/secrets-local.yaml`에서 주입
- `application-test.yaml`: H2 드라이버, 접속 정보는 `config/secrets-test.yaml`에서 주입
- **H2 PostgreSQL 호환 모드**: 두 secrets yaml 모두 `MODE=PostgreSQL`로 설정 — 마이그레이션 SQL이 H2/PostgreSQL 양쪽에서 동일하게 동작
- **로컬 H2 파일 모드** (issue #17): `local` 프로파일은 `jdbc:h2:file:./data/awgdas-local;...`로 영속 저장 (재시작 후 데이터 유지). `test` 프로파일은 in-memory 유지. `data/awgdas-local.*`는 `.gitignore` 제외.
- **첫 마이그레이션 + 첫 엔티티** (PR #15)
  - `src/main/resources/db/migration/V20260428155500__create_users_table.sql` — `users` 테이블 (id BIGSERIAL, username UNIQUE, password_hash, email, role, created_at, updated_at)
  - `src/main/resources/db/migration/V20260428155600__seed_initial_admin.sql` — 초기 관리자 1명 INSERT (BCrypt 해시 포함)
  - `domain/User.java` — 첫 JPA 엔티티
  - `repository/UserRepository.java` — 첫 Spring Data JPA 리포지터리
  - 시간 컬럼은 Hibernate `@CreationTimestamp`/`@UpdateTimestamp`로 관리 (SQL `DEFAULT` 미사용 — `ddl-auto=validate` 정합성)

## 미구현 / TODO

- 운영용 PostgreSQL 프로파일 미정의
- 추가 도메인 엔티티(게임 명세, 요청, 갤러리 등) 없음

## 알려진 제약

- `ddl-auto=validate`이므로 엔티티 추가 시 반드시 Flyway 마이그레이션이 함께 와야 부팅 가능. (자세한 규칙: `docs/harness/database-rules.md`)
- 마이그레이션 파일명 형식: `V{yyyyMMddHHmmss}__{desc}.sql`
- secrets 파일은 git에 포함되지 않음 — `config/secrets-local.yaml` 등은 로컬에 별도 작성 필요.
- Flyway seed 마이그레이션은 멱등성 가드(`WHERE NOT EXISTS` 등) 미사용 — Flyway 버전 관리에 위임.
- **로컬 파일 모드 제약**: 이미 적용된 `V*.sql` 파일을 수정하면 Flyway checksum mismatch로 부팅 실패. 새 V만 추가. 부득이한 경우 `data/awgdas-local.*` 삭제 후 재기동 (로컬 한정).

## 관련 파일 경로

- `src/main/resources/application.yaml`
- `src/main/resources/application-local.yaml`
- `src/main/resources/application-test.yaml`
- `src/main/resources/db/migration/V20260428155500__create_users_table.sql`
- `src/main/resources/db/migration/V20260428155600__seed_initial_admin.sql`
- `src/main/java/com/jongwoo_dev/awgdas_v2/domain/User.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/repository/UserRepository.java`
- `config/secrets.example.yaml` (로컬 + 운영 예시 병행)
- `.gitignore` (`data/awgdas-local.*` 제외)
- `docs/harness/database-rules.md` (규칙)
