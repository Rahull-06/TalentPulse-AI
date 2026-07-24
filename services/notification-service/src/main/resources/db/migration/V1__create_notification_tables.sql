-- V1: Notification Service tables

CREATE TABLE notifications (
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL,
    type        VARCHAR(40)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     TEXT         NOT NULL,
    link        VARCHAR(500) NULL,
    read_flag   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, read_flag);

CREATE TABLE email_logs (
    id                  UUID PRIMARY KEY,
    to_email            VARCHAR(180) NOT NULL,
    subject             VARCHAR(250) NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    provider_response   TEXT         NULL,
    notification_id     UUID         NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_email_logs_to_email ON email_logs (to_email);
CREATE INDEX idx_email_logs_status ON email_logs (status);
