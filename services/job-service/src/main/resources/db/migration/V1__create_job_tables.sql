-- V1: Job Service tables

CREATE TABLE jobs (
    id               UUID PRIMARY KEY,
    organization_id  UUID           NOT NULL,
    created_by       UUID           NOT NULL,
    title            VARCHAR(200)   NOT NULL,
    description      TEXT           NOT NULL,
    location         VARCHAR(150)   NOT NULL,
    employment_type  VARCHAR(20)    NOT NULL,
    experience_min   INT            NOT NULL,
    experience_max   INT            NOT NULL,
    salary_min       NUMERIC(12, 2) NULL,
    salary_max       NUMERIC(12, 2) NULL,
    currency         VARCHAR(10)    NULL,
    status           VARCHAR(20)    NOT NULL,
    published_at     TIMESTAMPTZ    NULL,
    closed_at        TIMESTAMPTZ    NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_jobs_organization_id ON jobs (organization_id);
CREATE INDEX idx_jobs_organization_status ON jobs (organization_id, status);
CREATE INDEX idx_jobs_status ON jobs (status);

CREATE TABLE job_skills (
    id          UUID PRIMARY KEY,
    job_id      UUID         NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    skill_name  VARCHAR(100) NOT NULL,
    skill_type  VARCHAR(20)  NOT NULL,
    weight      INT          NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_job_skills_job_id ON job_skills (job_id);
