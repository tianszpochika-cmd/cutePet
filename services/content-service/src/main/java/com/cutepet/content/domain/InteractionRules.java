package com.cutepet.content.domain;

import java.util.List;
import java.util.Set;

/**
 * 互动与评论纯规则（T3.4 · 需求-资讯 C3/C4/C5 · U16）。
 */
public final class InteractionRules {

    public static final int COMMENT_MAX = 500;

    /** 收藏分组 Tab（IA：内容/商品/清单/场所/路线 —— 内容侧承载枚举一致性） */
    public static final List<String> FAVORITE_TABS = List.of("内容", "商品", "清单", "场所", "路线");

    /** 机审词表（stub，词库运维归管理端）：SUSPECT=仅自己可见待审，REJECT=直接拒绝 */
    public static final List<String> SUSPECT_WORDS = List.of("加微信", "私聊我", "包治");
    public static final List<String> REJECT_WORDS = List.of("违禁药", "代购疫苗");

    public enum MachineState { PASS, SUSPECT, REJECT }

    private InteractionRules() {
    }

    /** 幂等点赞：返回是否发生状态变化 */
    public static boolean likeChanged(boolean previouslyLiked, boolean wantLike) {
        return previouslyLiked != wantLike;
    }

    public static boolean validFavoriteTab(String tab) {
        return FAVORITE_TABS.contains(tab);
    }

    public static boolean validComment(String content) {
        return content != null && !content.isBlank() && content.trim().length() <= COMMENT_MAX;
    }

    /**
     * 一层回复：回复“某条回复”时归位到其根评论（parentId = root）。
     * targetParentId = 被回复评论；targetParentParentId = 被回复评论的父（null 表示根）。
     */
    public static Long resolveParentId(Long targetParentId, Long targetParentParentId) {
        if (targetParentId == null) {
            return null;
        }
        return targetParentParentId != null ? targetParentParentId : targetParentId;
    }

    /** 评论机审：REJECT 优先于 SUSPECT；命中即止 */
    public static MachineState machineReview(String content) {
        if (content == null) {
            return MachineState.REJECT;
        }
        for (String w : REJECT_WORDS) {
            if (content.contains(w)) {
                return MachineState.REJECT;
            }
        }
        for (String w : SUSPECT_WORDS) {
            if (content.contains(w)) {
                return MachineState.SUSPECT; // 仅自己可见（U16）
            }
        }
        return MachineState.PASS;
    }

    /** SUSPECT 评论对他人不可见、对作者可见（灰条“审核中”） */
    public static boolean visibleToOthers(MachineState state) {
        return state == MachineState.PASS;
    }

    public static boolean visibleToAuthor(MachineState state) {
        return state != MachineState.REJECT;
    }

    /** 关注幂等 */
    public static boolean followChanged(boolean alreadyFollowing, boolean wantFollow) {
        return alreadyFollowing != wantFollow;
    }

    /** 评论删除：仅本人或 comment.manage 权限（管理端） */
    public static boolean canDeleteComment(long actorId, long authorId, boolean hasManagePermission) {
        return actorId == authorId || hasManagePermission;
    }

    /** U16 封禁作者内容：从流中隐藏 */
    public static boolean feedVisible(boolean authorBanned) {
        return !authorBanned;
    }

    public static Set<String> interactionTargets() {
        return Set.of("ARTICLE", "COMMENT", "ROUTE", "LIST");
    }
}
