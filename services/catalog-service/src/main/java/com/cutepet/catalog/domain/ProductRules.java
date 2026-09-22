package com.cutepet.catalog.domain;

import java.util.List;
import java.util.Locale;

/**
 * 商品纯规则（T5.1 + T5.3 ★ · 需求-用品 E1 · BR-06 · U23/U77/U88）。
 */
public final class ProductRules {

    public static final List<String> CATEGORIES = List.of(
            "主粮", "零食", "玩具", "清洁护理", "医疗保健", "出行用品", "服饰窝垫");

    /** 禁止品类关键词（BR-06/U77：兽药、处方类一律不入库——单条/CSV/标签三通道同规则） */
    public static final List<String> BANNED_KEYWORDS = List.of("兽药", "处方", "处方粮", "兽用处方");

    public enum State { ON_SHELF, OFF_SHELF, CLEARED }

    /** 来源类型（T5.3 来源字段必填） */
    public static final List<String> SOURCE_TYPES = List.of("EDITORIAL", "BRAND_INFO", "USER_SUBMIT", "CSV_IMPORT");

    public static final int HOT_WINDOW_DAYS = 30;
    public static final double HOT_FAVORITE_WEIGHT = 3.0;

    private ProductRules() {
    }

    public static boolean validCategory(String category) {
        return CATEGORIES.contains(category);
    }

    /** 兽药/处方类黑名单：类目或名称命中即拒（U23/U77 三通道一致） */
    public static boolean banned(String category, String name) {
        String cat = category == null ? "" : category;
        String nm = name == null ? "" : name;
        for (String kw : BANNED_KEYWORDS) {
            if (cat.contains(kw) || nm.contains(kw)) {
                return true;
            }
        }
        // 医疗保健类目仅限护理保健非药品：名称含"药/片/注射"亦拦（保守黑名单）
        if ("医疗保健".equals(cat)) {
            String lower = nm.toLowerCase(Locale.ROOT);
            if (nm.contains("药") || lower.contains("injection") || nm.contains("针剂")) {
                return true;
            }
        }
        return false;
    }

    public static void assertCreatable(String category, String name, String sourceType) {
        if (!validCategory(category)) {
            throw new IllegalArgumentException("类目不合法（7 类白名单）");
        }
        if (banned(category, name)) {
            throw new IllegalArgumentException("禁止品类：兽药/处方类不入库（BR-06/U77）");
        }
        if (sourceType == null || !SOURCE_TYPES.contains(sourceType)) {
            throw new IllegalArgumentException("来源字段必填且合法（T5.3）");
        }
    }

    /** CSV 导入逐行同规则——不能绕（U77） */
    public static int[] validateImportRows(List<String[]> rows) {
        int accepted = 0;
        int rejected = 0;
        for (String[] row : rows) {
            try {
                String category = row.length > 1 ? row[1] : "";
                String name = row.length > 0 ? row[0] : "";
                String source = row.length > 2 ? row[2] : "";
                assertCreatable(category, name, source);
                accepted++;
            } catch (IllegalArgumentException ex) {
                rejected++;
            }
        }
        return new int[] { accepted, rejected };
    }

    /** 热度口径（决议）：近 30 天 浏览 + 收藏×3；超窗浏览不计 */
    public static double hotScore(long viewsIn30d, long favorites, long viewsOlderThan30d) {
        return viewsIn30d + favorites * HOT_FAVORITE_WEIGHT; // 超窗部分明确不计
    }

    /** 状态机：上/下架；治理清除仅治理动作产生 */
    public static boolean transition(State from, State to) {
        return switch (from) {
            case ON_SHELF -> to == State.OFF_SHELF || to == State.CLEARED;
            case OFF_SHELF -> to == State.ON_SHELF || to == State.CLEARED;
            case CLEARED -> false; // 治理清除终态（解禁走人工复核，本地阶段扩展）
        };
    }

    /** U88 关联展示映射：普通下架保留历史展示+已下架标；治理清除去敏感（引用处不可见原文） */
    public static String referenceDisplay(State productState) {
        return switch (productState) {
            case ON_SHELF -> "VISIBLE";
            case OFF_SHELF -> "OFF_SHELF_BADGE";   // 标记已下架，历史评测仍可读
            case CLEARED -> "REMOVED_TEXT_ONLY";   // 去敏感：仅留纯文本占位
        };
    }

    public static boolean canResubmitListing(State productState) {
        return productState != State.CLEARED; // 治理清除后禁止原样再上（须整改重录）
    }

    /** 评测来源标注（强制字段——视觉设计总纲升级项） */
    public static boolean editorTestAllowed(boolean hasEditorPermission, boolean authorIsStaff) {
        return hasEditorPermission && authorIsStaff; // U77：普通用户不可标"编辑部测试"
    }

    public static boolean validPriceRange(Double min, Double max) {
        if (min == null && max == null) {
            return true;
        }
        if (min == null || max == null) {
            return min != null ? min >= 0 : max >= 0;
        }
        return min >= 0 && max >= min;
    }
}
