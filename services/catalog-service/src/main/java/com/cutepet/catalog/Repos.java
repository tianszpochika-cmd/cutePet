package com.cutepet.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** catalog Spring Data 仓库集合。 */
interface Repos {

    interface ProductRepo extends JpaRepository<ProductEntity, Long> {
        List<ProductEntity> findByState(String state);

        List<ProductEntity> findByCategoryAndState(String category, String state);

        List<ProductEntity> findByStateOrderByHotScoreDesc(String state);
    }

    interface ProductFavoriteRepo extends JpaRepository<ProductFavoriteEntity, ProductFavKey> {
        long countByProductId(Long productId);
    }

    interface ProductListRepo extends JpaRepository<ProductListEntity, Long> {
        List<ProductListEntity> findByState(String state);

        List<ProductListEntity> findByKindAndState(String kind, String state);
    }

    interface ListItemRepo extends JpaRepository<ListItemEntity, ListItemKey> {
        List<ListItemEntity> findByListIdOrderByOrdAsc(Long listId);
    }

    interface BindingRepo extends JpaRepository<ArticleProductBindingEntity, BindingKey> {
        List<ArticleProductBindingEntity> findByArticleId(Long articleId);
    }

    interface ImportJobRepo extends JpaRepository<ProductImportJobEntity, Long> {
    }
}
