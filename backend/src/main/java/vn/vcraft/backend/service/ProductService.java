package vn.vcraft.backend.service;

import jakarta.persistence.EntityManager;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.List;
import vn.vcraft.backend.dto.ManagedProduct;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.ProcessStepInput;
import vn.vcraft.backend.dto.ProductInput;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.model.ProcessStep;
import vn.vcraft.backend.model.Workshop;
import vn.vcraft.backend.repository.CategoryRepository;
import vn.vcraft.backend.repository.ProcessStepRepository;
import static org.springframework.http.HttpStatus.*;
import vn.vcraft.backend.dto.CatalogOption;
import vn.vcraft.backend.dto.CatalogQuery;
import vn.vcraft.backend.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vcraft.backend.dto.ProductResponse;
import vn.vcraft.backend.model.Product;
import vn.vcraft.backend.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository products;

    private final CategoryRepository categories;
    private final ProcessStepRepository steps;
    private final EntityManager entities;

    public ProductService(ProductRepository products, CategoryRepository categories,
                          ProcessStepRepository steps, EntityManager entities) {
        this.products = products;
        this.categories = categories;
        this.steps = steps;
        this.entities = entities;
    }

    public List<ProductResponse> list() {
        return products.findCatalog().stream().map(this::response).toList();
    }

    public PageResponse<ProductResponse> page(CatalogQuery query) {
        Sort sort = switch (query.getSort()) {
            case "name-asc" -> Sort.by("name").ascending().and(Sort.by("id"));
            case "name-desc" -> Sort.by("name").descending().and(Sort.by("id"));
            default -> Sort.by("id");
        };
        var request = PageRequest.of(query.getPage(), query.getSize(), sort);
        String keyword = query.getQ() == null ? "" : query.getQ().strip();
        return PageResponse.from(products.findCatalogPage(query.getWorkshop(), query.getCategory(),
                keyword, request).map(this::response));
    }

    public List<CatalogOption> categories() {
        return products.findCategoryOptions();
    }

    public Optional<ProductResponse> detail(String slug) {
        return products.findCatalogProduct(slug).map(this::response);
    }

    public Optional<ProductResponse> detail(long id) {
        return products.findCatalogProduct(id).map(this::response);
    }

    public List<ProcessStepInput> publicSteps(String slug) {
        var product = products.findCatalogProduct(slug).orElseThrow(ProductService::notFound);
        return processSteps(product.getId());
    }

    public PageResponse<ManagedProduct> ownPage(Actor actor, PageQuery query) {
        var page = PageRequest.of(query.getPage(), query.getSize(), Sort.by("id").descending());
        return PageResponse.from(products.findOwned(actor.workshop(), page).map(product -> managed(product, List.of())));
    }

    public ManagedProduct ownDetail(Actor actor, long id) {
        var product = products.findOwned(actor.workshop(), id).orElseThrow(ProductService::notFound);
        return managed(product, processSteps(id));
    }

    @Transactional
    public ManagedProduct create(Actor actor, ProductInput input) {
        var workshop = entities.getReference(Workshop.class, actor.workshop());
        var product = products.save(new Product(workshop, newSlug(input.name()), category(input), input));
        return managed(product, List.of());
    }

    @Transactional
    public ManagedProduct update(Actor actor, long id, ProductInput input) {
        var product = lockedProduct(actor, id);
        product.update(category(input), input);
        return managed(product, processSteps(id));
    }

    @Transactional
    public ManagedProduct setSteps(Actor actor, long id, List<ProcessStepInput> input) {
        var product = lockedProduct(actor, id);
        steps.removeForProduct(id);
        for (int position = 0; position < input.size(); position++) {
            steps.save(new ProcessStep(product, position, input.get(position)));
        }
        return managed(product, processSteps(id));
    }

    @Transactional
    public void hideByAdmin(long id) {
        products.lockById(id).orElseThrow(ProductService::notFound).hideByAdmin();
    }

    private Product lockedProduct(Actor actor, long id) {
        return products.lockOwned(actor.workshop(), id).orElseThrow(ProductService::notFound);
    }

    private vn.vcraft.backend.model.Category category(ProductInput input) {
        return categories.findBySlug(input.category()).orElseThrow(() ->
                new ApiException(BAD_REQUEST, "INVALID_CATEGORY", "Phân loại sản phẩm không tồn tại."));
    }

    private List<ProcessStepInput> processSteps(long id) {
        return steps.findByProductIdOrderByPosition(id).stream().map(ProcessStep::response).toList();
    }

    private ManagedProduct managed(Product product, List<ProcessStepInput> process) {
        return new ManagedProduct(product.getId(), product.getSlug(), product.getName(),
                product.getCategory().slug(), product.getDescription(), product.getReferencePrice(),
                product.isPriceVisible(), product.isHidden(), product.isAdminHidden(), process);
    }

    private static String newSlug(String name) {
        String base = Normalizer.normalize(name.toLowerCase(Locale.ROOT).replace('đ', 'd'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (base.isEmpty()) base = "san-pham";
        return base.substring(0, Math.min(75, base.length())) + "-" + UUID.randomUUID();
    }

    private static ApiException notFound() {
        return new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm.");
    }

    private ProductResponse response(Product product) {
        return new ProductResponse(product.getId(), product.getSlug(), product.getName(),
                product.getWorkshop().name(), product.getCategory().name(),
                product.getDescription(), product.getImagePath(), product.getWorkshop().slug(),
                product.isPriceVisible() ? product.getReferencePrice() : null);
    }
}
