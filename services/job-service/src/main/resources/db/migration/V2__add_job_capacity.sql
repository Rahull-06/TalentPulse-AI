-- openings = headcount to hire; max_applicants = application cap (null = unlimited)
ALTER TABLE jobs
    ADD COLUMN openings INTEGER,
    ADD COLUMN max_applicants INTEGER;

COMMENT ON COLUMN jobs.openings IS 'How many people the company wants to hire for this role';
COMMENT ON COLUMN jobs.max_applicants IS 'Max candidates allowed to apply (NULL = unlimited)';
