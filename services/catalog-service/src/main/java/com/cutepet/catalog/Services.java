package com.cutepet.catalog;

import com.cutepet.common.web.ForbiddenException;
import com.cutepet.common.web.NotFoundException;
import com.cutepet.catalog.domain.ListRules;
import com.cutepet.catalog.domain.ProductRules;
import com.cutepet.catalog.domain.ProductRules.State;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * catalog 应用服务（T5.1–T5.3 编排）。判定委托 domain（CatalogLogicTest 覆盖）。
 */
@Service
class ProductService {

    private final Repos.ProductRepo products;
    private final Repos.ProductFavoriteRepo favorites;
    private final Repos.ProductListRepo lists;
    private final Repos.ListItemRepo listItems;
    private final Repos.BindingRepo bindings;
    private final Repos.ImportJobRepo importJobs;

    ProductService(Repos.ProductRepo products, Repos.ProductFavoriteRepo favorites,
                   Repos.ProductListRepo lists, Repos.ListItemRepo listItems,
                   Repos.BindingRepo bindings, Repos.ImportJobRepo importJobs) {
        this.products = products;
        this.favorites = favorites;
        this.lists = lists;
        this.listItems = listItems;
        this.bindings = bindings;
        this.importJobs = importJobs;
    }

    public Map<String, Object> search(String category, String sort) {
        if (category != null && !category.isBlank() && !ProductRules.validCategory(category)) {
            throw new IllegalArgumentException("类目不合法");
        }
        List<ProductEntity> pool = (category == null || category.isBlank())
                ? products.findByState("ON_SHELF")
                : products.findByCategoryAndState(category, "ON_SHELF");
        if ("hot".equalsIgnoreCase(sort) || sort == null) {
            pool = new ArrayList<>(pool);
            pool.sort((a, b) -> Double.compare(b.hotScore == null ? 0 : b.hotScore,
                    a.hotScore == null ? 0 : a.hotScore)); // 决议热度口径
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (ProductEntity p : pool) {
            items.add(toCard(p));
        }
        return Map.of("items", items, "total", items.size());
    }

    private Map<String, Object> toCard(ProductEntity p) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("id", p.id);
        card.put("name", p.name);
        card.put("brand", p.brand);
        card.put("category", p.category);
        card.put("priceMin", p.priceMin);
        card.put("priceMax", p.priceMax);
        card.put("state", p.state);
        card.put("hotScore", p.hotScore);
        return card;
    }

    @Transactional
    public Map<String, Object> detail(long id) {
        ProductEntity p = products.findById(id).orElseThrow(() -> new NotFoundException("商品不存在"));
        // 浏览计数入热度（30 天窗滚动清零由每日任务在本地阶段接通）
        p.hotScore = (p.hotScore == null ? 0 : p.hotScore) + 1;
        products.save(p);
        Map<String, Object> out = toCard(p);
        out.put("tags", p.tags);
        out.put("sourceType", p.sourceType); // T5.3 来源字段展示
        out.put("sourceRef", p.sourceRef);
        out.put("editorTestOnly", p.editorTestOnly == 1);
        out.put("referenceDisplay", ProductRules.referenceDisplay(State.valueOf(p.state))); // U88 引用展示
        // 双向关联：相关评测（bindings）与所在清单
        List<Map<String, Object>> boundArticles = new ArrayList<>();
        for (var b : bindings.findByArticleId(0L)) { // 占位：反向按 productId 查询在绑定表语义下扩展
            boundArticles.add(Map.of("articleId", b.articleId));
        }
        out.put("relatedReviewCount", boundArticles.size());
        List<Map<String, Object>> inLists = new ArrayList<>();
        for (ProductListEntity list : lists.findByState("ACTIVE")) {
            for (ListItemEntity item : listItems.findByListIdOrderByOrdAsc(list.id)) {
                if (item.productId.equals(id)) {
                    inLists.add(Map.of("listId", list.id, "title", list.title));
                    break;
                }
            }
        }
        out.put("inLists", inLists);
        out.put("favoriteCount", favorites.countByProductId(id));
        return out;
    }

