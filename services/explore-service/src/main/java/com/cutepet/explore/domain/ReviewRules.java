package com.cutepet.explore.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 评价纯规则（T4.2 · 决议：1天1条/设备≤5/全5短文复审/新号先审后计 · U19/U81）。
 */
public final class ReviewRules {

    public enum State { VISIBLE, PENDING, SELF_VISIBLE, HIDDEN }

    public enum SpamVerdict { OK, DUPLICATE_DAY, DEVICE_LIMIT, SUSPECT_REVIEW }

    private ReviewRules() {
    }

    public static boolean scoreValid(int score) {
        return score >= 1 && score <= 5;
    }

    public static boolean threeScoresValid(int a, int b, int c) {
        return scoreValid(a) && scoreValid(b) && scoreValid(c);
    }

    /** 三维均值（两位小数） */
    public static double avg(int friendly, int env, int service) {
        double raw = (friendly + env + service) / 3.0;
        return Math.round(raw * 100.0) / 100.0;
    }

    /** 防刷裁决：同人同场所同日 → 拒；同设备当日 >5 → 拒；全5且正文<10字 → 复审(SUSPECT) */
    public static SpamVerdict spamCheck(long sameUserTodayCount, long sameDeviceTodayCount,
                                        int[] scores, String content) {
        if (sameUserTodayCount >= 1) {
            return SpamVerdict.DUPLICATE_DAY; // 1 天 1 条（决议）
        }
        if (sameDeviceTodayCount >= 5) {
            return SpamVerdict.DEVICE_LIMIT;  // 同设备 ≤5（决议）
        }
        boolean allFive = scores != null && scores.length == 3
                && scores[0] == 5 && scores[1] == 5 && scores[2] == 5;
        String text = content == null ? "" : content.trim();
        if (allFive && text.length() < 10) {
            return SpamVerdict.SUSPECT_REVIEW; // 进复审队列
        }
        return SpamVerdict.OK;
    }

    /**
     * 状态判定（U81）：新号（注册 <7 天）先审后计 → PENDING；
     * SUSPECT → PENDING（仅本人可见 SELF_VISIBLE 语义由查询层映射）；否则 VISIBLE。
     */
    public static State stateFor(long accountAgeDays, SpamVerdict verdict) {
        if (accountAgeDays < 7) {
            return State.PENDING;
        }
        if (verdict == SpamVerdict.SUSPECT_REVIEW) {
            return State.PENDING;
        }
        return State.VISIBLE;
    }

    /** 均值只计 VISIBLE（被隐藏/举报成立/待审不计 —— U81） */
    public static boolean countsInAvg(State state) {
        return state == State.VISIBLE;
    }

    public static boolean visibleToAuthor(State state) {
        return state == State.VISIBLE || state == State.PENDING || state == State.SELF_VISIBLE;
    }

    public static boolean visibleToOthers(State state) {
        return state == State.VISIBLE;
    }

    /** 重算均值：仅有效样本；无样本 → null（暂无评分，恢复后重算 —— U81） */
    public static Double recomputeAvg(List<State> states, List<Double> avgs) {
        double sum = 0;
        int n = 0;
        for (int i = 0; i < states.size(); i++) {
            if (countsInAvg(states.get(i))) {
                sum += avgs.get(i);
                n++;
            }
        }
        return n == 0 ? null : Math.round(sum / n * 100.0) / 100.0;
    }

    /** 图片重复检测：同一用户提交的图片指纹列表存在重复 → 拒（决议） */
    public static boolean imageDuplicate(List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return false;
        }
        Set<String> seen = new HashSet<>();
        for (String h : hashes) {
            if (!seen.add(h)) {
                return true;
            }
        }
        return false;
    }

    /** 防刷 day_key 构造（同人同场所同日唯一键） */
    public static String dayKey(long userId, long poiId, LocalDate date) {
        return userId + "#" + poiId + "#" + date;
    }
}
