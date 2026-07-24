package com.talentpulse.auth.enums;

/**
 * Who is using the system?
 * Stored in DB as text (CANDIDATE / RECRUITER / ADMIN).
 * Used by Spring Security for authorization (who can call which API).
 */
public enum Role {
    CANDIDATE,
    RECRUITER,
    ADMIN
}
