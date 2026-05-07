---
domain: auth
status: in-progress
last-updated: 2026-05-08
related-issues: [#4, #15, #19]
roadmap-refs: [RM-PRODUCT-019, RM-PRODUCT-020]
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
- **관리자 — 하위 계정 CRUD** (RM-PRODUCT-020, 이슈 #19)
  - `controller/AdminUserController.java` — `/admin/users` 목록(필터·페이징), `/new` 생성 폼, `POST /admin/users` 생성, `/{id}/edit` 수정 폼, `POST /{id}` 수정, `POST /{id}/reset-password` 비밀번호 재설정, `POST /{id}/toggle` 활성/비활성, `POST /{id}/delete` 삭제. 모두 redirect + flash message
  - `service/AdminUserService.java` — CRUD + 임시 비밀번호 발급(create/reset) + last-admin 가드 + 자기 보호. `User` 비즈니스 메서드(`enable`/`disable`/`updatePasswordHash`/`updateRole`/`updateEmail`)로 영속 객체 수정
  - 자기 보호 규칙: 자기 자신 비활성/삭제/ADMIN→USER 강등 차단(`SelfModificationException`)
  - last-admin 가드: 활성 admin이 1명 이하일 때 admin 비활성/삭제/강등 차단(`LastAdminException`)
  - 비활성 계정 로그인 거부: `service/DbUserDetailsService.java`가 `User.isEnabled()`를 `UserDetails.disabled()`로 매핑 → Spring Security가 `DisabledException` 발생 → `templates/login.html`이 `session.SPRING_SECURITY_LAST_EXCEPTION` 클래스를 검사해 "계정이 비활성화되었습니다" 메시지 분기
  - DTO: `dto/CreateUserRequest.java`, `dto/UpdateUserRequest.java` (Java records, `@NotBlank`/`@Size`/`@Email`/`@NotNull` 검증), `dto/UserListItem.java` (목록 표시용)
  - 예외 인프라: `exception/UserNotFoundException`, `exception/UsernameAlreadyExistsException`, `exception/SelfModificationException`, `exception/LastAdminException`, `exception/GlobalExceptionHandler`(@ControllerAdvice — 4종 예외를 redirect + errorMessage flash로 처리)
  - 비밀번호 생성: `util/PasswordGenerator.java` — `SecureRandom` 12자 영숫자, 정적 유틸
  - 템플릿: `templates/admin/users/list.html`(역할/활성 필터 + 페이징), `form.html`(생성), `edit.html`(수정 — username readonly)
  - `templates/admin/index.html`에 `/admin/users` 진입 링크 추가
  - 테스트: `service/AdminUserServiceTest`(Mockito 단위, 자기 보호/last-admin 케이스 포함), `controller/AdminUserControllerTest`(WebMvcTest, 권한 매트릭스 + CSRF + validation + flash 검증), `util/PasswordGeneratorTest`, `repository/UserRepositoryTest` 확장(`findByEnabled`/`findByRole`/`findByRoleAndEnabled`/`countByRoleAndEnabled`), `service/DbUserDetailsServiceTest` 확장(비활성 케이스), `config/SecurityConfigIntegrationTest` 확장(`/admin/users/**` 매트릭스 + 비활성 계정 로그인 거부 통합 검증)

## 미구현 / TODO

- 외부 OAuth 연동 (Google, GitHub 등)
- 본인 비밀번호 변경(자기 자신용 — 본 작업의 reset-password는 관리자 강제 재설정)
- Remember-me, 세션 관리 정책
- 페이징/필터 UI 접근성(ARIA, 키보드 내비)은 별도 UX 이슈로 분리

## 알려진 제약

- 초기 관리자(`admin`/`admin123`)는 **개발 편의용**. 운영에서는 첫 부팅 후 즉시 비밀번호 변경 또는 별도 마이그레이션으로 교체해야 함.
- CSRF 활성 상태 — 폼 제출 시 토큰 필요.
- BCrypt seed는 Flyway 버전 관리에 위임(멱등성 가드 미사용). 재배포 시 `flyway_schema_history`가 보존되면 1회만 실행됨.
- 관리자 CRUD는 hard delete만 지원 — 감사 로그/소프트 삭제는 별도 요구사항.
- 임시 비밀번호는 redirect flash로 1회 표시 후 사라짐 — 관리자가 즉시 사용자에게 전달해야 함(이메일 발송 미구현).
- 비활성 계정 로그인 거부 메시지 분기는 `session.SPRING_SECURITY_LAST_EXCEPTION` 클래스 검사 기반. failureHandler 커스텀 도입 시 더 명시적으로 변경 가능.

## 관련 파일 경로

- `src/main/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfig.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/LoginController.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/AdminController.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/AdminUserController.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/domain/User.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/domain/Role.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/dto/CreateUserRequest.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/dto/UpdateUserRequest.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/dto/UserListItem.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/exception/GlobalExceptionHandler.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/exception/UserNotFoundException.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/exception/UsernameAlreadyExistsException.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/exception/SelfModificationException.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/exception/LastAdminException.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/repository/UserRepository.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/service/AdminUserService.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/service/DbUserDetailsService.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/util/PasswordGenerator.java`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/admin/index.html`
- `src/main/resources/templates/admin/users/list.html`
- `src/main/resources/templates/admin/users/form.html`
- `src/main/resources/templates/admin/users/edit.html`
- `src/main/resources/db/migration/V20260428155500__create_users_table.sql`
- `src/main/resources/db/migration/V20260428155600__seed_initial_admin.sql`
- `src/main/resources/db/migration/V20260507170000__add_user_enabled.sql`
- `src/test/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfigIntegrationTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/LoginControllerTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/AdminControllerTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/AdminUserControllerTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/repository/UserRepositoryTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/service/AdminUserServiceTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/service/DbUserDetailsServiceTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/util/PasswordGeneratorTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/tools/BcryptHashGenerator.java`
