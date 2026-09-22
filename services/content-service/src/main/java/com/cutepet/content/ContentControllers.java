package com.cutepet.content;

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

import java.util.List;
import java.util.Map;

/**
 * content 控制器（契约 /articles/** /channels /search /topics /specials /comments /authors /
 * /me/submissions /me/favorites /admin/submissions|articles|comments|taxonomy|banners）。
 * dev shim：X-User-Id / X-Permissions。
 */
@RestController
class ContentControllers {

    private final ArticleService articleService;
    private final SubmissionService submissionService;
    private final AdminReviewService reviewService;
    private final InteractionService interactionService;

    ContentControllers(ArticleService articleService, SubmissionService submissionService,
                       AdminReviewService reviewService, InteractionService interactionService) {
        this.articleService = articleService;
        this.submissionService = submissionService;
        this.reviewService = reviewService;
        this.interactionService = interactionService;
    }

    private long userOf(String header) {
        if (header == null || header.isBlank()) {
            throw new IllegalArgumentException("缺少用户身份（X-User-Id）");
        }
        return Long.parseLong(header.trim());
    }

    private void requirePermission(String header, String permission) {
        if (!PermissionCatalog.has(PermissionCatalog.parseHeader(header), permission)) {
            throw new ForbiddenException("缺少权限: " + permission);
        }
    }

    // ---------- 公开 ----------

    public record CommentReq(Long parentId, String content) {
    }

    public record SubmissionReq(String kind, String title, String body, String channel, String cover, String tags) {
    }

    public record SubmissionPatchReq(String title, String body, String cover, String tags) {
    }

    public record RejectReq(String note) {
    }

    public record TakedownReq(boolean governance, String reason) {
    }

    public record DirectPublishReq(String channel, boolean requireReviewer) {
    }

    public record ArticleUpdateReq(String title, String body) {
    }

    public record PinReq(boolean top) {
    }

    public record TagReq(String name) {
    }

    public record BannerReq(String slot, String title, String targetUrl) {
    }

    @GetMapping("/channels")
    public List<Map<String, Object>> channels() {
        return articleService.channelsList();
    }

    @GetMapping("/articles")
    public Map<String, Object> feed(@RequestParam(required = false) String channel,
                                    @RequestParam(required = false, defaultValue = "1") int page,
                                    @RequestParam(required = false, defaultValue = "12") int size) {
        return articleService.feed(channel, page, size);
    }

    @GetMapping("/articles/{slug}")
    public Map<String, Object> article(@PathVariable String slug) {
        return articleService.detail(slug);
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String q,
                                      @RequestParam(required = false, defaultValue = "20") int limit) {
        return articleService.search(q, limit);
    }

    @GetMapping("/topics")
    public List<Map<String, Object>> topics() {
        return articleService.topicsList();
    }

    @GetMapping("/specials/{id}")
    public Map<String, Object> special(@PathVariable long id) {
        return articleService.special(id);
    }

    @GetMapping("/authors/{id}")
    public Map<String, Object> author(@PathVariable long id) {
        return interactionService.author(id);
    }

    @GetMapping("/articles/{id}/comments")
    public List<Map<String, Object>> comments(@PathVariable long id,
                                              @RequestHeader(value = "X-User-Id", required = false) String viewer) {
        Long viewerId = viewer == null || viewer.isBlank() ? null : Long.parseLong(viewer.trim());
        return interactionService.listComments(id, viewerId);
    }

    @GetMapping("/banners")
    public List<Map<String, Object>> banners(@RequestParam String slot) {
        return articleService.banners(slot);
    }

    // ---------- 用户 ----------

