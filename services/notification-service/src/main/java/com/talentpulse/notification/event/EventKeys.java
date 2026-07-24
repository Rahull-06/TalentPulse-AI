package com.talentpulse.notification.event;

public final class EventKeys {

    public static final String EXCHANGE = "talentpulse.events";

    public static final String USER_REGISTERED = "user.registered";
    public static final String JOB_PUBLISHED = "job.published";
    public static final String APPLICATION_CREATED = "application.created";
    public static final String APPLICATION_STATUS_CHANGED = "application.status-changed";
    public static final String SCORE_COMPLETED = "score.completed";

    public static final String Q_USER_REGISTERED = "notification.user-registered";
    public static final String Q_JOB_PUBLISHED = "notification.job-published";
    public static final String Q_APPLICATION_CREATED = "notification.application-created";
    public static final String Q_STATUS_CHANGED = "notification.status-changed";
    public static final String Q_SCORE_COMPLETED = "notification.score-completed";

    private EventKeys() {
    }
}
