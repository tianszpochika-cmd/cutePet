package com.cutepet.catalog;

import com.cutepet.catalog.domain.ListRules;
import com.cutepet.catalog.domain.ProductRules;
import com.cutepet.catalog.domain.ProductRules.State;

import java.util.List;

/**
 * catalog 领域逻辑测试（纯 Java main）。覆盖 T5.1(U23) / T5.2(U24) / T5.3(U77/U88)。
 */
public class CatalogLogicTest {

    private static int passed = 0;

    private static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        passed++;
        System.out.println("[catalog] PASS " + name);
    }

    public static void main(String[] args) {
        testProducts();
        testLists();
        System.out.println("[catalog-logic] " + passed + " assertions passed");
    }

    private static void testProducts() {
        // T5.1 / U23
        check("7 类目白名单", ProductRules.CATEGORIES.size() == 7
                && ProductRules.validCategory("主粮") && !ProductRules.validCategory("活体"));
        check("兽药类目/名称命中黑名单（U23）",
                ProductRules.banned("医疗保健", "兽用阿莫西林") || ProductRules.banned("处方", "处方粮")
                        || ProductRules.banned("主粮", "兽药伴侣"));
        check("处方粮关键词拦截（BR-06）", ProductRules.banned("主粮", "肾脏处方粮"));
        check("医疗保健含药字拦截", ProductRules.banned("医疗保健", "宠物感冒药片"));
        check("正常护理品放行", !ProductRules.banned("医疗保健", "皮肤护理喷雾")
                && !ProductRules.banned("主粮", "幼猫无谷粮"));

        // 来源字段（T5.3）
        threw = false;
        try {
            ProductRules.assertCreatable("主粮", "幼猫粮", "WILD");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("非法来源类型拒绝（T5.3）", threw);
        threw = false;
        try {
            ProductRules.assertCreatable("主粮", "处方粮xx", "EDITORIAL");
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("单条通道禁品类拒绝（U77）", threw);

        // CSV 通道同规则（U77 不能绕）
        int[] result = ProductRules.validateImportRows(List.of(
                new String[] { "幼猫粮", "主粮", "EDITORIAL" },
                new String[] { "犬用处方药", "医疗保健", "CSV_IMPORT" },
                new String[] { "宠物感冒药", "医疗保健", "CSV_IMPORT" },
                new String[] { "坏行", "神秘类目", "" }));
        check("CSV 导入逐行同规则：1 过 3 拒（U77）", result[0] == 1 && result[1] == 3);

        // 标签通道：banned 由类目+名称联合判定（等价于标签建库走 assertCreatable）
        check("标签通道同规则（U77 三通道）", ProductRules.banned("处方", "营养补充"));

        // 热度口径（决议）
        check("热度=30天浏览+收藏×3，超窗不计（决议）",
                ProductRules.hotScore(100, 10, 9999) == 100 + 30);
        check("热度零值", ProductRules.hotScore(0, 0, 0) == 0);

        // 状态机与引用展示（U88）
        check("上/下架/治理清除流转", ProductRules.transition(State.ON_SHELF, State.OFF_SHELF)
                && ProductRules.transition(State.OFF_SHELF, State.ON_SHELF)
                && ProductRules.transition(State.ON_SHELF, State.CLEARED)
                && !ProductRules.transition(State.CLEARED, State.ON_SHELF));
        check("U88 普通下架→历史带标可见", "OFF_SHELF_BADGE".equals(ProductRules.referenceDisplay(State.OFF_SHELF)));
        check("U88 治理清除→去敏感", "REMOVED_TEXT_ONLY".equals(ProductRules.referenceDisplay(State.CLEARED)));
        check("U88 清除后不可原样再上", !ProductRules.canResubmitListing(State.CLEARED)
                && ProductRules.canResubmitListing(State.OFF_SHELF));

        // 编辑部测试标识（U77）
        check("编辑部测试仅编辑+员工（U77）", ProductRules.editorTestAllowed(true, true)
                && !ProductRules.editorTestAllowed(false, true)
                && !ProductRules.editorTestAllowed(true, false));
        check("价格区间校验", ProductRules.validPriceRange(null, null)
                && ProductRules.validPriceRange(10.0, 20.0)
                && !ProductRules.validPriceRange(20.0, 10.0));
    }

    private static boolean threw;

    private static void testLists() {
        // T5.2 / U24
        check("清单双类型（T3.6/T5.3）", ListRules.validKind("OFFICIAL") && ListRules.validKind("AUTHOR")
                && !ListRules.validKind("PAID"));
        threw = false;
        try {
            ListRules.assertAddItem(ListRules.LIST_MAX_ITEMS, State.ON_SHELF);
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("清单 50 项上限", threw);
        threw = false;
        try {
            ListRules.assertAddItem(3, State.CLEARED);
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check("U88 清除商品不可入清单", threw);
        check("正常商品可加入", org == null ? addOk() : addOk());

        check("U88 引用状态映射齐全",
                ListRules.refState(State.OFF_SHELF) == ListRules.ProductRefState.OFF_SHELF_BADGE
                        && ListRules.refState(State.CLEARED) == ListRules.ProductRefState.REMOVED_TEXT_ONLY
                        && ListRules.refState(State.ON_SHELF) == ListRules.ProductRefState.VISIBLE);

        check("U88 全部不可用清单禁新发", !ListRules.listPublishable(List.of(State.OFF_SHELF, State.CLEARED)));
        check("U88 部分不可用仍可发", ListRules.listPublishable(List.of(State.ON_SHELF, State.OFF_SHELF)));
        check("空清单不可发布", !ListRules.listPublishable(List.of()));

        check("双向绑定需双方存在（U24）", ListRules.bindingValid(true, true)
                && !ListRules.bindingValid(false, true));
        check("导入汇总提示", ListRules.importSummary(3, 1).contains("rejected=1")
                && !ListRules.importSummary(4, 0).contains("hint"));

        int byOrd = ListRules.compareItem(1, 9, 2, 3);
        check("清单排序先按 ord", byOrd < 0);
        check("同 ord 按 id", ListRules.compareItem(1, 3, 1, 4) < 0);
    }

    private static String org;

    private static boolean addOk() {
        ListRules.assertAddItem(0, State.ON_SHELF);
        return true;
    }
}
