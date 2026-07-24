-- Runs once when the Postgres container is first created.
-- Auth Service uses: talentpulse_auth

CREATE DATABASE talentpulse_auth;

-- Ready for later services (created now so you don't recreate the volume later)
CREATE DATABASE talentpulse_job;
CREATE DATABASE talentpulse_candidate;
CREATE DATABASE talentpulse_scoring;
CREATE DATABASE talentpulse_notification;
