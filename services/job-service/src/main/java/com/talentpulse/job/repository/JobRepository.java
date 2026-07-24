package com.talentpulse.job.repository;

import com.talentpulse.job.entity.Job;
import com.talentpulse.job.enums.JobStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Page<Job> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Job> findByOrganizationIdAndStatus(UUID organizationId, JobStatus status, Pageable pageable);

    /** Candidate search: only PUBLISHED jobs, optional text/location filter */
    @Query("""
            SELECT j FROM Job j
            WHERE j.status = :published
              AND (:q IS NULL OR :q = ''
                   OR LOWER(j.title) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(j.description) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:location IS NULL OR :location = ''
                   OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
            """)
    Page<Job> searchPublished(
            @Param("published") JobStatus published,
            @Param("q") String q,
            @Param("location") String location,
            Pageable pageable
    );
}
