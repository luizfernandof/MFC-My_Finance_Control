ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE categories
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE transactions
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE refresh_tokens
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- In production this column is already TIMESTAMPTZ because the V1 entry was a
-- Flyway baseline over a Hibernate-created schema. Only convert installations
-- where the column still uses the type declared by V1__initial_schema.sql.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'refresh_tokens'
          AND column_name = 'expiry_date'
          AND data_type = 'timestamp without time zone'
    ) THEN
        ALTER TABLE refresh_tokens
            ALTER COLUMN expiry_date TYPE TIMESTAMPTZ
            USING expiry_date AT TIME ZONE 'UTC';
    END IF;
END $$;

ALTER TABLE transaction_groups
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMPTZ USING updated_at AT TIME ZONE 'UTC';

-- Drop the old case-sensitive constraints by their columns instead of their
-- names. Hibernate generated a different users.email constraint name in the
-- production baseline, while fresh Flyway installations use uc_users_email.
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT constraint_to_drop.conname
        FROM pg_constraint constraint_to_drop
        WHERE constraint_to_drop.conrelid = 'categories'::regclass
          AND constraint_to_drop.contype = 'u'
          AND (
              SELECT array_agg(attribute.attname::TEXT ORDER BY attribute.attname)
              FROM unnest(constraint_to_drop.conkey) AS constrained_column(attnum)
              JOIN pg_attribute attribute
                ON attribute.attrelid = constraint_to_drop.conrelid
               AND attribute.attnum = constrained_column.attnum
          ) = ARRAY['name', 'user_id']::TEXT[]
    LOOP
        EXECUTE format('ALTER TABLE categories DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

CREATE UNIQUE INDEX uc_categories_name_user_ci ON categories (user_id, LOWER(name));

DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT constraint_to_drop.conname
        FROM pg_constraint constraint_to_drop
        WHERE constraint_to_drop.conrelid = 'users'::regclass
          AND constraint_to_drop.contype = 'u'
          AND (
              SELECT array_agg(attribute.attname::TEXT ORDER BY attribute.attname)
              FROM unnest(constraint_to_drop.conkey) AS constrained_column(attnum)
              JOIN pg_attribute attribute
                ON attribute.attrelid = constraint_to_drop.conrelid
               AND attribute.attnum = constrained_column.attnum
          ) = ARRAY['email']::TEXT[]
    LOOP
        EXECUTE format('ALTER TABLE users DROP CONSTRAINT %I', constraint_name);
    END LOOP;
END $$;

CREATE UNIQUE INDEX uc_users_email_ci ON users (LOWER(email));

ALTER TABLE transactions
    ADD CONSTRAINT ck_transactions_amount_positive CHECK (amount > 0),
    ADD CONSTRAINT ck_transactions_type CHECK (type IN ('INCOME', 'EXPENSE'));

ALTER TABLE categories
    ADD CONSTRAINT ck_categories_type CHECK (type IN ('INCOME', 'EXPENSE'));

ALTER TABLE transaction_groups
    ADD CONSTRAINT ck_transaction_groups_type CHECK (type IN ('INSTALLMENT', 'RECURRING')),
    ADD CONSTRAINT ck_transaction_groups_installments CHECK (
        (type = 'INSTALLMENT' AND (total_installments IS NULL OR total_installments > 1))
        OR (type = 'RECURRING' AND total_installments IS NULL)
    );
