package com.cutepet.content;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** content Spring Data 仓库集合。 */
interface Repos {

    interface ChannelRepo extends JpaRepository<ChannelEntity, Long> {
        Optional<ChannelEntity> findByName(String name);

        List<ChannelEntity> findAllByOrderByOrdAsc();
    }

    interface TagRepo extends JpaRepository<TagEntity, Long> {
        Optional<TagEntity> findByName(String name);
    }

    interface TopicRepo extends JpaRepository<TopicEntity, Long> {
        List<TopicEntity> findByState(String state);
    }

    interface ArticleRepo extends JpaRepository<ArticleEntity, Long> {
        Optional<ArticleEntity> findBySlug(String slug);

        List<ArticleEntity> findByState(String state);

        List<ArticleEntity> findByStateAndIsTop(String state, int isTop);

        List<ArticleEntity> findByAuthorIdAndStateOrderByUpdatedAtDesc(Long authorId, String state);

        List<ArticleEntity> findByChannelIdAndState(Long channelId, String state);

        long countByAuthorIdAndState(Long authorId, String state);
    }

    interface VersionRepo extends JpaRepository<ArticleVersionEntity, Long> {
        List<ArticleVersionEntity> findByArticleIdOrderByVersionNoDesc(Long articleId);

        Optional<ArticleVersionEntity> findByArticleIdAndVersionNo(Long articleId, int versionNo);

        List<ArticleVersionEntity> findByStateOrderBySubmittedAtAsc(String state);

        Optional<ArticleVersionEntity> findByStateAndId(String state, Long id);

        List<ArticleVersionEntity> findByState(String state);
    }

    interface CommentRepo extends JpaRepository<CommentEntity, Long> {
        List<CommentEntity> findByArticleIdAndStateOrderByCreatedAtDesc(Long articleId, String state);

        List<CommentEntity> findByArticleIdOrderByCreatedAtDesc(Long articleId);

        long countByArticleIdAndState(Long articleId, String state);
    }

    interface LikeRepo extends JpaRepository<LikeEntity, Long> {
        boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

        void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

        long countByTargetTypeAndTargetId(String targetType, Long targetId);

        long countByUserIdAndTargetType(Long userId, String targetType);
    }

    interface FavoriteRepo extends JpaRepository<FavoriteEntity, Long> {
        Optional<FavoriteEntity> findByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

        void deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

        List<FavoriteEntity> findByUserIdAndTabOrderByCreatedAtDesc(Long userId, String tab);
    }

    interface FollowRepo extends JpaRepository<FollowEntity, Long> {
        boolean existsByUserIdAndAuthorId(Long userId, Long authorId);

        void deleteByUserIdAndAuthorId(Long userId, Long authorId);

        long countByAuthorId(Long authorId);

        long countByUserId(Long userId);
    }

    interface SpecialRepo extends JpaRepository<SpecialEntity, Long> {
    }

    interface SpecialItemRepo extends JpaRepository<SpecialItemEntity, SpecialItemKey> {
        List<SpecialItemEntity> findBySpecialIdOrderByOrdAsc(Long specialId);
    }

    interface BannerRepo extends JpaRepository<BannerEntity, Long> {
        List<BannerEntity> findBySlotAndStateOrderByOrdAsc(String slot, String state);

        List<BannerEntity> findByState(String state);
    }
}
