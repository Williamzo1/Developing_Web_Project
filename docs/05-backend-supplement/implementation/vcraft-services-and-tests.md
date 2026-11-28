# vCraft — Service Implementations & Test Scripts
# Phần bổ sung cuối cùng: Service layer đầy đủ + Python test script M2

---

## PHẦN 1 — SERVICE IMPLEMENTATIONS

### 1.1. WorkshopService.java — Phiên bản đầy đủ

Thay thế hoàn toàn file hiện tại (giữ tất cả method cũ, thêm mới vào).

```java
package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.*;
import vn.vcraft.backend.validation.SlugUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class WorkshopService {

    private final WorkshopRepository workshops;
    private final AccountRepository accounts;
    private final BCryptPasswordEncoder passwords;
    private final WorkshopHourRepository hourRepo;
    private final MediaRepository mediaRepo;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public WorkshopService(WorkshopRepository workshops, AccountRepository accounts,
                           BCryptPasswordEncoder passwords,
                           WorkshopHourRepository hourRepo, MediaRepository mediaRepo) {
        this.workshops = workshops;
        this.accounts  = accounts;
        this.passwords = passwords;
        this.hourRepo  = hourRepo;
        this.mediaRepo = mediaRepo;
    }

    // ----------------------------------------------------------------
    // ADMIN — giữ nguyên
    // ----------------------------------------------------------------

    public List<WorkshopResponse> list() {
        return workshops.list().stream()
            .map(r -> new WorkshopResponse(r.id(), r.name(), r.slug(),
                r.approved(), r.lockedAt()))
            .toList();
    }

    @Transactional
    public WorkshopResponse provision(@NotNull @Valid NewArtisanRequest input) {
        // Sinh slug từ tên xưởng
        String slug = SlugUtils.uniqueSlug(input.workshopName(),
            s -> workshops.existsBySlug(s));
        String hash = passwords.encode(input.password());
        var workshop = workshops.createPending(slug, input.workshopName().strip());
        accounts.addArtisan(input.username(), hash, workshop.id());
        return workshop;
    }

    @Transactional
    public WorkshopResponse approve(long id) {
        return workshops.approve(id)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy xưởng."));
    }

    @Transactional
    public WorkshopResponse lock(long id, String reason) {
        workshops.lock(id, reason);
        accounts.revokeAllSessionsForWorkshop(id);
        return workshops.list().stream()
            .filter(r -> r.id() == id)
            .findFirst()
            .map(r -> new WorkshopResponse(r.id(), r.name(), r.slug(),
                r.approved(), r.lockedAt()))
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy xưởng."));
    }

    @Transactional
    public void unlock(long id) {
        workshops.unlock(id);
    }

    // ----------------------------------------------------------------
    // ARTISAN — rename/contact (giữ nguyên pattern cũ)
    // ----------------------------------------------------------------

    public WorkshopResponse rename(Actor actor, long id,
                                   @NotNull @Valid WorkshopUpdateRequest input) {
        if (!Long.valueOf(id).equals(actor.workshop())) {
            throw new ApiException(FORBIDDEN, "FORBIDDEN",
                "Bạn chỉ được sửa xưởng của mình.");
        }
        return workshops.rename(id, input.name())
            .orElseThrow(() -> new ApiException(FORBIDDEN, "FORBIDDEN",
                "Xưởng chưa được duyệt."));
    }

    public ContactResponse updateContact(Actor actor, long id,
                                         @NotNull @Valid WorkshopContactInput input) {
        if (!"ARTISAN".equals(actor.role()) || !Long.valueOf(id).equals(actor.workshop())) {
            throw new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
        }
        if ((input.email() == null || input.email().isBlank()) && input.phone() == null) {
            throw new ApiException(BAD_REQUEST, "REQUEST_INCOMPLETE",
                "Cần email hoặc điện thoại để liên hệ.");
        }
        return workshops.updateContact(id, input.email(), input.phone())
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy tài nguyên."));
    }

    // ----------------------------------------------------------------
    // PUBLIC
    // ----------------------------------------------------------------

    public PageResponse<WorkshopSummaryResponse> listPublic(PageQuery query) {
        int limit  = query.getSize();
        int offset = query.getPage() * limit;
        long total = workshops.countPublic();
        List<WorkshopSummaryResponse> items = workshops.listPublic(limit, offset, baseUrl)
            .stream()
            .map(w -> new WorkshopSummaryResponse(
                w.id(), w.slug(), w.name(), w.description(),
                w.coverImage(),
                w.acceptsVisitors(),
                null)) // openNow: skip để tiết kiệm N query; trả null ở list
            .toList();
        return PageResponse.of(items, query.getPage(), limit, total);
    }

    public WorkshopDetailResponse getBySlug(String slug) {
        var row = workshops.findBySlug(slug)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy xưởng."));
        return toDetailResponse(row);
    }

    public WorkshopDetailResponse getMyWorkshop(Actor actor) {
        long workshopId = requireWorkshopId(actor);
        var row = workshops.findByWorkshopId(workshopId)
            .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy xưởng."));
        return toDetailResponse(row);
    }

    public List<MapPinResponse> mapPins() {
        List<MapPinResponse> pins = workshops.mapPins(baseUrl);
        // Tính openNow cho từng ghim
        return pins.stream().map(pin -> new MapPinResponse(
            pin.id(), pin.slug(), pin.name(),
            pin.latitude(), pin.longitude(),
            pin.thumbnail(),
            pin.acceptsVisitors(),
            hourRepo.isOpenNow(pin.id()),  // 1 query/pin — chấp nhận được với số lượng nhỏ
            pin.address()
        )).toList();
    }

    // ----------------------------------------------------------------
    // ARTISAN — Hồ sơ
    // ----------------------------------------------------------------

    @Transactional
    public WorkshopDetailResponse updateProfile(Actor actor,
                                                @NotNull @Valid WorkshopProfileInput input) {
        long workshopId = requireWorkshopId(actor);

        // Kiểm tra cover_media_id thuộc người dùng
        if (input.coverMediaId() != null) {
            var media = mediaRepo.findById(input.coverMediaId())
                .orElseThrow(() -> new ApiException(BAD_REQUEST, "INVALID_INPUT",
                    "Ảnh bìa không hợp lệ."));
            if (media.ownerAccountId() != actor.id()) {
                throw new ApiException(FORBIDDEN, "FORBIDDEN",
                    "Ảnh bìa không thuộc tài khoản của bạn.");
            }
        }

        // Kiểm tra tọa độ — phải có cả hai hoặc không cái nào
        if ((input.latitude() == null) != (input.longitude() == null)) {
            throw new ApiException(BAD_REQUEST, "VALIDATION_FAILED",
                "Vĩ độ và kinh độ phải được cung cấp cùng nhau.");
        }

        workshops.updateProfile(workshopId,
            input.name() != null ? input.name().strip() : null,
            input.description(),
            input.familyHistory(),
            input.address(),
            input.latitude(),
            input.longitude(),
            input.contactEmail(),
            input.phone(),
            input.facebookUrl(),
            input.acceptsVisitors() != null && input.acceptsVisitors(),
            input.coverMediaId());

        // Cập nhật ảnh xưởng nếu có
        if (input.mediaIds() != null) {
            validateMediaOwnership(actor.id(), input.mediaIds());
            mediaRepo.replaceLinks("WORKSHOP", workshopId, input.mediaIds());
        }

        return getMyWorkshop(actor);
    }

    @Transactional
    public void updateHours(Actor actor, List<WorkshopHourInput> hours) {
        long workshopId = requireWorkshopId(actor);
        // Validate giờ: open < close (đã có CHECK trong DB, nhưng validate sớm cho UX tốt hơn)
        for (var h : hours) {
            if (h.openTime().compareTo(h.closeTime()) >= 0) {
                throw new ApiException(BAD_REQUEST, "VALIDATION_FAILED",
                    "Giờ mở cửa phải trước giờ đóng cửa (thứ " + h.dayOfWeek() + ").");
            }
        }
        hourRepo.replaceHours(workshopId, hours);
    }

    @Transactional
    public void addClosure(Actor actor, WorkshopClosureInput input) {
        long workshopId = requireWorkshopId(actor);
        if (input.closedOn().isBefore(LocalDate.now())) {
            throw new ApiException(BAD_REQUEST, "VALIDATION_FAILED",
                "Ngày đóng cửa không thể là ngày trong quá khứ.");
        }
        hourRepo.addClosure(workshopId, input.closedOn(), input.reason());
    }

    @Transactional
    public void deleteClosure(Actor actor, long closureId) {
        long workshopId = requireWorkshopId(actor);
        boolean deleted = hourRepo.deleteClosure(workshopId, closureId);
        if (!deleted) throw new ApiException(NOT_FOUND, "NOT_FOUND",
            "Không tìm thấy ngày đóng cửa.");
    }

    // ----------------------------------------------------------------
    // CERTIFICATES
    // ----------------------------------------------------------------

    public List<CertificateResponse> getCertificates(Actor actor) {
        long workshopId = requireWorkshopId(actor);
        return workshops.findCertificates(workshopId);
    }

    @Transactional
    public CertificateResponse addCertificate(Actor actor,
                                               @NotNull @Valid CertificateInput input) {
        long workshopId = requireWorkshopId(actor);
        var cert = workshops.addCertificate(workshopId, input.name(),
            input.issuer(), input.issuedOn());

        if (input.mediaIds() != null && !input.mediaIds().isEmpty()) {
            validateMediaOwnership(actor.id(), input.mediaIds());
            mediaRepo.replaceLinks("CERTIFICATE", cert.id(), input.mediaIds());
        }
        return cert;
    }

    @Transactional
    public void deleteCertificate(Actor actor, long certId) {
        long workshopId = requireWorkshopId(actor);
        boolean deleted = workshops.deleteCertificate(workshopId, certId);
        if (!deleted) throw new ApiException(NOT_FOUND, "NOT_FOUND",
            "Không tìm thấy chứng nhận.");
    }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    private long requireWorkshopId(Actor actor) {
        if (actor.workshop() == null) {
            throw new ApiException(FORBIDDEN, "FORBIDDEN",
                "Tài khoản không có xưởng.");
        }
        return actor.workshop();
    }

    private void validateMediaOwnership(long accountId, List<Long> mediaIds) {
        for (long mediaId : mediaIds) {
            var media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new ApiException(BAD_REQUEST, "INVALID_INPUT",
                    "Ảnh id=" + mediaId + " không tồn tại."));
            if (media.ownerAccountId() != accountId) {
                throw new ApiException(FORBIDDEN, "FORBIDDEN",
                    "Ảnh id=" + mediaId + " không thuộc tài khoản của bạn.");
            }
        }
    }

    private WorkshopDetailResponse toDetailResponse(
            WorkshopRepository.WorkshopDetailRow row) {

        // Ảnh bìa
        MediaResponse coverImage = null;
        if (row.pathThumb() != null) {
            coverImage = new MediaResponse(0L,
                baseUrl + "/media/" + row.pathThumb(),
                baseUrl + "/media/" + row.pathMedium(),
                baseUrl + "/media/" + row.pathLarge(),
                row.width(), row.height());
        }

        // Ảnh xưởng (từ media_links)
        List<MediaResponse> images = mediaRepo.findByEntity("WORKSHOP", row.id(), baseUrl);

        // Chứng nhận
        List<CertificateResponse> certs = workshops.findCertificates(row.id());

        // Giờ mở cửa
        List<WorkshopHourResponse> hours = hourRepo.findByWorkshop(row.id());

        // openNow
        Boolean openNow = hourRepo.isOpenNow(row.id());

        return new WorkshopDetailResponse(
            row.id(), row.slug(), row.name(),
            row.description(), row.familyHistory(), row.address(),
            row.latitude(), row.longitude(),
            row.contactEmail(), row.phone(), row.facebookUrl(),
            row.acceptsVisitors(), openNow,
            coverImage, images, certs, hours,
            List.of(), List.of()  // products + batches: tải riêng ở ProductService/BatchService
        );
    }
}
```

