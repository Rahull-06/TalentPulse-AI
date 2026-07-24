package com.talentpulse.auth.enums;

/**
 * Is this user account allowed to log in?
 * LOCKED = temporary block (e.g. too many failed logins) — we can use later.
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}
