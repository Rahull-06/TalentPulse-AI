package com.talentpulse.candidate.service;

import com.talentpulse.candidate.client.JobClient;
import com.talentpulse.candidate.entity.Application;
import com.talentpulse.candidate.entity.CandidateProfile;
import com.talentpulse.candidate.entity.CandidateSkill;
import com.talentpulse.candidate.entity.Resume;
import com.talentpulse.candidate.enums.ApplicationStatus;
import com.talentpulse.candidate.event.ApplicationCreatedEvent;
import com.talentpulse.candidate.event.DomainEventPublisher;
import com.talentpulse.candidate.event.EventKeys;
import com.talentpulse.candidate.repository.ApplicationRepository;
import com.talentpulse.candidate.repository.CandidateSkillRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Re-publishes APPLICATION_CREATED so scoring re-runs after a resume is re-parsed.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationRescoringService {

    private static final Set<ApplicationStatus> TERMINAL = EnumSet.of(
            ApplicationStatus.SELECTED,
            ApplicationStatus.REJECTED
    );

    private final ApplicationRepository applicationRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final JobClient jobClient;
    private final DomainEventPublisher eventPublisher;

    public void rescoreOpenApplications(CandidateProfile profile, Resume resume) {
        List<Application> apps = applicationRepository.findByCandidateProfileId(profile.getId());
        List<String> candidateSkills = candidateSkillRepository.findByCandidateProfileId(profile.getId())
                .stream()
                .map(CandidateSkill::getSkillName)
                .toList();

        int published = 0;
        for (Application application : apps) {
            if (TERMINAL.contains(application.getStatus())) {
                continue;
            }
            try {
                JobClient.JobSnapshot job = jobClient.fetchJob(application.getJobId());
                ApplicationCreatedEvent event = ApplicationCreatedEvent.builder()
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
                publishAfterCommit(EventKeys.APPLICATION_CREATED, event);
                published++;
            } catch (Exception ex) {
                log.warn("Could not queue rescore for application {}: {}", application.getId(), ex.getMessage());
            }
        }
        if (published > 0) {
            log.info("Queued rescoring for {} open application(s) after resume update", published);
        }
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
}
