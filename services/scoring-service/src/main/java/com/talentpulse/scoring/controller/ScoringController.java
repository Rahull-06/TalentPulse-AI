package com.talentpulse.scoring.controller;

import com.talentpulse.scoring.dto.ComputeScoreRequest;
import com.talentpulse.scoring.dto.GenerateQuestionsRequest;
import com.talentpulse.scoring.dto.InterviewQuestionsResponse;
import com.talentpulse.scoring.dto.PageResponse;
import com.talentpulse.scoring.dto.ScoreResultResponse;
import com.talentpulse.scoring.security.AuthPrincipal;
import com.talentpulse.scoring.service.ScoringService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scoring")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;

    @PostMapping("/compute")
    public ResponseEntity<ScoreResultResponse> computeScore(
            @Valid @RequestBody ComputeScoreRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scoringService.computeScore(request, principal));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ScoreResultResponse> getScore(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(scoringService.getByApplication(applicationId, principal));
    }

    @GetMapping("/jobs/{jobId}/rankings")
    public ResponseEntity<PageResponse<ScoreResultResponse>> rankings(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(scoringService.rankings(jobId, principal, page, size));
    }

    @PostMapping("/applications/{applicationId}/interview-questions")
    public ResponseEntity<InterviewQuestionsResponse> generateQuestions(
            @PathVariable UUID applicationId,
            @Valid @RequestBody GenerateQuestionsRequest request,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        request.setApplicationId(applicationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scoringService.generateQuestions(request, principal));
    }

    @GetMapping("/applications/{applicationId}/interview-questions")
    public ResponseEntity<InterviewQuestionsResponse> latestQuestions(
            @PathVariable UUID applicationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(scoringService.latestQuestions(applicationId, principal));
    }
}
