ALTER TABLE transactions ADD COLUMN active BOOLEAN DEFAULT true;
UPDATE transactions SET active = true WHERE active IS NULL;
ALTER TABLE transactions ALTER COLUMN active SET NOT NULL;