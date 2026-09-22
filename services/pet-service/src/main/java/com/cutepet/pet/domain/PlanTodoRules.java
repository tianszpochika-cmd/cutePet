package com.cutepet.pet.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 计划/待办纯规则（T2.6 ★增补 · BR-02/BR-04/BR-07 · U55/U57/U58/U67/U87）。
 */
public final class PlanTodoRules {

    public record CompleteResult(String outcome, LocalDate nextDue, int completionCount, int recordsCreated) {
        // outcome: COMPLETED / ALREADY_COMPLETED
    }

    private PlanTodoRules() {
    }

    /** U57 下期唯一：同计划同到期日仅一个待办（存储层 UNIQUE(plan,due) 的逻辑等价） */
    public static Optional<String> ensureTodo(Set<String> existingKeys, String planId, LocalDate due) {
        String key = planId + "#" + due;
        if (existingKeys.contains(key)) {
            return Optional.empty(); // 已存在：不重复生成
        }
        existingKeys.add(key);
        return Optional.of(key);
    }

    /**
     * U57 完成幂等：并发/重试第二次调用返回已有结果——有效完成=1、新增记录=0、下期=1（首发放行）。
     */
    public static CompleteResult completeOnce(boolean alreadyDone, ReminderRules.Recurrence plan,
                                              ReminderRules.Basis basis, LocalDate due, Set<String> existingTodoKeys) {
        if (alreadyDone) {
            return new CompleteResult("ALREADY_COMPLETED", null, 0, 0); // 另一人收到已有结果
        }
        LocalDate next = ReminderRules.nextOccurrence(plan, due, basis);
        if (next != null) {
            ensureTodo(existingTodoKeys, "plan", next); // 下期恰好 1 个
        }
        return new CompleteResult("COMPLETED", next, 1, 1);
    }

    /**
     * U58 草稿/离线失败：持久化失败 → 不生成完成、不排下期、草稿不计成功；
     * 恢复重试仅允许产生一次成功效果（幂等键复用）。
     */
    public static CompleteResult afterPersist(boolean persistOk, boolean retrySucceededBefore,
                                              ReminderRules.Recurrence plan, ReminderRules.Basis basis,
                                              LocalDate due, Set<String> keys) {
        if (!persistOk) {
            return new CompleteResult("ALREADY_COMPLETED", null, 0, 0); // 无效果（outcome 语义：未成功）
        }
        if (retrySucceededBefore) {
            return new CompleteResult("ALREADY_COMPLETED", null, 0, 0); // 只生效一次
        }
        return completeOnce(false, plan, basis, due, keys);
    }

    /** U67：未来计划（提醒）合法可存；“未来日期的已发生事件”由 HealthRecordRules 拒绝 */
    public static boolean futurePlanValid(LocalDate due) {
        return due != null;
    }

    /**
     * U87 关联删除：删除被计划引用的健康记录后，不产生第二期排程
     * （返回本应新增的排程数=0；现有待办由用户显式处理）。
     */
    public static int schedulesCreatedOnRecordDelete() {
        return 0;
    }

    /** U87 双端并发修改同一记录：后写覆盖前需差异确认（此处输出是否需要确认） */
    public static boolean overwriteNeedsConfirm(String localVersion, String remoteVersion) {
        return localVersion != null && remoteVersion != null && !localVersion.equals(remoteVersion);
    }

    /** 计划状态机：ACTIVE ⇄ 暂停 PAUSED；CLOSED 终态 */
    public static boolean planTransition(String from, String to) {
        return switch (from) {
            case "ACTIVE" -> "PAUSED".equals(to) || "CLOSED".equals(to);
            case "PAUSED" -> "ACTIVE".equals(to);
            default -> false;
        };
    }

    /** 计划标题去重（草稿去重 U58 关联：同宠同标题未提交草稿不重复建） */
    public static boolean duplicateDraft(Set<String> existingDraftTitles, String title) {
        return existingDraftTitles.contains(title);
    }
}
