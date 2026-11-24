package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.*;
import vn.vcraft.backend.repository.*;
import vn.vcraft.backend.validation.SlugUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository products;
    private final ProcessStepRepository stepRepo;
    private final MediaRepository mediaRepo;
    private final ProductTagRepository tagRepo;
    private final BatchRepository batches;
    private final jakarta.persistence.EntityManager entities;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProductService(ProductRepository products, ProcessStepRepository stepRepo,
                          MediaRepository mediaRepo, ProductTagRepository tagRepo, jakarta.persistence.EntityManager entities, BatchRepository batches) {
        this.products = products;
        this.stepRepo = stepRepo;
        this.mediaRepo = mediaRepo;
        this.tagRepo = tagRepo;
        this.batches = batches;
        this.entities = entities;
    }

    // ----------------------------------------------------------------
    // PUBLIC — giữ nguyên + mở rộng
    // ----------------------------------------------------------------

    public List<ProductResponse> list() {
        return products.findCatalog().stream().map(this::legacyResponse).toList();
    }

    public PageResponse<ProductResponse> page(PageQuery query) {
        var request = PageRequest.of(query.getPage(), query.getSize(),
                Sort.by("id").ascending());
        return PageResponse.from(products.findCatalogPage(request).map(this::legacyResponse));
    }

    public Optional<ProductResponse> detail(long id) {
        return products.findCatalogProduct(id).map(this::legacyResponse);
    }

    /** GET /api/products/{idOrSlug} — hỗ trợ cả id và slug */
    public ProductDetailResponse detailByIdOrSlug(String idOrSlug) {
        try {
            long id = Long.parseLong(idOrSlug);
            return products.findCatalogProduct(id)
                    .map(this::toDetail)
                    .orElseThrow(() -> new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND",
                            "Không tìm thấy sản phẩm."));
        } catch (NumberFormatException e) {
            return products.findBySlugPublic(idOrSlug)
                    .map(this::toDetail)
                    .orElseThrow(() -> new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND",
                            "Không tìm thấy sản phẩm."));
        }
    }

    /** GET /api/products/page với filter đầy đủ */
    public PageResponse<ProductSummaryResponse> pageFiltered(PageQuery query,
                                                             Long workshopId,
                                                             Long categoryId,
                                                             String tagSlug) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(),
                Sort.by("id").ascending());

        // Filter theo tag: lấy id của tag từ slug
        // Nếu có tag filter thì dùng query khác (native SQL)
        if (tagSlug != null && !tagSlug.isBlank()) {
            return pageFilteredByTag(workshopId, categoryId, tagSlug, pageable);
        }

        Page<Product> page = products.findFiltered(workshopId, categoryId, pageable);
        return PageResponse.from(page.map(this::toSummary));
    }

    /** GET /api/products/{slug}/steps */
    public List<StepResponse> getSteps(String slug) {
        Product product = products.findBySlugPublic(slug)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND",
                        "Không tìm thấy sản phẩm."));
        return stepsOf(product.getId());
    }

    // ----------------------------------------------------------------
    // ARTISAN
    // ----------------------------------------------------------------

    public List<ProductSummaryResponse> listMine(Actor actor) {
        long workshopId = requireWorkshop(actor);
        return products.findByWorkshopAll(workshopId).stream()
                .map(this::toSummary).toList();
    }

    @Transactional
    public ProductDetailResponse create(Actor actor, ProductCreateInput input) {
        long workshopId = requireWorkshop(actor);

        String slug = SlugUtils.uniqueSlug(input.name(), products::existsBySlug);

        Product p = new Product();
        p.setSlug(slug);
        p.setName(input.name().strip());
        p.setDescription(input.description());
        p.setReferencePrice(input.referencePrice());
        p.setPriceVisible(input.priceVisible() == null || input.priceVisible());
        p.setHidden(false);
        p.setFeatured(false);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        if (entities.find(Category.class, input.categoryId()) == null) {
            throw new ApiException(BAD_REQUEST, "INVALID_INPUT", "Danh mục không hợp lệ.");
        }
        p.setWorkshop(entities.getReference(Workshop.class, workshopId));
        p.setCategory(entities.getReference(Category.class, input.categoryId()));
        products.saveAndFlush(p);

        if (input.tagIds() != null && !input.tagIds().isEmpty()) {
            tagRepo.replaceTags(p.getId(), input.tagIds());
        }
        if (input.mediaIds() != null && !input.mediaIds().isEmpty()) {
            validateMediaOwnership(actor.id(), input.mediaIds());
            mediaRepo.replaceLinks("PRODUCT", p.getId(), input.mediaIds());
        }

        return toDetail(products.findById(p.getId())
                .orElseThrow(() -> new ApiException(INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR", "Lỗi tạo sản phẩm.")));
    }

    @Transactional
    public ProductDetailResponse update(Actor actor, long productId,
                                        ProductUpdateInput input) {
        Product p = ownedProduct(actor, productId);
        p.setName(input.name().strip());
        p.setDescription(input.description());
        p.setReferencePrice(input.referencePrice());
        p.setPriceVisible(input.priceVisible() == null || input.priceVisible());
        p.setUpdatedAt(Instant.now());
        if (entities.find(Category.class, input.categoryId()) == null) {
            throw new ApiException(BAD_REQUEST, "INVALID_INPUT", "Danh mục không hợp lệ.");
        }
        p.setWorkshop(entities.getReference(Workshop.class, requireWorkshop(actor)));
        p.setCategory(entities.getReference(Category.class, input.categoryId()));
        products.saveAndFlush(p);

        if (input.tagIds() != null) {
            tagRepo.replaceTags(p.getId(), input.tagIds());
        }
        if (input.mediaIds() != null) {
            validateMediaOwnership(actor.id(), input.mediaIds());
            mediaRepo.replaceLinks("PRODUCT", p.getId(), input.mediaIds());
        }
        return toDetail(p);
    }

    /** "Xóa" = ẩn, không xóa DB vì lô có thể tham chiếu sản phẩm */
    @Transactional
    public void hide(Actor actor, long productId) {
        ownedProduct(actor, productId); // kiểm tra quyền
        products.hide(productId);
    }

    /** PUT /api/artisan/products/{id}/steps — thay toàn bộ công đoạn */
    @Transactional
    public List<StepResponse> updateSteps(Actor actor, long productId,
                                          List<StepInput> steps) {
        Product p = ownedProduct(actor, productId);

        stepRepo.findByProductId(p.getId()).forEach(step ->
                mediaRepo.replaceLinks("PROCESS_STEP", step.getId(), List.of()));
        stepRepo.deleteByProductId(p.getId());
        stepRepo.flush();

        // Tạo mới theo thứ tự
        for (int i = 0; i < steps.size(); i++) {
            StepInput s = steps.get(i);
            ProcessStep step = new ProcessStep();
            step.setProductId(p.getId());
            step.setPosition((short) i);
            step.setName(s.name().strip());
            step.setDescription(s.description());
            stepRepo.saveAndFlush(step);

            if (s.mediaIds() != null && !s.mediaIds().isEmpty()) {
                validateMediaOwnership(actor.id(), s.mediaIds());
                mediaRepo.replaceLinks("PROCESS_STEP", step.getId(), s.mediaIds());
            }
        }

        return stepsOf(p.getId());
    }

    @Transactional
    public void updateTags(Actor actor, long productId, List<Long> tagIds) {
        ownedProduct(actor, productId);
        tagRepo.replaceTags(productId, tagIds);
    }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    public ProductDetailResponse getMine(Actor actor, long id) {
        return toDetail(ownedProduct(actor, id));
    }

    private long requireWorkshop(Actor actor) {
        if (actor.workshop() == null) {
            throw new ApiException(FORBIDDEN, "FORBIDDEN",
                    "Tài khoản không có xưởng.");
        }
        return actor.workshop();
    }

    private Product ownedProduct(Actor actor, long productId) {
        long workshopId = requireWorkshop(actor);
        Product p = products.findById(productId)
                .orElseThrow(() -> new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND",
                        "Không tìm thấy sản phẩm."));
        if (!p.getWorkshop().getId().equals(workshopId)) {
            throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
        }
        return p;
    }

    private void validateMediaOwnership(long accountId, List<Long> mediaIds) {
        for (long mediaId : mediaIds) {
            var media = mediaRepo.findById(mediaId)
                    .orElseThrow(() -> new ApiException(BAD_REQUEST, "INVALID_INPUT",
                            "Ảnh id=" + mediaId + " không tồn tại."));
            if (media.ownerAccountId() != accountId) {
                throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
            }
        }
    }

    private List<StepResponse> stepsOf(long productId) {
        return stepRepo.findByProductId(productId).stream()
                .map(s -> new StepResponse(
                        s.getId(), s.getPosition(),
                        s.getName(), s.getDescription(),
                        mediaRepo.findByEntity("PROCESS_STEP", s.getId(), baseUrl)))
                .toList();
    }

    private ProductDetailResponse toDetail(Product p) {
        List<MediaResponse> images = mediaRepo.findByEntity("PRODUCT", p.getId(), baseUrl);
        List<StepResponse> steps  = stepsOf(p.getId());
        // Giá: ẩn nếu price_visible = false
        var price = Boolean.TRUE.equals(p.getPriceVisible()) ? p.getReferencePrice() : null;

        return new ProductDetailResponse(
                p.getId(), p.getSlug(), p.getName(),
                p.getWorkshop().slug(), p.getWorkshop().name(),
                p.getCategory().name(),
                p.getDescription(),
                price, p.getPriceVisible(),
                tagRepo.names(p.getId()),
                images, steps, batches.findPublicFiltered(null,p.getId(),null,PageRequest.of(0,50)).stream().map(b -> {
                    var media=mediaRepo.findByEntity("BATCH",b.getId(),baseUrl);
                    return new BatchSummaryResponse(b.getId(),b.getPublicCode(),b.getCode(),b.getName(),b.getKind(),
                            b.getQuantity(),b.getUnit(),Boolean.TRUE.equals(b.getPriceVisible())?b.getReferencePrice():null,
                            media.isEmpty()?null:media.get(0),b.getAvailableFrom());
                }).toList(), images.isEmpty()?p.getImagePath():images.get(0).largeUrl()
        );
    }

    private ProductSummaryResponse toSummary(Product p) {
        List<MediaResponse> images = mediaRepo.findByEntity("PRODUCT", p.getId(), baseUrl);
        MediaResponse cover = images.isEmpty() ? null : images.get(0);
        var price = Boolean.TRUE.equals(p.getPriceVisible()) ? p.getReferencePrice() : null;
        return new ProductSummaryResponse(
                p.getId(), p.getSlug(), p.getName(),
                p.getWorkshop().name(), p.getCategory().name(),
                price, cover, p.getDescription(), cover==null?p.getImagePath():cover.thumbUrl());
    }

    // Legacy response — giữ cho /api/products (mảng)
    private ProductResponse legacyResponse(Product p) {
        return new ProductResponse(p.getId(), p.getSlug(), p.getName(),
                p.getWorkshop().name(), p.getCategory().name(),
                p.getDescription(), p.getImagePath());
    }

    // Filter theo tag — sử dụng native query
    private PageResponse<ProductSummaryResponse> pageFilteredByTag(Long workshopId,
                                                                   Long categoryId,
                                                                   String tagSlug,
                                                                   Pageable pageable) {
        Page<Product> page = products.findByTag(workshopId, categoryId, tagSlug, pageable);
        return PageResponse.from(page.map(this::toSummary));
    }
}
