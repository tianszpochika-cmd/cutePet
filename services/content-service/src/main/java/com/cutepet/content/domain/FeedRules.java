package com.cutepet.content.domain;

import java.util.List;

/**
 * Feed 纯规则（T3.1 · 数据设计决议：运营精选置顶 + 近 7 天互动热度 · U13）。
 */
public final class FeedRules {

    public static final List<String> CHANNELS = List.of(
            "犬", "猫", "异宠", "营养", "疾病健康", "训练行为", "行业快讯");

    public static final int HOT_WINDOW_DAYS = 7;
    public static final int PAGE_SIZE_DEFAULT = 12;
    public static final int PAGE_SIZE_MAX = 50;

    private FeedRules() {
    }

    public static boolean validChannel(String channel) {
        return CHANNELS.contains(channel);
    }

    public static String channelSlug(String channel) {
        return switch (channel) {
            case "犬" -> "dog";
            case "猫" -> "cat";
            case "异宠" -> "exotic";
            case "营养" -> "nutrition";
            case "疾病健康" -> "health";
            case "训练行为" -> "training";
            case "行业快讯" -> "news";
            default -> "";
        };
    }

    public static boolean inHotWindow(int ageDays) {
        return ageDays >= 0 && ageDays <= HOT_WINDOW_DAYS;
    }

    /** 热度分：点赞1 + 评论2 + 收藏3；超 7 天窗口不参与热度（回退时间序） */
    public static long hotness(long likes, long comments, long favorites, int ageDays) {
        if (!inHotWindow(ageDays)) {
            return -1;
        }
        return likes + comments * 2 + favorites * 3;
    }

    /**
     * Feed 排序比较器语义：置顶(PGC 精选) → 热度降序 → 创建时间降序。
     * 返回负数 a 排前。
     */
    public static int compareFeed(boolean topA, long hotA, long createdA,
                                  boolean topB, long hotB, long createdB) {
        if (topA != topB) {
            return topA ? -1 : 1;
        }
        if (hotA != hotB) {
            return Long.compare(hotB, hotA);
        }
        return Long.compare(createdB, createdA);
    }

    /** 置顶仅限已发布内容 */
    public static boolean pinAllowed(boolean published) {
        return published;
    }

    /** 分页收敛（contracts.clampPage 镜像） */
    public static int[] clampPage(int page, int size) {
        int p = page >= 1 ? page : 1;
        int s = size >= 1 && size <= PAGE_SIZE_MAX ? size : PAGE_SIZE_DEFAULT;
        return new int[] { p, s };
    }
}
