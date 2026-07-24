package com.talentpulse.job.enums;

/**
 * Job lifecycle.
 * DRAFT → only recruiter sees it
 * PUBLISHED → candidates can view/apply
 * CLOSED → no new applications
 */
public enum JobStatus {
    DRAFT,
    PUBLISHED,
    CLOSED
}
