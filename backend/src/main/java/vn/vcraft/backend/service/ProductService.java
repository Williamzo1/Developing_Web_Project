package vn.vcraft.backend.service;

import java.util.List;
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

    public Optional<ProductResponse> detail(long id) {
        return products.findCatalogProduct(id).map(this::response);
    }

    private ProductResponse response(Product product) {
        return new ProductResponse(product.getId(), product.getSlug(), product.getName(),
                product.getWorkshop().name(), product.getCategory().name(),
                product.getDescription(), product.getImagePath());
    }
}
