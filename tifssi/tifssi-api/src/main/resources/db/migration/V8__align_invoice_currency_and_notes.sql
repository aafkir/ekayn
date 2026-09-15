-- These fields already exist in the Invoice entity. Align databases created
-- from migrations as well as older DEV databases, preserving existing values.
ALTER TABLE billing.invoice
    ADD COLUMN IF NOT EXISTS currency_code VARCHAR(3) NOT NULL DEFAULT 'EUR',
    ADD COLUMN IF NOT EXISTS notes VARCHAR(2000);
ALTER TABLE billing.invoice ALTER COLUMN currency_code DROP DEFAULT;
