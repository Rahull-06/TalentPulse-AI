package com.talentpulse.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.talentpulse.candidate.client.JobClient;
import com.talentpulse.candidate.dto.ApplicationResponse;
import com.talentpulse.candidate.dto.ApplyToJobRequest;
import com.talentpulse.candidate.entity.Application;
import com.talentpulse.candidate.entity.CandidateProfile;
import com.talentpulse.candidate.entity.Resume;
import com.talentpulse.candidate.enums.ApplicationStatus;
import com.talentpulse.candidate.enums.ParseStatus;
import com.talentpulse.candidate.enums.Role;
import com.talentpulse.candidate.event.DomainEventPublisher;
import com.talentpulse.candidate.exception.ConflictException;
import com.talentpulse.candidate.exception.ForbiddenActionException;
import com.talentpulse.candidate.repository.ApplicationRepository;
import com.talentpulse.candidate.repository.CandidateSkillRepository;
import com.talentpulse.candidate.repository.ResumeRepository;
import com.talentpulse.candidate.security.AuthPrincipal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private CandidateSkillRepository candidateSkillRepository;
    @Mock private CandidateProfileService candidateProfileService;
    @Mock private JobClient jobClient;
    @Mock private DomainEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationService applicationService;

    private AuthPrincipal candidate;
    private CandidateProfile profile;
    private Resume resume;

    @BeforeEach
    void setUp() {
        candidate = new AuthPrincipal(UUID.randomUUID(), "c@test.com", Role.CANDIDATE, null);
        profile = CandidateProfile.builder().userId(candidate.userId()).build();
        profile.setId(UUID.randomUUID());

        resume = Resume.builder()
                .candidateProfile(profile)
                .fileName("cv.pdf")
                .fileUrl("/tmp/cv.pdf")
                .fileType("pdf")
                .parseStatus(ParseStatus.SUCCESS)
                .primaryResume(true)
                .uploadedAt(Instant.now())
                .build();
        resume.setId(UUID.randomUUID());
    }

    @Test
    void apply_success_movesToAiScoring() {
        ApplyToJobRequest request = new ApplyToJobRequest();
        request.setJobId(UUID.randomUUID());
        request.setOrganizationId(UUID.randomUUID());

        when(candidateProfileService.getProfileEntity(candidate.userId())).thenReturn(profile);
        when(applicationRepository.existsByJobIdAndCandidateProfileId(request.getJobId(), profile.getId()))
                .thenReturn(false);
        when(resumeRepository.findByCandidateProfileIdAndPrimaryResumeTrue(profile.getId()))
                .thenReturn(Optional.of(resume));
        when(jobClient.fetchJob(request.getJobId())).thenReturn(
                new JobClient.JobSnapshot(
                        "Java Dev",
                        UUID.randomUUID(),
                        1,
                        null,
                        java.util.List.of("Java"),
                        java.util.List.of()
                )
        );
        when(candidateSkillRepository.findByCandidateProfileId(profile.getId()))
                .thenReturn(java.util.List.of());
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            if (app.getId() == null) {
                app.setId(UUID.randomUUID());
            }
            return app;
        });

        ApplicationResponse response = applicationService.apply(request, candidate);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.AI_SCORING);
        assertThat(response.getJobId()).isEqualTo(request.getJobId());
        assertThat(response.getStatusHistory()).isNotEmpty();
    }

    @Test
    void apply_duplicate_throwsConflict() {
        ApplyToJobRequest request = new ApplyToJobRequest();
        request.setJobId(UUID.randomUUID());
        request.setOrganizationId(UUID.randomUUID());

        when(candidateProfileService.getProfileEntity(candidate.userId())).thenReturn(profile);
        when(applicationRepository.existsByJobIdAndCandidateProfileId(request.getJobId(), profile.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(request, candidate))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void apply_asRecruiter_forbidden() {
        AuthPrincipal recruiter = new AuthPrincipal(
                UUID.randomUUID(), "r@test.com", Role.RECRUITER, UUID.randomUUID()
        );
        ApplyToJobRequest request = new ApplyToJobRequest();
        request.setJobId(UUID.randomUUID());
        request.setOrganizationId(recruiter.organizationId());

        assertThatThrownBy(() -> applicationService.apply(request, recruiter))
                .isInstanceOf(ForbiddenActionException.class);
    }
}
