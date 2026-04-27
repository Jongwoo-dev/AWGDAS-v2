---
area: security
status: in-progress
last-updated: 2026-04-27
related-issues: [#4]
roadmap-refs: []
---

# security — Spring Security 설정 현황

## 구현됨

- `config/SecurityConfig.java`
  - `SecurityFilterChain` 빈: formLogin 기반
  - 공개 경로: `/`, `/login`, `/css/**`, `/js/**`, `/images/**`
  - 그 외 모든 요청: 인증 필요
  - `loginPage=/login`, `defaultSuccessUrl=/`, 로그아웃 모든 사용자 허용
  - `BCryptPasswordEncoder` 빈 등록
  - `InMemoryUserDetailsManager`: `dev`/`dev` (USER) — **개발용 한정**
- 통합 테스트: `config/SecurityConfigIntegrationTest.java`
  - 공개 경로 접근, 보호 경로 리다이렉트, 로그아웃 동작 검증

## 미구현 / TODO

- DB 기반 `UserDetailsService` 전환 (auth 도메인 진척과 연동)
- 프로파일별 보안 설정 분리 (`local`/`dev`/`prod`)
- CSRF 정책 세분화 (현재 전역 활성)
- Remember-me, 세션 관리 정책
- HTTPS/HSTS, 보안 헤더 (운영 단계)

## 알려진 제약

- 인메모리 사용자는 **dev 편의용**. 운영 빌드에서 제거하거나 프로파일로 차단해야 함.
- 정적 리소스 경로(`/css/**` 등) 추가 시 SecurityConfig 공개 경로 갱신 필요.

## 관련 파일 경로

- `src/main/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfig.java`
- `src/test/java/com/jongwoo_dev/awgdas_v2/config/SecurityConfigIntegrationTest.java`
