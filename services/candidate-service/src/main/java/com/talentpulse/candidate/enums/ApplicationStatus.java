package com.talentpulse.candidate.enums;

/**
 * Application pipeline status.
 * AI scoring / recruiter actions move the candidate through these steps.
 */
public enum ApplicationStatus {
    APPLIED,
    SCREENING,
    AI_SCORING,
    RECRUITER_REVIEW,
    SHORTLISTED,
    INTERVIEW,
    SELECTED,
    REJECTED
}