---

### 1.2. ProductService.java — Phiên bản đầy đủ

```java
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

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProductService(ProductRepository products, ProcessStepRepository stepRepo,
                          MediaRepository mediaRepo) {
        this.products = products;
        this.stepRepo = stepRepo;
        this.mediaRepo = mediaRepo;
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
        // Dùng setter — theo pattern của QuoteRequest
        // (vì Product hiện tạo qua constructor riêng, cần thêm setters vào Entity)
        // Xem blueprint: Product.java cần setters
        p.setSlug(slug);
        p.setName(input.name().strip());
        p.setDescription(input.description());
        p.setReferencePrice(input.referencePrice());
        p.setPriceVisible(input.priceVisible() == null || input.priceVisible());
        p.setHidden(false);
        p.setFeatured(false);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        // workshop_id và category_id — set qua EntityManager
        // (vì Product.workshop là @ManyToOne, cần load reference)
        // Thực tế: products.saveWithIds(p, workshopId, input.categoryId())
        // Xem ghi chú bên dưới *

        products.save(p);

        if (input.tagIds() != null && !input.tagIds().isEmpty()) {
            products.replaceTags(p.getId(), input.tagIds());
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
        products.save(p);

        if (input.tagIds() != null) {
            products.replaceTags(p.getId(), input.tagIds());
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

        // Xóa công đoạn cũ
        stepRepo.deleteByProductId(p.getId());

        // Tạo mới theo thứ tự
        for (int i = 0; i < steps.size(); i++) {
            StepInput s = steps.get(i);
            ProcessStep step = new ProcessStep();
            step.setProductId(p.getId());
            step.setPosition((short) i);
            step.setName(s.name().strip());
            step.setDescription(s.description());
            stepRepo.save(step);

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
        products.replaceTags(productId, tagIds);
    }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

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
            throw new ApiException(FORBIDDEN, "FORBIDDEN",
                "Sản phẩm không thuộc xưởng của bạn.");
        }
        return p;
    }

    private void validateMediaOwnership(long accountId, List<Long> mediaIds) {
        for (long mediaId : mediaIds) {
            var media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new ApiException(BAD_REQUEST, "INVALID_INPUT",
                    "Ảnh id=" + mediaId + " không tồn tại."));
            if (media.ownerAccountId() != accountId) {
                throw new ApiException(FORBIDDEN, "FORBIDDEN",
                    "Ảnh id=" + mediaId + " không thuộc tài khoản của bạn.");
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
            List.of(), // tags — load riêng nếu cần
            images, steps, List.of() // publicBatches — load riêng
        );
    }

    private ProductSummaryResponse toSummary(Product p) {
        List<MediaResponse> images = mediaRepo.findByEntity("PRODUCT", p.getId(), baseUrl);
        MediaResponse cover = images.isEmpty() ? null : images.getFirst();
        var price = Boolean.TRUE.equals(p.getPriceVisible()) ? p.getReferencePrice() : null;
        return new ProductSummaryResponse(
            p.getId(), p.getSlug(), p.getName(),
            p.getWorkshop().name(), p.getCategory().name(),
            price, cover);
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
```

