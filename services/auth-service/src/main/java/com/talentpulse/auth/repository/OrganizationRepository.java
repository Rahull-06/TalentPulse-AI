package com.talentpulse.auth.repository;

import com.talentpulse.auth.entity.Organization;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for organizations.
 * JpaRepository gives save, findById, delete, etc. for free.
 */
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
