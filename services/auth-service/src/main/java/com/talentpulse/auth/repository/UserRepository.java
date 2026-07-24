package com.talentpulse.auth.repository;

import com.talentpulse.auth.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Database access for users (login accounts).
 * Method names like findByEmail are turned into SQL by Spring Data.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
