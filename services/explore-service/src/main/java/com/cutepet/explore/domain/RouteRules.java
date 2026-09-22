package com.cutepet.explore.domain;

import java.util.List;

/**
 * 遛宠路线纯规则（T4.4 · 选点连线 + 先审后发 · U21 关联）。
 */
public final class RouteRules {

    public enum State { PENDING, PUBLISHED, REJECTED }

    private RouteRules() {
    }

    /** 点串格式 "lng,lat;lng,lat;..."（地图选点连线，一期非真实轨迹——决议） */
    public static double totalDistanceKm(String points) {
        List<double[]> pts = parse(points);
        double sum = 0;
        for (int i = 1; i < pts.size(); i++) {
            double[] a = pts.get(i - 1);
            double[] b = pts.get(i);
            sum += PoiRules.distanceKm(a[0], a[1], b[0], b[1]);
        }
        return Math.round(sum * 1000.0) / 1000.0;
    }

    public static List<double[]> parse(String points) {
        if (points == null || points.isBlank()) {
            throw new IllegalArgumentException("路线点位必填");
        }
        java.util.List<double[]> out = new java.util.ArrayList<>();
        for (String seg : points.split(";")) {
            String[] parts = seg.trim().split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("点位格式非法: " + seg);
            }
            out.add(new double[] { Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()) });
        }
        if (out.size() < 2) {
            throw new IllegalArgumentException("选点连线至少 2 个点");
        }
        return out;
    }

    public static boolean transition(State from, State to) {
        return switch (from) {
            case PENDING -> to == State.PUBLISHED || to == State.REJECTED; // 先审后发
            case REJECTED -> to == State.PENDING;                          // 修改重提
            case PUBLISHED -> false;                                       // 发布后下线由治理处置
            default -> false;
        };
    }

    public static boolean feedVisible(State state) {
        return state == State.PUBLISHED;
    }

    /** 点赞/收藏幂等（与内容侧同语义） */
    public static boolean toggleChanged(boolean existed, boolean want) {
        return existed != want;
    }
}
