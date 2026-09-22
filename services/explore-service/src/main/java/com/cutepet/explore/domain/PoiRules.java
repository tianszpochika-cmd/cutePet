package com.cutepet.explore.domain;

import java.util.List;

/**
 * POI 检索纯规则（T4.1 · 决议：5/10/20km、综合分 0.6距离+0.4评分、无评价按3.5 · U18）。
 */
public final class PoiRules {

    public static final List<String> TYPES = List.of(
            "宠物店", "宠物医院", "宠物公园", "宠物友好餐厅", "寄养", "美容", "训练");

    public static final List<Integer> RADII_KM = List.of(5, 10, 20);
    public static final int DEFAULT_RADIUS_KM = 5;

    public static final double WEIGHT_DISTANCE = 0.6;
    public static final double WEIGHT_SCORE = 0.4;
    public static final double DEFAULT_RANK_SCORE = 3.5; // 无评价排名基准（决议）

    private PoiRules() {
    }

    public static boolean validType(String type) {
        return TYPES.contains(type);
    }

    public static boolean validRadius(int radiusKm) {
        return RADII_KM.contains(radiusKm);
    }

    /** Haversine 直线距离（km，“附近”口径=直线距离——决议） */
    public static double distanceKm(double lng1, double lat1, double lng2, double lat2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    public static boolean withinRadius(double distanceKm, int radiusKm) {
        return distanceKm <= radiusKm;
    }

    /** 展示均值：无有效评价 → null（U81 暂无评分，不伪造） */
    public static Double displayAvg(long visibleReviewCount, double visibleAvg) {
        return visibleReviewCount <= 0 ? null : visibleAvg;
    }

    /** 排名均值：无评价按 3.5 基准（决议） */
    public static double rankingAvg(long visibleReviewCount, double visibleAvg) {
        return visibleReviewCount <= 0 ? DEFAULT_RANK_SCORE : visibleAvg;
    }

    /** 综合分 = 0.6×距离归一（越近越好） + 0.4×评分归一（/5） */
    public static double compositeScore(double distanceKm, int radiusKm, long visibleCount, double visibleAvg) {
        double distNorm = 1.0 - Math.min(distanceKm / Math.max(1, radiusKm), 1.0);
        double scoreNorm = rankingAvg(visibleCount, visibleAvg) / 5.0;
        return WEIGHT_DISTANCE * distNorm + WEIGHT_SCORE * scoreNorm;
    }

    /** 比较器语义：综合分降序 */
    public static int compare(double compositeA, double compositeB) {
        return Double.compare(compositeB, compositeA);
    }
}
