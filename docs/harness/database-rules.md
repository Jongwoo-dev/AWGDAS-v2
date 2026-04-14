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
