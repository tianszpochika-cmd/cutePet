package com.cutepet.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/** catalog 持久化实体（映射 cutepet_catalog V1）。 */
@Entity
@Table(name = "products", schema = "cutepet_catalog")
class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    public String brand = "";
    public String category;
    public String specs = "";
    @Column(name = "price_min")
    public Double priceMin;
    @Column(name = "price_max")
    public Double priceMax;
    public String cover = "";
    public String tags = "";
    @Column(name = "source_type")
    public String sourceType = "EDITORIAL";
    @Column(name = "source_ref")
    public String sourceRef = "";
    @Column(name = "editor_test_only")
    public int editorTestOnly = 0;
    public String state = "ON_SHELF"; // ON_SHELF/OFF_SHELF/CLEARED
    @Column(name = "hot_score")
    public Double hotScore = 0.0;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}

final class ProductFavKey implements Serializable {
    Long userId;
    Long productId;

    ProductFavKey() {
    }

    ProductFavKey(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ProductFavKey k && userId.equals(k.userId) && productId.equals(k.productId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() ^ productId.hashCode();
    }
}

@Entity
@Table(name = "product_favorites", schema = "cutepet_catalog")
@IdClass(ProductFavKey.class)
class ProductFavoriteEntity {
    @Id
    @Column(name = "user_id")
    public Long userId;
    @Id
    @Column(name = "product_id")
    public Long productId;
    public LocalDateTime createdAt;
}

@Entity
@Table(name = "product_lists", schema = "cutepet_catalog")
class ProductListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "owner_user_id")
    public Long ownerUserId; // NULL=运营官方清单
    public String title;
    public String intro = "";
    public String kind = "OFFICIAL"; // OFFICIAL/AUTHOR
    public String state = "ACTIVE";
    public LocalDateTime createdAt;
}

final class ListItemKey implements Serializable {
    Long listId;
    Long productId;

    ListItemKey() {
    }

    ListItemKey(Long listId, Long productId) {
        this.listId = listId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ListItemKey k && listId.equals(k.listId) && productId.equals(k.productId);
    }

    @Override
    public int hashCode() {
        return listId.hashCode() ^ productId.hashCode();
    }
}

@Entity
@Table(name = "list_items", schema = "cutepet_catalog")
@IdClass(ListItemKey.class)
class ListItemEntity {
    @Id
    @Column(name = "list_id")
    public Long listId;
    @Id
    @Column(name = "product_id")
    public Long productId;
    public int ord = 0;
    public String reason = "";
}

final class BindingKey implements Serializable {
    Long articleId;
    Long productId;

    BindingKey() {
    }

    BindingKey(Long articleId, Long productId) {
        this.articleId = articleId;
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BindingKey k && articleId.equals(k.articleId) && productId.equals(k.productId);
    }

    @Override
    public int hashCode() {
        return articleId.hashCode() ^ productId.hashCode();
    }
}

@Entity
@Table(name = "article_product_bindings", schema = "cutepet_catalog")
@IdClass(BindingKey.class)
class ArticleProductBindingEntity {
    @Id
    @Column(name = "article_id")
    public Long articleId;
    @Id
    @Column(name = "product_id")
    public Long productId;
    public int ord = 0;
}

@Entity
@Table(name = "product_import_jobs", schema = "cutepet_catalog")
class ProductImportJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "created_by", nullable = false)
    public Long createdBy;
    public String filename;
    public int total;
    public int accepted;
    public int rejected;
    public String state = "PENDING";
    public LocalDateTime createdAt;
}
