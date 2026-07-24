package com.talentpulse.scoring.dto;

import com.talentpulse.scoring.enums.ScoringMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScoreResultResponse {

    private UUID id;
    private UUID applicationId;
    private UUID jobId;
    private UUID organizationId;
    private BigDecimal fitScore;
    private ScoringMode scoringMode;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String resumeSummary;
    private String explanation;
    private String modelName;
    private Instant createdAt;
}
