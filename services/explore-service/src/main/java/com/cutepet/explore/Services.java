package com.cutepet.explore;

import com.cutepet.common.web.ForbiddenException;
import com.cutepet.common.web.NotFoundException;
import com.cutepet.explore.domain.ActivityRules;
import com.cutepet.explore.domain.ActivityRules.OrgState;
import com.cutepet.explore.domain.ActivityRules.SignupDeny;
import com.cutepet.explore.domain.ActivityRules.State;
import com.cutepet.explore.domain.CorrectionRules;
import com.cutepet.explore.domain.PoiRules;
import com.cutepet.explore.domain.ReviewRules;
import com.cutepet.explore.domain.ReviewRules.SpamVerdict;
import com.cutepet.explore.domain.RouteRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * explore 应用服务（T4.1–T4.6 编排）。判定委托 domain（ExploreLogicTest 覆盖）。
 * dev shim：X-User-Id / X-Permissions；并发名额的 DB 唯一兜底随本地阶段补强。
 */
@Service
class PoiService {

    private final Repos.PoiRepo pois;
    private final Repos.ReviewRepo reviews;
    private final Repos.FavoritePoiRepo favorites;
    private final Repos.CorrectionRepo corrections;

    PoiService(Repos.PoiRepo pois, Repos.ReviewRepo reviews, Repos.FavoritePoiRepo favorites,
               Repos.CorrectionRepo corrections) {
        this.pois = pois;
        this.reviews = reviews;
        this.favorites = favorites;
        this.corrections = corrections;
    }

