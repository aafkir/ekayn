DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice' AND column_name = 'total_excl_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice' AND column_name = 'total_ht'
    ) THEN
        ALTER TABLE billing.invoice RENAME COLUMN total_excl_tax TO total_ht;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice' AND column_name = 'total_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice' AND column_name = 'total_vat'
    ) THEN
        ALTER TABLE billing.invoice RENAME COLUMN total_tax TO total_vat;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice' AND column_name = 'total_incl_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice' AND column_name = 'total_ttc'
    ) THEN
        ALTER TABLE billing.invoice RENAME COLUMN total_incl_tax TO total_ttc;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'line_description'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'description'
    ) THEN
        ALTER TABLE billing.invoice_line RENAME COLUMN line_description TO description;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'unit_price_excl_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'unit_price'
    ) THEN
        ALTER TABLE billing.invoice_line RENAME COLUMN unit_price_excl_tax TO unit_price;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'line_total_excl_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'total_ht'
    ) THEN
        ALTER TABLE billing.invoice_line RENAME COLUMN line_total_excl_tax TO total_ht;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'line_total_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'total_vat'
    ) THEN
        ALTER TABLE billing.invoice_line RENAME COLUMN line_total_tax TO total_vat;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'line_total_incl_tax'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'billing' AND table_name = 'invoice_line' AND column_name = 'total_ttc'
    ) THEN
        ALTER TABLE billing.invoice_line RENAME COLUMN line_total_incl_tax TO total_ttc;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'timesheets' AND table_name = 'time_entry' AND column_name = 'hours_worked'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'timesheets' AND table_name = 'time_entry' AND column_name = 'quantity'
    ) THEN
        ALTER TABLE timesheets.time_entry RENAME COLUMN hours_worked TO quantity;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'timesheets' AND table_name = 'time_entry' AND column_name = 'description'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'timesheets' AND table_name = 'time_entry' AND column_name = 'comment'
    ) THEN
        ALTER TABLE timesheets.time_entry RENAME COLUMN description TO comment;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'expenses' AND table_name = 'expense' AND column_name = 'description'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'expenses' AND table_name = 'expense' AND column_name = 'comment'
    ) THEN
        ALTER TABLE expenses.expense RENAME COLUMN description TO comment;
    END IF;
END $$;

ALTER TABLE projects.project
    ADD COLUMN IF NOT EXISTS contact_id BIGINT;

ALTER TABLE billing.invoice
    ADD COLUMN IF NOT EXISTS total_ht NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_vat NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_ttc NUMERIC(14, 2) NOT NULL DEFAULT 0;

UPDATE billing.invoice
SET total_ht = COALESCE(total_ht, 0),
    total_vat = COALESCE(total_vat, 0),
    total_ttc = COALESCE(total_ttc, 0);

ALTER TABLE billing.invoice
    ALTER COLUMN total_ht SET NOT NULL,
    ALTER COLUMN total_vat SET NOT NULL,
    ALTER COLUMN total_ttc SET NOT NULL,
    ALTER COLUMN total_ht DROP DEFAULT,
    ALTER COLUMN total_vat DROP DEFAULT,
    ALTER COLUMN total_ttc DROP DEFAULT;

ALTER TABLE billing.invoice_line
    ADD COLUMN IF NOT EXISTS line_type VARCHAR(20) NOT NULL DEFAULT 'SERVICE',
    ADD COLUMN IF NOT EXISTS description VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS unit VARCHAR(30) NOT NULL DEFAULT 'DAY',
    ADD COLUMN IF NOT EXISTS unit_price NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_ht NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_vat NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_ttc NUMERIC(14, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS source_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS source_id BIGINT;

UPDATE billing.invoice_line
SET line_type = COALESCE(line_type, 'SERVICE'),
    description = COALESCE(description, ''),
    unit = COALESCE(unit, 'DAY'),
    unit_price = COALESCE(unit_price, 0),
    total_ht = COALESCE(total_ht, 0),
    total_vat = COALESCE(total_vat, 0),
    total_ttc = COALESCE(total_ttc, 0);

ALTER TABLE billing.invoice_line
    ALTER COLUMN line_type SET NOT NULL,
    ALTER COLUMN description SET NOT NULL,
    ALTER COLUMN unit SET NOT NULL,
    ALTER COLUMN unit_price SET NOT NULL,
    ALTER COLUMN total_ht SET NOT NULL,
    ALTER COLUMN total_vat SET NOT NULL,
    ALTER COLUMN total_ttc SET NOT NULL,
    ALTER COLUMN line_type DROP DEFAULT,
    ALTER COLUMN description DROP DEFAULT,
    ALTER COLUMN unit DROP DEFAULT,
    ALTER COLUMN unit_price DROP DEFAULT,
    ALTER COLUMN total_ht DROP DEFAULT,
    ALTER COLUMN total_vat DROP DEFAULT,
    ALTER COLUMN total_ttc DROP DEFAULT;

ALTER TABLE timesheets.time_entry
    ADD COLUMN IF NOT EXISTS quantity NUMERIC(6, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS unit_type VARCHAR(20) NOT NULL DEFAULT 'DAY',
    ADD COLUMN IF NOT EXISTS comment VARCHAR(1000);

UPDATE timesheets.time_entry
SET quantity = COALESCE(quantity, 0),
    unit_type = COALESCE(unit_type, 'DAY');

ALTER TABLE timesheets.time_entry
    ALTER COLUMN quantity SET NOT NULL,
    ALTER COLUMN unit_type SET NOT NULL,
    ALTER COLUMN quantity DROP DEFAULT,
    ALTER COLUMN unit_type DROP DEFAULT;

ALTER TABLE expenses.expense
    ADD COLUMN IF NOT EXISTS mission_id BIGINT,
    ADD COLUMN IF NOT EXISTS comment VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(500);

ALTER TABLE expenses.expense
    ALTER COLUMN mission_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_project_contact' AND conrelid = 'projects.project'::regclass
    ) THEN
        ALTER TABLE projects.project
            ADD CONSTRAINT fk_project_contact FOREIGN KEY (contact_id) REFERENCES crm.contact (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_expense_mission' AND conrelid = 'expenses.expense'::regclass
    ) THEN
        ALTER TABLE expenses.expense
            ADD CONSTRAINT fk_expense_mission FOREIGN KEY (mission_id) REFERENCES projects.mission (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_project_contact_id ON projects.project (contact_id);
CREATE INDEX IF NOT EXISTS idx_expense_mission_id ON expenses.expense (mission_id);
