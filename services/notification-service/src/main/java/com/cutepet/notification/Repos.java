package com.cutepet.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** notification Spring Data 仓库集合。 */
interface Repos {

    interface MessageRepo extends JpaRepository<MessageEntity, Long> {
        List<MessageEntity> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);

        List<MessageEntity> findByUserIdAndState(Long userId, String state);

        List<MessageEntity> findByUserIdAndStateOrderByCreatedAtDesc(Long userId, String state);

        long countByUserIdAndState(Long userId, String state);

        List<MessageEntity> findByUserIdAndStateAndCategoryOrderByCreatedAtDesc(Long userId, String state, String category);
    }

    interface TemplateRepo extends JpaRepository<NotificationTemplateEntity, String> {
    }

    interface DeliveryRepo extends JpaRepository<DeliveryLogEntity, Long> {
    }

    interface PushRepo extends JpaRepository<PushChannelEntity, Long> {
        Optional<PushChannelEntity> findByDeviceId(String deviceId);

        List<PushChannelEntity> findByUserIdAndEnabled(Long userId, int enabled);
    }

    interface DispatchRepo extends JpaRepository<ReminderDispatchEntity, Long> {
        Optional<ReminderDispatchEntity> findByReminderIdAndDueDate(Long reminderId, LocalDate dueDate);

        List<ReminderDispatchEntity> findByState(String state);
    }
}