    /** T4.1 检索：类型/城市/半径过滤 + 综合分排序（0.6 距离 + 0.4 评分） */
    public Map<String, Object> search(Double lat, Double lng, Integer radiusKm, String city, String type) {
        int radius = radiusKm == null ? PoiRules.DEFAULT_RADIUS_KM : radiusKm;
        if (!PoiRules.validRadius(radius)) {
            throw new IllegalArgumentException("半径仅支持 5/10/20km（决议）");
        }
        if (type != null && !type.isBlank() && !PoiRules.validType(type)) {
            throw new IllegalArgumentException("场所类型不合法");
        }
        List<PoiEntity> pool = pois.findByState("NORMAL");
        List<Map<String, Object>> items = new ArrayList<>();
        for (PoiEntity p : pool) {
            if (city != null && !city.isBlank() && !city.equals(p.city)) {
                continue;
            }
            if (type != null && !type.isBlank() && !type.equals(p.type)) {
                continue;
            }
            double distance = 0;
            if (lat != null && lng != null) {
                distance = PoiRules.distanceKm(lng, lat, p.lng, p.lat);
                if (!PoiRules.withinRadius(distance, radius)) {
                    continue;
                }
            }
            double composite = PoiRules.compositeScore(distance, radius, p.reviewCount, p.avgScore == null ? 0 : p.avgScore);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.id);
            item.put("name", p.name);
            item.put("type", p.type);
            item.put("city", p.city);
            item.put("distanceKm", Math.round(distance * 100.0) / 100.0);
            item.put("avg", PoiRules.displayAvg(p.reviewCount, p.avgScore == null ? 0 : p.avgScore)); // null=暂无评分(U81)
            item.put("reviewCount", p.reviewCount);
            item.put("composite", Math.round(composite * 1000.0) / 1000.0);
            item.put("openHours", p.openHours);
            items.add(item);
        }
        items.sort((a, b) -> PoiRules.compare((Double) a.get("composite"), (Double) b.get("composite")));
        return Map.of("items", items, "radiusKm", radius, "total", items.size());
    }

    public Map<String, Object> detail(long poiId) {
        PoiEntity p = pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.id);
        out.put("name", p.name);
        out.put("type", p.type);
        out.put("city", p.city);
        out.put("lng", p.lng);
        out.put("lat", p.lat);
        out.put("address", p.address);
        out.put("phone", p.phone); // 游客脱敏在网关/前端层（决议：登录可见完整）
        out.put("openHours", p.openHours);
        out.put("attrs", p.attrs);
        out.put("state", p.state);
        out.put("avg", PoiRules.displayAvg(p.reviewCount, p.avgScore == null ? 0 : p.avgScore));
        out.put("reviewCount", p.reviewCount);
        out.put("navClicks", p.navClicks);
        return out;
    }

    @Transactional
    public Map<String, Object> navClick(long poiId) {
        PoiEntity p = pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        p.navClicks = p.navClicks + 1;
        pois.save(p);
        return Map.of("poiId", poiId, "navClicks", p.navClicks); // L5 转化计数
    }

    @Transactional
    public Map<String, Object> favorite(long userId, long poiId, boolean want) {
        pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        var key = new FavoritePoiKey(userId, poiId);
        if (want && !favorites.existsById(key)) {
            FavoritePoiEntity row = new FavoritePoiEntity();
            row.userId = userId;
            row.poiId = poiId;
            row.createdAt = LocalDateTime.now();
            favorites.save(row);
        } else if (!want && favorites.existsById(key)) {
            favorites.deleteById(key);
        }
        return Map.of("poiId", poiId, "favorited", want);
    }

    /** T4.3 纠错：字段校验 + 同类合并 + 落单 */
    @Transactional
    public Map<String, Object> createCorrection(long userId, long poiId, String field,
                                                String proposed, String evidence) {
        pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        if (!CorrectionRules.validField(field)) {
            throw new IllegalArgumentException("纠错字段不合法");
        }
        CorrectionRules.valueToWriteBack(proposed); // 校验
        List<CorrectionEntity> openSame = corrections.findByPoiIdAndStateAndFieldOrderByIdAsc(
                poiId, "OPEN", field);
        CorrectionEntity row = new CorrectionEntity();
        row.poiId = poiId;
        row.userId = userId;
        row.field = field;
        row.proposed = proposed.trim();
        row.evidence = evidence;
        row.createdAt = LocalDateTime.now();
        CorrectionRules.mergeTarget(openSame.stream().map(c -> c.id).toList())
                .ifPresent(firstId -> row.mergedInto = firstId); // 同类合并（U20）
        corrections.save(row);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.id);
        out.put("state", "OPEN");
        out.put("mergedInto", row.mergedInto);
        return out;
    }

    public List<CorrectionEntity> myCorrections(long userId) {
        return corrections.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<CorrectionEntity> openCorrections() {
        return corrections.findByStateOrderByIdAsc("OPEN");
    }

    @Transactional
    public Map<String, Object> resolveCorrection(long id, long handlerId, boolean accept, String note) {
        CorrectionEntity c = corrections.findById(id).orElseThrow(() -> new NotFoundException("纠错不存在"));
        CorrectionRules.assertTransition(CorrectionRules.State.valueOf(c.state),
                accept ? CorrectionRules.State.ACCEPTED : CorrectionRules.State.REJECTED);
        if (!accept && !CorrectionRules.canReject(note)) {
            throw new IllegalArgumentException("驳回必附理由");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (accept) {
            String value = CorrectionRules.valueToWriteBack(c.proposed);
            PoiEntity poi = pois.findById(c.poiId).orElseThrow();
            switch (c.field) {
                case "address" -> poi.address = value;
                case "phone" -> poi.phone = value;
                case "open_hours" -> poi.openHours = value;
                case "closed" -> poi.state = "CLOSED";
                case "attrs" -> poi.attrs = value;
                default -> { }
            }
            pois.save(poi);
            // 同组工单一并处理（合并语义）
            for (CorrectionEntity sibling : corrections.findByPoiIdAndStateAndFieldOrderByIdAsc(
                    c.poiId, "OPEN", c.field)) {
                if (!sibling.id.equals(c.id)) {
                    sibling.state = "ACCEPTED";
                    sibling.mergedInto = c.id;
                    sibling.handlerId = handlerId;
                    corrections.save(sibling);
                }
            }
            c.state = "ACCEPTED";
        } else {
            c.state = "REJECTED";
            c.note = note;
        }
        c.handlerId = handlerId;
        corrections.save(c);
        out.put("id", c.id);
        out.put("state", c.state);
        out.put("notify", CorrectionRules.notificationFor(CorrectionRules.State.valueOf(c.state))); // 双向通知
        return out;
    }

    /** U81 重算均值：仅 VISIBLE 计入；无有效 → null */
    @Transactional
    public void recomputeAvg(long poiId) {
        PoiEntity poi = pois.findById(poiId).orElseThrow();
        List<ReviewRules.State> states = new ArrayList<>();
        List<Double> avgs = new ArrayList<>();
        for (PoiReviewEntity r : reviews.findByPoiIdOrderByCreatedAtDesc(poiId)) {
            states.add(ReviewRules.State.valueOf(r.state));
            avgs.add(ReviewRules.avg(r.scoreFriendly, r.scoreEnv, r.scoreService));
        }
        Double avg = ReviewRules.recomputeAvg(states, avgs);
        long visibleCount = states.stream().filter(ReviewRules::countsInAvg).count();
        poi.avgScore = avg == null ? 0.0 : avg;
        poi.reviewCount = (int) visibleCount;
        pois.save(poi);
    }

    // ---------- 管理侧 POI ----------

    @Transactional
    public Map<String, Object> adminCreate(String name, String type, String city, Double lng, Double lat,
                                           String address, String phone, String openHours, String attrs) {
        if (!PoiRules.validType(type)) {
            throw new IllegalArgumentException("场所类型不合法");
        }
        PoiEntity p = new PoiEntity();
        p.name = name;
        p.type = type;
        p.city = city;
        p.lng = lng;
        p.lat = lat;
        p.address = address == null ? "" : address;
        p.phone = phone == null ? "" : phone;
        p.openHours = openHours == null ? "" : openHours;
        p.attrs = attrs == null ? "" : attrs;
        p.createdAt = LocalDateTime.now();
        p = pois.save(p);
        return Map.of("id", p.id, "state", p.state);
    }

    @Transactional
    public Map<String, Object> adminUpdate(long poiId, String address, String phone, String openHours,
                                           String attrs) {
        PoiEntity p = pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        if (address != null) {
            p.address = address;
        }
        if (phone != null) {
            p.phone = phone;
        }
        if (openHours != null) {
            p.openHours = openHours;
        }
        if (attrs != null) {
            p.attrs = attrs;
        }
        pois.save(p);
        return Map.of("id", p.id, "updated", true);
    }

    @Transactional
    public Map<String, Object> adminClose(long poiId) {
        PoiEntity p = pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        p.state = "CLOSED";
        pois.save(p);
        return Map.of("id", p.id, "state", "CLOSED");
    }
}

@Service
class ReviewService {

    private final Repos.PoiRepo pois;
    private final Repos.ReviewRepo reviews;
    private final PoiService poiService;
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    ReviewService(Repos.PoiRepo pois, Repos.ReviewRepo reviews, PoiService poiService) {
        this.pois = pois;
        this.reviews = reviews;
        this.poiService = poiService;
    }

    /** T4.2 写评价：防刷裁决 → 状态判定 → 落库（唯一键兜底）→ 重算均值 */
    @Transactional
    public Map<String, Object> create(long userId, long poiId, int friendly, int env, int service,
                                      String content, String deviceKey, long accountAgeDays,
                                      List<String> imageHashes) {
        pois.findById(poiId).orElseThrow(() -> new NotFoundException("场所不存在"));
        if (!ReviewRules.threeScoresValid(friendly, env, service)) {
            throw new IllegalArgumentException("三维评分需 1–5");
        }
        if (ReviewRules.imageDuplicate(imageHashes)) {
            throw new IllegalArgumentException("评价图片重复（决议）");
        }
        LocalDate today = LocalDate.now();
        String dayKey = ReviewRules.dayKey(userId, poiId, today);
        long sameUserToday = reviews.countByPoiIdAndUserIdAndDayKey(poiId, userId, dayKey);
        long sameDeviceToday = deviceKey == null || deviceKey.isBlank() ? 0
                : reviews.countByDeviceKeyAndDayKeyEndingWith(deviceKey, today.toString());
        SpamVerdict verdict = ReviewRules.spamCheck(sameUserToday, sameDeviceToday,
                new int[] { friendly, env, service }, content);
        if (verdict == SpamVerdict.DUPLICATE_DAY) {
            throw new IllegalStateException("同一场所每日仅可评价一次（防刷决议）");
        }
        if (verdict == SpamVerdict.DEVICE_LIMIT) {
            throw new IllegalStateException("本设备今日评价次数已达上限（防刷决议）");
        }
        ReviewRules.State state = ReviewRules.stateFor(accountAgeDays, verdict);

        PoiReviewEntity row = new PoiReviewEntity();
        row.poiId = poiId;
        row.userId = userId;
        row.scoreFriendly = friendly;
        row.scoreEnv = env;
        row.scoreService = service;
        row.content = content == null ? "" : content;
        row.state = state.name();
        row.deviceKey = deviceKey;
        row.dayKey = dayKey;
        row.createdAt = LocalDateTime.now();
        reviews.save(row);
        poiService.recomputeAvg(poiId); // U81：状态变化即重算
        log.info("[review] created id={} state={} verdict={}", row.id, row.state, verdict);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.id);
        out.put("state", row.state);
        out.put("verdict", verdict.name());
        out.put("countsInAvg", ReviewRules.countsInAvg(state));
        return out;
    }

    public List<Map<String, Object>> list(long poiId, Long viewerId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (PoiReviewEntity r : reviews.findByPoiIdOrderByCreatedAtDesc(poiId)) {
            ReviewRules.State state = ReviewRules.State.valueOf(r.state);
            if (viewerId != null && viewerId.equals(r.userId)) {
                // 本人可见自己的（PENDING 展示"审核中"）
                out.add(toItem(r));
            } else if (ReviewRules.visibleToOthers(state)) {
                out.add(toItem(r));
            }
        }
        return out;
    }

    private Map<String, Object> toItem(PoiReviewEntity r) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", r.id);
        item.put("userId", r.userId);
        item.put("avg", ReviewRules.avg(r.scoreFriendly, r.scoreEnv, r.scoreService));
        item.put("content", r.content);
        item.put("state", r.state);
        item.put("createdAt", r.createdAt == null ? "" : r.createdAt.toString());
        return item;
    }

    @Transactional
    public Map<String, Object> adminHide(long reviewId, boolean restore) {
        PoiReviewEntity r = reviews.findById(reviewId).orElseThrow(() -> new NotFoundException("评价不存在"));
        r.state = restore ? ReviewRules.State.VISIBLE.name() : ReviewRules.State.HIDDEN.name();
        reviews.save(r);
        poiService.recomputeAvg(r.poiId); // 隐藏/恢复后重算（U81）
        return Map.of("id", r.id, "state", r.state, "recomputed", true);
    }
}

