package com.talentpulse.scoring.enums;

/**
 * How the fit score was produced.
 * RULE_BASED keeps the product working when Gemini is down.
 */
public enum ScoringMode {
    AI,
    RULE_BASED
}
