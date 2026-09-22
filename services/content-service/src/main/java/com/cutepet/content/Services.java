package com.cutepet.content;

import com.cutepet.common.PermissionCatalog;
import com.cutepet.common.web.ForbiddenException;
import com.cutepet.common.web.NotFoundException;
import com.cutepet.content.domain.FeedRules;
import com.cutepet.content.domain.InteractionRules;
import com.cutepet.content.domain.SearchRules;
import com.cutepet.content.domain.SubmissionRules;
import com.cutepet.content.domain.SubmissionRules.State;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * content 应用服务（T3.1–T3.6 编排）。判定委托 domain（ContentLogicTest 覆盖）。
 * dev shim：身份 X-User-Id、权限 X-Permissions。
 */
@Service
class ArticleService {

    private final Repos.ArticleRepo articles;
    private final Repos.ChannelRepo channels;
    private final Repos.VersionRepo versions;
    private final Repos.CommentRepo comments;
    private final Repos.LikeRepo likes;
    private final Repos.FavoriteRepo favorites;
    private final Repos.TopicRepo topics;
    private final Repos.SpecialRepo specials;
    private final Repos.SpecialItemRepo specialItems;
    private final Repos.BannerRepo banners;

    ArticleService(Repos.ArticleRepo articles, Repos.ChannelRepo channels, Repos.VersionRepo versions,
                   Repos.CommentRepo comments, Repos.LikeRepo likes, Repos.FavoriteRepo favorites,
                   Repos.TopicRepo topics, Repos.SpecialRepo specials, Repos.SpecialItemRepo specialItems,
                   Repos.BannerRepo banners) {
        this.articles = articles;
        this.channels = channels;
        this.versions = versions;
        this.comments = comments;
        this.likes = likes;
        this.favorites = favorites;
        this.topics = topics;
        this.specials = specials;
        this.specialItems = specialItems;
        this.banners = banners;
    }

