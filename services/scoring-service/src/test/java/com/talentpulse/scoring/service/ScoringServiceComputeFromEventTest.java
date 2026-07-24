package com.talentpulse.scoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.talentpulse.scoring.dto.ScoreResultResponse;
import com.talentpulse.scoring.entity.ScoreResult;
import com.talentpulse.scoring.enums.ScoringMode;
import com.talentpulse.scoring.repository.InterviewQuestionSetRepository;
import com.talentpulse.scoring.repository.ScoreResultRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScoringServiceComputeFromEventTest {

    @Mock private ScoreResultRepository scoreResultRepository;
    @Mock private InterviewQuestionSetRepository interviewQuestionSetRepository;
    @Mock private RuleBasedScoringEngine ruleBasedScoringEngine;
    @Mock private AiScoringClient aiScoringClient;

    @InjectMocks
    private ScoringService scoringService;

    @Test
    void computeFromEvent_usesRuleEngineWhenAiEmpty() {
        UUID applicationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        when(aiScoringClient.tryScore(anyList(), anyList(), anyList(), nullable(String.class), nullable(String.class)))
                .thenReturn(Optional.empty());
        when(ruleBasedScoringEngine.score(anyList(), anyList(), anyList(), nullable(String.class), nullable(String.class)))
                .thenReturn(new RuleBasedScoringEngine.RuleScoreResult(
                        new BigDecimal("80.00"),
                        List.of("Java"),
                        List.of(),
                        "Strong match on required skills",
                        "Resume summary"
                ));
        when(scoreResultRepository.findByApplicationId(applicationId)).thenReturn(Optional.empty());
        when(scoreResultRepository.save(any(ScoreResult.class))).thenAnswer(invocation -> {
            ScoreResult result = invocation.getArgument(0);
            if (result.getId() == null) {
                result.setId(UUID.randomUUID());
            }
            return result;
        });

        ScoreResultResponse response = scoringService.computeFromEvent(
                applicationId,
                jobId,
                orgId,
                List.of("Java"),
                List.of(),
                List.of("Java"),
                "Java engineer",
                "Backend"
        );

        assertThat(response.getFitScore()).isEqualByComparingTo("80.00");
        assertThat(response.getScoringMode()).isEqualTo(ScoringMode.RULE_BASED);
        assertThat(response.getApplicationId()).isEqualTo(applicationId);
    }
}
