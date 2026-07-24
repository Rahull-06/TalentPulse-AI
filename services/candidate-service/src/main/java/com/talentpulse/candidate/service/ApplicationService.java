package com.talentpulse.candidate.service;

import com.talentpulse.candidate.client.JobClient;
import com.talentpulse.candidate.dto.ApplicationResponse;
import com.talentpulse.candidate.dto.ApplyToJobRequest;
import com.talentpulse.candidate.dto.CandidateMapper;
import com.talentpulse.candidate.dto.PageResponse;
import com.talentpulse.candidate.dto.RejectApplicationRequest;
import com.talentpulse.candidate.dto.StatusChangeRequest;
import com.talentpulse.candidate.entity.Application;
import com.talentpulse.candidate.entity.ApplicationStatusHistory;
import com.talentpulse.candidate.entity.CandidateProfile;
import com.talentpulse.candidate.entity.CandidateSkill;
import com.talentpulse.candidate.entity.Resume;
import com.talentpulse.candidate.enums.ApplicationStatus;
import com.talentpulse.candidate.enums.Role;
import com.talentpulse.candidate.event.ApplicationCreatedEvent;
import com.talentpulse.candidate.event.ApplicationStatusChangedEvent;
import com.talentpulse.candidate.event.DomainEventPublisher;
import com.talentpulse.candidate.event.EventKeys;
import com.talentpulse.candidate.exception.ConflictException;
import com.talentpulse.candidate.exception.ForbiddenActionException;
import com.talentpulse.candidate.exception.InvalidApplicationStateException;
import com.talentpulse.candidate.exception.ResourceNotFoundException;
import com.talentpulse.candidate.repository.ApplicationRepository;
import com.talentpulse.candidate.repository.CandidateSkillRepository;
import com.talentpulse.candidate.repository.ResumeRepository;
import com.talentpulse.candidate.security.AuthPrincipal;
import java.time.Instant;
import java.util.List;
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
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final CandidateProfileService candidateProfileService;
    private final JobClient jobClient;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public ApplicationResponse apply(ApplyToJobRequest request, AuthPrincipal principal) {
        requireCandidate(principal);
        CandidateProfile profile = candidateProfileService.getProfileEntity(principal.userId());

        if (applicationRepository.existsByJobIdAndCandidateProfileId(request.getJobId(), profile.getId())) {
            throw new ConflictException("You already applied to this job");
        }

        JobClient.JobSnapshot jobSnapshot = jobClient.fetchJob(request.getJobId());
        if (jobSnapshot.maxApplicants() != null) {
            long applied = applicationRepository.countByJobId(request.getJobId());
            if (applied >= jobSnapshot.maxApplicants()) {
                throw new ConflictException(
                        "This role has reached its applicant limit (" + jobSnapshot.maxApplicants() + ")"
                );
            }
        }

        Resume resume = resolveResume(profile.getId(), request.getResumeId());

        Application application = Application.builder()
                .jobId(request.getJobId())
                .organizationId(request.getOrganizationId())
                .candidateProfile(profile)
                .resume(resume)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(request.getCoverLetter())
                .appliedAt(Instant.now())
                .build();

        addHistory(application, null, ApplicationStatus.APPLIED, principal.userId(), "Application submitted");
        Application saved = applicationRepository.save(application);

        moveStatus(saved, ApplicationStatus.SCREENING, null, "Moved to screening");
        moveStatus(saved, ApplicationStatus.AI_SCORING, null, "Queued for AI/rule scoring");
        Application persisted = applicationRepository.save(saved);

        ApplicationCreatedEvent event = buildCreatedEvent(persisted, profile, resume);
        publishAfterCommit(EventKeys.APPLICATION_CREATED, event);

        return CandidateMapper.toApplicationResponse(persisted, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> myApplications(AuthPrincipal principal, int page, int size) {
        requireCandidate(principal);
        CandidateProfile profile = candidateProfileService.getProfileEntity(principal.userId());
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> apps = applicationRepository.findByCandidateProfileId(profile.getId(), pageable);
        return CandidateMapper.toApplicationPage(apps, true);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getMyApplication(UUID applicationId, AuthPrincipal principal) {
        requireCandidate(principal);
        CandidateProfile profile = candidateProfileService.getProfileEntity(principal.userId());
        Application application = applicationRepository
                .findByIdAndCandidateProfileId(applicationId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return CandidateMapper.toApplicationResponse(application, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> listForJob(
            UUID jobId,
            ApplicationStatus status,
            AuthPrincipal principal,
            int page,
            int size
    ) {
        requireRecruiter(principal);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> apps = status == null
                ? applicationRepository.findByJobIdAndOrganizationId(jobId, principal.organizationId(), pageable)
                : applicationRepository.findByJobIdAndOrganizationIdAndStatus(
                        jobId, principal.organizationId(), status, pageable
                );
        return CandidateMapper.toApplicationPage(apps, false);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getForRecruiter(UUID applicationId, AuthPrincipal principal) {
        requireRecruiter(principal);
        Application application = applicationRepository
                .findByIdAndOrganizationId(applicationId, principal.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return CandidateMapper.toApplicationResponse(application, true);
    }

    @Transactional
    public ApplicationResponse shortlist(UUID applicationId, StatusChangeRequest request, AuthPrincipal principal) {
        return changeStatus(applicationId, ApplicationStatus.SHORTLISTED, principal, request.getNote());
    }

    @Transactional
    public ApplicationResponse moveToInterview(UUID applicationId, StatusChangeRequest request, AuthPrincipal principal) {
        return changeStatus(applicationId, ApplicationStatus.INTERVIEW, principal, request.getNote());
    }

    @Transactional
    public ApplicationResponse select(UUID applicationId, StatusChangeRequest request, AuthPrincipal principal) {
        return changeStatus(applicationId, ApplicationStatus.SELECTED, principal, request.getNote());
    }

    @Transactional
    public ApplicationResponse reject(UUID applicationId, RejectApplicationRequest request, AuthPrincipal principal) {
        return changeStatus(applicationId, ApplicationStatus.REJECTED, principal, request.getReason());
    }

    /** Called by SCORE_COMPLETED consumer after scoring finishes. */
    @Transactional
    public ApplicationResponse markRecruiterReview(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getStatus() == ApplicationStatus.RECRUITER_REVIEW
                || application.getStatus() == ApplicationStatus.SHORTLISTED
                || application.getStatus() == ApplicationStatus.INTERVIEW
                || application.getStatus() == ApplicationStatus.SELECTED
                || application.getStatus() == ApplicationStatus.REJECTED) {
            return CandidateMapper.toApplicationResponse(application, true);
        }
        moveStatus(application, ApplicationStatus.RECRUITER_REVIEW, null, "Scoring completed");
        return CandidateMapper.toApplicationResponse(applicationRepository.save(application), true);
    }

    private ApplicationResponse changeStatus(
            UUID applicationId,
            ApplicationStatus toStatus,
            AuthPrincipal principal,
            String note
    ) {
        requireRecruiter(principal);
        Application application = applicationRepository
                .findByIdAndOrganizationId(applicationId, principal.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        ApplicationStatus from = application.getStatus();
        validateTransition(from, toStatus);
        moveStatus(application, toStatus, principal.userId(), note);
        Application saved = applicationRepository.save(application);

        ApplicationStatusChangedEvent event = ApplicationStatusChangedEvent.builder()
                .applicationId(saved.getId())
                .jobId(saved.getJobId())
                .organizationId(saved.getOrganizationId())
                .candidateUserId(saved.getCandidateProfile().getUserId())
                .fromStatus(from.name())
                .toStatus(toStatus.name())
                .note(note)
                .build();
        publishAfterCommit(EventKeys.APPLICATION_STATUS_CHANGED, event);

        return CandidateMapper.toApplicationResponse(saved, true);
    }

    private ApplicationCreatedEvent buildCreatedEvent(
            Application application,
            CandidateProfile profile,
            Resume resume
    ) {
        JobClient.JobSnapshot job = jobClient.fetchJob(application.getJobId());
        List<String> candidateSkills = candidateSkillRepository.findByCandidateProfileId(profile.getId())
                .stream()
                .map(CandidateSkill::getSkillName)
                .toList();

        return ApplicationCreatedEvent.builder()
                .applicationId(application.getId())
                .jobId(application.getJobId())
                .organizationId(application.getOrganizationId())
                .candidateUserId(profile.getUserId())
                .candidateProfileId(profile.getId())
                .recruiterUserId(job.createdBy())
                .jobTitle(job.title())
                .requiredSkills(job.requiredSkills())
                .preferredSkills(job.preferredSkills())
                .candidateSkills(candidateSkills)
                .resumeText(resume.getParsedText())
                .build();
    }

    private void publishAfterCommit(String routingKey, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    eventPublisher.publish(routingKey, payload);
                }
            });
        } else {
            eventPublisher.publish(routingKey, payload);
        }
    }

    private void validateTransition(ApplicationStatus from, ApplicationStatus to) {
        boolean allowed = switch (to) {
            case SHORTLISTED -> from == ApplicationStatus.RECRUITER_REVIEW
                    || from == ApplicationStatus.SCREENING
                    || from == ApplicationStatus.AI_SCORING
                    || from == ApplicationStatus.APPLIED;
            case INTERVIEW -> from == ApplicationStatus.SHORTLISTED;
            case SELECTED -> from == ApplicationStatus.INTERVIEW || from == ApplicationStatus.SHORTLISTED;
            case REJECTED -> from != ApplicationStatus.SELECTED && from != ApplicationStatus.REJECTED;
            default -> false;
        };
        if (!allowed) {
            throw new InvalidApplicationStateException(
                    "Cannot move from " + from + " to " + to
            );
        }
    }

    private void moveStatus(Application application, ApplicationStatus toStatus, UUID changedBy, String note) {
        ApplicationStatus from = application.getStatus();
        application.setStatus(toStatus);
        addHistory(application, from, toStatus, changedBy, note);
    }

    private void addHistory(
            Application application,
            ApplicationStatus from,
            ApplicationStatus to,
            UUID changedBy,
            String note
    ) {
        application.addStatusHistory(ApplicationStatusHistory.builder()
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .note(note)
                .changedAt(Instant.now())
                .build());
    }

    private Resume resolveResume(UUID profileId, UUID resumeId) {
        if (resumeId != null) {
            return resumeRepository.findByIdAndCandidateProfileId(resumeId, profileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
        }
        return resumeRepository.findByCandidateProfileIdAndPrimaryResumeTrue(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload a resume before applying"));
    }

    private void requireCandidate(AuthPrincipal principal) {
        if (principal == null || principal.role() != Role.CANDIDATE) {
            throw new ForbiddenActionException("Only candidates can apply");
        }
    }

    private void requireRecruiter(AuthPrincipal principal) {
        if (principal == null
                || (principal.role() != Role.RECRUITER && principal.role() != Role.ADMIN)) {
            throw new ForbiddenActionException("Only recruiters can manage applications");
        }
        if (principal.organizationId() == null) {
            throw new ForbiddenActionException("Recruiter must belong to an organization");
        }
    }
}