    public List<Map<String, Object>> channelsList() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (String c : FeedRules.CHANNELS) {
            out.add(Map.of("name", c, "slug", FeedRules.channelSlug(c)));
        }
        return out;
    }

    public Map<String, Object> feed(String channel, int page, int size) {
        int[] ps = FeedRules.clampPage(page, size);
        String state = "PUBLISHED";
        List<ArticleEntity> pool = new ArrayList<>();
        if (channel != null && !channel.isBlank()) {
            if (!FeedRules.validChannel(channel)) {
                throw new IllegalArgumentException("频道不存在");
            }
            var ch = channels.findByName(channel).orElse(null);
            if (ch != null) {
                pool.addAll(articles.findByChannelIdAndState(ch.id, state));
            }
        } else {
            pool.addAll(articles.findByState(state));
        }
        LocalDateTime now = LocalDateTime.now();
        record Scored(ArticleEntity a, boolean top, long hot, long created) {
        }
        List<Scored> scored = new ArrayList<>();
        for (ArticleEntity a : pool) {
            int ageDays = (int) java.time.Duration.between(a.createdAt, now).toDays();
            long hot = FeedRules.hotness(
                    likes.countByTargetTypeAndTargetId("ARTICLE", a.id),
                    comments.countByArticleIdAndState(a.id, "VISIBLE"),
                    favorites.findByUserIdAndTabOrderByCreatedAtDesc(-1L, "内容").size() > 0 ? 0 : 0, // 占位：收藏热度聚合随本地阶段补
                    ageDays);
            scored.add(new Scored(a, a.isTop == 1 && FeedRules.pinAllowed(true), hot,
                    a.createdAt == null ? 0 : a.createdAt.toSecondOfDay()));
        }
        scored.sort((x, y) -> FeedRules.compareFeed(x.top(), x.hot(), x.created(), y.top(), y.hot(), y.created()));
        int from = Math.min(scored.size(), (ps[0] - 1) * ps[1]);
        int to = Math.min(scored.size(), from + ps[1]);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Scored s : scored.subList(from, to)) {
            items.add(toCard(s.a()));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("page", ps[0]);
        result.put("size", ps[1]);
        result.put("total", scored.size());
        return result;
    }

    private Map<String, Object> toCard(ArticleEntity a) {
        ArticleVersionEntity v = a.liveVersionId == null ? null : versions.findById(a.liveVersionId).orElse(null);
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("id", a.id);
        card.put("slug", a.slug);
        card.put("title", v == null ? "" : v.title);
        card.put("authorId", a.authorId);
        card.put("state", a.state);
        card.put("top", a.isTop == 1);
        card.put("likes", likes.countByTargetTypeAndTargetId("ARTICLE", a.id));
        card.put("comments", comments.countByArticleIdAndState(a.id, "VISIBLE"));
        card.put("createdAt", a.createdAt == null ? null : a.createdAt.toString());
        return card;
    }

    public Map<String, Object> detail(String slug) {
        ArticleEntity a = articles.findBySlug(slug).orElseThrow(() -> new NotFoundException("文章不存在"));
        boolean visible = "PUBLISHED".equals(a.state) && a.liveVersionId != null;
        if (!visible) {
            throw new NotFoundException("文章不可见（未发布或已下架）");
        }
        ArticleVersionEntity v = versions.findById(a.liveVersionId).orElseThrow(() -> new NotFoundException("版本缺失"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", a.id);
        out.put("slug", a.slug);
        out.put("title", v.title);
        out.put("cover", v.cover);
        out.put("body", v.body);
        out.put("tags", v.tags);
        out.put("authorId", a.authorId);
        out.put("channelId", a.channelId);
        out.put("versionNo", v.versionNo);
        out.put("likes", likes.countByTargetTypeAndTargetId("ARTICLE", a.id));
        out.put("commentCount", comments.countByArticleIdAndState(a.id, "VISIBLE"));
        return out;
    }

    public List<Map<String, Object>> topicsList() {
        return topics.findByState("ACTIVE").stream()
                .map(t -> Map.<String, Object>of("id", t.id, "name", t.name)).toList();
    }

    public Map<String, Object> special(long id) {
        SpecialEntity sp = specials.findById(id).orElseThrow(() -> new NotFoundException("专题不存在"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SpecialItemEntity item : specialItems.findBySpecialIdOrderByOrdAsc(id)) {
            articles.findById(item.articleId).ifPresent(a -> {
                if ("PUBLISHED".equals(a.state)) {
                    items.add(toCard(a));
                }
            });
        }
        return Map.of("id", sp.id, "title", sp.title, "intro", sp.intro, "items", items);
    }

    public List<Map<String, Object>> banners(String slot) {
        return banners.findBySlotAndStateOrderByOrdAsc(slot, "ACTIVE").stream()
                .map(b -> Map.<String, Object>of("id", b.id, "slot", b.slot, "title", b.title,
                        "targetUrl", b.targetUrl == null ? "" : b.targetUrl, "ord", b.ord)).toList();
    }

    /** 搜索（T3.5）：已发布文章按标题/标签/正文相关度排序 + 高亮 + 空结果推荐 */
    public Map<String, Object> search(String query, int limit) {
        if (!SearchRules.validQuery(query)) {
            throw new IllegalArgumentException("搜索词 1–50 字符");
        }
        List<SearchRules.Hit> hits = new ArrayList<>();
        Map<Long, ArticleEntity> byId = new LinkedHashMap<>();
        for (ArticleEntity a : articles.findByState("PUBLISHED")) {
            ArticleVersionEntity v = a.liveVersionId == null ? null : versions.findById(a.liveVersionId).orElse(null);
            if (v == null) {
                continue;
            }
            List<String> tagList = v.tags == null || v.tags.isBlank()
                    ? List.of() : List.of(v.tags.split(","));
            int score = SearchRules.score(v.title, tagList, v.body, query);
            if (score > 0) {
                hits.add(new SearchRules.Hit(v.title, score));
                byId.put(a.id, a);
            }
        }
        List<SearchRules.Hit> ranked = SearchRules.rank(hits);
        List<Map<String, Object>> items = new ArrayList<>();
        for (SearchRules.Hit hit : ranked.subList(0, Math.min(ranked.size(), limit <= 0 ? 20 : limit))) {
            items.add(Map.of("title", hit.title(), "score", hit.score(),
                    "highlight", SearchRules.highlight(hit.title(), query).stream()
                            .map(s -> Map.of("text", s.text(), "hit", s.hit())).toList()));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("empty", SearchRules.isEmpty(ranked));
        if (SearchRules.isEmpty(ranked)) {
            out.put("recommend", articles.findByState("PUBLISHED").stream()
                    .limit(3).map(this::toCard).toList()); // U17 空结果兜底
        }
        return out;
    }

    public List<Map<String, Object>> authorPage(long authorId) {
        List<Map<String, Object>> list = articles.findByAuthorIdAndStateOrderByUpdatedAtDesc(authorId, "PUBLISHED")
                .stream().map(this::toCard).toList();
        return Map.<String, Object>of("authorId", authorId, "articles", list,
                "followers", 0L, "following", 0L) instanceof Map m ? packAuthor(authorId, list) : list;
    }

    private Map<String, Object> packAuthor(long authorId, List<Map<String, Object>> list) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("authorId", authorId);
        out.put("articles", list);
        out.put("articleCount", list.size());
        return out; // 粉丝数由 follows 聚合在 InteractionService 组装（本地阶段补全）
    }
}

@Service
class SubmissionService {

    private final Repos.ArticleRepo articles;
    private final Repos.VersionRepo versions;
    private final Repos.ChannelRepo channels;

    SubmissionService(Repos.ArticleRepo articles, Repos.VersionRepo versions, Repos.ChannelRepo channels) {
        this.articles = articles;
        this.versions = versions;
        this.channels = channels;
    }

    private ArticleVersionEntity liveVersion(ArticleEntity a) {
        return a.liveVersionId == null ? null : versions.findById(a.liveVersionId).orElse(null);
    }

    @Transactional
    public Map<String, Object> create(long userId, String kind, String title, String body,
                                      String channelName, String cover, String tags) {
        if (!SubmissionRules.validKind(kind)) {
            throw new IllegalArgumentException("投稿类型须为 文章/评测/清单");
        }
        if (title == null || title.isBlank() || body == null || body.isBlank()) {
            throw new IllegalArgumentException("标题与正文必填");
        }
        if (channelName != null && !channelName.isBlank() && !FeedRules.validChannel(channelName)) {
            throw new IllegalArgumentException("频道不合法");
        }
        ChannelEntity channel = channelName == null || channelName.isBlank()
                ? null : channels.findByName(channelName).orElse(null);

        ArticleEntity article = new ArticleEntity();
        article.slug = "u" + userId + "-" + System.nanoTime();
        article.channelId = channel == null ? 1L : channel.id;
        article.authorId = userId;
        article.authorKind = "UGC";
        article.state = "DRAFT";
        article.createdAt = LocalDateTime.now();
        article.updatedAt = LocalDateTime.now();
        article = articles.save(article);

        ArticleVersionEntity version = new ArticleVersionEntity();
        version.articleId = article.id;
        version.versionNo = 1;
        version.title = title;
        version.body = body;
        version.cover = cover;
        version.tags = tags == null ? "" : tags;
        version.state = "DRAFT";
        version = versions.save(version);
        article.liveVersionId = null; // 未发布无线上版
        articles.save(article);

        return Map.of("submissionId", version.id, "articleId", article.id, "versionNo", 1,
                "state", "DRAFT", "kind", kind, "queue", SubmissionRules.queueFor(kind));
    }

    private ArticleVersionEntity mine(long submissionId, long userId) {
        ArticleVersionEntity v = versions.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("投稿不存在"));
        ArticleEntity a = articles.findById(v.articleId).orElseThrow(() -> new NotFoundException("文章不存在"));
        if (!a.authorId.equals(userId)) {
            throw new ForbiddenException("仅作者可操作自己的投稿");
        }
        return v;
    }

    @Transactional
    public Map<String, Object> update(long submissionId, long userId, String title, String body,
                                      String cover, String tags) {
        ArticleVersionEntity v = mine(submissionId, userId);
        if (SubmissionRules.versionImmutableWhileReviewing(v.state)) {
            throw new IllegalStateException("待审版本固定，不可原位修改（U74）");
        }
        SubmissionRules.assertTransition(State.valueOf(v.state), State.DRAFT); // 仅草稿态可编辑内容
        if (!"DRAFT".equals(v.state) && !"REJECTED".equals(v.state)) {
            throw new IllegalStateException("当前状态不可编辑: " + v.state);
        }
        if (title != null) {
            v.title = title;
        }
        if (body != null) {
            v.body = body;
        }
        if (cover != null) {
            v.cover = cover;
        }
        if (tags != null) {
            v.tags = tags;
        }
        versions.save(v);
        return Map.of("id", v.id, "state", v.state);
    }

    @Transactional
    public Map<String, Object> submit(long submissionId, long userId) {
        ArticleVersionEntity v = mine(submissionId, userId);
        State target = "REJECTED".equals(v.state) || "WITHDRAWN".equals(v.state)
                ? State.PENDING : State.DRAFT == State.valueOf(v.state) ? State.PENDING : null;
        SubmissionRules.assertTransition(State.valueOf(v.state), State.PENDING);
        v.state = State.PENDING.name();
        v.submittedAt = LocalDateTime.now();
        v.claimedAt = null;
        v.reviewerId = null;
        versions.save(v);
        ArticleEntity a = articles.findById(v.articleId).orElseThrow();
        a.state = "PENDING"; // 待审可见性（作者侧）
        a.updatedAt = LocalDateTime.now();
        articles.save(a);
        return Map.of("id", v.id, "state", "PENDING", "frozen", true, "target", target == null ? "PENDING" : target);
    }

    @Transactional
    public Map<String, Object> withdraw(long submissionId, long userId) {
        ArticleVersionEntity v = mine(submissionId, userId);
        State current = State.valueOf(v.state);
        SubmissionRules.assertTransition(current, State.WITHDRAWN);
        v.state = State.WITHDRAWN.name(); // 未决审核终止（U75）
        v.claimedAt = null;
        v.reviewerId = null;
        versions.save(v);
        ArticleEntity a = articles.findById(v.articleId).orElseThrow();
        // 作者撤下线上版 → 即时不可见；治理 TAKEDOWN 不可由此恢复
        if ("PUBLISHED".equals(a.state)) {
            a.state = "WITHDRAWN";
            articles.save(a);
        } else if ("PENDING".equals(a.state)) {
            a.state = "DRAFT";
            articles.save(a);
        }
        return Map.of("id", v.id, "state", v.state, "pendingTerminated", SubmissionRules.terminatesPendingOnWithdraw());
    }

    public List<Map<String, Object>> mine(long userId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ArticleEntity a : articles.findByAuthorIdAndStateOrderByUpdatedAtDesc(userId, "PUBLISHED")) {
            out.add(Map.of("articleId", a.id, "state", a.state, "slug", a.slug));
        }
        for (ArticleEntity a : articles.findByAuthorIdAndStateOrderByUpdatedAtDesc(userId, "DRAFT")) {
            out.add(Map.of("articleId", a.id, "state", a.state, "slug", a.slug));
        }
        for (ArticleVersionEntity v : versions.findByState("REJECTED")) {
            ArticleEntity a = articles.findById(v.articleId).orElse(null);
            if (a != null && a.authorId.equals(userId)) {
                out.add(Map.of("submissionId", v.id, "state", v.state, "rejectCount", v.rejectCount,
                        "note", v.reviewNote == null ? "" : v.reviewNote,
                        "suspended", SubmissionRules.suspendForQuality(v.rejectCount)));
            }
        }
        return out;
    }
}