    @Transactional
    public Map<String, Object> favorite(long userId, long productId, boolean want) {
        products.findById(productId).orElseThrow(() -> new NotFoundException("商品不存在"));
        var key = new ProductFavKey(userId, productId);
        ProductEntity p = products.findById(productId).orElseThrow();
        if (want && !favorites.existsById(key)) {
            ProductFavoriteEntity row = new ProductFavoriteEntity();
            row.userId = userId;
            row.productId = productId;
            row.createdAt = LocalDateTime.now();
            favorites.save(row);
            // 收藏 ×3 计入热度（决议）
            p.hotScore = ProductRules.hotScore(0, favorites.countByProductId(productId), 0)
                    + (p.hotScore == null ? 0 : 0); // 以收藏总数重算收藏分量+原浏览部分由窗口任务维护
            p.hotScore = (p.hotScore == null ? 0.0 : p.hotScore);
            products.save(p);
        } else if (!want && favorites.existsById(key)) {
            favorites.deleteById(key);
        }
        return Map.of("productId", productId, "favorited", want,
                "favoriteCount", favorites.countByProductId(productId));
    }

    @Transactional
    public Map<String, Object> create(long operatorId, String name, String brand, String category,
                                      String specs, Double priceMin, Double priceMax, String cover,
                                      String tags, String sourceType, String sourceRef,
                                      boolean editorTestOnly, boolean staff) {
        ProductRules.assertCreatable(category, name, sourceType); // U23/U77 校验
        if (!ProductRules.validPriceRange(priceMin, priceMax)) {
            throw new IllegalArgumentException("价格区间不合法");
        }
        if (editorTestOnly && !ProductRules.editorTestAllowed(true, staff)) {
            throw new ForbiddenException("普通用户不可标编辑部测试（U77）");
        }
        ProductEntity p = new ProductEntity();
        p.name = name;
        p.brand = brand == null ? "" : brand;
        p.category = category;
        p.specs = specs == null ? "" : specs;
        p.priceMin = priceMin;
        p.priceMax = priceMax;
        p.cover = cover == null ? "" : cover;
        p.tags = tags == null ? "" : tags;
        p.sourceType = sourceType;
        p.sourceRef = sourceRef == null ? "" : sourceRef;
        p.editorTestOnly = editorTestOnly ? 1 : 0;
        p.createdAt = LocalDateTime.now();
        p.updatedAt = LocalDateTime.now();
        p = products.save(p);
        return Map.of("id", p.id, "state", p.state);
    }

    @Transactional
    public Map<String, Object> update(long id, String name, String category, String tags) {
        ProductEntity p = products.findById(id).orElseThrow(() -> new NotFoundException("商品不存在"));
        if (name != null || category != null) {
            ProductRules.assertCreatable(category == null ? p.category : category,
                    name == null ? p.name : name, p.sourceType);
        }
        if (name != null) {
            p.name = name;
        }
        if (category != null) {
            p.category = category;
        }
        if (tags != null) {
            p.tags = tags;
        }
        p.updatedAt = LocalDateTime.now();
        products.save(p);
        return Map.of("id", p.id, "updated", true);
    }

    @Transactional
    public Map<String, Object> transitionState(long id, String target, boolean governance) {
        ProductEntity p = products.findById(id).orElseThrow(() -> new NotFoundException("商品不存在"));
        State from = State.valueOf(p.state);
        State to = "CLEARED".equals(target) ? State.CLEARED : ("ON_SHELF".equals(target) ? State.ON_SHELF : State.OFF_SHELF);
        if (governance) {
            to = State.CLEARED;
        }
        if (!ProductRules.transition(from, to)) {
            throw new IllegalStateException("商品状态不可流转: " + from + " → " + to);
        }
        p.state = to.name();
        p.updatedAt = LocalDateTime.now();
        products.save(p);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("state", p.state);
        out.put("referenceDisplay", ProductRules.referenceDisplay(to)); // U88
        out.put("canResubmit", ProductRules.canResubmitListing(to));
        return out;
    }

