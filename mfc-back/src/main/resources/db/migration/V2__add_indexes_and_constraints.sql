ALTER TABLE categories ADD CONSTRAINT uc_categories_name_user UNIQUE (name, user_id);

CREATE INDEX idx_transactions_user_date ON transactions (user_id, date);
CREATE INDEX idx_transactions_user_category ON transactions (user_id, category_id);
CREATE INDEX idx_categories_user_id ON categories (user_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);