@Service
class RouteService {

    private final Repos.RouteRepo routes;
    private final Repos.FavoriteRouteRepo favorites;
    private final Map<Long, Long> likeCounts = new java.util.concurrent.ConcurrentHashMap<>();

    RouteService(Repos.RouteRepo routes, Repos.FavoriteRouteRepo favorites) {
        this.routes = routes;
        this.favorites = favorites;
    }

    @Transactional
    public Map<String, Object> create(long userId, String city, String name, String points,
                                      int durationMin, String difficulty, String note) {
        double distanceKm = RouteRules.totalDistanceKm(points); // 解析校验 + 距离（U21）
        UgcRouteEntity row = new UgcRouteEntity();
        row.userId = userId;
        row.city = city;
        row.name = name;
        row.points = points;
        row.distanceM = (int) Math.round(distanceKm * 1000);
        row.durationMin = durationMin;
        row.difficulty = difficulty == null ? "简单" : difficulty;
        row.note = note == null ? "" : note;
        row.state = "PENDING"; // 先审后发
        row.createdAt = LocalDateTime.now();
        routes.save(row);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.id);
        out.put("state", "PENDING");
        out.put("distanceKm", distanceKm);
        return out;
    }

    public List<UgcRouteEntity> list(String city) {
        return city == null || city.isBlank() ? routes.findByStateOrderByCreatedAtDesc("PUBLISHED")
                : routes.findByCityAndState(city, "PUBLISHED");
    }

    public UgcRouteEntity detail(long id) {
        UgcRouteEntity r = routes.findById(id).orElseThrow(() -> new NotFoundException("路线不存在"));
        if (!RouteRules.feedVisible(RouteRules.State.valueOf(r.state))) {
            throw new NotFoundException("路线未发布");
        }
        return r;
    }

    @Transactional
    public Map<String, Object> like(long userId, long routeId) {
        detail(routeId);
        long count = likeCounts.merge(routeId, 1L, Long::sum); // 本地计数；持久化随 likes 表接入（本地阶段）
        return Map.of("routeId", routeId, "liked", true, "likes", count);
    }

    @Transactional
    public Map<String, Object> favorite(long userId, long routeId, boolean want) {
        detail(routeId);
        var key = new FavoriteRouteKey(userId, routeId);
        if (want && !favorites.existsById(key)) {
            FavoriteRouteEntity row = new FavoriteRouteEntity();
            row.userId = userId;
            row.routeId = routeId;
            row.createdAt = LocalDateTime.now();
            favorites.save(row);
        } else if (!want && favorites.existsById(key)) {
            favorites.deleteById(key);
        }
        return Map.of("routeId", routeId, "favorited", want);
    }

    @Transactional
    public Map<String, Object> adminReview(long routeId, boolean approve, String note) {
        UgcRouteEntity r = routes.findById(routeId).orElseThrow(() -> new NotFoundException("路线不存在"));
        RouteRules.State current = RouteRules.State.valueOf(r.state);
        RouteRules.State target = approve ? RouteRules.State.PUBLISHED : RouteRules.State.REJECTED;
        if (current == RouteRules.State.PUBLISHED) {
            throw new IllegalStateException("已发布路线不可再审（下线走治理）");
        }
        if (!RouteRules.transition(current, target)) {
            throw new IllegalStateException("路线状态不可流转: " + current + " → " + target); // 驳回件须先重提再审
        }
        if (!approve && (note == null || note.isBlank())) {
            throw new IllegalArgumentException("驳回必附意见");
        }
        r.state = target.name();
        r.reviewNote = note;
        routes.save(r);
        return Map.of("id", r.id, "state", r.state);
    }
}

