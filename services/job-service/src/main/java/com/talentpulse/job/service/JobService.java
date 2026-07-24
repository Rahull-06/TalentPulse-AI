package com.talentpulse.job.service;

import com.talentpulse.job.dto.CreateJobRequest;
import com.talentpulse.job.dto.JobMapper;
import com.talentpulse.job.dto.JobResponse;
import com.talentpulse.job.dto.JobSkillRequest;
import com.talentpulse.job.dto.PageResponse;
import com.talentpulse.job.dto.UpdateJobRequest;
import com.talentpulse.job.entity.Job;
import com.talentpulse.job.entity.JobSkill;
import com.talentpulse.job.enums.JobStatus;
import com.talentpulse.job.enums.Role;
import com.talentpulse.job.event.DomainEventPublisher;
import com.talentpulse.job.event.EventKeys;
import com.talentpulse.job.event.JobPublishedEvent;
import com.talentpulse.job.exception.ForbiddenActionException;
import com.talentpulse.job.exception.InvalidJobStateException;
import com.talentpulse.job.exception.ResourceNotFoundException;
import com.talentpulse.job.repository.JobRepository;
import com.talentpulse.job.security.AuthPrincipal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public JobResponse createJob(CreateJobRequest request, AuthPrincipal principal) {
        requireRecruiterWithOrg(principal);

        Job job = Job.builder()
                .organizationId(principal.organizationId())
                .createdBy(principal.userId())
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .location(request.getLocation().trim())
                .employmentType(request.getEmploymentType())
                .experienceMin(request.getExperienceMin())
                .experienceMax(request.getExperienceMax())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .currency(request.getCurrency())
                .openings(request.getOpenings())
                .maxApplicants(request.getMaxApplicants())
                .status(JobStatus.DRAFT)
                .build();

        applySkills(job, request.getSkills());
        return JobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse updateJob(UUID jobId, UpdateJobRequest request, AuthPrincipal principal) {
        Job job = getOrgJob(jobId, principal);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new InvalidJobStateException("Closed jobs cannot be edited");
        }

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setLocation(request.getLocation().trim());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceMin(request.getExperienceMin());
        job.setExperienceMax(request.getExperienceMax());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setCurrency(request.getCurrency());
        job.setOpenings(request.getOpenings());
        job.setMaxApplicants(request.getMaxApplicants());

        job.clearSkills();
        applySkills(job, request.getSkills());

        return JobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse publishJob(UUID jobId, AuthPrincipal principal) {
        Job job = getOrgJob(jobId, principal);

        if (job.getStatus() != JobStatus.DRAFT && job.getStatus() != JobStatus.PUBLISHED) {
            throw new InvalidJobStateException("Only DRAFT jobs can be published");
        }
        if (job.getSkills().isEmpty()) {
            throw new InvalidJobStateException("Add at least one skill before publishing");
        }

        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(Instant.now());
        job.setClosedAt(null);
        Job saved = jobRepository.save(job);

        JobPublishedEvent event = JobPublishedEvent.builder()
                .jobId(saved.getId())
                .organizationId(saved.getOrganizationId())
                .createdBy(saved.getCreatedBy())
                .title(saved.getTitle())
                .build();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publish(EventKeys.JOB_PUBLISHED, event);
                }
            });
        } else {
            eventPublisher.publish(EventKeys.JOB_PUBLISHED, event);
        }

        return JobMapper.toResponse(saved);
    }

    @Transactional
    public JobResponse closeJob(UUID jobId, AuthPrincipal principal) {
        Job job = getOrgJob(jobId, principal);

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new InvalidJobStateException("Only PUBLISHED jobs can be closed");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(Instant.now());
        return JobMapper.toResponse(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(UUID jobId, AuthPrincipal principal) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() == JobStatus.PUBLISHED) {
            return JobMapper.toResponse(job);
        }

        // Draft/closed: only same-org recruiter/admin
        if (principal == null
                || principal.organizationId() == null
                || !principal.organizationId().equals(job.getOrganizationId())
                || (principal.role() != Role.RECRUITER && principal.role() != Role.ADMIN)) {
            throw new ResourceNotFoundException("Job not found");
        }

        return JobMapper.toResponse(job);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> searchPublished(String q, String location, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Job> jobs = jobRepository.searchPublished(
                JobStatus.PUBLISHED,
                blankToNull(q),
                blankToNull(location),
                pageable
        );
        return JobMapper.toJobPage(jobs);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobResponse> listMyOrganizationJobs(
            AuthPrincipal principal,
            JobStatus status,
            int page,
            int size
    ) {
        requireRecruiterWithOrg(principal);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Job> jobs = status == null
                ? jobRepository.findByOrganizationId(principal.organizationId(), pageable)
                : jobRepository.findByOrganizationIdAndStatus(principal.organizationId(), status, pageable);

        return JobMapper.toJobPage(jobs);
    }

    private Job getOrgJob(UUID jobId, AuthPrincipal principal) {
        requireRecruiterWithOrg(principal);
        return jobRepository.findByIdAndOrganizationId(jobId, principal.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private void requireRecruiterWithOrg(AuthPrincipal principal) {
        if (principal == null
                || (principal.role() != Role.RECRUITER && principal.role() != Role.ADMIN)) {
            throw new ForbiddenActionException("Only recruiters can manage jobs");
        }
        if (principal.organizationId() == null) {
            throw new ForbiddenActionException("Recruiter must belong to an organization");
        }
    }

    private void applySkills(Job job, java.util.List<JobSkillRequest> skills) {
        for (JobSkillRequest skillRequest : skills) {
            JobSkill skill = JobSkill.builder()
                    .skillName(skillRequest.getSkillName().trim())
                    .skillType(skillRequest.getSkillType())
                    .weight(skillRequest.getWeight() == null ? 1 : skillRequest.getWeight())
                    .build();
            job.addSkill(skill);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
