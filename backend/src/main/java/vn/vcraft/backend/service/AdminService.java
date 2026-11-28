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
    private final TagRepository tags;
    private final MediaRepository media;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AdminService(WorkshopRepository workshopRepo, ProductRepository productRepo,
                        BatchRepository batchRepo, NewsRepository newsRepo,
                        AccountRepository accountRepo, TagRepository tags, MediaRepository media) {
        this.media = media;
        this.tags=tags;
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
                tags.countQuoteRequests(),
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
                newsRepo::existsBySlug);

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
        newsRepo.saveAndFlush(post);
        attachCover(post.getId(), actor.id(), input.coverMediaId());
        return toNewsResponse(post);
    }

    @Transactional
    public NewsResponse updateNews(Actor actor,long id, NewsCreateInput input) {
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
        newsRepo.saveAndFlush(post);
        attachCover(post.getId(), actor.id(), input.coverMediaId());
        return toNewsResponse(post);
    }

    @Transactional
    public void deleteNews(long id) {
        if (!newsRepo.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                    "Không tìm thấy tin.");
        }
        media.replaceLinks("NEWS",id,List.of());
        newsRepo.deleteById(id);
    }


    public PageResponse<NewsResponse> listNewsPublic(PageQuery q,String kind) {
        return PageResponse.from(newsRepo.findPublished(kind,PageRequest.of(q.getPage(),q.getSize())).map(this::toNewsResponse));
    }
    public NewsResponse newsPublic(String slug) {
        return toNewsResponse(newsRepo.findBySlugPublic(slug).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Không tìm thấy tin.")));
    }
    public List<TagResponse> listTags() { return tags.list(); }
    @Transactional public TagResponse createTag(TagInput i) { return tags.create(i); }
    @Transactional public TagResponse updateTag(long id,TagInput i) {
        return tags.update(id,i).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Không tìm thấy tag."));
    }
    @Transactional public void deleteTag(long id) {
        if (!tags.delete(id)) throw new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Không tìm thấy tag.");
    }
    // ----------------------------------------------------------------
    // Utility
    // ----------------------------------------------------------------

    private NewsResponse toNewsResponse(NewsPost n) {
        return new NewsResponse(
                n.getId(), n.getSlug(), n.getTitle(), n.getBody(),
                n.getKind(), n.getEventStart(), n.getEventEnd(), n.getEventPlace(),
                media.findByEntity("NEWS",n.getId(),baseUrl).stream().findFirst().orElse(null),
                n.getPublishedAt(), n.getCreatedAt());
    }
    private void attachCover(long id,long accountId,Long coverId) {
        if (coverId != null && media.findById(coverId).filter(m -> m.ownerAccountId()==accountId).isEmpty())
            throw new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND","Không tìm thấy ảnh.");
        media.replaceLinks("NEWS",id,coverId==null ? List.of():List.of(coverId));
    }
}