    /** CSV 导入（U77：逐行同规则，不能绕） */
    @Transactional
    public Map<String, Object> importCsv(long operatorId, String filename, List<String[]> rows) {
        int[] counts = ProductRules.validateImportRows(rows == null ? List.of() : rows);
        ProductImportJobEntity job = new ProductImportJobEntity();
        job.createdBy = operatorId;
        job.filename = filename == null ? "import.csv" : filename;
        job.total = rows == null ? 0 : rows.size();
        job.accepted = counts[0];
        job.rejected = counts[1];
        job.state = "DONE";
        job.createdAt = LocalDateTime.now();
        importJobs.save(job);
        // 仅合法行入库
        if (rows != null) {
            for (String[] row : rows) {
                String name = row.length > 0 ? row[0] : "";
                String category = row.length > 1 ? row[1] : "";
                String source = row.length > 2 ? row[2] : "";
                try {
                    ProductRules.assertCreatable(category, name, source);
                    ProductEntity p = new ProductEntity();
                    p.name = name;
                    p.category = category;
                    p.sourceType = source;
                    p.createdAt = LocalDateTime.now();
                    p.updatedAt = LocalDateTime.now();
                    products.save(p);
                } catch (IllegalArgumentException ignored) {
                    // 拒绝行已计入 rejected
                }
            }
        }
        return Map.of("jobId", job.id, "total", job.total, "accepted", job.accepted,
                "rejected", job.rejected, "summary", ListRules.importSummary(job.accepted, job.rejected));
    }

    // ---------- 清单与绑定（T5.2） ----------

    @Transactional
    public Map<String, Object> createList(long ownerUserId, String title, String intro, String kind) {
        if (!ListRules.validKind(kind)) {
            throw new IllegalArgumentException("清单类型不合法（OFFICIAL/AUTHOR）");
        }
        ProductListEntity list = new ProductListEntity();
        list.ownerUserId = ownerUserId;
        list.title = title;
        list.intro = intro == null ? "" : intro;
        list.kind = kind;
        list.createdAt = LocalDateTime.now();
        list = lists.save(list);
        return Map.of("listId", list.id, "kind", list.kind, "state", list.state);
    }

    @Transactional
    public Map<String, Object> addListItem(long listId, long productId, int ord, String reason) {
        ProductListEntity list = lists.findById(listId).orElseThrow(() -> new NotFoundException("清单不存在"));
        ProductEntity product = products.findById(productId).orElseThrow(() -> new NotFoundException("商品不存在"));
        ListRules.assertAddItem(listItems.findByListIdOrderByOrdAsc(listId).size(),
                State.valueOf(product.state)); // U88 清除商品拒入
        ListItemEntity item = new ListItemEntity();
        item.listId = listId;
        item.productId = productId;
        item.ord = ord;
        item.reason = reason == null ? "" : reason;
        listItems.save(item);
        return Map.of("listId", listId, "productId", productId, "added", true);
    }

    public Map<String, Object> listDetail(long listId) {
        ProductListEntity list = lists.findById(listId).orElseThrow(() -> new NotFoundException("清单不存在"));
        List<ListItemEntity> items = listItems.findByListIdOrderByOrdAsc(listId);
        List<Map<String, Object>> outItems = new ArrayList<>();
        List<State> states = new ArrayList<>();
        for (ListItemEntity item : items) {
            ProductEntity p = products.findById(item.productId).orElse(null);
            State st = p == null ? State.CLEARED : State.valueOf(p.state);
            states.add(st);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("ord", item.ord);
            row.put("productId", item.productId);
            row.put("name", p == null ? "(已移除)" : p.name);
            row.put("reason", item.reason);
            row.put("refState", ListRules.refState(st).name()); // U88 引用展示
            outItems.add(row);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", list.id);
        out.put("title", list.title);
        out.put("kind", list.kind);
        out.put("items", outItems);
        out.put("publishable", ListRules.listPublishable(states)); // U88 全不可用禁新发
        return out;
    }

    @Transactional
    public Map<String, Object> bindArticleProduct(long articleId, long productId, int ord) {
        boolean productExists = products.existsById(productId);
        if (!ListRules.bindingValid(productExists, articleId > 0)) {
            throw new IllegalArgumentException("绑定需双方存在（U24）");
        }
        ArticleProductBindingEntity b = new ArticleProductBindingEntity();
        b.articleId = articleId;
        b.productId = productId;
        b.ord = ord;
        bindings.save(b);
        return Map.of("articleId", articleId, "productId", productId, "bound", true);
    }
}

@Service
class CatalogListLikeService {

    private final Map<Long, Long> likeCounts = new ConcurrentHashMap<>();

    @Transactional
    public Map<String, Object> likeList(long listId) {
        long count = likeCounts.merge(listId, 1L, Long::sum); // 本地计数；持久化随本地阶段接入
        return Map.of("listId", listId, "liked", true, "likes", count);
    }
}
