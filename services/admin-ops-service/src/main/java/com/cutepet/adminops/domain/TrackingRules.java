package com.cutepet.adminops.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 埋点纯规则（T1.8 · 需求-账号 §5 隐私清单第6条：埋点不得含个人信息）。
 */
public final class TrackingRules {

    private static final Pattern EVENT_NAME = Pattern.compile("^[a-z][a-z0-9_.]{2,48}$");
    private static final Pattern PHONE_LIKE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern EMAIL_LIKE = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final int MAX_PROPS = 20;
    private static final long MAX_SKEW_MS = 7L * 24 * 3600 * 1000; // 客户端时钟容差 7 天

    /** 首期白名单（扩事件需过评审入表） */
    public static final List<String> ALLOWED_EVENTS = List.of(
            "page_view", "click", "record_created", "reminder_completed", "article_view",
            "article_like", "article_favorite", "poi_view", "poi_nav_click", "review_created",
            "signup_created", "favorite_created", "login_success", "share_triggered");

    private TrackingRules() {
    }

    public static boolean validEventName(String name) {
        return name != null && EVENT_NAME.matcher(name).matches();
    }

    public static boolean allowedEvent(String name) {
        return ALLOWED_EVENTS.contains(name);
    }

    /** 校验结果：空 map = 通过；否则为错误码列表 */
    public static Map<String, String> validate(String event, Long timestampMs, String sessionId,
                                               String deviceId, Map<String, Object> props) {
        Map<String, String> errors = new HashMap<>();
        if (!validEventName(event)) {
            errors.put("event", "EVENT_NAME_INVALID");
        } else if (!allowedEvent(event)) {
            errors.put("event", "EVENT_NOT_ALLOWLISTED");
        }
        if (sessionId == null || sessionId.isBlank()) {
            errors.put("sessionId", "SESSION_REQUIRED");
        }
        if (deviceId == null || deviceId.isBlank()) {
            errors.put("deviceId", "DEVICE_REQUIRED");
        }
        if (timestampMs == null) {
            errors.put("timestamp", "TIMESTAMP_REQUIRED");
        } else {
            long skew = Math.abs(System.currentTimeMillis() - timestampMs);
            if (skew > MAX_SKEW_MS) {
                errors.put("timestamp", "TIMESTAMP_SKEW");
            }
        }
        if (props != null) {
            if (props.size() > MAX_PROPS) {
                errors.put("props", "TOO_MANY_PROPS");
            }
            for (Map.Entry<String, Object> e : props.entrySet()) {
                String value = e.getValue() == null ? "" : String.valueOf(e.getValue());
                if (PHONE_LIKE.matcher(value).find() || EMAIL_LIKE.matcher(value).find()) {
                    errors.put("props." + e.getKey(), "PII_NOT_ALLOWED"); // 隐私清单：埋点禁 PII
                }
            }
        }
        return errors;
    }

    /** 事件命名合规导出（运营侧校验同源） */
    public static boolean schemaConsistent() {
        return ALLOWED_EVENTS.stream().allMatch(TrackingRules::validEventName);
    }
}
