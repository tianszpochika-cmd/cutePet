package com.cutepet.catalog;

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
 * catalog 控制器（契约 /products/** /lists/** + admin 商品组）。dev shim：X-User-Id/X-Permissions。
 */
@RestController
class CatalogControllers {

    private final ProductService productService;
    private final CatalogListLikeService listLikeService;
    private final Repos.ProductListRepo listRepo;

    CatalogControllers(ProductService productService, CatalogListLikeService listLikeService,
                       Repos.ProductListRepo listRepo) {
        this.productService = productService;
        this.listLikeService = listLikeService;
        this.listRepo = listRepo;
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

    public record ProductCreateReq(String name, String brand, String category, String specs,
                                   Double priceMin, Double priceMax, String cover, String tags,
                                   String sourceType, String sourceRef, Boolean editorTestOnly, Boolean staff) {
    }

    public record ProductPatchReq(String name, String category, String tags) {
    }

    public record StateReq(String target, Boolean governance) {
    }

    public record ImportReq(String filename, List<String[]> rows) {
    }

    public record ListCreateReq(String title, String intro, String kind) {
    }

    public record ListItemReq(Long productId, Integer ord, String reason) {
    }

    public record BindReq(Long articleId, Integer ord) {
    }

    @GetMapping("/products")
    public Map<String, Object> products(@RequestParam(required = false) String category,
                                        @RequestParam(required = false, defaultValue = "hot") String sort) {
        return productService.search(category, sort);
    }

    @GetMapping("/products/{id}")
    public Map<String, Object> product(@PathVariable long id) {
        return productService.detail(id);
    }

    @PostMapping("/products/{id}/favorite")
    public Map<String, Object> favorite(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                        @PathVariable long id) {
        return productService.favorite(userOf(uid), id, true);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/products/{id}/favorite")
    public Map<String, Object> unfavorite(@RequestHeader(value = "X-User-Id", required = false) String uid,
                                          @PathVariable long id) {
        return productService.favorite(userOf(uid), id, false);
    }

    @GetMapping("/lists")
    public List<ProductListEntity> lists(@RequestParam(required = false) String kind) {
        return kind == null || kind.isBlank() ? listRepo.findByState("ACTIVE")
                : listRepo.findByKindAndState(kind, "ACTIVE");
    }

    @GetMapping("/lists/{id}")
    public Map<String, Object> list(@PathVariable long id) {
        return productService.listDetail(id);
    }

    @PostMapping("/lists/{id}/like")
    public Map<String, Object> listLike(@PathVariable long id) {
        return listLikeService.likeList(id);
    }

    // ---------- 管理侧 ----------

    @PostMapping("/admin/products")
    public Map<String, Object> adminCreateProduct(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @RequestBody ProductCreateReq req) {
        requirePermission(perms, "product.create.edit");
        return productService.create(userOf(uid), req.name(), req.brand(), req.category(), req.specs(),
                req.priceMin(), req.priceMax(), req.cover(), req.tags(), req.sourceType(), req.sourceRef(),
                Boolean.TRUE.equals(req.editorTestOnly()), Boolean.TRUE.equals(req.staff()));
    }

    @PatchMapping("/admin/products/{id}")
    public Map<String, Object> adminUpdateProduct(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody ProductPatchReq req) {
        requirePermission(perms, "product.create.edit");
        return productService.update(id, req.name(), req.category(), req.tags());
    }

    @PostMapping("/admin/products/{id}/takedown")
    public Map<String, Object> adminTakedown(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody StateReq req) {
        boolean governance = Boolean.TRUE.equals(req.governance());
        requirePermission(perms, governance ? "product.create.edit" : "product.takedown");
        return productService.transitionState(id, req.target() == null ? "OFF_SHELF" : req.target(), governance);
    }

    @PostMapping("/admin/products/import")
    public Map<String, Object> adminImport(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @RequestBody ImportReq req) {
        requirePermission(perms, "product.import.csv");
        return productService.importCsv(userOf(uid), req.filename(), req.rows());
    }

    @PostMapping("/admin/lists")
    public Map<String, Object> adminCreateList(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @RequestHeader(value = "X-User-Id", required = false) String uid,
            @RequestBody ListCreateReq req) {
        requirePermission(perms, "list.manage");
        return productService.createList(userOf(uid), req.title(), req.intro(), req.kind());
    }

    @PostMapping("/admin/products/{id}/bind")
    public Map<String, Object> adminBind(
            @RequestHeader(value = "X-Permissions", required = false) String perms,
            @PathVariable long id, @RequestBody BindReq req) {
        requirePermission(perms, "review.bind.product");
        if (req.articleId() == null) {
            throw new IllegalArgumentException("articleId 必填");
        }
        return productService.bindArticleProduct(req.articleId(), id, req.ord() == null ? 0 : req.ord());
    }
}
