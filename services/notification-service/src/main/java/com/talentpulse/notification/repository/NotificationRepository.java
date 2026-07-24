package com.talentpulse.notification.repository;

import com.talentpulse.notification.entity.Notification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndReadFlagFalse(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.readFlag = true
            WHERE n.userId = :userId AND n.readFlag = false
            """)
    int markAllRead(@Param("userId") UUID userId);
}
