package vn.vcraft.backend.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.*;
import vn.vcraft.backend.repository.*;
import vn.vcraft.backend.dto.*;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BatchService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final BatchRepository batchRepo;
    private final MediaRepository mediaRepo;
    private final BatchMilestoneRepository milestoneRepo;
    private final ProcessStepRepository stepRepo;
    private final WorkshopRepository workshopRepo;
    private final ProductRepository productRepo;

    public BatchService(
            BatchRepository batchRepo,
            MediaRepository mediaRepo,
            BatchMilestoneRepository milestoneRepo,
            ProcessStepRepository stepRepo,
            WorkshopRepository workshopRepo,
            ProductRepository productRepo) {

        this.batchRepo = batchRepo;
        this.mediaRepo = mediaRepo;
        this.milestoneRepo = milestoneRepo;
        this.stepRepo = stepRepo;
        this.workshopRepo = workshopRepo;
        this.productRepo = productRepo;
    }


    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public PageResponse<BatchSummaryResponse> listPublic(PageQuery query, Long productId, Long workshopId, String kind) {
        return PageResponse.from(batchRepo.findPublicFiltered(workshopId, productId, kind,
                org.springframework.data.domain.PageRequest.of(query.getPage(), query.getSize())).map(this::toSummary));
    }
    public List<BatchSummaryResponse> listMine(Actor actor) {
        return batchRepo.findByWorkshopId(requireWorkshop(actor)).stream().map(this::toSummary).toList();
    }
    public BatchDetailResponse getMine(Actor actor, long id) { return toDetail(ownedBatch(actor,id)); }

    public BatchDetailResponse getLandingPage(String code, String token) {
        Batch b = publicBatch(code);
        if ("PRIVATE".equals(b.getVisibility()) && (token == null || b.getShareToken() == null ||
                !java.security.MessageDigest.isEqual(token.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        b.getShareToken().getBytes(java.nio.charset.StandardCharsets.UTF_8)))) throw notFound();
        return toDetail(b);
    }
    private Batch publicBatch(String code) {
        Batch b = batchRepo.findByPublicCode(code).orElseThrow(this::notFound);
        if (Boolean.TRUE.equals(b.getHidden()) || workshopRepo.findByWorkshopId(b.getWorkshopId())
                .flatMap(w -> workshopRepo.findBySlug(w.slug())).isEmpty()) throw notFound();
        return b;
    }
    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Không tìm thấy lô hàng.");
    }
    private long requireWorkshop(Actor actor) {
        if (!"ARTISAN".equals(actor.role()) || actor.workshop() == null)
            throw new ApiException(HttpStatus.FORBIDDEN,"FORBIDDEN","Tài khoản không có xưởng.");
        return actor.workshop();
    }
    private Batch ownedBatch(Actor actor,long id) {
        long workshop = requireWorkshop(actor);
        Batch b = batchRepo.findById(id).orElseThrow(this::notFound);
        if (!b.getWorkshopId().equals(workshop)) throw notFound();
        return b;
    }
    private void validateProduct(Actor actor, long productId) {
        Product p = productRepo.findById(productId).orElseThrow(this::notFound);
        if (!p.getWorkshop().getId().equals(requireWorkshop(actor))) throw notFound();
    }
    private void validateMedia(Actor actor,List<Long> ids) {
        if (ids == null) return;
        for (long id : ids) {
            if (mediaRepo.findById(id).filter(m -> m.ownerAccountId() == actor.id()).isEmpty()) throw notFound();
        }
    }
    private String randomCode(int length) {
        String alphabet="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder result = new StringBuilder();
        for (int i=0;i<length;i++) result.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return result.toString();
    }
    @Transactional
    public BatchDetailResponse create(Actor actor, BatchCreateInput input) {
        validateProduct(actor,input.productId());
        validateMedia(actor,input.mediaIds());
        Batch b = new Batch();
        String code;
        do { code=randomCode(10); } while(batchRepo.existsByPublicCode(code));
        b.setPublicCode(code);
        b.setWorkshopId(requireWorkshop(actor));
        b.setHidden(false); b.setFeatured(false); b.setCreatedAt(Instant.now());
        apply(b,new BatchUpdateInput(input.productId(),input.code(),input.name(),input.description(),
                input.kind(),input.quantity(),input.unit(),input.referencePrice(),input.priceVisible(),
                input.supplyCapacity(),input.availableFrom(),input.visibility(),input.partnerNote(),input.mediaIds()));
        batchRepo.saveAndFlush(b);
        if (input.mediaIds()!=null) mediaRepo.replaceLinks("BATCH",b.getId(),input.mediaIds());
        return toDetail(b);
    }
    @Transactional
    public BatchDetailResponse update(Actor actor,long id,BatchUpdateInput input) {
        Batch b=ownedBatch(actor,id);
        validateProduct(actor,input.productId()); validateMedia(actor,input.mediaIds());
        if (!input.productId().equals(b.getProductId()) && !milestoneRepo.findByBatchId(id).isEmpty())
            throw new ApiException(HttpStatus.CONFLICT,"DATA_CONFLICT","Không thể đổi sản phẩm khi lô đã có mốc sản xuất.");
        apply(b,input); batchRepo.saveAndFlush(b);
        if(input.mediaIds()!=null) mediaRepo.replaceLinks("BATCH",id,input.mediaIds());
        return toDetail(b);
    }
    private void apply(Batch b,BatchUpdateInput i) {
        b.setProductId(i.productId()); b.setCode(i.code().strip()); b.setName(i.name());
        b.setDescription(i.description()); b.setKind(i.kind()); b.setQuantity(i.quantity());
        b.setUnit(i.unit()); b.setReferencePrice(i.referencePrice());
        b.setPriceVisible(i.priceVisible()==null || i.priceVisible());
        b.setSupplyCapacity(i.supplyCapacity()); b.setAvailableFrom(i.availableFrom());
        b.setVisibility(i.visibility()==null ? "PUBLIC" : i.visibility()); b.setPartnerNote(i.partnerNote());
        b.setUpdatedAt(Instant.now());
    }
    @Transactional
    public String generateShareToken(Actor actor,long id) {
        Batch b=ownedBatch(actor,id); b.setShareToken(randomCode(32)); b.setUpdatedAt(Instant.now());
        batchRepo.save(b);
        return frontendUrl+"/batches/"+b.getPublicCode()+"?token="+b.getShareToken();
    }
    @Transactional
    public void revokeShareToken(Actor actor,long id) {
        Batch b=ownedBatch(actor,id); b.setShareToken(null); b.setUpdatedAt(Instant.now()); batchRepo.save(b);
    }
    public byte[] generatePublicQr(String code) throws Exception {
        Batch b=publicBatch(code);
        if (!"PUBLIC".equals(b.getVisibility())) throw notFound();
        return encodeQr(frontendUrl+"/batches/"+code);
    }
    public byte[] generateArtisanQr(Actor actor,long id) throws Exception {
        Batch b=ownedBatch(actor,id);
        if ("PRIVATE".equals(b.getVisibility()) && b.getShareToken()==null)
            throw new ApiException(HttpStatus.CONFLICT,"SHARE_TOKEN_REQUIRED","Hãy tạo liên kết chia sẻ trước khi tải QR.");
        return encodeQr(frontendUrl+"/batches/"+b.getPublicCode()+
                ("PRIVATE".equals(b.getVisibility()) ? "?token="+b.getShareToken() : ""));
    }
    private byte[] encodeQr(String url) throws Exception {
        var matrix=new QRCodeWriter().encode(url,BarcodeFormat.QR_CODE,300,300);
        var out=new ByteArrayOutputStream(); MatrixToImageWriter.writeToStream(matrix,"PNG",out); return out.toByteArray();
    }
    private BatchSummaryResponse toSummary(Batch b) {
        var imgs=mediaRepo.findByEntity("BATCH",b.getId(),baseUrl);
        return new BatchSummaryResponse(b.getId(),b.getPublicCode(),b.getCode(),b.getName(),b.getKind(),b.getQuantity(),
                b.getUnit(),Boolean.TRUE.equals(b.getPriceVisible()) ? b.getReferencePrice():null,
                imgs.isEmpty()?null:imgs.get(0),b.getAvailableFrom());
    }

    /** PUT /api/artisan/batches/{id}/milestones — thay toàn bộ mốc */
    @Transactional
    public void updateMilestones(Actor actor, long batchId, List<MilestoneInput> inputs) {
        var batch = ownedBatch(actor, batchId);

        for (MilestoneInput input : inputs) {
            validateMedia(actor, input.mediaIds());
            if (input.processStepId() != null && stepRepo.findById(input.processStepId())
                    .filter(step -> step.getProductId().equals(batch.getProductId())).isEmpty())
                throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_INPUT","Công đoạn không thuộc sản phẩm của lô.");
        }
        // Xóa media_links của các mốc cũ trước khi xóa mốc
        milestoneRepo.findByBatchId(batchId).forEach(m ->
                mediaRepo.replaceLinks("BATCH_MILESTONE", m.getId(), List.of()));

        milestoneRepo.deleteByBatchId(batchId);
        milestoneRepo.flush();

        for (MilestoneInput input : inputs) {
            BatchMilestone m = new BatchMilestone();
            m.setBatchId(batchId);
            m.setProcessStepId(input.processStepId());
            m.setTitle(input.title().strip());
            m.setNote(input.note());
            m.setHappenedOn(input.happenedOn());
            m.setCreatedAt(java.time.Instant.now());
            milestoneRepo.saveAndFlush(m);

            if (input.mediaIds() != null && !input.mediaIds().isEmpty()) {
                mediaRepo.replaceLinks("BATCH_MILESTONE", m.getId(), input.mediaIds());
            }
        }

        batch.setUpdatedAt(java.time.Instant.now());
        batchRepo.save(batch);
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
            MediaResponse cover = productImages.isEmpty() ? null : productImages.get(0);
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
}
