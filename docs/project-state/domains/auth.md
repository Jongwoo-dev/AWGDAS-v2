---
domain: auth
status: in-progress
last-updated: 2026-04-27
related-issues: [#4]
roadmap-refs: []
---

# auth — 인증/로그인

## 구현됨

- **dev-login** (PR #4)
  - `config/SecurityConfig.java` — `InMemoryUserDetailsManager`로 `dev`/`dev` 사용자 1명, BCrypt 인코더
  - `controller/LoginController.java` — `GET /login` → `login.html`
  - `templates/login.html` — Thymeleaf 로그인 폼
  - 인증 정책: `/`, `/login`, `/css/**`, `/js/**`, `/images/**`는 공개. 그 외는 인증 필요.
  - formLogin: `defaultSuccessUrl=/`, 로그아웃은 모든 사용자 허용
  - 테스트: `controller/LoginControllerTest.java` (WebMvcTest), `config/SecurityConfigIntegrationTest.java` (SpringBootTest)

## 미구현 / TODO

- 실제 사용자 영속화 (현재 인메모리만) — DB 기반 `UserDetailsService`로 전환 필요
- 회원가입 플로우
- 외부 OAuth 연동 (Google, GitHub 등)
- 비밀번호 재설정
- 권한/역할 체계 확장 (현재 `USER` 단일)

## 알려진 제약

- **dev-login은 의도된 단순화**다. 운영 환경에서는 사용 금지. 추후 프로파일 분리(`local`/`dev`에서만 활성화) 예정.
- CSRF 활성 상태 — 폼 제출 시 토큰 필요.

## 관련 파일 경로

- `src/main/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfig.java`
- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/LoginController.java`
- `src/main/resources/templates/login.html`
- `src/test/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfigIntegrationTest.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/LoginControllerTest.java`
