package com.cutepet.notification;

import com.cutepet.notification.domain.MessageRules;
import com.cutepet.notification.domain.MessageRules.Category;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * notification 领域逻辑测试（纯 Java main）。覆盖 T1.7 / U59。
 */
public class NotificationLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[notification] PASS " + name);
    }

    public static void main(String[] args) {
        // 分类与深链
        check("五类消息齐全", Category.values().length == 5);
        check("提醒深链定位对象", "/reminders?focus=reminder:9".equals(MessageRules.deepLink(Category.REMINDER, "reminder", 9)));
        check("审核结果深链到投稿状态", "/me/submissions/5".equals(MessageRules.deepLink(Category.REVIEW, "submission", 5)));
        check("家庭深链", "/families?focus=3".equals(MessageRules.deepLink(Category.FAMILY, "family", 3)));
        check("互动深链到对象", "/article/7".equals(MessageRules.deepLink(Category.INTERACTION, "article", 7)));

        // 已读幂等
        check("UNREAD→READ 有变更", MessageRules.markReadTransition("UNREAD"));
        check("READ 重复标记无变更（幂等）", !MessageRules.markReadTransition("READ"));
        check("未知状态不流转", !MessageRules.markReadTransition("DELETED"));

        List<String> states = List.of("UNREAD", "READ", "UNREAD");
        check("未读计数", MessageRules.unreadCount(states) == 2);

        // U59
        check("U59 待处理才推送", MessageRules.shouldPush("PENDING") && !MessageRules.shouldPush("DONE"));
        check("无通道仅站内", MessageRules.channels(false, true, false).equals(List.of("STATION")));
        check("通道开启且非免打扰含推送", MessageRules.channels(true, true, false).equals(List.of("STATION", "PUSH_STUB")));
        check("免打扰抑制推送", MessageRules.channels(true, true, true).equals(List.of("STATION")));
        check("用户关闭推送", MessageRules.channels(true, false, false).equals(List.of("STATION")));

        // 调度幂等（U57 关联：同期待办唯一 → 单次完成）
        String key = MessageRules.dispatchKey(7, "2026-10-01");
        java.util.Set<String> seen = new HashSet<>();
        check("首次调度胜出", MessageRules.firstDispatchWins(seen, key));
        check("重复调度被拒（幂等）", !MessageRules.firstDispatchWins(seen, key));

        // 模板渲染
        check("模板变量替换", "你好，旺财该打疫苗了".equals(
                MessageRules.render("你好，{pet}该打{type}了", Map.of("pet", "旺财", "type", "疫苗"))));
        check("缺失变量保留占位便于发现", "{pet}缺失".equals(MessageRules.render("{pet}缺失", Map.of())));

        System.out.println("[notification-logic] " + passed + " assertions passed");
    }
}