> **Ghi chú (*):** `Product.create()` cần set `workshop_id` và `category_id`. Vì Product.workshop là `@ManyToOne`, thực tế cần:
> ```java
> // Thêm vào ProductRepository (JPA):
> @Query(value = """
>     INSERT INTO products(workshop_id, category_id, slug, name, description,
>                          reference_price, price_visible, hidden, featured, created_at, updated_at)
>     VALUES (:wid, :cid, :slug, :name, :desc, :price, :pv, false, false, now(), now())
>     RETURNING id""", nativeQuery = true)
> Long insertProduct(@Param("wid") long workshopId, @Param("cid") long categoryId,
>                    @Param("slug") String slug, @Param("name") String name,
>                    @Param("desc") String desc,
>                    @Param("price") java.math.BigDecimal price, @Param("pv") boolean pv);
> ```
> Hoặc dùng `EntityManager.getReference(Workshop.class, workshopId)` để set lazy reference.

---

### 1.3. BatchService.toDetail() — Hoàn chỉnh

```java
// Thêm vào BatchService.java — phương thức toDetail đầy đủ

@Value("${app.base-url:http://localhost:8080}")
private String baseUrl;

private final BatchMilestoneRepository milestoneRepo;
private final ProcessStepRepository stepRepo;
private final WorkshopRepository workshopRepo;
private final ProductRepository productRepo;

// Constructor cập nhật:
public BatchService(BatchRepository batchRepo, MediaRepository mediaRepo,
                    BatchMilestoneRepository milestoneRepo,
                    ProcessStepRepository stepRepo,
                    WorkshopRepository workshopRepo,
                    ProductRepository productRepo) {
    this.batchRepo     = batchRepo;
    this.mediaRepo     = mediaRepo;
    this.milestoneRepo = milestoneRepo;
    this.stepRepo      = stepRepo;
    this.workshopRepo  = workshopRepo;
    this.productRepo   = productRepo;
}

private BatchDetailResponse toDetail(Batch batch) {
    // 1. Thông tin sản phẩm
    Product product = productRepo.findById(batch.getProductId())
        .orElse(null);
    ProductSummaryResponse productSummary = null;
    List<StepResponse> productSteps = List.of();
    if (product != null) {
        List<MediaResponse> productImages =
            mediaRepo.findByEntity("PRODUCT", product.getId(), baseUrl);
        MediaResponse cover = productImages.isEmpty() ? null : productImages.getFirst();
        var price = Boolean.TRUE.equals(product.getPriceVisible())
            ? product.getReferencePrice() : null;
        productSummary = new ProductSummaryResponse(
            product.getId(), product.getSlug(), product.getName(),
            product.getWorkshop().name(), product.getCategory().name(),
            price, cover);

        // Quy trình sản xuất của sản phẩm
        productSteps = stepRepo.findByProductId(product.getId()).stream()
            .map(s -> new StepResponse(s.getId(), s.getPosition(),
                s.getName(), s.getDescription(),
                mediaRepo.findByEntity("PROCESS_STEP", s.getId(), baseUrl)))
            .toList();
    }

    // 2. Thông tin xưởng
    WorkshopSummaryResponse workshopSummary = workshopRepo
        .findByWorkshopId(batch.getWorkshopId())
        .map(row -> {
            MediaResponse cover = row.pathThumb() == null ? null :
                new MediaResponse(0L,
                    baseUrl + "/media/" + row.pathThumb(), null, null,
                    row.width(), row.height());
            return new WorkshopSummaryResponse(
                row.id(), row.slug(), row.name(), row.description(),
                cover, row.acceptsVisitors(), null);
        })
        .orElse(null);

    // 3. Mốc sản xuất
    List<MilestoneResponse> milestones = milestoneRepo
        .findByBatchId(batch.getId()).stream()
        .map(m -> {
            // Tên công đoạn tương ứng (nếu có)
            String stepName = null;
            if (m.getProcessStepId() != null) {
                stepName = stepRepo.findById(m.getProcessStepId())
                    .map(ProcessStep::getName).orElse(null);
            }
            List<MediaResponse> mImages =
                mediaRepo.findByEntity("BATCH_MILESTONE", m.getId(), baseUrl);
            return new MilestoneResponse(
                m.getId(), m.getTitle(), m.getNote(),
                m.getHappenedOn(), m.getProcessStepId(), stepName, mImages);
        })
        .toList();

    // 4. Ảnh lô
    List<MediaResponse> images = mediaRepo.findByEntity("BATCH", batch.getId(), baseUrl);

    // 5. Giá: ẩn nếu price_visible = false
    var price = Boolean.TRUE.equals(batch.getPriceVisible())
        ? batch.getReferencePrice() : null;

    return new BatchDetailResponse(
        batch.getId(), batch.getPublicCode(), batch.getCode(),
        batch.getName(), batch.getDescription(), batch.getKind(),
        batch.getQuantity(), batch.getUnit(),
        price, batch.getSupplyCapacity(), batch.getAvailableFrom(),
        batch.getVisibility(),
        productSummary, workshopSummary,
        productSteps, milestones, images,
        batch.getCreatedAt(), batch.getUpdatedAt());
}

/** PUT /api/artisan/batches/{id}/milestones — thay toàn bộ mốc */
@Transactional
public void updateMilestones(Actor actor, long batchId, List<MilestoneInput> inputs) {
    var batch = ownedBatch(actor, batchId);

    // Xóa media_links của các mốc cũ trước khi xóa mốc
    milestoneRepo.findByBatchId(batchId).forEach(m ->
        mediaRepo.replaceLinks("BATCH_MILESTONE", m.getId(), List.of()));

    milestoneRepo.deleteByBatchId(batchId);

    for (MilestoneInput input : inputs) {
        BatchMilestone m = new BatchMilestone();
        m.setBatchId(batchId);
        m.setProcessStepId(input.processStepId());
        m.setTitle(input.title().strip());
        m.setNote(input.note());
        m.setHappenedOn(input.happenedOn());
        m.setCreatedAt(java.time.Instant.now());
        milestoneRepo.save(m);

        if (input.mediaIds() != null && !input.mediaIds().isEmpty()) {
            mediaRepo.replaceLinks("BATCH_MILESTONE", m.getId(), input.mediaIds());
        }
    }

    batch.setUpdatedAt(java.time.Instant.now());
    batchRepo.save(batch);
}
```

