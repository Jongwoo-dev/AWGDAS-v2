---
area: database
status: in-progress
last-updated: 2026-05-09
related-issues: [#15, #17, #19, #21]
roadmap-refs: [RM-PRODUCT-019, RM-PRODUCT-023, RM-PRODUCT-020, RM-PRODUCT-021]
---

# database — 데이터베이스/마이그레이션

## 구현됨

- `application.yaml`: `spring.jpa.hibernate.ddl-auto=validate`, `spring.flyway.enabled=true`, `spring.jpa.open-in-view=false`, `spring.profiles.active=local` (default — `./gradlew bootRun`만으로 local 프로파일 활성. 운영 배포 시 `SPRING_PROFILES_ACTIVE` 환경변수로 override)
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
- **users.enabled 컬럼 + UserRepository 확장** (이슈 #19, RM-PRODUCT-020)
  - `src/main/resources/db/migration/V20260507170000__add_user_enabled.sql` — `enabled BOOLEAN NOT NULL DEFAULT TRUE` 컬럼 추가 + `idx_users_enabled` 인덱스. `DEFAULT TRUE`로 기존 admin seed 행은 자동 활성. H2 PostgreSQL mode + PostgreSQL 양쪽 호환
  - `domain/User.java` — `enabled` 필드(`@Column nullable=false`, 기본값 `true`) + 비즈니스 메서드(`enable`/`disable`/`updatePasswordHash`/`updateRole`/`updateEmail`)
  - `repository/UserRepository.java` — `findByEnabled(boolean, Pageable)`, `findByRole(Role, Pageable)`, `findByRoleAndEnabled(Role, boolean, Pageable)`, `countByRoleAndEnabled(Role, boolean)` 추가
- **users.quota 컬럼** (이슈 #21, RM-PRODUCT-021)
  - `src/main/resources/db/migration/V20260509000700__add_user_quota.sql` — `quota INT NOT NULL DEFAULT 10` 컬럼 + `chk_users_quota_non_negative CHECK (quota >= 0)` 제약. `DEFAULT 10`으로 기존 모든 행에 자동 적용. 도메인 가드(`User.adjustQuota` delta < 1 거부) 위의 추가 방어선. 인덱스는 사용 사례 부재로 미생성. H2 PostgreSQL mode + PostgreSQL 양쪽 호환
  - `domain/User.java` — `quota` 필드(`@Column nullable=false`, builder 폴백 시 기본 10) + `adjustQuota(int positiveDelta)` 비즈니스 메서드

## 미구현 / TODO

- 운영용 PostgreSQL 프로파일 미정의
- 추가 도메인 엔티티(게임 명세, 요청, 갤러리 등) 없음
- 할당량 차감(-1) 모델 — 게임 생성 기능 진입 시 별도 이슈로

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
- `src/main/resources/db/migration/V20260507170000__add_user_enabled.sql`
- `src/main/resources/db/migration/V20260509000700__add_user_quota.sql`
- `src/main/java/com/jongwoo_dev/awgdas_v2/domain/User.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/repository/UserRepository.java`
- `config/secrets.example.yaml` (로컬 + 운영 예시 병행)
- `.gitignore` (`data/awgdas-local.*` 제외)
- `docs/harness/database-rules.md` (규칙)
