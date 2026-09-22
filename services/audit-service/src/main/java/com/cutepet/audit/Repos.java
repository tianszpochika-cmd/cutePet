package com.cutepet.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/** audit Spring Data 仓库集合。 */
interface Repos {

    interface AuditLogRepo extends JpaRepository<AuditLogEntity, Long> {
        List<AuditLogEntity> findByActionOrderByCreatedAtDesc(String action);

        List<AuditLogEntity> findByOperatorOrderByCreatedAtDesc(String operator);

        List<AuditLogEntity> findAllByOrderByCreatedAtDesc();

        List<AuditLogEntity> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);
    }
}
