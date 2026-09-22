package com.cutepet.catalog.domain;

import java.util.List;

/**
 * 清单与关联纯规则（T5.2 + T5.3 ★ · U24/U77/U88）。
 */
public final class ListRules {

    /** 清单类型（T3.6/T5.3）：官方运营 / 用户作者 */
    public static final List<String> KINDS = List.of("OFFICIAL", "AUTHOR");

    public static final int LIST_MAX_ITEMS = 50;

    public enum ProductRefState { VISIBLE, OFF_SHELF_BADGE, REMOVED_TEXT_ONLY }

    private ListRules() {
    }

    public static boolean validKind(String kind) {
        return KINDS.contains(kind);
    }

    public static void assertAddItem(int currentSize, ProductRules.State productState) {
        if (currentSize >= LIST_MAX_ITEMS) {
            throw new IllegalArgumentException("清单最多 " + LIST_MAX_ITEMS + " 项");
        }
        if (productState == ProductRules.State.CLEARED) {
            throw new IllegalArgumentException("治理清除商品不可加入清单（U88）");
        }
    }

    /** U88：清单内商品状态 → 引用展示（下架带标 / 清除去敏感） */
    public static ProductRefState refState(ProductRules.State state) {
        return ProductRefState.valueOf(ProductRules.referenceDisplay(state));
    }

    /**
     * U88：清单全部商品不可用 → 禁止新发布该清单（已发布转隐藏）。
     * 返回 true = 可发布。
     */
    public static boolean listPublishable(List<ProductRules.State> itemStates) {
        if (itemStates == null || itemStates.isEmpty()) {
            return false; // 空清单不可发布
        }
        long unavailable = itemStates.stream()
                .filter(s -> s == ProductRules.State.OFF_SHELF || s == ProductRules.State.CLEARED)
                .count();
        return unavailable < itemStates.size(); // 全部不可用才禁新发
    }

    /** 商品↔评测双向绑定（T5.2）：绑定双方存在性由服务层保证，此处约束方向语义一致 */
    public static boolean bindingValid(boolean productExists, boolean articleExists) {
        return productExists && articleExists;
    }

    /** CSV 导入作业统计口径（与 ProductRules.validateImportRows 一致） */
    public static String importSummary(int accepted, int rejected) {
        return "accepted=" + accepted + ",rejected=" + rejected
                + (rejected > 0 ? ",hint=检查禁止品类/来源字段" : "");
    }

    /** 清单排序：ord 升序、同 ord 按 id */
    public static int compareItem(int ordA, long idA, int ordB, long idB) {
        if (ordA != ordB) {
            return Integer.compare(ordA, ordB);
        }
        return Long.compare(idA, idB);
    }
}
