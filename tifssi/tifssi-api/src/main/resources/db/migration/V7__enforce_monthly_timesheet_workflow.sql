ALTER TABLE timesheets.timesheet ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Preserve existing monthly decisions. Orphan daily entries are attached to a
-- draft sheet, never converted into a monthly approval from individual statuses.
INSERT INTO timesheets.timesheet (profile_id, year_number, month_number, status, created_at, updated_at)
SELECT DISTINCT profile_id, EXTRACT(YEAR FROM work_date)::INTEGER,
       EXTRACT(MONTH FROM work_date)::INTEGER, 'DRAFT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM timesheets.time_entry
ON CONFLICT (profile_id, year_number, month_number) DO NOTHING;

UPDATE timesheets.time_entry e SET timesheet_id = t.id
FROM timesheets.timesheet t
WHERE t.profile_id = e.profile_id
  AND t.year_number = EXTRACT(YEAR FROM e.work_date)::INTEGER
  AND t.month_number = EXTRACT(MONTH FROM e.work_date)::INTEGER
  AND e.timesheet_id IS DISTINCT FROM t.id;

ALTER TABLE timesheets.time_entry ALTER COLUMN timesheet_id SET NOT NULL;
ALTER TABLE timesheets.timesheet ADD CONSTRAINT ck_timesheet_year CHECK (year_number >= 2000);
ALTER TABLE timesheets.timesheet ADD CONSTRAINT ck_timesheet_status CHECK (status IN ('DRAFT','SUBMITTED','VALIDATED','REJECTED'));
CREATE INDEX ix_time_entry_timesheet ON timesheets.time_entry(timesheet_id);
