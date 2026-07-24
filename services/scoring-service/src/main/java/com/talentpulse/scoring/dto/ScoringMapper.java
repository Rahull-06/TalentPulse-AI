package com.talentpulse.scoring.dto;

import com.talentpulse.scoring.entity.InterviewQuestionSet;
import com.talentpulse.scoring.entity.ScoreResult;
import java.util.List;
import org.springframework.data.domain.Page;

public final class ScoringMapper {

    private ScoringMapper() {
    }

    public static ScoreResultResponse toScoreResponse(ScoreResult result) {
        return ScoreResultResponse.builder()
                .id(result.getId())
                .applicationId(result.getApplicationId())
                .jobId(result.getJobId())
                .organizationId(result.getOrganizationId())
                .fitScore(result.getFitScore())
                .scoringMode(result.getScoringMode())
                .matchedSkills(result.getMatchedSkills())
                .missingSkills(result.getMissingSkills())
                .resumeSummary(result.getResumeSummary())
                .explanation(result.getExplanation())
                .modelName(result.getModelName())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public static InterviewQuestionsResponse toQuestionsResponse(InterviewQuestionSet set) {
        return InterviewQuestionsResponse.builder()
                .id(set.getId())
                .applicationId(set.getApplicationId())
                .jobId(set.getJobId())
                .questions(set.getQuestions())
                .focusSkills(set.getFocusSkills())
                .generatedBy(set.getGeneratedBy())
                .createdAt(set.getCreatedAt())
                .build();
    }

    public static PageResponse<ScoreResultResponse> toScorePage(Page<ScoreResult> page) {
        List<ScoreResultResponse> content = page.getContent().stream()
                .map(ScoringMapper::toScoreResponse)
                .toList();
        return PageResponse.<ScoreResultResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
