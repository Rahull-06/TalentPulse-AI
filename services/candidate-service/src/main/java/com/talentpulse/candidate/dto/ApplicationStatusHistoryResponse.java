package com.talentpulse.candidate.dto;

import com.talentpulse.candidate.enums.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationStatusHistoryResponse {

    private UUID id;
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private UUID changedBy;
    private String note;
    private Instant changedAt;
}