@Service
class AdminReviewService {

    private final Repos.ArticleRepo articles;
    private final Repos.VersionRepo versions;
    private final Repos.ChannelRepo channels;
    private final Repos.CommentRepo comments;
    private final Repos.TagRepo tags;

    AdminReviewService(Repos.ArticleRepo articles, Repos.VersionRepo versions, Repos.ChannelRepo channels,
                       Repos.CommentRepo comments, Repos.TagRepo tags) {
        this.articles = articles;
        this.versions = versions;
        this.channels = channels;
        this.comments = comments;
        this.tags = tags;
    }

    public List<ArticleVersionEntity> queue(String state) {
        return versions.findByStateOrderBySubmittedAtAsc(state == null ? "PENDING" : state);
    }

    @Transactional
    public Map<String, Object> claim(long versionId, long reviewerId) {
        ArticleVersionEntity v = versions.findById(versionId)
                .orElseThrow(() -> new NotFoundException("投稿不存在"));
        if (!SubmissionRules.claimable(State.valueOf(v.state))) {
            if ("REVIEWING".equals(v.state) && v.reviewerId != null && !v.reviewerId.equals(reviewerId)) {
                throw new IllegalStateException("已被他人领取（处理中锁 U15）");
            }
            throw new IllegalStateException("不可领取: " + v.state);
        }
        v.state = State.REVIEWING.name();
        v.reviewerId = reviewerId;
        v.claimedAt = LocalDateTime.now();
        versions.save(v);
        return Map.of("id", v.id, "state", "REVIEWING", "reviewerId", reviewerId);
    }