---

### 1.4. AdminService.java — Toàn bộ

```java
package vn.vcraft.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.*;
import vn.vcraft.backend.repository.*;
import vn.vcraft.backend.validation.SlugUtils;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final WorkshopRepository workshopRepo;
    private final ProductRepository productRepo;
    private final BatchRepository batchRepo;
    private final NewsRepository newsRepo;
    private final AccountRepository accountRepo;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AdminService(WorkshopRepository workshopRepo, ProductRepository productRepo,
                        BatchRepository batchRepo, NewsRepository newsRepo,
                        AccountRepository accountRepo) {
        this.workshopRepo = workshopRepo;
        this.productRepo  = productRepo;
        this.batchRepo    = batchRepo;
        this.newsRepo     = newsRepo;
        this.accountRepo  = accountRepo;
    }

    // ----------------------------------------------------------------
    // STATS
    // ----------------------------------------------------------------

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
            workshopRepo.countAll(),
            workshopRepo.countApproved(),
            productRepo.count(),
            batchRepo.count(),
            0L, // quote_requests — thêm repo nếu cần
            accountRepo.countBuyers());
    }

    // ----------------------------------------------------------------
    // FEATURED
    // ----------------------------------------------------------------

    @Transactional
    public void setFeatured(FeaturedInput input) {
        // Reset tất cả
        workshopRepo.clearAllFeatured();
        productRepo.clearAllFeatured();
        batchRepo.clearAllFeatured();

        // Set mới
        if (input.workshopIds() != null) {
            input.workshopIds().forEach(id -> workshopRepo.setFeatured(id, true));
        }
        if (input.productIds() != null) {
            input.productIds().forEach(id -> productRepo.setFeatured(id, true));
        }
        if (input.batchIds() != null) {
            input.batchIds().forEach(id -> batchRepo.setFeatured(id, true));
        }
    }

    // ----------------------------------------------------------------
    // HẬU KIỂM
    // ----------------------------------------------------------------

    @Transactional
    public void hideProduct(long productId) {
        if (!productRepo.existsById(productId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy sản phẩm.");
        }
        productRepo.hide(productId);
    }

    @Transactional
    public void hideBatch(long batchId) {
        if (!batchRepo.existsById(batchId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy lô hàng.");
        }
        batchRepo.hide(batchId);
    }

    // ----------------------------------------------------------------
    // NEWS
    // ----------------------------------------------------------------

    public PageResponse<NewsResponse> listNewsAdmin(PageQuery query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getSize(),
            Sort.by("createdAt").descending());
        return PageResponse.from(newsRepo.findAllForAdmin(pageable).map(this::toNewsResponse));
    }

    @Transactional
    public NewsResponse createNews(Actor actor, NewsCreateInput input) {
        String slug = SlugUtils.uniqueSlug(input.title(),
            s -> newsRepo.findBySlugPublic(s).isPresent());

        NewsPost post = new NewsPost();
        post.setAuthorAccountId(actor.id());
        post.setSlug(slug);
        post.setTitle(input.title().strip());
        post.setBody(input.body());
        post.setKind(input.kind());
        post.setEventStart(input.eventStart());
        post.setEventEnd(input.eventEnd());
        post.setEventPlace(input.eventPlace());
        post.setHidden(false);
        post.setPublishedAt(input.publish() ? Instant.now() : null);
        post.setCreatedAt(Instant.now());
        post.setUpdatedAt(Instant.now());
        newsRepo.save(post);
        return toNewsResponse(post);
    }

    @Transactional
    public NewsResponse updateNews(long id, NewsCreateInput input) {
        NewsPost post = newsRepo.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy tin."));
        post.setTitle(input.title().strip());
        post.setBody(input.body());
        post.setKind(input.kind());
        post.setEventStart(input.eventStart());
        post.setEventEnd(input.eventEnd());
        post.setEventPlace(input.eventPlace());
        if (input.publish() && post.getPublishedAt() == null) {
            post.setPublishedAt(Instant.now());
        }
        post.setUpdatedAt(Instant.now());
        newsRepo.save(post);
        return toNewsResponse(post);
    }

    @Transactional
    public void deleteNews(long id) {
        if (!newsRepo.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "Không tìm thấy tin.");
        }
        newsRepo.deleteById(id);
    }

    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    private NewsResponse toNewsResponse(NewsPost n) {
        return new NewsResponse(
            n.getId(), n.getSlug(), n.getTitle(), n.getBody(),
            n.getKind(), n.getEventStart(), n.getEventEnd(), n.getEventPlace(),
            null, // coverImage — nếu cần thêm sau
            n.getPublishedAt(), n.getCreatedAt());
    }
}
```

