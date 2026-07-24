package com.talentpulse.scoring.service;

import com.talentpulse.scoring.dto.ComputeScoreRequest;
import com.talentpulse.scoring.dto.GenerateQuestionsRequest;
import com.talentpulse.scoring.dto.InterviewQuestionsResponse;
import com.talentpulse.scoring.dto.PageResponse;
import com.talentpulse.scoring.dto.ScoreResultResponse;
import com.talentpulse.scoring.dto.ScoringMapper;
import com.talentpulse.scoring.entity.InterviewQuestionSet;
import com.talentpulse.scoring.entity.ScoreResult;
import com.talentpulse.scoring.enums.QuestionSource;
import com.talentpulse.scoring.enums.Role;
import com.talentpulse.scoring.enums.ScoringMode;
import com.talentpulse.scoring.exception.ForbiddenActionException;
import com.talentpulse.scoring.exception.ResourceNotFoundException;
import com.talentpulse.scoring.repository.InterviewQuestionSetRepository;
import com.talentpulse.scoring.repository.ScoreResultRepository;
import com.talentpulse.scoring.security.AuthPrincipal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final ScoreResultRepository scoreResultRepository;
    private final InterviewQuestionSetRepository interviewQuestionSetRepository;
    private final RuleBasedScoringEngine ruleBasedScoringEngine;
    private final AiScoringClient aiScoringClient;

    @Transactional
    public ScoreResultResponse computeScore(ComputeScoreRequest request, AuthPrincipal principal) {
        requireAuthenticated(principal);
        return computeInternal(
                request.getApplicationId(),
                request.getJobId(),
                request.getOrganizationId(),
                request.getRequiredSkills(),
                request.getPreferredSkills(),
                request.getCandidateSkills(),
                request.getResumeText(),
                request.getJobTitle()
        );
    }

    /** Used by APPLICATION_CREATED Rabbit listener (no JWT). */
    @Transactional
    public ScoreResultResponse computeFromEvent(
            UUID applicationId,
            UUID jobId,
            UUID organizationId,
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> candidateSkills,
            String resumeText,
            String jobTitle
    ) {
        return computeInternal(
                applicationId,
                jobId,
                organizationId,
                requiredSkills,
                preferredSkills,
                candidateSkills,
                resumeText,
                jobTitle
        );
    }

    private ScoreResultResponse computeInternal(
            UUID applicationId,
            UUID jobId,
            UUID organizationId,
            List<String> requiredSkills,
            List<String> preferredSkills,
            List<String> candidateSkills,
            String resumeText,
            String jobTitle
    ) {
        var ai = aiScoringClient.tryScore(
                requiredSkills,
                preferredSkills,
                candidateSkills,
                resumeText,
                jobTitle
        );

        ScoreResult result = scoreResultRepository.findByApplicationId(applicationId)
                .orElseGet(ScoreResult::new);

        result.setApplicationId(applicationId);
        result.setJobId(jobId);
        result.setOrganizationId(organizationId);

        if (ai.isPresent()) {
            var aiResult = ai.get();
            result.setFitScore(aiResult.fitScore());
            result.setMatchedSkills(aiResult.matchedSkills());
            result.setMissingSkills(aiResult.missingSkills());
            result.setExplanation(aiResult.explanation());
            result.setResumeSummary(aiResult.resumeSummary());
            result.setModelName(aiResult.modelName());
            result.setScoringMode(ScoringMode.AI);
        } else {
            var rule = ruleBasedScoringEngine.score(
                    requiredSkills,
                    preferredSkills,
                    candidateSkills,
                    resumeText,
                    jobTitle
            );
            result.setFitScore(rule.fitScore());
            result.setMatchedSkills(rule.matchedSkills());
            result.setMissingSkills(rule.missingSkills());
            result.setExplanation(rule.explanation());
            result.setResumeSummary(rule.resumeSummary());
            result.setModelName(null);
            result.setScoringMode(ScoringMode.RULE_BASED);
        }

        return ScoringMapper.toScoreResponse(scoreResultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public ScoreResultResponse getByApplication(UUID applicationId, AuthPrincipal principal) {
        requireAuthenticated(principal);
        ScoreResult result = scoreResultRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Score not found for application"));

        if (principal.role() == Role.RECRUITER || principal.role() == Role.ADMIN) {
            if (principal.organizationId() == null
                    || !principal.organizationId().equals(result.getOrganizationId())) {
                throw new ForbiddenActionException("Not allowed to view this score");
            }
        }
        // Candidates: in a fuller design we'd verify application ownership via Candidate Service.
        return ScoringMapper.toScoreResponse(result);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScoreResultResponse> rankings(
            UUID jobId,
            AuthPrincipal principal,
            int page,
            int size
    ) {
        requireRecruiter(principal);
        Page<ScoreResult> results = scoreResultRepository
                .findByJobIdAndOrganizationIdOrderByFitScoreDesc(
                        jobId,
                        principal.organizationId(),
                        PageRequest.of(page, size)
                );
        return ScoringMapper.toScorePage(results);
    }

    @Transactional
    public InterviewQuestionsResponse generateQuestionsForApplication(
            UUID applicationId,
            UUID jobId,
            String jobTitle,
            List<String> matchedSkills,
            List<String> missingSkills
    ) {
        List<String> focus = matchedSkills == null ? List.of() : matchedSkills;
        List<String> missing = missingSkills == null ? List.of() : missingSkills;

        var aiQuestions = aiScoringClient.tryGenerateQuestions(jobTitle, focus, missing);

        List<String> questions;
        QuestionSource source;
        if (aiQuestions.isPresent()) {
            questions = aiQuestions.get();
            source = QuestionSource.AI;
        } else {
            questions = ruleBasedScoringEngine.generateTemplateQuestions(jobTitle, focus, missing);
            source = QuestionSource.TEMPLATE;
        }

        InterviewQuestionSet set = InterviewQuestionSet.builder()
                .applicationId(applicationId)
                .jobId(jobId)
                .questions(questions)
                .focusSkills(missing.isEmpty() ? focus : missing)
                .generatedBy(source)
                .build();

        return ScoringMapper.toQuestionsResponse(interviewQuestionSetRepository.save(set));
    }

    @Transactional
    public InterviewQuestionsResponse generateQuestions(
            GenerateQuestionsRequest request,
            AuthPrincipal principal
    ) {
        requireRecruiter(principal);
        return generateQuestionsForApplication(
                request.getApplicationId(),
                request.getJobId(),
                request.getJobTitle(),
                request.getFocusSkills(),
                request.getMissingSkills()
        );
    }

    @Transactional(readOnly = true)
    public InterviewQuestionsResponse latestQuestions(UUID applicationId, AuthPrincipal principal) {
        requireRecruiter(principal);
        InterviewQuestionSet set = interviewQuestionSetRepository
                .findFirstByApplicationIdOrderByCreatedAtDesc(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("No interview questions found"));
        return ScoringMapper.toQuestionsResponse(set);
    }

    private void requireAuthenticated(AuthPrincipal principal) {
        if (principal == null) {
            throw new ForbiddenActionException("Authentication required");
        }
    }

    private void requireRecruiter(AuthPrincipal principal) {
        requireAuthenticated(principal);
        if (principal.role() != Role.RECRUITER && principal.role() != Role.ADMIN) {
            throw new ForbiddenActionException("Only recruiters can access this operation");
        }
        if (principal.organizationId() == null) {
            throw new ForbiddenActionException("Recruiter must belong to an organization");
        }
    }
}
