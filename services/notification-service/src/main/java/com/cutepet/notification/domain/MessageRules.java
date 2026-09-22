package com.cutepet.notification.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消息中心纯规则（T1.7 · 需求-管理端 §2.6 / 功能设计-账号 §4 · U59）。
 */
public final class MessageRules {

    public enum Category { REMINDER, INTERACTION, REVIEW, FAMILY, SYSTEM }

    private MessageRules() {
    }

    /** 分类 → 深链（点击跳对应对象，见移动端深链路由表） */
    public static String deepLink(Category category, String targetType, long targetId) {
        return switch (category) {
            case REMINDER -> "/reminders?focus=" + targetType + ":" + targetId;
            case INTERACTION -> "/" + targetType + "/" + targetId;
            case REVIEW -> "/me/submissions/" + targetId;
            case FAMILY -> "/families?focus=" + targetId;
            case SYSTEM -> "/messages";
        };
    }

    /** 已读幂等：UNREAD→READ；重复标记仍为 READ 且返回 false（无新变更） */
    public static boolean markReadTransition(String state) {
        if ("READ".equals(state)) {
            return false;
        }
        return "UNREAD".equals(state);
    }

    public static long unreadCount(List<String> states) {
        return states.stream().filter("UNREAD"::equals).count();
    }

    // ---------- 提醒触达（U59） ----------

    /** 已完成不补推 */
    public static boolean shouldPush(String todoState) {
        return "PENDING".equals(todoState);
    }

    /** 调度幂等键（uk reminder_id+due_date）：首次完成即胜出 */
    public static String dispatchKey(long reminderId, String dueDate) {
        return reminderId + "#" + dueDate;
    }

    public static boolean firstDispatchWins(java.util.Set<String> seen, String key) {
        return seen.add(key);
    }

    /** 无推送通道 → 仅站内（U59：无通道写仅站内） */
    public static List<String> channels(boolean hasPushChannel, boolean pushEnabled, boolean dndActive) {
        List<String> plan = new ArrayList<>();
        plan.add("STATION"); // 站内恒有
        if (hasPushChannel && pushEnabled && !dndActive) {
            plan.add("PUSH_STUB");
        }
        return plan;
    }

    /** 模板渲染：{key} 占位替换；缺 key 保留原样（运营可发现） */
    public static String render(String template, Map<String, String> vars) {
        if (template == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            int open = template.indexOf('{', i);
            if (open < 0) {
                out.append(template, i, template.length());
                break;
            }
            int close = template.indexOf('}', open);
            if (close < 0) {
                out.append(template, i, template.length());
                break;
            }
            out.append(template, i, open);
            String key = template.substring(open + 1, close);
            out.append(vars.getOrDefault(key, "{" + key + "}"));
            i = close + 1;
        }
        return out.toString();
    }

    /** 全部已读仅作用于未读（返回受影响条数语义由调用方统计） */
    public static boolean affectsUnread(String state) {
        return "UNREAD".equals(state);
    }
}
