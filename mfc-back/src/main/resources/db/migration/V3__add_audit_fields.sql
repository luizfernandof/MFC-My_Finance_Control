ALTER TABLE users ADD COLUMN created_at TIMESTAMP;
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE categories ADD COLUMN created_at TIMESTAMP;
ALTER TABLE categories ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE transactions ADD COLUMN created_at TIMESTAMP;
ALTER TABLE transactions ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE refresh_tokens ADD COLUMN created_at TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN updated_at TIMESTAMP;

UPDATE users SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL;
UPDATE categories SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL;
UPDATE transactions SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL;
UPDATE refresh_tokens SET created_at = NOW(), updated_at = NOW() WHERE created_at IS NULL;

ALTER TABLE users ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE users ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE categories ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE categories ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE transactions ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE transactions ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE refresh_tokens ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE refresh_tokens ALTER COLUMN updated_at SET NOT NULL;