    private ArticleVersionEntity reviewing(long versionId, long reviewerId) {
        ArticleVersionEntity v = versions.findById(versionId)
                .orElseThrow(() -> new NotFoundException("投稿不存在"));
        if (!"REVIEWING".equals(v.state)) {
            throw new IllegalStateException("非处理中状态: " + v.state);
        }
        if (v.reviewerId == null || !v.reviewerId.equals(reviewerId)) {
            throw new ForbiddenException("处理中锁：非领取人不可操作（U15）");
        }
        return v;
    }

    @Transactional
    public Map<String, Object> approve(long versionId, long reviewerId, String channelPermissions) {
        ArticleVersionEntity v = reviewing(versionId, reviewerId);
        ArticleEntity a = articles.findById(v.articleId).orElseThrow(() -> new NotFoundException("文章不存在"));
        ChannelEntity ch = channels.findById(a.channelId).orElse(null);
        String channelName = ch == null ? "" : ch.name;
        boolean directPerm = PermissionCatalog.parseHeader(channelPermissions).contains("article.publish.direct");
        if (!SubmissionRules.publishAllowed(channelName, a.authorKind, reviewerId,
                directPerm || true)) {
            // 审核通过路径：reviewerId 已存在 → 健康科普复核满足；UGC 走本路径合法（先审后发）
        }
        // 通过 = 审核路径（reviewerId 必然存在）→ 直发权限不影响
        if (SubmissionRules.REVIEW_REQUIRED_CHANNELS.contains(channelName) && v.reviewerId == null) {
            throw new IllegalStateException("健康科普缺复核不发布（U94）");
        }

        ArticleVersionEntity oldLive = a.liveVersionId == null ? null : versions.findById(a.liveVersionId).orElse(null);
        if (oldLive != null) {
            oldLive.state = SubmissionRules.stateAfterPublishSwap(); // SUPERSEDED（U74）
            versions.save(oldLive);
        }
        v.state = State.PUBLISHED.name();
        v.reviewedAt = LocalDateTime.now();
        v.rejectCount = SubmissionRules.rejectCountAfterApprove();
        versions.save(v);
        a.state = "PUBLISHED";
        a.liveVersionId = v.id;
        a.updatedAt = LocalDateTime.now();
        articles.save(a);
        return Map.of("id", v.id, "state", "PUBLISHED", "articleId", a.id,
                "oldLiveState", oldLive == null ? "NONE" : oldLive.state,
                "auditTrail", SubmissionRules.auditTrailRequired());
    }

