package com.talentpulse.candidate.dto;

import com.talentpulse.candidate.enums.ApplicationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationResponse {

    private UUID id;
    private UUID jobId;
    private UUID organizationId;
    private UUID candidateProfileId;
    private UUID resumeId;
    private ApplicationStatus status;
    private String coverLetter;
    private Instant appliedAt;
    private Instant updatedAt;
    private List<ApplicationStatusHistoryResponse> statusHistory;
}
