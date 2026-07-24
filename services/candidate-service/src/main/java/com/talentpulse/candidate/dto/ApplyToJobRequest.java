package com.talentpulse.candidate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplyToJobRequest {

    @NotNull
    private UUID jobId;

    /** Optional — uses primary resume if null */
    private UUID resumeId;

    /** Required for recruiter tenant filter until Job client exists — pass org from job in v1 */
    @NotNull
    private UUID organizationId;

    @Size(max = 5000)
    private String coverLetter;
}