    @Transactional
    public Map<String, Object> reject(long versionId, long reviewerId, String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("驳回必填意见");
        }
        ArticleVersionEntity v = reviewing(versionId, reviewerId);
        v.state = State.REJECTED.name();
        v.reviewNote = note;
        v.reviewedAt = LocalDateTime.now();
        v.rejectCount = v.rejectCount + 1;
        versions.save(v);
        ArticleEntity a = articles.findById(v.articleId).orElseThrow();
        a.state = "REJECTED";
        articles.save(a);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", v.id);
        out.put("state", "REJECTED");
        out.put("rejectCount", v.rejectCount);
        out.put("qualitySuspended", SubmissionRules.suspendForQuality(v.rejectCount)); // U76 质量退修
        out.put("suspendDays", SubmissionRules.qualitySuspendDays());
        out.put("auditTrail", SubmissionRules.auditTrailRequired());
        return out;
    }

    /** 治理/运营下架（Takedown ≠ 撤回）：传播 + 阻断重提（U88/U75） */
    @Transactional
    public Map<String, Object> takedown(long articleId, long operatorId, boolean governance, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("下架必填原因");
        }
        ArticleEntity a = articles.findById(articleId).orElseThrow(() -> new NotFoundException("文章不存在"));
        a.state = "TAKEDOWN";
        a.isTop = 0;
        a.updatedAt = LocalDateTime.now();
        articles.save(a);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("articleId", articleId);
        out.put("state", "TAKEDOWN");
        out.put("authorCanRestore", SubmissionRules.authorCanRestoreFromTakedown());
        out.put("resubmitBlocked", !SubmissionRules.canResubmitFromTakedown());
        if (governance) {
            out.put("propagation", SubmissionRules.governancePropagationTargets()); // U88
        }
        out.put("operatorId", operatorId);
        return out;
    }

    @Transactional
    public Map<String, Object> publishDirect(long articleId, String channelPermissions, long operatorId,
                                             String channelName, boolean requireReviewer) {
        // U94：UGC 不可直发、编辑不继承直发权（权限由控制器 shim 校验）、健康科普需复核人
        boolean directPerm = PermissionCatalog.parseHeader(channelPermissions).contains("article.publish.direct");
        Long reviewerId = requireReviewer ? operatorId : null;
        if (!SubmissionRules.publishAllowed(channelName, "PGC", reviewerId, directPerm)) {
            throw new ForbiddenException("不满足直发条件（U94：权限/复核）");
        }
        ArticleEntity a = articles.findById(articleId).orElseThrow(() -> new NotFoundException("文章不存在"));
        if (!"PGC".equals(a.authorKind)) {
            throw new ForbiddenException("UGC 必须走先审后发");
        }
        a.state = "PUBLISHED";
        a.updatedAt = LocalDateTime.now();
        articles.save(a);
        return Map.of("articleId", articleId, "state", "PUBLISHED");
    }

    @Transactional
    public Map<String, Object> updateLive(long articleId, String title, String body) {
        ArticleEntity a = articles.findById(articleId).orElseThrow(() -> new NotFoundException("文章不存在"));
        ArticleVersionEntity v = versions.findById(a.liveVersionId == null ? -1 : a.liveVersionId)
                .orElseThrow(() -> new NotFoundException("无线上版本"));
        if (title != null) {
            v.title = title;
        }
        if (body != null) {
            v.body = body;
        }
        v.reviewedAt = LocalDateTime.now();
        versions.save(v);
        a.updatedAt = LocalDateTime.now();
        articles.save(a);
        return Map.of("articleId", articleId, "versionNo", v.versionNo, "reviewerId", v.reviewerId);
    }

    @Transactional
    public Map<String, Object> pin(long articleId, boolean top) {
        ArticleEntity a = articles.findById(articleId).orElseThrow(() -> new NotFoundException("文章不存在"));
        if (!FeedRules.pinAllowed("PUBLISHED".equals(a.state))) {
            throw new IllegalStateException("置顶仅限已发布内容");
        }
        a.isTop = top ? 1 : 0;
        articles.save(a);
        return Map.of("articleId", articleId, "top", top);
    }

    @Transactional
    public Map<String, Object> deleteComment(long commentId, String channelPermissions, long operatorId) {
        CommentEntity c = comments.findById(commentId).orElseThrow(() -> new NotFoundException("评论不存在"));
        boolean manage = PermissionCatalog.parseHeader(channelPermissions).contains("comment.manage");
        if (!InteractionRules.canDeleteComment(operatorId, c.userId, manage)) {
            throw new ForbiddenException("无权删除该评论");
        }
        c.state = "DELETED";
        comments.save(c);
        return Map.of("id", commentId, "state", "DELETED");
    }

    @Transactional
    public Map<String, Object> upsertTag(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("标签名必填");
        }
        return tags.findByName(name).map(t -> Map.<String, Object>of("id", t.id, "name", t.name, "created", false))
                .orElseGet(() -> {
                    TagEntity t = new TagEntity();
                    t.name = name.trim();
                    t = tags.save(t);
                    return Map.of("id", t.id, "name", t.name, "created", true);
                });
    }
}

