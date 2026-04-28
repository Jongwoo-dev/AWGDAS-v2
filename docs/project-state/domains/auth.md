---
domain: auth
status: in-progress
last-updated: 2026-04-29
related-issues: [#4, #15]
roadmap-refs: [RM-PRODUCT-019]
---

# auth — 인증/로그인

## 구현됨

- **dev-login** (PR #4)
  - `controller/LoginController.java` — `GET /login` → `login.html`
  - `templates/login.html` — Thymeleaf 로그인 폼
  - 인증 정책: `/`, `/login`, `/css/**`, `/js/**`, `/images/**`는 공개. 그 외는 인증 필요.
  - formLogin: `defaultSuccessUrl=/`, 로그아웃은 모든 사용자 허용
  - 테스트: `controller/LoginControllerTest.java` (WebMvcTest), `config/SecurityConfigIntegrationTest.java` (SpringBootTest)
- **DB 기반 사용자 인증** (PR #15, RM-PRODUCT-019)
  - `domain/User.java` — JPA 엔티티 (`@CreationTimestamp`/`@UpdateTimestamp`로 시간 관리, `Role` enum을 `@Enumerated(STRING)`)
  - `domain/Role.java` — `ADMIN`, `USER` enum
  - `repository/UserRepository.java` — `JpaRepository<User, Long>` + `findByUsername`
  - `service/DbUserDetailsService.java` — Spring Security `UserDetailsService` 구현, `ROLE_` 접두사 부여
  - Flyway 마이그레이션 2개:
    - `V20260428155500__create_users_table.sql` — `users` 테이블
    - `V20260428155600__seed_initial_admin.sql` — 초기 관리자 seed
  - **로컬 dev 자격증명**: `admin` / `admin123` (Flyway seed로 주입됨, BCrypt hash 사용)
  - BCrypt 해시 재생성 유틸: `src/test/java/.../tools/BcryptHashGenerator.java`
- **관리자 페이지 (placeholder)** (PR #15)
  - `controller/AdminController.java` — `GET /admin` → `admin/index.html`
  - 권한 분기: `/admin/**`은 `ROLE_ADMIN`만, 그 외 인증 사용자는 일반 페이지
  - 테스트: `controller/AdminControllerTest.java` (WebMvcTest), `config/SecurityConfigIntegrationTest.java`에 `/admin/**` 권한 매트릭스 추가

## 미구현 / TODO

- 회원가입 플로우 — 셀프 가입 없음 (관리자 발급 기반, RM-PRODUCT-020)
- 외부 OAuth 연동 (Google, GitHub 등)
- 비밀번호 재설정
- Remember-me, 세션 관리 정책
- `dev`/`dev` 인메모리 사용자는 PR #15에서 완전히 제거됨 (필요 시 admin/admin123 사용)

## 알려진 제약

- 초기 관리자(`admin`/`admin123`)는 **개발 편의용**. 운영에서는 첫 부팅 후 즉시 비밀번호 변경 또는 별도 마이그레이션으로 교체해야 함.
- CSRF 활성 상태 — 폼 제출 시 토큰 필요.
- BCrypt seed는 Flyway 버전 관리에 위임(멱등성 가드 미사용). 재배포 시 `flyway_schema_history`가 보존되면 1회만 실행됨.

## 관련 파일 경로

- `src/main/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfig.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/LoginController.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/AdminController.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/domain/User.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/domain/Role.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/repository/UserRepository.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/service/DbUserDetailsService.java`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/admin/index.html`
- `src/main/resources/db/migration/V20260428155500__create_users_table.sql`
- `src/main/resources/db/migration/V20260428155600__seed_initial_admin.sql`
- `src/test/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfigIntegrationTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/LoginControllerTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/AdminControllerTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/repository/UserRepositoryTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/service/DbUserDetailsServiceTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/tools/BcryptHashGenerator.java`
