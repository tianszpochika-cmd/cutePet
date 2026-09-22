package com.cutepet.pet.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 健康摘要纯规则（T2.5 · 需求 B6 · U79 隐私与包含规则）。 */
public final class SummaryRules {

    public record ModuleDecision(String module, String status, String reason) {
        // status: INCLUDED / CANCELLED / MISSING
    }

    public record SummaryView(List<String> includedModules, List<ModuleDecision> omitted,
                              List<String> recordIds, boolean phoneExcluded) {
    }

    private SummaryRules() {
    }

    /** 隐私过滤：摘要不含主人手机号等身份字段（需求 B6） */
    public static boolean exposesOwnerPhone() {
        return false;
    }

    /**
     * U79：默认摘要包含“早于 30 天的长期过敏”与“当前仍在用的药”；
     * 用户取消的模块与数据缺项均以可见条目列出（不静默丢弃）。
     */
    public static SummaryView build(List<String> selectedModules,
                                    List<String> availableModules,
                                    List<String> allergyRecordIdsOver30Days,
                                    List<String> currentMedRecordIds,
                                    List<String> otherRecordIds) {
        List<String> included = new ArrayList<>();
        List<ModuleDecision> omitted = new ArrayList<>();

        for (String m : availableModules) {
            if (selectedModules.contains(m)) {
                included.add(m);
            } else {
                omitted.add(new ModuleDecision(m, "CANCELLED", "用户取消该模块"));
            }
        }
        for (String m : selectedModules) {
            if (!availableModules.contains(m)) {
                omitted.add(new ModuleDecision(m, "MISSING", "无数据（缺项可见）"));
            }
        }

        List<String> recordIds = new ArrayList<>();
        recordIds.addAll(allergyRecordIdsOver30Days); // 长期过敏保留（U79）
        recordIds.addAll(currentMedRecordIds);        // 当前用药保留（U79）
        if (included.contains("健康记录")) {
            recordIds.addAll(otherRecordIds);
        }
        return new SummaryView(included, omitted, recordIds, !exposesOwnerPhone());
    }

    /** 摘要为静态快照：生成时间即内容时间点 */
    public static String snapshotNote(LocalDate generatedOn) {
        return "数据截至 " + generatedOn;
    }
}