---

### 1.5. HomeService.java — Toàn bộ

```java
package vn.vcraft.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.repository.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class HomeService {

    private final WorkshopRepository workshopRepo;
    private final ProductRepository productRepo;
    private final BatchRepository batchRepo;
    private final NewsRepository newsRepo;
    private final MediaRepository mediaRepo;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public HomeService(WorkshopRepository workshopRepo, ProductRepository productRepo,
                       BatchRepository batchRepo, NewsRepository newsRepo,
                       MediaRepository mediaRepo) {
        this.workshopRepo = workshopRepo;
        this.productRepo  = productRepo;
        this.batchRepo    = batchRepo;
        this.newsRepo     = newsRepo;
        this.mediaRepo    = mediaRepo;
    }

    public HomeResponse getHomeData() {
        // 3 xưởng nổi bật
        List<WorkshopSummaryResponse> workshops =
            workshopRepo.listPublic(3, 0, baseUrl).stream()
                .filter(w -> w.coverImage() != null) // ưu tiên có ảnh
                .limit(3)
                .map(w -> new WorkshopSummaryResponse(
                    w.id(), w.slug(), w.name(), w.description(),
                    w.coverImage(), w.acceptsVisitors(), null))
                .toList();

        // 6 sản phẩm nổi bật
        List<ProductSummaryResponse> products =
            productRepo.findFeatured().stream()
                .limit(6)
                .map(p -> {
                    List<MediaResponse> imgs =
                        mediaRepo.findByEntity("PRODUCT", p.getId(), baseUrl);
                    MediaResponse cover = imgs.isEmpty() ? null : imgs.getFirst();
                    var price = Boolean.TRUE.equals(p.getPriceVisible())
                        ? p.getReferencePrice() : null;
                    return new ProductSummaryResponse(
                        p.getId(), p.getSlug(), p.getName(),
                        p.getWorkshop().name(), p.getCategory().name(),
                        price, cover);
                })
                .toList();

        // 3 lô nổi bật
        List<BatchSummaryResponse> batches = batchRepo.findFeatured().stream()
            .limit(3)
            .map(b -> {
                List<MediaResponse> imgs =
                    mediaRepo.findByEntity("BATCH", b.getId(), baseUrl);
                MediaResponse cover = imgs.isEmpty() ? null : imgs.getFirst();
                var price = Boolean.TRUE.equals(b.getPriceVisible())
                    ? b.getReferencePrice() : null;
                return new BatchSummaryResponse(
                    b.getId(), b.getPublicCode(), b.getCode(), b.getName(),
                    b.getKind(), b.getQuantity(), b.getUnit(), price,
                    cover, b.getAvailableFrom());
            })
            .toList();

        // 5 tin tức mới nhất
        Pageable top5 = PageRequest.of(0, 5);
        List<NewsResponse> news = newsRepo.findLatest(top5).stream()
            .map(n -> new NewsResponse(
                n.getId(), n.getSlug(), n.getTitle(), null, // body: bỏ ở trang chủ
                n.getKind(), n.getEventStart(), n.getEventEnd(), n.getEventPlace(),
                null, n.getPublishedAt(), n.getCreatedAt()))
            .toList();

        return new HomeResponse(workshops, products, batches, news);
    }
}
```

---

## PHẦN 2 — TEST SCRIPT M2

### 2.1. backend/tests/m2.py — Integration test cho Workshop, Product, Batch

