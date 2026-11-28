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
                workshopRepo.listFeatured(baseUrl).stream()
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
                            MediaResponse cover = imgs.isEmpty() ? null : imgs.get(0);
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
                    MediaResponse cover = imgs.isEmpty() ? null : imgs.get(0);
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
