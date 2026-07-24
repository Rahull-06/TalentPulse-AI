package com.talentpulse.candidate.dto;

import com.talentpulse.candidate.entity.Application;
import com.talentpulse.candidate.entity.ApplicationStatusHistory;
import com.talentpulse.candidate.entity.CandidateProfile;
import com.talentpulse.candidate.entity.CandidateSkill;
import com.talentpulse.candidate.entity.Resume;
import java.util.List;
import org.springframework.data.domain.Page;

public final class CandidateMapper {

    private CandidateMapper() {
    }

    public static CandidateProfileResponse toProfileResponse(CandidateProfile profile) {
        return CandidateProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .headline(profile.getHeadline())
                .summary(profile.getSummary())
                .experienceYears(profile.getExperienceYears())
                .location(profile.getLocation())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .phone(profile.getPhone())
                .skills(profile.getSkills().stream().map(CandidateMapper::toSkillResponse).toList())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public static CandidateSkillResponse toSkillResponse(CandidateSkill skill) {
        return CandidateSkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .proficiency(skill.getProficiency())
                .source(skill.getSource())
                .build();
    }

    public static ResumeResponse toResumeResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .fileName(resume.getFileName())
                .fileUrl(resume.getFileUrl())
                .fileType(resume.getFileType())
                .parseStatus(resume.getParseStatus())
                .primaryResume(resume.isPrimaryResume())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }

    public static ApplicationResponse toApplicationResponse(Application application, boolean includeHistory) {
        List<ApplicationStatusHistoryResponse> history = includeHistory
                ? application.getStatusHistory().stream()
                .map(CandidateMapper::toHistoryResponse)
                .toList()
                : List.of();

        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .organizationId(application.getOrganizationId())
                .candidateProfileId(application.getCandidateProfile().getId())
                .resumeId(application.getResume().getId())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .statusHistory(history)
                .build();
    }

    public static ApplicationStatusHistoryResponse toHistoryResponse(ApplicationStatusHistory history) {
        return ApplicationStatusHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .changedBy(history.getChangedBy())
                .note(history.getNote())
                .changedAt(history.getChangedAt())
                .build();
    }

    public static PageResponse<ApplicationResponse> toApplicationPage(Page<Application> page, boolean includeHistory) {
        List<ApplicationResponse> content = page.getContent().stream()
                .map(app -> toApplicationResponse(app, includeHistory))
                .toList();
        return PageResponse.<ApplicationResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
