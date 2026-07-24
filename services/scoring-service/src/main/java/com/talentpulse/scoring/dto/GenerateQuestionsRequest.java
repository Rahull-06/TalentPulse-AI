package com.talentpulse.scoring.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateQuestionsRequest {

    /** Set from path variable in controller */
    private UUID applicationId;

    @NotNull
    private UUID jobId;

    private String jobTitle;

    private List<String> focusSkills;

    private List<String> missingSkills;
}