    @PostMapping("/articles/{id}/like")
    public Map<String, Object> like(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                    @PathVariable long id) {
        return interactionService.like(userOf(uid), "ARTICLE", id, true);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/articles/{id}/like")
    public Map<String, Object> unlike(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                      @PathVariable long id) {
        return interactionService.like(userOf(uid), "ARTICLE", id, false);
    }

    @PostMapping("/articles/{id}/favorite")
    public Map<String, Object> favorite(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                        @PathVariable long id) {
        return interactionService.favorite(userOf(uid), "内容", "ARTICLE", id, true);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/articles/{id}/favorite")
    public Map<String, Object> unfavorite(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @PathVariable long id) {
        return interactionService.favorite(userOf(uid), "内容", "ARTICLE", id, false);
    }

    @PostMapping("/articles/{id}/comments")
    public Map<String, Object> createComment(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                             @PathVariable long id, @RequestBody CommentReq req) {
        return interactionService.comment(userOf(uid), id, req.parentId(), req.content());
    }

    @PostMapping("/comments/{id}/like")
    public Map<String, Object> likeComment(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                           @PathVariable long id) {
        return interactionService.like(userOf(uid), "COMMENT", id, true);
    }

    @PostMapping("/authors/{id}/follow")
    public Map<String, Object> follow(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                      @PathVariable long id) {
        return interactionService.follow(userOf(uid), id, true);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/authors/{id}/follow")
    public Map<String, Object> unfollow(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                        @PathVariable long id) {
        return interactionService.follow(userOf(uid), id, false);
    }

    @GetMapping("/me/favorites")
    public List<FavoriteEntity> myFavorites(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                            @RequestParam(required = false, defaultValue = "内容") String tab) {
        return interactionService.myFavorites(userOf(uid), tab);
    }

    @PostMapping("/me/submissions")
    public Map<String, Object> createSubmission(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                                @RequestBody SubmissionReq req) {
        return submissionService.create(userOf(uid), req.kind(), req.title(), req.body(),
                req.channel(), req.cover(), req.tags());
    }

    @PatchMapping("/me/submissions/{id}")
    public Map<String, Object> patchSubmission(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                               @PathVariable long id, @RequestBody SubmissionPatchReq req) {
        return submissionService.update(id, userOf(uid), req.title(), req.body(), req.cover(), req.tags());
    }

    @PostMapping("/me/submissions/{id}/submit")
    public Map<String, Object> submit(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                      @PathVariable long id) {
        return submissionService.submit(id, userOf(uid));
    }

    @PostMapping("/me/submissions/{id}/withdraw")
    public Map<String, Object> withdraw(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                        @PathVariable long id) {
        return submissionService.withdraw(id, userOf(uid));
    }

    @GetMapping("/me/submissions")
    public List<Map<String, Object>> mySubmissions(
            @RequestHeader(value = "X-User-Id", required = false) String uid) {
        return submissionService.mine(userOf(uid));
    }

    // ---------- 管理 ----------

    @GetMapping("/admin/submissions")
    public List<ArticleVersionEntity> adminQueue(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestParam(required = false, defaultValue = "PENDING") String state) {
        requirePermission(perms, "review.article");
        return reviewService.queue(state);
    }

    @PostMapping("/admin/submissions/{id}/claim")
    public Map<String, Object> adminClaim(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id) {
        requirePermission(perms, "review.article");
        return reviewService.claim(id, userOf(uid));
    }

    @PostMapping("/admin/submissions/{id}/approve")
    public Map<String, Object> adminApprove(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id) {
        requirePermission(perms, "review.article");
        return reviewService.approve(id, userOf(uid), perms);
    }

    @PostMapping("/admin/submissions/{id}/reject")
    public Map<String, Object> adminReject(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id, @RequestBody RejectReq req) {
        requirePermission(perms, "review.article");
        return reviewService.reject(id, userOf(uid), req.note());
    }

    @PostMapping("/admin/articles")
    public Map<String, Object> adminCreateArticle() {
        return Map.of("note", "PGC 直发文由发布接口承载（编辑创建→publish.direct）");
    }

    @PostMapping("/admin/articles/{id}/publish")
    public Map<String, Object> adminPublish(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id, @RequestBody DirectPublishReq req) {
        requirePermission(perms, "article.publish.direct"); // U94：编辑不继承直发权
        return reviewService.publishDirect(id, perms, userOf(uid), req.channel(), req.requireReviewer());
    }

    @PatchMapping("/admin/articles/{id}")
    public Map<String, Object> adminUpdateArticle(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody ArticleUpdateReq req) {
        requirePermission(perms, "article.edit.any");
        return reviewService.updateLive(id, req.title(), req.body());
    }

    @PostMapping("/admin/articles/{id}/takedown")
    public Map<String, Object> adminTakedown(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id, @RequestBody TakedownReq req) {
        requirePermission(perms, "article.takedown");
        return reviewService.takedown(id, userOf(uid), req.governance(), req.reason());
    }

    @PostMapping("/admin/articles/{id}/pin")
    public Map<String, Object> adminPin(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody PinReq req) {
        requirePermission(perms, "article.pinned.schedule");
        return reviewService.pin(id, req.top());
    }

    @PostMapping("/admin/comments/{id}/delete")
    public Map<String, Object> adminDeleteComment(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @PathVariable long id) {
        requirePermission(perms, "comment.manage");
        return reviewService.deleteComment(id, perms, userOf(uid));
    }

    @PostMapping("/admin/taxonomy")
    public Map<String, Object> adminTaxonomy(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestBody TagReq req) {
        requirePermission(perms, "taxonomy.manage");
        return reviewService.upsertTag(req.name());
    }

    @PostMapping("/admin/banners")
    public Map<String, Object> adminBanner(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestBody BannerReq req) {
        requirePermission(perms, "banner.manage");
        return Map.of("slot", req.slot(), "title", req.title(),
                "note", "落库经 banners 表（V2 迁移），排期字段随编辑表单扩展");
    }
}
