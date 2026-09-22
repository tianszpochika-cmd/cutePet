package com.cutepet.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** notification 持久化实体（映射 cutepet_notification V1）。 */
@Entity
@Table(name = "messages", schema = "cutepet_notification")
class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    public String category; // REMINDER/INTERACTION/REVIEW/FAMILY/SYSTEM
    public String title;
    public String body = "";
    @Column(name = "deep_link")
    public String deepLink;
    public String state = "UNREAD"; // UNREAD/READ
    @Column(name = "read_at")
    public LocalDateTime readAt;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "notification_templates", schema = "cutepet_notification")
class NotificationTemplateEntity {
    @Id
    @Column(name = "tpl_key")
    public String tplKey;
    public String channel;
    @Column(name = "title_tpl")
    public String titleTpl;
    @Column(name = "body_tpl")
    public String bodyTpl;
    public int enabled = 1;
    public LocalDateTime updatedAt;
}

@Entity
@Table(name = "delivery_log", schema = "cutepet_notification")
class DeliveryLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "message_id", nullable = false)
    public Long messageId;
    public String channel;
    public String state;
    public int retries = 0;
    public String error;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "push_channels", schema = "cutepet_notification")
class PushChannelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "device_id", nullable = false, unique = true)
    public String deviceId;
    public String token = "";
    public String platform = "WEB_H5";
    public int stub = 1;
    public int enabled = 1;
}

@Entity
@Table(name = "reminder_dispatches", schema = "cutepet_notification")
class ReminderDispatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "reminder_id", nullable = false)
    public Long reminderId;
    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;
    public String state = "PENDING"; // PENDING/DONE/FAILED
    public String todoState = "PENDING"; // 关联待办态（U59：DONE 不补推）
    public int attempts = 0;
    public LocalDateTime createdAt;
}