@Service
class ActivityService {

    private final Repos.ActivityRepo activities;
    private final Repos.SignupRepo signups;
    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    ActivityService(Repos.ActivityRepo activities, Repos.SignupRepo signups) {
        this.activities = activities;
        this.signups = signups;
    }

    public List<ActivityEntity> list(String city) {
        return city == null || city.isBlank() ? activities.findByStateOrderByBeginsAtAsc("PUBLISHED")
                : activities.findByCityAndState(city, "PUBLISHED");
    }

    public ActivityEntity detail(long id) {
        return activities.findById(id).orElseThrow(() -> new NotFoundException("活动不存在"));
    }

    /** T4.5/U68–69 报名：窗口/名额/重复/同意 全判定后落库 */
    @Transactional
    public Map<String, Object> signup(long userId, long activityId, String name, String phone,
                                      String consentVersion, String consentAt) {
        ActivityEntity a = detail(activityId);
        State state = State.valueOf(a.state);
        if (state == State.ENDED || a.endsAt.isBefore(LocalDateTime.now())) {
            state = State.ENDED;
        }
        LocalDate now = LocalDate.now();
        int active = (int) signups.countByActivityIdAndState(activityId, "ACTIVE");
        boolean userActive = signups.existsByActivityIdAndUserIdAndState(activityId, userId, "ACTIVE");
        SignupDeny deny = ActivityRules.denyReason(state,
                a.beginsAt.toLocalDate(),
                a.signupDeadline == null ? null : a.signupDeadline.toLocalDate(),
                state == State.CHANGED, a.quota, active, userActive,
                ActivityRules.consentValid(consentVersion, consentAt), now);
        if (deny != null) {
            throw new IllegalArgumentException("报名被拒: " + deny); // U68/U69 拒因透传
        }
        ActivitySignupEntity row = new ActivitySignupEntity();
        row.activityId = activityId;
        row.userId = userId;
        row.name = name;
        row.phone = phone;
        row.state = "ACTIVE";
        row.consentVersion = consentVersion;
        row.consentAt = consentAt;
        row.createdAt = LocalDateTime.now();
        signups.save(row);
        // 并发兜底：保存后复核名额（DB 级唯一/锁在本地阶段补强；超限即回滚抛错）
        long after = signups.countByActivityIdAndState(activityId, "ACTIVE");
        if (ActivityRules.decide(a.quota, (int) after) == ActivityRules.SignupOutcome.DENIED) {
            row.state = "CANCELLED";
            signups.save(row);
            throw new IllegalStateException("名额已被他人抢占（并发回滚）");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("signupId", row.id);
        out.put("state", "ACTIVE");
        out.put("consentRecorded", true);
        out.put("activeCount", after);
        return out;
    }

    @Transactional
    public Map<String, Object> cancelSignup(long userId, long activityId) {
        ActivitySignupEntity row = signups.findByActivityIdAndUserIdAndState(activityId, userId, "ACTIVE")
                .stream().findFirst().orElseThrow(() -> new NotFoundException("无有效报名"));
        row.state = "CANCELLED"; // 名额释放；重报名=新历史行（U68）
        signups.save(row);
        return Map.of("signupId", row.id, "state", "CANCELLED", "reSignupAllowed",
                ActivityRules.allowReSignupAfterCancel(false));
    }

    public List<ActivitySignupEntity> signupsOf(long activityId, long actorId, boolean hasExportPermission) {
        ActivityEntity a = detail(activityId);
        if (!ActivityRules.canViewSignups(actorId, a.orgUserId, hasExportPermission)) {
            throw new ForbiddenException("仅组织者可查看名单（U72）");
        }
        return signups.findByActivityIdAndState(activityId, "ACTIVE");
    }

    public Map<String, Object> exportSignups(long activityId, long actorId, boolean hasExportPermission) {
        ActivityEntity a = detail(activityId);
        if (!ActivityRules.canExportSignups(a.orgUserId, actorId, hasExportPermission)) {
            throw new ForbiddenException("导出需组织者身份 + activity.export 权限 + 审计（U72）");
        }
        List<ActivitySignupEntity> rows = signups.findByActivityIdAndState(activityId, "ACTIVE");
        return Map.of("activityId", activityId, "count", rows.size(),
                "audit", "SIGNUP_EXPORT_AUDIT", "rows", rows.stream()
                        .map(r -> Map.of("userId", r.userId, "name", r.name, "phone", r.phone)).toList());
    }

    /** T4.6/U70 修改（有报名时改动 → CHANGED 暂停新报名 + 核实中标注） */
    @Transactional
    public Map<String, Object> edit(long activityId, long actorId, String address, LocalDateTime beginsAt) {
        ActivityEntity a = detail(activityId);
        if (!a.orgUserId.equals(actorId)) {
            throw new ForbiddenException("仅组织者可编辑");
        }
        if (ActivityRules.normalEditBlocked(State.valueOf(a.state))) {
            throw new IllegalStateException("活动已取消，普通编辑不可恢复（U71）");
        }
        boolean hasSignups = signups.countByActivityIdAndState(activityId, "ACTIVE") > 0;
        if (hasSignups) {
            a.state = State.CHANGED.name();
            a.changeNote = "信息核实中（变更待审核）"; // U70 旧信息标注
        }
        if (address != null) {
            a.address = address;
        }
        if (beginsAt != null) {
            a.beginsAt = beginsAt;
        }
        activities.save(a);
        return Map.of("id", a.id, "state", a.state, "signupsPaused",
                ActivityRules.pauseSignupsOnEdit(State.valueOf(a.state)));
    }

    /** U70 变更审核：通过回发布并通知；驳回丢弃变更回发布（旧信息保留） */
    @Transactional
    public Map<String, Object> reviewChange(long activityId, boolean approved) {
        ActivityEntity a = activities.findById(activityId).orElseThrow(() -> new NotFoundException("活动不存在"));
        if (!State.CHANGED.name().equals(a.state)) {
            throw new IllegalStateException("非变更审核态");
        }
        String result = ActivityRules.changeReviewResult(approved);
        a.state = "PUBLISHED"; // 通过=生效；驳回=变更丢弃回到发布态
        if (!approved) {
            a.changeNote = "变更被驳回，保留原信息";
        } else {
            a.changeNote = "变更已通过核实";
        }
        activities.save(a);
        return Map.of("id", a.id, "state", a.state, "reviewResult", result,
                "notify", approved ? "CHANGE_APPROVED_NOTIFY_SIGNUPS" : "CHANGE_REJECTED_KEPT_OLD");
    }

    /** T4.6/U71 取消活动：有效报名转 ACTIVITY_CANCELLED；失败通知进接手队列 */
    @Transactional
    public Map<String, Object> cancel(long activityId, long actorId) {
        ActivityEntity a = detail(activityId);
        if (!a.orgUserId.equals(actorId)) {
            throw new ForbiddenException("仅组织者/运营可取消");
        }
        a.state = State.CANCELLED.name();
        activities.save(a);
        int affected = 0;
        for (ActivitySignupEntity s : signups.findByActivityIdAndState(activityId, "ACTIVE")) {
            s.state = ActivityRules.signupStateAfterActivityCancelled();
            signups.save(s);
            affected++;
        }
        log.info("[activity] cancelled id={} signups={} recoveryQueue={}", activityId, affected,
                ActivityRules.failedNotifyGoesToRecoveryQueue());
        return Map.of("id", activityId, "state", "CANCELLED", "signupsAffected", affected,
                "notifyRecoveryQueue", ActivityRules.failedNotifyGoesToRecoveryQueue(),
                "normalEditBlocked", ActivityRules.normalEditBlocked(State.CANCELLED));
    }

    @Transactional
    public Map<String, Object> adminPublish(long activityId) {
        ActivityEntity a = activities.findById(activityId).orElseThrow(() -> new NotFoundException("活动不存在"));
        if (!ActivityRules.transition(State.valueOf(a.state), State.PUBLISHED) && !State.CHANGED.name().equals(a.state)) {
            throw new IllegalStateException("状态不可流转: " + a.state);
        }
        a.state = State.PUBLISHED.name();
        activities.save(a);
        return Map.of("id", a.id, "state", a.state);
    }

    @Transactional
    public Map<String, Object> adminEnd(long activityId) {
        ActivityEntity a = activities.findById(activityId).orElseThrow(() -> new NotFoundException("活动不存在"));
        a.state = State.ENDED.name();
        activities.save(a);
        return Map.of("id", a.id, "state", "ENDED");
    }

    /** U86 报名信息到期清理（每日扫描：结束 +90 天） */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public int cleanupExpiredSignups() {
        LocalDate today = LocalDate.now();
        int cleaned = 0;
        for (ActivityEntity a : activities.findAll()) {
            if (a.endsAt == null || !ActivityRules.cleanupDue(a.endsAt.toLocalDate(), today)) {
                continue;
            }
            for (ActivitySignupEntity s : signups.findByActivityIdAndState(a.id, "ACTIVE")) {
                // 平台字段脱敏清除（导出副本失效 + 通知组织者删除 —— 动作清单见 domain）
                s.name = "";
                s.phone = "";
                signups.save(s);
                cleaned++;
            }
            if (cleaned > 0) {
                log.info("[retention] activity={} cleaned actions={}", a.id, ActivityRules.cleanupActions());
            }
        }
        return cleaned;
    }
}

@Service
class OrgAdoptionService {

