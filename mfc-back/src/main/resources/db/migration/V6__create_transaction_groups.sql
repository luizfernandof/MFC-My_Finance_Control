CREATE TABLE transaction_groups (
    id UUID NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    total_installments INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tg_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_transaction_groups_user_id ON transaction_groups (user_id);

INSERT INTO transaction_groups (id, user_id, type, total_installments, created_at, updated_at)
SELECT DISTINCT
    t.group_id::uuid,
    t.user_id,
    CASE
        WHEN t.description ~ '\(\d+/\d+\)' THEN 'INSTALLMENT'
        WHEN t.description LIKE '%[Recorrente]%' THEN 'RECURRING'
        ELSE 'INSTALLMENT'
    END,
    NULL::INTEGER,
    NOW(),
    NOW()
FROM transactions t
WHERE t.group_id IS NOT NULL;

ALTER TABLE transactions ADD COLUMN transaction_group_id UUID REFERENCES transaction_groups (id);

UPDATE transactions SET transaction_group_id = group_id::uuid WHERE group_id IS NOT NULL;

ALTER TABLE transactions DROP COLUMN group_id;