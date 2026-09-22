package com.cutepet.adminops;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** admin-ops Spring Data 仓库集合。 */
interface Repos {

    interface AdminUserRepo extends JpaRepository<AdminUserEntity, Long> {
        Optional<AdminUserEntity> findByUsername(String username);
    }

    interface RoleRepo extends JpaRepository<RoleEntity, Long> {
        Optional<RoleEntity> findByName(String name);
    }

    interface RolePermissionRepo extends JpaRepository<RolePermissionEntity, Long> {
        List<RolePermissionEntity> findByRoleId(Long roleId);

        void deleteByRoleId(Long roleId);
    }

    interface AdminUserRoleRepo extends JpaRepository<AdminUserRoleEntity, Long> {
    }

    interface ReportRepo extends JpaRepository<ReportTicketEntity, Long> {
        List<ReportTicketEntity> findByStateOrderByCreatedAtAsc(String state);

        List<ReportTicketEntity> findByReporterUserIdOrderByCreatedAtDesc(Long reporterUserId);
    }

    interface CopyrightRepo extends JpaRepository<CopyrightNoticeEntity, Long> {
        List<CopyrightNoticeEntity> findByStateOrderByCreatedAtAsc(String state);
    }

    interface CounterRepo extends JpaRepository<CounterNoticeEntity, Long> {
        List<CounterNoticeEntity> findByCopyrightId(Long copyrightId);
    }

    interface AppealRepo extends JpaRepository<AppealEntity, Long> {
        List<AppealEntity> findByStateOrderByCreatedAtAsc(String state);

        List<AppealEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    }

    interface ModerationRepo extends JpaRepository<ModerationActionEntity, Long> {
        List<ModerationActionEntity> findByUserIdAndState(Long userId, String state);

        List<ModerationActionEntity> findByState(String state);
    }

    interface DisputeRepo extends JpaRepository<FamilyDisputeEntity, Long> {
        List<FamilyDisputeEntity> findByStateOrderByCreatedAtAsc(String state);
    }

    interface SettingRepo extends JpaRepository<SystemSettingEntity, String> {
    }
}