    private final Repos.OrgCredentialRepo credentials;
    private final Repos.AdoptionRepo adoptions;

    OrgAdoptionService(Repos.OrgCredentialRepo credentials, Repos.AdoptionRepo adoptions) {
        this.credentials = credentials;
        this.adoptions = adoptions;
    }

    private OrgCredentialEntity cred(long userId) {
        return credentials.findByUserId(userId).orElseGet(() -> {
            OrgCredentialEntity c = new OrgCredentialEntity();
            c.userId = userId;
            c.state = OrgState.NONE.name();
            c.createdAt = LocalDateTime.now();
            return c;
        });
    }

    /** U89 机构认证状态流转（材料不公开） */
    @Transactional
    public Map<String, Object> orgApply(long userId, String materialPath) {
        OrgCredentialEntity c = cred(userId);
        OrgState next = ActivityRules.orgTransition(OrgState.valueOf(c.state), "apply", c.expiresAt, LocalDate.now());
        c.state = next.name();
        c.materialPath = materialPath == null ? "" : materialPath;
        c.updatedAt = LocalDateTime.now();
        credentials.save(c);
        return Map.of("userId", userId, "state", c.state, "materialPublic", ActivityRules.credentialMaterialPublic());
    }

    @Transactional
    public Map<String, Object> orgReview(long userId, String action, Long reviewerId, LocalDate expiresAt) {
        OrgCredentialEntity c = credentials.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("无认证记录"));
        OrgState next = ActivityRules.orgTransition(OrgState.valueOf(c.state), action, expiresAt, LocalDate.now());
        if (next == OrgState.valueOf(c.state)) {
            throw new IllegalStateException("认证状态不可按 " + action + " 流转: " + c.state);
        }
        c.state = next.name();
        c.expiresAt = expiresAt != null ? expiresAt : c.expiresAt;
        c.reviewedBy = reviewerId;
        c.updatedAt = LocalDateTime.now();
        credentials.save(c);
        return Map.of("userId", userId, "state", c.state,
                "directPublish", ActivityRules.orgDirectPublish(next));
    }

