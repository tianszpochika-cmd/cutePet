package com.cutepet.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/** content 持久化实体（映射 cutepet_content V1 + banners）。 */
@Entity
@Table(name = "channels", schema = "cutepet_content")
class ChannelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String name;
    @Column(nullable = false, unique = true)
    public String slug;
    public int ord = 0;
}

@Entity
@Table(name = "tags", schema = "cutepet_content")
class TagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String name;
}

@Entity
@Table(name = "topics", schema = "cutepet_content")
class TopicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String name;
    public String state = "ACTIVE";
}

@Entity
@Table(name = "articles", schema = "cutepet_content")
class ArticleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String slug;
    @Column(name = "channel_id", nullable = false)
    public Long channelId;
    @Column(name = "author_id", nullable = false)
    public Long authorId;
    @Column(name = "author_kind")
    public String authorKind = "UGC"; // PGC/UGC
    public String state = "DRAFT";    // 见 SubmissionRules 状态机
    @Column(name = "live_version_id")
    public Long liveVersionId;
    @Column(name = "is_top")
    public int isTop = 0;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

@Entity
@Table(name = "article_versions", schema = "cutepet_content")
class ArticleVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "article_id", nullable = false)
    public Long articleId;
    @Column(name = "version_no", nullable = false)
    public int versionNo;
    @Column(nullable = false)
    public String title;
    public String cover;
    @Column(nullable = false)
    public String body;
    public String tags = "";
    public String state = "DRAFT";
    @Column(name = "reject_count")
    public int rejectCount = 0;
    @Column(name = "review_note")
    public String reviewNote;
    @Column(name = "reviewer_id")
    public Long reviewerId;
    @Column(name = "claimed_at")
    public LocalDateTime claimedAt;
    @Column(name = "submitted_at")
    public LocalDateTime submittedAt;
    @Column(name = "reviewed_at")
    public LocalDateTime reviewedAt;
}

@Entity
@Table(name = "comments", schema = "cutepet_content")
class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "article_id", nullable = false)
    public Long articleId;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "parent_id")
    public Long parentId;
    @Column(nullable = false)
    public String content;
    public String state = "VISIBLE";       // VISIBLE/HIDDEN/DELETED
    @Column(name = "machine_state")
    public String machineState = "PASS";   // PASS/SUSPECT/REJECT
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "likes", schema = "cutepet_content")
class LikeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "target_type", nullable = false)
    public String targetType;
    @Column(name = "target_id", nullable = false)
    public Long targetId;
}

@Entity
@Table(name = "favorites", schema = "cutepet_content")
class FavoriteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(nullable = false)
    public String tab; // 内容/商品/清单/场所/路线
    @Column(name = "target_type", nullable = false)
    public String targetType;
    @Column(name = "target_id", nullable = false)
    public Long targetId;
    @Column(name = "group_name")
    public String groupName = "默认";
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "follows", schema = "cutepet_content")
class FollowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false)
    public Long userId;
    @Column(name = "author_id", nullable = false)
    public Long authorId;
}

@Entity
@Table(name = "specials", schema = "cutepet_content")
class SpecialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String title;
    public String intro = "";
    public String state = "ACTIVE";
}

final class SpecialItemKey implements Serializable {
    Long specialId;
    Long articleId;

    SpecialItemKey() {
    }

    SpecialItemKey(Long specialId, Long articleId) {
        this.specialId = specialId;
        this.articleId = articleId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpecialItemKey k)) {
            return false;
        }
        return specialId.equals(k.specialId) && articleId.equals(k.articleId);
    }

    @Override
    public int hashCode() {
        return specialId.hashCode() ^ articleId.hashCode();
    }
}

@Entity
@Table(name = "special_items", schema = "cutepet_content")
@IdClass(SpecialItemKey.class)
class SpecialItemEntity {
    @Id
    @Column(name = "special_id")
    public Long specialId;
    @Id
    @Column(name = "article_id")
    public Long articleId;
    public int ord = 0;
}

@Entity
@Table(name = "banners", schema = "cutepet_content")
class BannerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public String slot;
    public String title = "";
    @Column(name = "target_url")
    public String targetUrl;
    @Column(name = "starts_at")
    public LocalDateTime startsAt;
    @Column(name = "ends_at")
    public LocalDateTime endsAt;
    public String state = "ACTIVE";
    public int ord = 0;
}
