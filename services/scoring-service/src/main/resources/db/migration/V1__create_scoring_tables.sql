-- V1: Scoring Service tables

CREATE TABLE score_results (
    id                UUID PRIMARY KEY,
    application_id    UUID           NOT NULL,
    job_id            UUID           NOT NULL,
    organization_id   UUID           NOT NULL,
    fit_score         NUMERIC(5, 2)  NOT NULL,
    scoring_mode      VARCHAR(20)    NOT NULL,
    matched_skills    JSONB          NOT NULL DEFAULT '[]'::jsonb,
    missing_skills    JSONB          NOT NULL DEFAULT '[]'::jsonb,
    resume_summary    TEXT           NULL,
    explanation       TEXT           NOT NULL,
    model_name        VARCHAR(100)   NULL,
    created_at        TIMESTAMPTZ    NOT NULL,
    updated_at        TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uk_score_application UNIQUE (application_id),
    CONSTRAINT chk_fit_score_range CHECK (fit_score >= 0 AND fit_score <= 100)
);

CREATE INDEX idx_score_results_job_score ON score_results (job_id, fit_score DESC);
CREATE INDEX idx_score_results_organization_id ON score_results (organization_id);
CREATE INDEX idx_score_results_application_id ON score_results (application_id);

CREATE TABLE interview_questions (
    id                UUID PRIMARY KEY,
    application_id    UUID         NOT NULL,
    job_id            UUID         NOT NULL,
    questions         JSONB        NOT NULL DEFAULT '[]'::jsonb,
    focus_skills      JSONB        NOT NULL DEFAULT '[]'::jsonb,
    generated_by      VARCHAR(20)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_interview_questions_application_id ON interview_questions (application_id);
CREATE INDEX idx_interview_questions_job_id ON interview_questions (job_id);
