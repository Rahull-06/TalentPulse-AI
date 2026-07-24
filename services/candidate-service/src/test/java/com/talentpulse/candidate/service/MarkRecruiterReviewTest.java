package com.talentpulse.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.talentpulse.candidate.client.JobClient;
import com.talentpulse.candidate.dto.ApplicationResponse;
import com.talentpulse.candidate.entity.Application;
import com.talentpulse.candidate.entity.CandidateProfile;
import com.talentpulse.candidate.entity.Resume;
import com.talentpulse.candidate.enums.ApplicationStatus;
import com.talentpulse.candidate.enums.ParseStatus;
import com.talentpulse.candidate.event.DomainEventPublisher;
import com.talentpulse.candidate.repository.ApplicationRepository;
import com.talentpulse.candidate.repository.CandidateSkillRepository;
import com.talentpulse.candidate.repository.ResumeRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkRecruiterReviewTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private CandidateSkillRepository candidateSkillRepository;
    @Mock private CandidateProfileService candidateProfileService;
    @Mock private JobClient jobClient;
    @Mock private DomainEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void markRecruiterReview_movesFromAiScoring() {
        Application application = sampleApplication(ApplicationStatus.AI_SCORING);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse response = applicationService.markRecruiterReview(application.getId());

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.RECRUITER_REVIEW);
    }

    @Test
    void markRecruiterReview_idempotentWhenAlreadyReviewed() {
        Application application = sampleApplication(ApplicationStatus.RECRUITER_REVIEW);

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        ApplicationResponse response = applicationService.markRecruiterReview(application.getId());

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.RECRUITER_REVIEW);
    }

    private static Application sampleApplication(ApplicationStatus status) {
        CandidateProfile profile = CandidateProfile.builder().userId(UUID.randomUUID()).build();
        profile.setId(UUID.randomUUID());

        Resume resume = Resume.builder()
                .candidateProfile(profile)
                .fileName("cv.pdf")
                .fileUrl("/tmp/cv.pdf")
                .fileType("pdf")
                .parseStatus(ParseStatus.SUCCESS)
                .primaryResume(true)
                .uploadedAt(Instant.now())
                .build();
        resume.setId(UUID.randomUUID());

        Application application = Application.builder()
                .jobId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .candidateProfile(profile)
                .resume(resume)
                .status(status)
                .appliedAt(Instant.now())
                .build();
        application.setId(UUID.randomUUID());
        return application;
    }
}
