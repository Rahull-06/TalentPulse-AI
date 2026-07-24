package com.talentpulse.scoring.dto;

import com.talentpulse.scoring.enums.QuestionSource;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewQuestionsResponse {

    private UUID id;
    private UUID applicationId;
    private UUID jobId;
    private List<String> questions;
    private List<String> focusSkills;
    private QuestionSource generatedBy;
    private Instant createdAt;
}
