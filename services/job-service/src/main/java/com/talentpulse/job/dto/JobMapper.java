package com.talentpulse.job.dto;

import com.talentpulse.job.entity.Job;
import com.talentpulse.job.entity.JobSkill;
import java.util.List;
import org.springframework.data.domain.Page;

public final class JobMapper {

    private JobMapper() {
    }

    public static JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .organizationId(job.getOrganizationId())
                .createdBy(job.getCreatedBy())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .status(job.getStatus())
                .openings(job.getOpenings())
                .maxApplicants(job.getMaxApplicants())
                .publishedAt(job.getPublishedAt())
                .closedAt(job.getClosedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .skills(job.getSkills().stream().map(JobMapper::toSkillResponse).toList())
                .build();
    }

    public static JobSkillResponse toSkillResponse(JobSkill skill) {
        return JobSkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .skillType(skill.getSkillType())
                .weight(skill.getWeight())
                .build();
    }

    public static <T> PageResponse<T> toPage(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    public static PageResponse<JobResponse> toJobPage(Page<Job> page) {
        List<JobResponse> content = page.getContent().stream().map(JobMapper::toResponse).toList();
        return PageResponse.<JobResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
