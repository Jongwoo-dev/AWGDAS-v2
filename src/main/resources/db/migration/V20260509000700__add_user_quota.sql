ALTER TABLE users ADD COLUMN quota INT NOT NULL DEFAULT 10;
ALTER TABLE users ADD CONSTRAINT chk_users_quota_non_negative CHECK (quota >= 0);