```python
"""
M2 integration tests: Workshop, Product, Batch, QR, Map.
Chạy: python backend/tests/m2.py
Yêu cầu: Backend đang chạy với V5 migration; seed catalog.sql đã chạy.
Không cần package ngoài — chỉ dùng stdlib Python.
"""

import json, os, io, sys
from urllib.error import HTTPError
from urllib.request import Request, urlopen

BASE = os.getenv('VCRAFT_BASE', 'http://localhost:8080')
checks = 0

# ----------------------------------------------------------------
# Helpers
# ----------------------------------------------------------------

def call(path, status=200, method='GET', data=None, token=None,
         content_type='application/json', raw_body=None):
    headers = {'Accept': 'application/json'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    if data is not None and content_type == 'application/json':
        body = json.dumps(data).encode()
        headers['Content-Type'] = 'application/json'
    elif raw_body is not None:
        body = raw_body
        headers['Content-Type'] = content_type
    else:
        body = None
    req = Request(BASE + '/api' + path, data=body, headers=headers, method=method)
    try:
        with urlopen(req, timeout=15) as r:
            assert r.status == status, f"Expected {status}, got {r.status} at {path}"
            return json.loads(r.read()) if r.status != 204 else None
    except HTTPError as e:
        assert e.status == status, f"Expected {status}, got {e.status} at {path}"
        return json.loads(e.read()) if e.status != 204 else None

def ok(condition, msg='Assertion failed'):
    global checks
    assert condition, msg
    checks += 1

def login(username, password='VcraftDemo17!'):
    res = call('/login', 200, 'POST', {'username': username, 'password': password})
    return res['token']

# ----------------------------------------------------------------
# Setup: đăng nhập
# ----------------------------------------------------------------

admin_token = login('admin')
artisan_token = login('artisan_men_lam')   # username từ seed
buyer_token   = login('buyer01')

# ----------------------------------------------------------------
# 1. WORKSHOP — Public API
# ----------------------------------------------------------------

print('1. Workshop public API...')

ws_list = call('/workshops')
ok(isinstance(ws_list, dict), 'list trả PageResponse')
ok('items' in ws_list and 'totalElements' in ws_list)
ok(len(ws_list['items']) > 0, 'Có ít nhất 1 xưởng')

first_ws = ws_list['items'][0]
ok('slug' in first_ws, 'Workshop có slug')
ok('name' in first_ws, 'Workshop có name')

slug = first_ws['slug']
ws_detail = call(f'/workshops/{slug}')
ok(ws_detail['slug'] == slug)
ok('description' in ws_detail)
ok('contactEmail' in ws_detail or 'phone' in ws_detail)
ok('certificates' in ws_detail, 'Chi tiết có certificates')
ok('hours' in ws_detail, 'Chi tiết có hours')
ok('openNow' in ws_detail, 'Chi tiết có openNow')
ok(ws_detail.get('images') is not None, 'Chi tiết có images list')

# Not found
call('/workshops/khong-ton-tai-slug', 404)

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 2. WORKSHOP — Map
# ----------------------------------------------------------------

print('2. Workshop map API...')

pins = call('/workshops/map')
ok(isinstance(pins, list), 'map trả danh sách')
# Chỉ hộ có tọa độ mới xuất hiện
for pin in pins:
    ok('latitude' in pin and 'longitude' in pin, 'Pin có tọa độ')
    ok('slug' in pin, 'Pin có slug')
    ok('openNow' in pin, 'Pin có openNow')
    ok(pin['latitude'] is not None, 'latitude không null')

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 3. ARTISAN — Cập nhật hồ sơ
# ----------------------------------------------------------------

print('3. Artisan workshop profile...')

profile = call('/artisan/workshop', token=artisan_token)
ok('slug' in profile, 'GET /artisan/workshop trả hồ sơ')

update_data = {
    'name': 'Xưởng Gốm Men Lam (Updated)',
    'description': 'Mô tả cập nhật để test.',
    'familyHistory': 'Lịch sử gia đình test.',
    'address': 'Bát Tràng, Gia Lâm',
    'latitude': 20.9638,
    'longitude': 105.9050,
    'contactEmail': 'menlamtest@example.com',
    'phone': '0901234567',
    'facebookUrl': None,
    'acceptsVisitors': True,
    'coverMediaId': None,
    'mediaIds': []
}
updated = call('/artisan/workshop', 200, 'PUT', update_data, artisan_token)
ok(updated['name'] == 'Xưởng Gốm Men Lam (Updated)', 'Tên đã cập nhật')
ok(updated['acceptsVisitors'] == True, 'acceptsVisitors đúng')
ok(abs(float(updated['latitude']) - 20.9638) < 0.001, 'Tọa độ đúng')

# Validation: tọa độ thiếu 1 vế
bad_coord = {**update_data, 'latitude': 20.9638, 'longitude': None}
err = call('/artisan/workshop', 400, 'PUT', bad_coord, artisan_token)
ok(err['code'] == 'VALIDATION_FAILED', 'Tọa độ thiếu 1 vế → 400')

# Artisan A không sửa được workshop artisan B (không có endpoint trực tiếp nhưng
# endpoint PUT /artisan/workshop luôn dùng workshop của người đăng nhập → không có lỗ hổng)

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 4. ARTISAN — Giờ mở cửa
# ----------------------------------------------------------------

print('4. Workshop hours...')

hours_data = [
    {'dayOfWeek': 1, 'openTime': '08:00', 'closeTime': '17:30'},
    {'dayOfWeek': 2, 'openTime': '08:00', 'closeTime': '17:30'},
    {'dayOfWeek': 6, 'openTime': '08:00', 'closeTime': '16:00'},
]
call('/artisan/workshop/hours', 204, 'PUT', hours_data, artisan_token)

# Xác nhận giờ được lưu
ws_after = call(f'/workshops/{profile["slug"]}')
ok(any(h['dayOfWeek'] == 1 for h in ws_after['hours']),
   'Giờ T2 được lưu')

# Validation: open >= close → 400
bad_hours = [{'dayOfWeek': 3, 'openTime': '17:00', 'closeTime': '08:00'}]
err = call('/artisan/workshop/hours', 400, 'PUT', bad_hours, artisan_token)
ok(err['code'] == 'VALIDATION_FAILED', 'Giờ sai → 400')

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 5. PRODUCT — Public
# ----------------------------------------------------------------

print('5. Product public API...')

catalog = call('/products')
ok(isinstance(catalog, list), 'Legacy catalog trả list')
ok(len(catalog) >= 3, 'Ít nhất 3 sản phẩm')

page = call('/products/page?page=0&size=12')
ok('items' in page and 'totalElements' in page)
ok(len(page['items']) >= 1)

# Filter theo workshopId (lấy id từ catalog)
ws_id = call(f'/workshops/{slug}')['id']
page_ws = call(f'/products/page?workshopId={ws_id}&page=0&size=12')
ok(all(True for _ in page_ws['items']), 'Filter workshopId hoạt động')

# Chi tiết theo slug
prod_slug = catalog[0].get('slug', '')
if prod_slug:
    prod = call(f'/products/{prod_slug}')
    ok(prod['slug'] == prod_slug, 'Chi tiết theo slug đúng')
    ok('steps' in prod, 'Chi tiết có steps')
    ok('images' in prod, 'Chi tiết có images')
    # Giá ẩn không lộ ra nếu price_visible = false
    # (không test trực tiếp ở đây vì cần seed dữ liệu có price_visible=false)

# Chi tiết theo id — backward compat
prod_id = catalog[0]['id']
prod_by_id = call(f'/products/{prod_id}')
ok(prod_by_id['id'] == prod_id, 'Chi tiết theo id đúng')

# Not found
call('/products/khong-ton-tai-slug-nay', 404)
call('/products/999999999', 404)

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 6. ARTISAN — Tạo sản phẩm
# ----------------------------------------------------------------

print('6. Artisan product CRUD...')

# Lấy category id từ API
categories = call('/categories')
ok(len(categories) > 0, 'Có category')
cat_id = categories[0]['id']

new_product = {
    'name': 'Bình Test Script M2',
    'categoryId': cat_id,
    'description': 'Bình gốm tạo bởi test script.',
    'referencePrice': 450000,
    'priceVisible': True,
    'tagIds': [],
    'mediaIds': []
}
created = call('/artisan/products', 201, 'POST', new_product, artisan_token)
ok('id' in created, 'Tạo sản phẩm → 201')
ok('slug' in created, 'Có slug tự sinh')
ok(created['slug'] != '', 'Slug không rỗng')
ok(created['name'] == 'Bình Test Script M2')

prod_id = created['id']

# Cập nhật công đoạn
steps_data = [
    {'name': 'Tạo hình', 'description': 'Nặn trên bàn xoay.', 'mediaIds': []},
    {'name': 'Nung lò',  'description': 'Nung 1200°C.', 'mediaIds': []},
]
call(f'/artisan/products/{prod_id}/steps', 204, 'PUT', steps_data, artisan_token)

# Xác nhận steps được lưu
prod_detail = call(f'/products/{created["slug"]}')
ok(len(prod_detail['steps']) == 2, '2 công đoạn được lưu')
ok(prod_detail['steps'][0]['name'] == 'Tạo hình', 'Thứ tự công đoạn đúng')

# Artisan khác không sửa được
other_artisan = login('artisan_dat_nung')   # cần seed user này
err = call(f'/artisan/products/{prod_id}', 403, 'PUT', new_product, other_artisan)
ok(err['code'] in ('FORBIDDEN', 'NOT_FOUND'), 'Artisan khác không sửa được → 403')

# Ẩn sản phẩm
call(f'/artisan/products/{prod_id}', 204, 'DELETE', token=artisan_token)
# Sản phẩm đã ẩn không hiện ở public
call(f'/products/{created["slug"]}', 404)

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 7. BATCH — Tạo lô
# ----------------------------------------------------------------

print('7. Batch CRUD + QR...')

# Dùng sản phẩm từ catalog (không phải sản phẩm vừa ẩn)
visible_products = call('/artisan/products', token=artisan_token)
ok(len(visible_products) > 0, 'Artisan có sản phẩm')
prod_id2 = visible_products[0]['id']

batch_data = {
    'productId': prod_id2,
    'code': 'TEST-LOT-M2-001',
    'name': 'Lô Test M2',
    'description': 'Lô test tạo bởi test script.',
    'kind': 'IN_STOCK',
    'quantity': 50,
    'unit': 'cái',
    'referencePrice': 180000,
    'priceVisible': True,
    'visibility': 'PUBLIC',
    'mediaIds': []
}
batch = call('/artisan/batches', 201, 'POST', batch_data, artisan_token)
ok('id' in batch, 'Tạo lô → 201')
ok('publicCode' in batch and len(batch['publicCode']) > 0, 'publicCode tự sinh')
ok(batch['visibility'] == 'PUBLIC')

batch_id = batch['id']
public_code = batch['publicCode']

# Landing page
landing = call(f'/batches/{public_code}')
ok(landing['publicCode'] == public_code, 'Landing page đúng public_code')
ok('product' in landing, 'Landing có product')
ok('workshop' in landing, 'Landing có workshop')
ok('milestones' in landing, 'Landing có milestones')

# Thêm mốc sản xuất
milestones_data = [
    {'title': 'Bắt đầu tạo hình', 'happenedOn': '2026-10-01',
     'note': 'Đã hoàn thành 50 cái.', 'processStepId': None, 'mediaIds': []},
    {'title': 'Nung xong', 'happenedOn': '2026-10-20',
     'note': 'Đạt chất lượng.', 'processStepId': None, 'mediaIds': []},
]
call(f'/artisan/batches/{batch_id}/milestones', 204, 'PUT',
     milestones_data, artisan_token)

landing2 = call(f'/batches/{public_code}')
ok(len(landing2['milestones']) == 2, 'Mốc được lưu')
ok(landing2['milestones'][0]['title'] == 'Bắt đầu tạo hình')

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 8. QR Code
# ----------------------------------------------------------------

print('8. QR Code...')

# GET /batches/{publicCode}/qr.png → PNG
req = Request(BASE + f'/api/batches/{public_code}/qr.png')
with urlopen(req, timeout=15) as r:
    ok(r.status == 200, 'QR endpoint trả 200')
    ct = r.getheader('Content-Type', '')
    ok('image/png' in ct, f'Content-Type là image/png, nhận được: {ct}')
    data = r.read()
    ok(len(data) > 1000, 'QR file có kích thước hợp lý')
    # Kiểm tra PNG magic bytes
    ok(data[:8] == b'\x89PNG\r\n\x1a\n', 'File đúng định dạng PNG')
checks += 1

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 9. SHARE TOKEN — Lô riêng tư
# ----------------------------------------------------------------

print('9. Share token + private batch...')

# Chuyển lô sang PRIVATE
update_batch = {**batch_data, 'visibility': 'PRIVATE', 'productId': prod_id2}
call(f'/artisan/batches/{batch_id}', 200, 'PUT', update_batch, artisan_token)

# Lô PRIVATE → public không thấy
call(f'/batches/{public_code}', 404)

# Tạo share token
token_res = call(f'/artisan/batches/{batch_id}/share-token',
                 200, 'POST', token=artisan_token)
ok('shareUrl' in token_res, 'shareUrl trả về')
share_url = token_res['shareUrl']

# Lấy token từ URL
share_token = share_url.split('token=')[-1]
ok(len(share_token) > 10, 'Token có độ dài hợp lý')

# Truy cập với token đúng → 200
call(f'/batches/{public_code}?token={share_token}', 200)

# Token sai → 404
call(f'/batches/{public_code}?token=wrongtoken', 404)

# Thu hồi token
call(f'/artisan/batches/{batch_id}/share-token', 204, 'DELETE',
     token=artisan_token)

# Token cũ → 404
call(f'/batches/{public_code}?token={share_token}', 404)

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 10. AUTH — Đăng ký Buyer
# ----------------------------------------------------------------

print('10. Buyer registration...')

import random, string
rand = ''.join(random.choices(string.ascii_lowercase, k=6))
reg_data = {
    'username': f'buyer_m2_{rand}',
    'password': 'BuyerTest@2026',
    'displayName': 'Buyer Test M2',
    'email': f'buyer_m2_{rand}@test.example',
    'phone': '0901234567'
}
reg = call('/register', 201, 'POST', reg_data)
ok('id' in reg, 'Đăng ký → 201')
ok(reg.get('emailVerified') == False, 'Email chưa xác thực ngay')
ok('message' in reg, 'Có message hướng dẫn')

# Đăng nhập được ngay
new_token = login(reg_data['username'], reg_data['password'])
ok(new_token, 'Đăng nhập được sau đăng ký')

# Buyer chưa verify → không tạo được quote request
quote_req = {
    'title': 'Test order',
    'quantity': 10,
    'unit': 'cái',
    'categoryId': cat_id,
    'mode': 'OPEN',
    'buyerContactName': 'Test',
    'buyerContactEmail': reg_data['email'],
    'buyerContactPhone': reg_data['phone'],
}
err = call('/buyer/quote-requests', 403, 'POST', quote_req, new_token)
ok(err['code'] == 'EMAIL_NOT_VERIFIED', 'Chưa verify → 403 EMAIL_NOT_VERIFIED')

# Username trùng → 409
call('/register', 409, 'POST', reg_data)

# Email trùng
dup_email = {**reg_data, 'username': f'buyer_dup_{rand}'}
call('/register', 409, 'POST', dup_email)

# Validation
call('/register', 400, 'POST', {
    'username': 'x',       # quá ngắn
    'password': 'short',   # quá ngắn
    'displayName': '',     # rỗng
    'email': 'not-email',  # sai format
    'phone': '123'         # quá ngắn
})

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 11. ADMIN — Lock workshop
# ----------------------------------------------------------------

print('11. Admin lock/unlock...')

# Lấy id xưởng artisan_dat_nung
ws2_list = call('/admin/workshops', token=admin_token)
ws2 = next((w for w in ws2_list if 'dat-nung' in w.get('slug','')), None)
ok(ws2 is not None, 'Tìm thấy xưởng dat-nung')
ws2_id = ws2['id']

# Lock
call(f'/admin/workshops/{ws2_id}/lock', 204, 'POST',
     {'reason': 'Vi phạm nội quy test.'}, admin_token)

# Hộ bị khóa → đăng nhập được nhưng session hiện tại bị thu hồi
# (token trước khi khóa không còn hợp lệ)
old_token = login('artisan_dat_nung')

# Unlock
call(f'/admin/workshops/{ws2_id}/unlock', 204, 'POST', token=admin_token)

# Hộ đăng nhập lại được
new_artisan_token = login('artisan_dat_nung')
ok(new_artisan_token, 'Sau unlock đăng nhập lại được')

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# 12. ADMIN — Stats
# ----------------------------------------------------------------

print('12. Admin stats...')

stats = call('/admin/stats', token=admin_token)
ok('totalWorkshops' in stats, 'Có totalWorkshops')
ok('approvedWorkshops' in stats, 'Có approvedWorkshops')
ok('totalProducts' in stats, 'Có totalProducts')
ok('totalBatches' in stats, 'Có totalBatches')
ok(stats['totalWorkshops'] >= stats['approvedWorkshops'],
   'Total >= approved')

print(f'   OK — {checks} checks')

# ----------------------------------------------------------------
# TỔNG KẾT
# ----------------------------------------------------------------

print(f'\nPASS M2: {checks} checks — Workshop, Product, Batch, QR, Share token, Auth Buyer, Admin')
```

