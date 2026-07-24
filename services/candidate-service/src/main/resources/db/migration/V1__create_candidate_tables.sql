-- V1: Candidate Service tables

CREATE TABLE candidate_profiles (
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL UNIQUE,
    headline          VARCHAR(200) NULL,
    summary           TEXT         NULL,
    experience_years  INT          NULL,
    location          VARCHAR(150) NULL,
    linkedin_url      VARCHAR(255) NULL,
    github_url        VARCHAR(255) NULL,
    phone             VARCHAR(30)  NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE TABLE resumes (
    id                     UUID PRIMARY KEY,
    candidate_profile_id   UUID         NOT NULL REFERENCES candidate_profiles (id) ON DELETE CASCADE,
    file_name              VARCHAR(255) NOT NULL,
    file_url               VARCHAR(500) NOT NULL,
    file_type              VARCHAR(20)  NOT NULL,
    parsed_text            TEXT         NULL,
    parse_status           VARCHAR(20)  NOT NULL,
    primary_resume         BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_at            TIMESTAMPTZ  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_resumes_profile_id ON resumes (candidate_profile_id);

CREATE TABLE candidate_skills (
    id                     UUID PRIMARY KEY,
    candidate_profile_id   UUID         NOT NULL REFERENCES candidate_profiles (id) ON DELETE CASCADE,
    skill_name             VARCHAR(100) NOT NULL,
    proficiency            VARCHAR(20)  NULL,
    source                 VARCHAR(20)  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_candidate_skills_profile_id ON candidate_skills (candidate_profile_id);

CREATE TABLE applications (
    id                     UUID PRIMARY KEY,
    job_id                 UUID         NOT NULL,
    organization_id        UUID         NOT NULL,
    candidate_profile_id   UUID         NOT NULL REFERENCES candidate_profiles (id),
    resume_id              UUID         NOT NULL REFERENCES resumes (id),
    status                 VARCHAR(30)  NOT NULL,
    cover_letter           TEXT         NULL,
    applied_at             TIMESTAMPTZ  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_application_job_candidate UNIQUE (job_id, candidate_profile_id)
);

CREATE INDEX idx_applications_job_id ON applications (job_id);
CREATE INDEX idx_applications_job_status ON applications (job_id, status);
CREATE INDEX idx_applications_profile_id ON applications (candidate_profile_id);
CREATE INDEX idx_applications_organization_id ON applications (organization_id);

CREATE TABLE application_status_history (
    id               UUID PRIMARY KEY,
    application_id   UUID         NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    from_status      VARCHAR(30)  NULL,
    to_status        VARCHAR(30)  NOT NULL,
    changed_by       UUID         NULL,
    note             TEXT         NULL,
    changed_at       TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_app_status_history_application_id ON application_status_history (application_id);