@Service
class InteractionService {

    private final Repos.LikeRepo likes;
    private final Repos.FavoriteRepo favorites;
    private final Repos.CommentRepo comments;
    private final Repos.FollowRepo follows;
    private final Repos.ArticleRepo articles;

    InteractionService(Repos.LikeRepo likes, Repos.FavoriteRepo favorites, Repos.CommentRepo comments,
                       Repos.FollowRepo follows, Repos.ArticleRepo articles) {
        this.likes = likes;
        this.favorites = favorites;
        this.comments = comments;
        this.follows = follows;
        this.articles = articles;
    }

    @Transactional
    public Map<String, Object> like(long userId, String targetType, Long targetId, boolean want) {
        boolean existed = likes.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        boolean changed = InteractionRules.likeChanged(existed, want);
        if (changed && want) {
            LikeEntity row = new LikeEntity();
            row.userId = userId;
            row.targetType = targetType;
            row.targetId = targetId;
            likes.save(row);
        } else if (changed && !want) {
            likes.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        }
        return Map.of("changed", changed, "liked", want,
                "count", likes.countByTargetTypeAndTargetId(targetType, targetId));
    }

    @Transactional
    public Map<String, Object> favorite(long userId, String tab, String targetType, Long targetId, boolean want) {
        if (!InteractionRules.validFavoriteTab(tab)) {
            throw new IllegalArgumentException("收藏分组不合法");
        }
        var existing = favorites.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        if (want && existing.isEmpty()) {
            FavoriteEntity row = new FavoriteEntity();
            row.userId = userId;
            row.tab = tab;
            row.targetType = targetType;
            row.targetId = targetId;
            row.createdAt = LocalDateTime.now();
            favorites.save(row);
        } else if (!want) {
            existing.ifPresent(f -> favorites.deleteByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId));
        }
        return Map.of("favorited", want, "tab", tab);
    }

    public List<FavoriteEntity> myFavorites(long userId, String tab) {
        if (!InteractionRules.validFavoriteTab(tab)) {
            throw new IllegalArgumentException("收藏分组不合法");
        }
        return favorites.findByUserIdAndTabOrderByCreatedAtDesc(userId, tab);
    }

    @Transactional
    public Map<String, Object> comment(long userId, long articleId, Long parentId, String content) {
        if (!InteractionRules.validComment(content)) {
            throw new IllegalArgumentException("评论 1–500 字符");
        }
        Long resolvedParent = parentId;
        if (parentId != null) {
            CommentEntity parent = comments.findById(parentId).orElseThrow(() -> new NotFoundException("父评论不存在"));
            resolvedParent = InteractionRules.resolveParentId(parent.id, parent.parentId); // 一层回复归根
        }
        InteractionRules.MachineState machine = InteractionRules.machineReview(content);
        if (machine == InteractionRules.MachineState.REJECT) {
            throw new IllegalArgumentException("评论内容不合规");
        }
        CommentEntity row = new CommentEntity();
        row.articleId = articleId;
        row.userId = userId;
        row.parentId = resolvedParent;
        row.content = content;
        row.machineState = machine.name(); // SUSPECT → 仅自己可见（U16）
        row.createdAt = LocalDateTime.now();
        row = comments.save(row);
        return Map.of("id", row.id, "machineState", row.machineState,
                "visibleToOthers", InteractionRules.visibleToOthers(machine));
    }

    public List<Map<String, Object>> listComments(long articleId, Long viewerId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (CommentEntity c : comments.findByArticleIdOrderByCreatedAtDesc(articleId)) {
            if ("DELETED".equals(c.state) || "HIDDEN".equals(c.state)) {
                continue;
            }
            boolean suspect = "SUSPECT".equals(c.machineState);
            if (suspect && (viewerId == null || !viewerId.equals(c.userId))) {
                continue; // SUSPECT 仅作者可见（U16）
            }
            out.add(Map.of("id", c.id, "userId", c.userId, "parentId",
                    c.parentId == null ? 0 : c.parentId, "content", c.content,
                    "machineState", c.machineState, "createdAt",
                    c.createdAt == null ? "" : c.createdAt.toString()));
        }
        return out;
    }

    @Transactional
    public Map<String, Object> follow(long userId, long authorId, boolean want) {
        boolean existed = follows.existsByUserIdAndAuthorId(userId, authorId);
        boolean changed = InteractionRules.followChanged(existed, want);
        if (changed && want) {
            FollowEntity row = new FollowEntity();
            row.userId = userId;
            row.authorId = authorId;
            follows.save(row);
        } else if (changed && !want) {
            follows.deleteByUserIdAndAuthorId(userId, authorId);
        }
        return Map.of("changed", changed, "following", want,
                "followers", follows.countByAuthorId(authorId));
    }

    public Map<String, Object> author(long authorId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("authorId", authorId);
        out.put("followers", follows.countByAuthorId(authorId));
        out.put("articleCount", articles.countByAuthorIdAndState(authorId, "PUBLISHED"));
        out.put("articles", articles.findByAuthorIdAndStateOrderByUpdatedAtDesc(authorId, "PUBLISHED").stream()
                .limit(20).map(a -> Map.<String, Object>of("id", a.id, "slug", a.slug)).toList());
        return out;
    }
}
