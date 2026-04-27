---
area: testing
status: in-progress
last-updated: 2026-04-27
related-issues: [#4]
roadmap-refs: []
---

# testing — 테스트 전략 현황

## 구현됨

- 컨트롤러 단위 테스트: `@WebMvcTest({Controller.class})` + `@Import(SecurityConfig.class)` 패턴
  - `controller/HomeControllerTest.java`
  - `controller/LoginControllerTest.java`
- 보안 통합 테스트: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`
  - `config/SecurityConfigIntegrationTest.java`
- 컨텍스트 부팅 테스트: `AwgdasV2ApplicationTests.java`
- DisplayName은 한국어 사용
- 테스트 프로파일: `application-test.yaml` + `secrets-test.yaml` (H2)

## 미구현 / TODO

- `@DataJpaTest` 사용 사례 (Repository 등장 시)
- `@ExtendWith(MockitoExtension.class)` 서비스 단위 테스트 (Service 등장 시)
- 테스트 커버리지 측정 도구 (JaCoCo 등) 미도입

## 알려진 제약

- 모든 신규 기능에 테스트 필수 (`docs/harness/testing-rules.md`)
- 테스트 파일명: `{ClassName}Test.java` (대상 클래스와 동일 패키지)
- `secrets-test.yaml`은 git 미포함 — 로컬 작성 필요.

## 관련 파일 경로

- `src/test/java/com/jongwoo_dev/awgdas_v2/`
- `docs/harness/testing-rules.md` (규칙)
