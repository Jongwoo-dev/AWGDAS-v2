# Testing Rules

## Test Strategy by Layer

### Repository Layer
- **Annotation**: `@DataJpaTest`
- **Database**: H2 in-memory (Flyway migrations applied automatically)
- **Scope**: CRUD operations, custom query methods, constraints
- **Dependencies**: real JPA repository, no mocking

### Service Layer
- **Annotation**: `@ExtendWith(MockitoExtension.class)`
- **Dependencies**: mock repositories via `@Mock` + `@InjectMocks`
- **Scope**: business logic, validation rules, edge cases
- **No Spring context** — pure unit tests

### Controller Layer
- **Annotation**: `@WebMvcTest(TargetController.class)`
- **Dependencies**: mock services via `@MockBean`
- **Scope**: request mapping, input validation, response format, security rules
- **Use `MockMvc`** for HTTP request simulation

### Integration Tests
- **Annotation**: `@SpringBootTest`
- **Use sparingly** — only for critical end-to-end paths
- **Scope**: full request lifecycle, cross-layer interactions

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Unit test | `{ClassName}Test.java` | `UserServiceTest.java` |
| Integration test | `{ClassName}IntegrationTest.java` | `UserFlowIntegrationTest.java` |

Test classes mirror the main source structure:
```
src/test/java/com/jongwoo_dev/awgdas_v2/
├── repository/UserRepositoryTest.java
├── service/UserServiceTest.java
├── controller/UserControllerTest.java
└── integration/UserFlowIntegrationTest.java
```

## Rules

1. **Every new feature requires tests** — no exceptions
2. **Write tests alongside each layer** — not deferred to the end of implementation
3. **All tests must pass before PR**: `./gradlew test`
4. **Never modify existing tests to make the build pass** — fix the code, not the tests
5. **No skipped or ignored tests** in committed code (`@Disabled` must not be committed)
6. **Use `@DisplayName`** for descriptive test names (Korean or English)

## Test Verification Commands

```
./gradlew test                    # run all tests (mid-development)
./gradlew test --tests "*.UserServiceTest"  # run specific test class
./gradlew clean build             # full build + test (pre-PR)
```

## What NOT To Do

- Do not use `@SpringBootTest` when `@DataJpaTest`, `@WebMvcTest`, or plain unit test suffices
- Do not test framework behavior (e.g., "does Spring Security work?") — test your configuration
- Do not leave `System.out.println` in tests — use assertions
- Do not create test utility classes until the same pattern appears 3+ times
