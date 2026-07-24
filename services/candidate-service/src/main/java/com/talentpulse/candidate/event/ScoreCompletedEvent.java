package com.talentpulse.candidate.event;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreCompletedEvent {

    private UUID applicationId;
    private UUID jobId;
    private UUID organizationId;
    private UUID candidateUserId;
    private BigDecimal fitScore;
    private String scoringMode;
}
