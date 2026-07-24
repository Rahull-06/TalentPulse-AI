package com.talentpulse.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.talentpulse.job.dto.CreateJobRequest;
import com.talentpulse.job.dto.JobResponse;
import com.talentpulse.job.dto.JobSkillRequest;
import com.talentpulse.job.entity.Job;
import com.talentpulse.job.enums.EmploymentType;
import com.talentpulse.job.enums.JobStatus;
import com.talentpulse.job.enums.Role;
import com.talentpulse.job.enums.SkillType;
import com.talentpulse.job.event.DomainEventPublisher;
import com.talentpulse.job.exception.ForbiddenActionException;
import com.talentpulse.job.exception.InvalidJobStateException;
import com.talentpulse.job.repository.JobRepository;
import com.talentpulse.job.security.AuthPrincipal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private JobService jobService;

    private AuthPrincipal recruiter;
    private CreateJobRequest request;

    @BeforeEach
    void setUp() {
        recruiter = new AuthPrincipal(
                UUID.randomUUID(),
                "recruiter@test.com",
                Role.RECRUITER,
                UUID.randomUUID()
        );

        JobSkillRequest skill = new JobSkillRequest();
        skill.setSkillName("Java");
        skill.setSkillType(SkillType.REQUIRED);
        skill.setWeight(5);

        request = new CreateJobRequest();
        request.setTitle("Java Developer");
        request.setDescription("Build Spring Boot APIs");
        request.setLocation("Bangalore");
        request.setEmploymentType(EmploymentType.FULL_TIME);
        request.setExperienceMin(2);
        request.setExperienceMax(5);
        request.setSkills(List.of(skill));
    }

    @Test
    void createJob_asRecruiter_savesDraft() {
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(UUID.randomUUID());
            return job;
        });

        JobResponse response = jobService.createJob(request, recruiter);

        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(response.getTitle()).isEqualTo("Java Developer");
        assertThat(response.getOrganizationId()).isEqualTo(recruiter.organizationId());

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertThat(captor.getValue().getSkills()).hasSize(1);
    }

    @Test
    void createJob_asCandidate_forbidden() {
        AuthPrincipal candidate = new AuthPrincipal(
                UUID.randomUUID(), "c@test.com", Role.CANDIDATE, null
        );

        assertThatThrownBy(() -> jobService.createJob(request, candidate))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void publishJob_fromDraft_succeeds() {
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder()
                .organizationId(recruiter.organizationId())
                .createdBy(recruiter.userId())
                .title("Java Developer")
                .description("desc")
                .location("Pune")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceMin(1)
                .experienceMax(3)
                .status(JobStatus.DRAFT)
                .build();
        job.setId(jobId);
        job.addSkill(com.talentpulse.job.entity.JobSkill.builder()
                .skillName("Java")
                .skillType(SkillType.REQUIRED)
                .weight(5)
                .build());

        when(jobRepository.findByIdAndOrganizationId(jobId, recruiter.organizationId()))
                .thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobResponse response = jobService.publishJob(jobId, recruiter);

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        assertThat(response.getPublishedAt()).isNotNull();
    }

    @Test
    void publishJob_whenClosed_throwsConflict() {
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder()
                .organizationId(recruiter.organizationId())
                .createdBy(recruiter.userId())
                .title("Java Developer")
                .description("desc")
                .location("Pune")
                .employmentType(EmploymentType.FULL_TIME)
                .experienceMin(1)
                .experienceMax(3)
                .status(JobStatus.CLOSED)
                .build();
        job.setId(jobId);

        when(jobRepository.findByIdAndOrganizationId(jobId, recruiter.organizationId()))
                .thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.publishJob(jobId, recruiter))
                .isInstanceOf(InvalidJobStateException.class);
    }
}
