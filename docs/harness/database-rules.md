# Database & Flyway Migration Rules

## Core Principle

**`spring.jpa.hibernate.ddl-auto=validate` in all environments.**

Hibernate validates the schema against entity mappings but never modifies the database.
All schema changes go through Flyway migrations — no exceptions.

## Migration File Naming

Format: `V{yyyyMMddHHmmss}__{description}.sql`

- Timestamp-based versioning (not sequential) to avoid conflicts in parallel work
- Double underscore `__` between version and description
- Description in `snake_case`

Examples:
```
V20260415120000__create_users_table.sql
V20260415130000__add_email_to_users.sql
V20260416090000__create_game_sessions_table.sql
```

## Rules

1. **Every schema change must have a migration** — tables, columns, indexes, constraints
2. **Never modify an existing migration file** — always create a new one
3. **One migration = one logical change** — one table creation, one column addition, one constraint change
4. **Prefer idempotent statements** — use `IF NOT EXISTS`, `IF EXISTS` where possible
5. **Separate commits for migrations (recommended)** — commit the migration before the code that depends on it. This is recommended, not mandatory, but becomes important as the project matures
6. **Never use `ddl-auto=create`, `update`, or `create-drop`** — in any profile, in any environment

## Migration Location

```
src/main/resources/db/migration/
```

Flyway picks up `.sql` files from this directory automatically.

## Profile Configuration

### Production (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://...
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

### Development / Test (H2)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
  h2:
    console:
      enabled: true
```

H2 + Flyway: Flyway applies all migrations to the in-memory H2 database on startup.
Hibernate then validates entity mappings against the resulting schema.

## Writing Migrations

### Creating Tables
```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Adding Columns
```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;
```

### Adding Indexes
```sql
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
```

## What NOT To Do

- Do not add `spring.jpa.hibernate.ddl-auto=update` "just for development"
- Do not manually edit the `flyway_schema_history` table
- Do not rename or reorder existing migration files
- Do not put DML (INSERT/UPDATE/DELETE) in schema migration files unless absolutely necessary

## Local H2 File Mode (issue #17 / RM-PRODUCT-023)

The `local` profile uses **H2 in file mode** so data survives app restarts (admin account, sub-accounts, quotas, etc.). The `test` profile remains in-memory.

### Profile summary

| Profile | URL | Persistence |
|---------|-----|-------------|
| `local` | `jdbc:h2:file:./data/awgdas-local;MODE=PostgreSQL;...` | Files in `./data/`, survives restart |
| `test` | `jdbc:h2:mem:testdb;MODE=PostgreSQL;...` | In-memory, fresh per JVM |
| `prod` (future) | `jdbc:postgresql://...` | PostgreSQL |

H2 creates the `./data/` directory automatically on first run — no manual setup needed.
The generated files (`data/awgdas-local.mv.db`, `data/awgdas-local.trace.db`, `data/awgdas-local.lock.db`) are excluded via `.gitignore`.

### Critical: do NOT modify applied migration files

In file mode, Flyway records each applied migration's checksum in `flyway_schema_history`. If you edit an already-applied `V*.sql` file, the next boot fails with:

```
Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 20260428155500
-> Applied to database : <old checksum>
-> Resolved locally    : <new checksum>
```

**Rule (file-mode specific reinforcement of the existing rule above):** never edit an existing `V*.sql`. Always add a new `V` file.

If you absolutely must rewrite history during local development (e.g., the migration was wrong and never reached anyone else):
1. Stop the app.
2. Delete the local H2 database: `rm data/awgdas-local.*`
3. Restart — Flyway re-applies every `V*.sql` from scratch.

This recovery is **local-only** and never applies to shared environments. In production / shared dev DBs, ship a corrective `V` file instead.
