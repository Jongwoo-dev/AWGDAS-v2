ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX idx_users_enabled ON users(enabled);
