package vn.vcraft.backend.service;

import java.util.List;
import vn.vcraft.backend.dto.PageQuery;
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

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public List<ProductResponse> list() {
        return products.findCatalog().stream().map(this::response).toList();
    }

    public PageResponse<ProductResponse> page(PageQuery query) {
        var request = PageRequest.of(query.getPage(), query.getSize(), Sort.by("id").ascending());
        return PageResponse.from(products.findCatalogPage(request).map(this::response));
    }

    public Optional<ProductResponse> detail(long id) {
        return products.findCatalogProduct(id).map(this::response);
    }

    private ProductResponse response(Product product) {
        return new ProductResponse(product.getId(), product.getSlug(), product.getName(),
                product.getWorkshop().name(), product.getCategory().name(),
                product.getDescription(), product.getImagePath());
    }
}