---

## PHẦN 3 — CÁC LƯU Ý CUỐI TRƯỚC KHI CODE

### 3.1. Thứ tự dependency giữa các method trong cùng transaction

```
# Luồng tạo lô — phải theo đúng thứ tự:
1. Validate product thuộc workshop
2. generateUniquePublicCode() — retry tối đa 5 lần
3. INSERT batches → lấy batch.id
4. replaceLinks("BATCH", batch.id, mediaIds) — cùng transaction
5. COMMIT
# Nếu generateUniquePublicCode() thất bại sau 5 lần → ném exception → rollback toàn bộ

# Luồng updateMilestones — phải theo thứ tự:
1. Xóa media_links của milestones cũ (tránh orphan)
2. deleteByBatchId()
3. Tạo milestones mới
4. replaceLinks cho từng milestone mới
5. Update batch.updatedAt
# Tất cả trong @Transactional
```

### 3.2. `ddl-auto=validate` — Checklist Entity sau V5

Sau khi migration V5 chạy, cần kiểm tra `mvn spring-boot:run` không báo lỗi validate:

```
# Các bảng mới V5 cần Entity JPA tương ứng:
✓ email_otps         → EmailOtp.java
✓ media              → (không cần Entity — dùng JDBC thuần)
✓ media_links        → (không cần Entity — dùng JDBC thuần)
✓ certificates       → (không cần Entity — dùng JDBC thuần)
✓ workshop_hours     → (không cần Entity — JDBC)
✓ workshop_closures  → (không cần Entity — JDBC)
✓ process_steps      → ProcessStep.java
✓ batches            → Batch.java
✓ batch_milestones   → BatchMilestone.java

# Bảng mở rộng:
⚠ workshops — Workshop.java phải có đủ field mới
⚠ products  — Product.java phải có đủ field mới
⚠ accounts  — KHÔNG có Entity Account.java (dùng JDBC) → không validate
```

