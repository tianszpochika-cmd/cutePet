package com.cutepet.notification;

import com.cutepet.common.web.NotFoundException;
import com.cutepet.notification.domain.MessageRules;
import com.cutepet.notification.domain.MessageRules.Category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 消息中心服务（T1.7 · 契约 /messages 路由组）。 */
@Service
class MessageService {

    private final Repos.MessageRepo messages;

    MessageService(Repos.MessageRepo messages) {
        this.messages = messages;
    }

    public Map<String, Object> list(long userId, String category, String state) {
        List<MessageEntity> rows = (state == null || state.isBlank()
                ? messages.findTop100ByUserIdOrderByCreatedAtDesc(userId)
                : ("UNREAD".equals(state)
                        ? messages.findByUserIdAndStateOrderByCreatedAtDesc(userId, "UNREAD")
                        : messages.findByUserIdAndStateAndCategoryOrderByCreatedAtDesc(userId, state,
                                category == null ? "SYSTEM" : category)));
        long unread = messages.countByUserIdAndState(userId, "UNREAD");
        return Map.of("items", rows, "unread", unread);
    }

    @Transactional
    public Map<String, Object> read(long userId, long messageId) {
        MessageEntity row = messages.findById(messageId)
                .orElseThrow(() -> new NotFoundException("消息不存在"));
        if (row.userId != userId) {
            throw new IllegalArgumentException("非本人消息");
        }
        boolean changed = MessageRules.markReadTransition(row.state); // 幂等
        if (changed) {
            row.state = "READ";
            row.readAt = LocalDateTime.now();
            messages.save(row);
        }
        return Map.of("id", row.id, "state", row.state, "changed", changed);
    }

    @Transactional
    public Map<String, Object> readAll(long userId) {
        int affected = 0;
        for (MessageEntity row : messages.findByUserIdAndState(userId, "UNREAD")) {
            if (MessageRules.affectsUnread(row.state)) {
                row.state = "READ";
                row.readAt = LocalDateTime.now();
                messages.save(row);
                affected++;
            }
        }
        return Map.of("affected", affected);
    }

    @Transactional
    public MessageEntity publish(long userId, Category category, String title, String body, String targetType, long targetId) {
        MessageEntity row = new MessageEntity();
        row.userId = userId;
        row.category = category.name();
        row.title = title;
        row.body = body == null ? "" : body;
        row.deepLink = MessageRules.deepLink(category, targetType, targetId);
        row.createdAt = LocalDateTime.now();
        return messages.save(row);
    }
}

/**
 * 提醒调度（T1.7 · U59）：每分钟扫描 PENDING 调度单。
 * 幂等：reminder+due 唯一 + firstDispatchWins 集合护栏；已完成待办不补推；无通道仅站内。
 */
@Service
class DispatchScheduler {

    private final Repos.DispatchRepo dispatches;
    private final Repos.PushRepo pushes;
    private final MessageService messageService;
    private final Repos.DeliveryRepo deliveries;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();
    private static final Logger log = LoggerFactory.getLogger(DispatchScheduler.class);

    DispatchScheduler(Repos.DispatchRepo dispatches, Repos.PushRepo pushes, MessageService messageService,
                      Repos.DeliveryRepo deliveries) {
        this.dispatches = dispatches;
        this.pushes = pushes;
        this.messageService = messageService;
        this.deliveries = deliveries;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void tick() {
        for (ReminderDispatchEntity job : dispatches.findByState("PENDING")) {
            String key = MessageRules.dispatchKey(job.reminderId, job.dueDate.toString());
            if (!MessageRules.firstDispatchWins(inFlight, key)) {
                continue; // 幂等护栏
            }
            if (!MessageRules.shouldPush(job.todoState)) {
                job.state = "DONE"; // U59：已完成不补推
                dispatches.save(job);
                continue;
            }
            try {
                boolean hasPush = !pushes.findByUserIdAndEnabled(1L, 1).isEmpty(); // 语义占位：实际按 job 用户查
                List<String> channels = MessageRules.channels(hasPush, true, false);
                MessageEntity msg = messageService.publish(1L, Category.REMINDER,
                        "提醒待处理", "你有一条提醒待处理", "reminder", job.reminderId);
                for (String channel : channels) {
                    DeliveryLogEntity d = new DeliveryLogEntity();
                    d.messageId = msg.id;
                    d.channel = channel;
                    d.state = "SENT";
                    d.createdAt = LocalDateTime.now();
                    deliveries.save(d);
                }
                job.state = "DONE";
                job.attempts++;
                dispatches.save(job);
            } catch (RuntimeException ex) {
                job.attempts++;
                job.state = job.attempts >= 3 ? "FAILED" : "PENDING";
                dispatches.save(job);
                log.warn("[dispatch] failed reminder={} attempt={}: {}", job.reminderId, job.attempts, ex.getMessage());
            }
        }
    }
}

/** 通知模板（管理端 /admin/notify/templates 维护；渲染走 MessageRules.render）。 */
@Service
class TemplateService {

    private final Repos.TemplateRepo templates;

    TemplateService(Repos.TemplateRepo templates) {
        this.templates = templates;
    }

    @Transactional
    public Map<String, Object> upsert(String key, String channel, String titleTpl, String bodyTpl) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("模板 key 必填");
        }
        NotificationTemplateEntity row = templates.findById(key).orElseGet(NotificationTemplateEntity::new);
        row.tplKey = key;
        row.channel = channel == null ? "STATION" : channel;
        row.titleTpl = titleTpl;
        row.bodyTpl = bodyTpl;
        row.updatedAt = LocalDateTime.now();
        templates.save(row);
        String rendered = MessageRules.render(bodyTpl, java.util.Map.of());
        return Map.of("key", key, "preview", rendered);
    }

    public List<NotificationTemplateEntity> list() {
        return templates.findAll();
    }
}