    public OrgState orgStateOf(long userId) {
        return credentials.findByUserId(userId)
                .map(c -> OrgState.valueOf(c.state)).orElse(OrgState.NONE);
    }

    @Transactional
    public Map<String, Object> adoptionCreate(long userId, String city, String title, String content,
                                              String contact, LocalDate expireOn) {
        AdoptionEntity row = new AdoptionEntity();
        row.orgUserId = userId;
        row.city = city;
        row.title = title;
        row.content = content;
        row.contact = contact;
        row.expireOn = expireOn;
        row.state = "PENDING"; // 领养始终人工审（U89）
        row.createdAt = LocalDateTime.now();
        adoptions.save(row);
        return Map.of("id", row.id, "state", "PENDING", "manualReview", ActivityRules.adoptionAlwaysManualReview());
    }

    public List<Map<String, Object>> adoptionsVisible(String city, LocalDate today) {
        List<AdoptionEntity> pool = city == null || city.isBlank() ? adoptions.findAllByOrderByCreatedAtDesc()
                : adoptions.findByCity(city);
        List<Map<String, Object>> out = new ArrayList<>();
        for (AdoptionEntity a : pool) {
            if (ActivityRules.adoptionPublicVisible(a.expireOn, today, a.state)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", a.id);
                item.put("city", a.city);
                item.put("title", a.title);
                item.put("content", a.content);
                item.put("contact", a.contact);
                item.put("expireOn", a.expireOn == null ? null : a.expireOn.toString());
                item.put("hasSignupButton", ActivityRules.adoptionHasSignupButton()); // U90 恒 false
                item.put("hasTradeButton", ActivityRules.adoptionHasTradeButton());   // U90 恒 false
                out.add(item);
            }
        }
        return out;
    }

    @Transactional
    public Map<String, Object> adminPublishAdoption(long id) {
        AdoptionEntity a = adoptions.findById(id).orElseThrow(() -> new NotFoundException("领养不存在"));
        a.state = "PUBLISHED";
        adoptions.save(a);
        return Map.of("id", id, "state", a.state, "manualReview", true);
    }
}
