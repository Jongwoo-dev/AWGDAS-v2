# AWGDAS-v2

Autonomous Web Game Dev Agent System v2 — Spring Boot based web application.

Repository: `Jongwoo-dev/AWGDAS-v2`

## Tech Stack

- Java 25, Spring Boot 4.0.5, Gradle 9.4.1
- Spring Data JPA + PostgreSQL (prod) / H2 (dev/test)
- Flyway for DB migrations
- Spring Security + Thymeleaf (thymeleaf-extras-springsecurity6)
- Lombok
- JUnit 5 (JUnit Platform)

## Commands

```
./gradlew build          # compile + test
./gradlew test           # tests only (mid-development verification)
./gradlew bootRun        # run app
./gradlew clean build    # clean rebuild (final verification before PR)
```

## Project Structure

```
src/main/java/com/jongwoo_dev/awgdas_v2/
├── AwgdasV2Application.java
├── config/          # Spring @Configuration classes
├── domain/          # JPA entities (one per aggregate)
├── repository/      # Spring Data JPA repository interfaces
├── service/         # Business logic (@Service)
├── controller/      # Web controllers (@Controller) — thin, delegate to services
├── dto/             # Request/Response DTOs (Java records)
└── exception/       # Custom exceptions + @ControllerAdvice handler

src/main/resources/
├── application.yaml
├── db/migration/    # Flyway SQL migration files
├── templates/       # Thymeleaf templates
└── static/          # CSS, JS, images

src/test/java/com/jongwoo_dev/awgdas_v2/
└── (mirrors main structure)
```

## Core Conventions

1. **Constructor injection only** — use `@RequiredArgsConstructor`, never field `@Autowired`
2. **Never expose entities** — use DTOs (Java records) at controller boundaries
3. **Controllers are thin** — delegate to services immediately, no business logic
4. **`ddl-auto=validate` always** — never use `create`, `update`, or `create-drop`
5. **All schema changes via Flyway** — see `docs/harness/database-rules.md`
6. **Validation** — `@Valid` on controller params, constraints on DTO fields
7. **Exceptions** — custom exceptions extend `RuntimeException`, handle via `@ControllerAdvice`

## Git Workflow

- **Branch naming**: `issue/{number}-{short-kebab-description}` (e.g., `issue/12-user-authentication`)
- **Commit format**: conventional commits in English (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`)
- **Issue reference**: include `Refs #N` or `Closes #N` in commit body
- **Never commit directly to `main`** — always use feature branches

## Issue-Based Workflow

> Details: `docs/harness/issue-workflow.md`

- `/issue-start {N}` → user approval → implement → `/issue-pr {N}` (new `agent-ready` issues only)
- `changes-requested` rework: manual instruction in Phase 1, `/issue-revise` command in Phase 2

## Required Permissions

Add these to your local `.claude/settings.local.json`:

```
Bash(git:*), Bash(./gradlew *), Bash(./gradlew.bat *),
Bash(gh issue *), Bash(gh pr *), Bash(gh label *)
```

## See Also

- `docs/harness/issue-workflow.md` — Issue-based development workflow details
- `docs/harness/database-rules.md` — Flyway / DB migration rules
- `docs/harness/testing-rules.md` — Testing strategy and conventions
