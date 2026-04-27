---
domain: home
status: stable
last-updated: 2026-04-27
related-issues: [#4]
roadmap-refs: []
---

# home — 홈 페이지

## 구현됨

- `controller/HomeController.java` — `GET /` → `home.html` (로직 없음)
- `templates/home.html` — Thymeleaf 정적 환영 페이지
- 인증 불필요 (Security 설정에서 공개 경로)
- 테스트: `controller/HomeControllerTest.java` (WebMvcTest)

## 미구현 / TODO

- 인증 사용자에게는 다른 콘텐츠 노출 (현재 동일 페이지)
- 동적 데이터 (예: 최근 활동, 사용자 정보)

## 알려진 제약

- 현 단계 목표상 단순 페이지 유지. 동적화는 인증 도메인 확장 이후 검토.

## 관련 파일 경로

- `src/main/java/com/jongwoo_dev/awgdas_v2/controller/HomeController.java`
- `src/main/resources/templates/home.html`
- `src/test/java/com/jongwoo_dev/awgdas_v2/controller/HomeControllerTest.java`
