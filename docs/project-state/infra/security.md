---
area: security
status: in-progress
last-updated: 2026-05-08
related-issues: [#4, #15, #19]
roadmap-refs: [RM-PRODUCT-019, RM-PRODUCT-020]
---

# security — Spring Security 설정 현황

## 구현됨

- `config/SecurityConfig.java`
  - `SecurityFilterChain` 빈: formLogin 기반
  - 공개 경로: `/`, `/login`, `/css/**`, `/js/**`, `/images/**`
  - `/admin/**` → `hasRole("ADMIN")` (PR #15)
  - 그 외 모든 요청: 인증 필요
  - `loginPage=/login`, `defaultSuccessUrl=/`, 로그아웃 모든 사용자 허용
  - `BCryptPasswordEncoder` 빈 등록
- **DB 기반 `UserDetailsService`** (PR #15)
  - `service/DbUserDetailsService.java` — `UserRepository`로 사용자 조회, `Role` enum을 `ROLE_*` 권한으로 변환
  - Spring Security 6 자동 디스커버리 사용 — `SecurityConfig`가 `UserDetailsService`를 명시적으로 주입하지 않음
  - 인메모리 사용자(`dev`/`dev`)는 완전히 제거됨
- **비활성 계정 로그인 거부** (이슈 #19, RM-PRODUCT-020)
  - `service/DbUserDetailsService.java`가 `UserDetails.builder().disabled(!user.isEnabled())`로 `User.enabled` 매핑
  - 비활성 계정 인증 시 Spring Security가 `DisabledException` 발생 → `/login?error`로 redirect
  - `templates/login.html`이 `session.SPRING_SECURITY_LAST_EXCEPTION.class.simpleName == 'DisabledException'`을 검사해 "계정이 비활성화되었습니다" 메시지 분기
- 통합 테스트: `config/SecurityConfigIntegrationTest.java`
  - 공개 경로 접근, 보호 경로 리다이렉트, 로그아웃 동작 검증
  - `/admin/**` 권한 매트릭스: `ROLE_ADMIN` 200, `ROLE_USER` 403, 익명 → `/login` 리다이렉트
  - `/admin/users/**` 권한 매트릭스 동일하게 검증 (이슈 #19)
  - 비활성 계정 실제 로그인 시도(`formLogin()` + `unauthenticated()`) 검증 (이슈 #19)

## 미구현 / TODO

- 프로파일별 보안 설정 분리 (`local`/`dev`/`prod`)
- CSRF 정책 세분화 (현재 전역 활성)
- Remember-me, 세션 관리 정책
- HTTPS/HSTS, 보안 헤더 (운영 단계)
- 권한/역할 체계 확장 (현재 `ADMIN` / `USER` 두 종류)

## 알려진 제약

- 초기 관리자 자격증명(`admin`/`admin123`)은 Flyway seed로 모든 프로파일에서 부팅 시 주입. 운영에서는 별도 마이그레이션/외부 도구로 교체.
- 정적 리소스 경로(`/css/**` 등) 추가 시 SecurityConfig 공개 경로 갱신 필요.
- `/admin/**`는 `AdminController` placeholder + `AdminUserController` 사용자 CRUD를 포함. 두 컨트롤러 모두 `hasRole("ADMIN")` 매처에 자동 포함.
- 비활성 계정 메시지 분기는 `session.SPRING_SECURITY_LAST_EXCEPTION` 클래스 검사에 의존 — 더 명시적인 UX가 필요하면 `formLogin().failureHandler(...)`로 `?error=disabled`처럼 분리하는 방식으로 진화 가능.

## 관련 파일 경로

- `src/main/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfig.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/service/DbUserDetailsService.java`
- `src/main/resources/templates/login.html`
- `src/test/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfigIntegrationTest.java`
