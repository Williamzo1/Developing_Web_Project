package vn.vcraft.backend.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.CatalogQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.dto.ProductResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.service.ProductService;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api")
class ProductController {
    private final ProductService products;

    ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping("/products")
    List<ProductResponse> list() {
        return products.list();
    }

    @GetMapping("/products/page")
    PageResponse<ProductResponse> page(@Valid @ModelAttribute CatalogQuery query) {
        return products.page(query);
    }

    @GetMapping("/categories")
    List<vn.vcraft.backend.dto.CatalogOption> categories() {
        return products.categories();
    }

    @GetMapping("/products/by-slug/{slug}")
    ProductResponse detail(@PathVariable String slug) {
        return products.detail(slug).orElseThrow(() ->
                new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
    }

    @GetMapping("/products/by-slug/{slug}/steps")
    List<vn.vcraft.backend.dto.ProcessStepInput> steps(@PathVariable String slug) {
        return products.publicSteps(slug);
    }

    @GetMapping("/products/{id}")
    ProductResponse detail(@PathVariable long id) {
        return products.detail(id).orElseThrow(() ->
                new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
    }
}