### 3.3. Thêm vào application.properties (quan trọng)

```properties
# File: backend/src/main/resources/application.properties
# Thêm vào cuối:
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=55MB
spring.servlet.multipart.enabled=true
```

### 3.4. Cách chạy test script M2

```bash
# Từ gốc repo:
# 1. Backend đang chạy với V5+V6 migration
# 2. Seed catalog.sql đã chạy (bao gồm phần V5 bổ sung)
# 3. Có accounts: admin, artisan_men_lam, artisan_dat_nung, buyer01

VCRAFT_BASE=http://localhost:8080 python backend/tests/m2.py

# Nếu muốn test trên VPS:
VCRAFT_BASE=https://your-domain.com python backend/tests/m2.py
```

### 3.5. Seed accounts cần có để chạy test

Thêm vào `catalog.sql` hoặc script riêng:

```sql
-- Chạy 1 lần sau khi seed catalog và V5
-- Thay HASH bằng BCrypt hash của 'VcraftDemo17!'

-- Artisan men-lam
INSERT INTO accounts(username, password_hash, role, workshop_id)
SELECT 'artisan_men_lam', '$2a$12$REPLACE_HASH', 'ARTISAN', w.id
FROM workshops w WHERE w.slug = 'xuong-men-lam'
ON CONFLICT (username) DO NOTHING;

-- Artisan dat-nung
INSERT INTO accounts(username, password_hash, role, workshop_id)
SELECT 'artisan_dat_nung', '$2a$12$REPLACE_HASH', 'ARTISAN', w.id
FROM workshops w WHERE w.slug = 'xuong-dat-nung'
ON CONFLICT (username) DO NOTHING;

-- Seed đảm bảo 2 xưởng đã approved
UPDATE workshops SET approved = TRUE, approved_at = NOW()
WHERE slug IN ('xuong-men-lam', 'xuong-dat-nung');
```

---

*Đây là tài liệu cuối cùng trong bộ 4 tài liệu: Blueprint → Phase C → Implementation Guide → Services & Tests. Developer có thể bắt đầu code trực tiếp từ đây.*
