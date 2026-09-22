package com.cutepet.explore;

import com.cutepet.common.PermissionCatalog;
import com.cutepet.common.web.ForbiddenException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * explore 控制器（契约 /pois/** /routes/** /activities/** /adoptions + admin 探索组）。
 * dev shim：X-User-Id / X-Permissions。
 */
@RestController
class ExploreControllers {

    private final PoiService poiService;
    private final ReviewService reviewService;
    private final RouteService routeService;
    private final ActivityService activityService;
    private final OrgAdoptionService orgAdoptionService;

    ExploreControllers(PoiService poiService, ReviewService reviewService, RouteService routeService,
                       ActivityService activityService, OrgAdoptionService orgAdoptionService) {
        this.poiService = poiService;
        this.reviewService = reviewService;
        this.routeService = routeService;
        this.activityService = activityService;
        this.orgAdoptionService = orgAdoptionService;
    }

    private long userOf(String header) {
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("缺少用户身份（X-User-Id）");
        }
        return Long.parseLong(header.trim());
    }

    private boolean hasPermission(String header, String permission) {
        return PermissionCatalog.has(PermissionCatalog.parseHeader(header), permission);
    }

    private void requirePermission(String header, String permission) {
        if (!hasPermission(header, permission)) {
            throw new ForbiddenException("缺少权限: " + permission);
        }
    }

    // ---------- DTO ----------

    public record ReviewReq(int scoreFriendly, int scoreEnv, int scoreService, String content,
                            String deviceKey, Long accountAgeDays, List<String> imageHashes) {
    }

    public record CorrectionReq(String field, String proposed, String evidence) {
    }

    public record ResolveCorrectionReq(boolean accept, String note, Long handlerId) {
    }

    public record RouteCreateReq(String city, String name, String points, Integer durationMin,
                                 String difficulty, String note) {
    }

    public record RouteReviewReq(boolean approve, String note) {
    }

    public record SignupReq(String name, String phone, String consentVersion, String consentAt) {
    }

    public record ActivityEditReq(String address, LocalDateTime beginsAt) {
    }

    public record ActivityReviewReq(boolean approved) {
    }

    public record PoiCreateReq(String name, String type, String city, Double lng, Double lat,
                               String address, String phone, String openHours, String attrs) {
    }

    public record PoiUpdateReq(String address, String phone, String openHours, String attrs) {
    }

    public record OrgApplyReq(String materialPath) {
    }

    public record OrgReviewReq(String action, Long reviewerId, LocalDate expiresAt) {
    }

    public record AdoptionReq(String city, String title, String content, String contact, LocalDate expireOn) {
    }

    // ---------- POI ----------

    @GetMapping("/pois")
    public Map<String, Object> searchPois(@RequestParam(required = false) Double lat,
                                          @RequestParam(required = false) Double lng,
                                          @RequestParam(required = false) Integer radius,
                                          @RequestParam(required = false) String city,
                                          @RequestParam(required = false) String type) {
        return poiService.search(lat, lng, radius, city, type);
    }

    @GetMapping("/pois/{id}")
    public Map<String, Object> poi(@PathVariable long id) {
        return poiService.detail(id);
    }

    @PostMapping("/pois/{id}/nav-click")
    public Map<String, Object> navClick(@PathVariable long id) {
        return poiService.navClick(id);
    }

    @PostMapping("/pois/{id}/favorite")
    public Map<String, Object> poiFavorite(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                           @PathVariable long id) {
        return poiService.favorite(userOf(uid), id, true);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/pois/{id}/favorite")
    public Map<String, Object> poiUnfavorite(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                             @PathVariable long id) {
        return poiService.favorite(userOf(uid), id, false);
    }

    @PostMapping("/pois/{id}/reviews")
    public Map<String, Object> createReview(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @PathVariable long id, @RequestBody ReviewReq req) {
        long age = req.accountAgeDays() == null ? 100L : req.accountAgeDays();
        return reviewService.create(userOf(uid), id, req.scoreFriendly(), req.scoreEnv(), req.scoreService(),
                req.content(), req.deviceKey(), age, req.imageHashes());
    }

    @GetMapping("/pois/{id}/reviews")
    public List<Map<String, Object>> reviews(@PathVariable long id,
                                             @RequestHeader(value = "X-User-Id", required = false) String viewer) {
        Long viewerId = viewer == null || viewer.isBlank() ? null : Long.parseLong(viewer.trim());
        return reviewService.list(id, viewerId);
    }

    @PostMapping("/pois/{id}/corrections")
    public Map<String, Object> correction(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @PathVariable long id, @RequestBody CorrectionReq req) {
        return poiService.createCorrection(userOf(uid), id, req.field(), req.proposed(), req.evidence());
    }

    // ---------- 路线 ----------

    @GetMapping("/routes")
    public List<UgcRouteEntity> routes(@RequestParam(required = false) String city) {
        return routeService.list(city);
    }

    @PostMapping("/routes")
    public Map<String, Object> createRoute(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                           @RequestBody RouteCreateReq req) {
        return routeService.create(userOf(uid), req.city(), req.name(), req.points(),
                req.durationMin() == null ? 30 : req.durationMin(), req.difficulty(), req.note());
    }

    @GetMapping("/routes/{id}")
    public UgcRouteEntity route(@PathVariable long id) {
        return routeService.detail(id);
    }

    @PostMapping("/routes/{id}/like")
    public Map<String, Object> likeRoute(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                         @PathVariable long id) {
        return routeService.like(userOf(uid), id);
    }

    @PostMapping("/routes/{id}/favorite")
    public Map<String, Object> favoriteRoute(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                             @PathVariable long id) {
        return routeService.favorite(userOf(uid), id, true);
    }

    // ---------- 活动 ----------

    @GetMapping("/activities")
    public List<ActivityEntity> activities(@RequestParam(required = false) String city) {
        return activityService.list(city);
    }

    @GetMapping("/activities/{id}")
    public ActivityEntity activity(@PathVariable long id) {
        return activityService.detail(id);
    }

    @PostMapping("/activities/{id}/signup")
    public Map<String, Object> signup(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                      @PathVariable long id, @RequestBody SignupReq req) {
        return activityService.signup(userOf(uid), id, req.name(), req.phone(),
                req.consentVersion(), req.consentAt());
    }

    @PostMapping("/activities/{id}/signup/cancel")
    public Map<String, Object> cancelSignup(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @PathVariable long id) {
        return activityService.cancelSignup(userOf(uid), id);
    }

    // ---------- 领养 ----------

    @GetMapping("/adoptions")
    public List<Map<String, Object>> adoptions(@RequestParam(required = false) String city) {
        return orgAdoptionService.adoptionsVisible(city, LocalDate.now()); // U90 无报名/交易按钮
    }

    // ---------- 管理侧 ----------

    @PostMapping("/admin/pois")
    public Map<String, Object> adminPoiCreate(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestBody PoiCreateReq req) {
        requirePermission(perms, "poi.create.edit");
        return poiService.adminCreate(req.name(), req.type(), req.city(), req.lng(), req.lat(),
                req.address(), req.phone(), req.openHours(), req.attrs());
    }

    @PatchMapping("/admin/pois/{id}")
    public Map<String, Object> adminPoiUpdate(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody PoiUpdateReq req) {
        requirePermission(perms, "poi.create.edit");
        return poiService.adminUpdate(id, req.address(), req.phone(), req.openHours(), req.attrs());
    }

    @PostMapping("/admin/pois/{id}/close")
    public Map<String, Object> adminPoiClose(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id) {
        requirePermission(perms, "poi.close");
        return poiService.adminClose(id);
    }

    @GetMapping("/admin/corrections")
    public List<CorrectionEntity> adminCorrections(
            @RequestHeader(value = "X-Permissions", required = false) String perms) {
        requirePermission(perms, "correction.handle");
        return poiService.openCorrections();
    }

    @PostMapping("/admin/corrections/{id}/resolve")
    public Map<String, Object> adminResolveCorrection(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id, @RequestBody ResolveCorrectionReq req) {
        requirePermission(perms, "correction.handle");
        return poiService.resolveCorrection(id, userOf(uid), req.accept(), req.note());
    }

    @PostMapping("/admin/reviews/{id}/hide")
    public Map<String, Object> adminHideReview(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestParam(required = false, defaultValue = "false") boolean restore) {
        requirePermission(perms, "ugv.review.hide");
        return reviewService.adminHide(id, restore);
    }

    @PostMapping("/admin/routes/{id}/review")
    public Map<String, Object> adminRouteReview(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody RouteReviewReq req) {
        requirePermission(perms, "route.approve");
        return routeService.adminReview(id, req.approve(), req.note());
    }

    @PostMapping("/admin/activities/{id}/review")
    public Map<String, Object> adminActivityReview(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody ActivityReviewReq req) {
        requirePermission(perms, "activity.manage");
        if (req.approved()) {
            return activityService.adminPublish(id);
        }
        return activityService.reviewChange(id, false); // 驳回=变更丢弃（U70）
    }

    @GetMapping("/admin/activities/{id}/signups/export")
    public Map<String, Object> adminExportSignups(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id) {
        requirePermission(perms, "activity.export"); // U72 双条件由 service 再判组织者
        return activityService.exportSignups(id, userOf(uid), hasPermission(perms, "activity.export"));
    }

    // ---------- 机构认证（U89） ----------

    @PostMapping("/org/credentials/apply")
    public Map<String, Object> orgApply(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                        @RequestBody OrgApplyReq req) {
        return orgAdoptionService.orgApply(userOf(uid), req.materialPath());
    }

    @PostMapping("/admin/org-credentials/{userId}/review")
    public Map<String, Object> orgReview(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long userId, @RequestBody OrgReviewReq req) {
        requirePermission(perms, "activity.manage");
        return orgAdoptionService.orgReview(userId, req.action(), userOf(uid), req.expiresAt());
    }
}
