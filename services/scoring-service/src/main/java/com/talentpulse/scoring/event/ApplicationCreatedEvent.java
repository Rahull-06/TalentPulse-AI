package com.talentpulse.scoring.event;

import java.util.List;
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
public class ApplicationCreatedEvent {

    private UUID applicationId;
    private UUID jobId;
    private UUID organizationId;
    private UUID candidateUserId;
    private UUID candidateProfileId;
    private String jobTitle;
    private List<String> requiredSkills;
    private List<String> preferredSkills;
    private List<String> candidateSkills;
    private String resumeText;
}
