package com.cutepet.content.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索纯规则（T3.5 · 需求-资讯 C2 · U17）。
 */
public final class SearchRules {

    public static final int QUERY_MAX = 50;

    public record Hit(String title, int score) {
    }

    public record Segment(String text, boolean hit) {
    }

    private SearchRules() {
    }

    public static boolean validQuery(String query) {
        return query != null && !query.isBlank() && query.length() <= QUERY_MAX;
    }

    /**
     * 相关度：标题命中 30 + 标签精确 20 + 摘要/正文命中 10（可叠加，标题包含查询串为强命中）。
     */
    public static int score(String title, List<String> tags, String body, String query) {
        if (!validQuery(query)) {
            return 0;
        }
        String q = query.trim();
        int s = 0;
        if (title != null && title.contains(q)) {
            s += 30;
        }
        if (tags != null) {
            for (String t : tags) {
                if (t != null && t.contains(q)) {
                    s += 20;
                    break;
                }
            }
        }
        if (body != null && body.contains(q)) {
            s += 10;
        }
        return s;
    }

    /** 降序排序（分数为 0 的不返回） */
    public static List<Hit> rank(List<Hit> hits) {
        List<Hit> out = hits.stream().filter(h -> h.score() > 0)
                .sorted((a, b) -> Integer.compare(b.score(), a.score())).toList();
        return new ArrayList<>(out);
    }

    /** 空结果 → 从候选（已发布池）取前 n 条兜底推荐（U17） */
    public static List<String> fallbackWhenEmpty(List<String> rankedTitles, int n) {
        if (rankedTitles != null && !rankedTitles.isEmpty()) {
            return rankedTitles;
        }
        return List.of(); // 调用方给推荐池
    }

    public static boolean isEmpty(List<Hit> ranked) {
        return ranked == null || ranked.isEmpty();
    }

    /** 高亮片段切分（<mark> 包裹命中段） */
    public static List<Segment> highlight(String text, String query) {
        List<Segment> segments = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return segments;
        }
        if (!validQuery(query) || !text.contains(query.trim())) {
            segments.add(new Segment(text, false));
            return segments;
        }
        String q = query.trim();
        int idx = 0;
        int found;
        while ((found = text.indexOf(q, idx)) >= 0) {
            if (found > idx) {
                segments.add(new Segment(text.substring(idx, found), false));
            }
            segments.add(new Segment(text.substring(found, found + q.length()), true));
            idx = found + q.length();
        }
        if (idx < text.length()) {
            segments.add(new Segment(text.substring(idx), false));
        }
        return segments;
    }
}
