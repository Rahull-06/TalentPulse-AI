package com.talentpulse.notification.repository;

import com.talentpulse.notification.entity.EmailLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
}
