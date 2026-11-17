package vn.vcraft.backend.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.dto.ProductResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.service.ProductService;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/products")
class ProductController {
    private final ProductService products;

    ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    List<ProductResponse> list() {
        return products.list();
    }

    @GetMapping("/page")
    PageResponse<ProductResponse> page(@Valid @ModelAttribute PageQuery query) {
        return products.page(query);
    }

    @GetMapping("/{id}")
    ProductResponse detail(@PathVariable long id) {
        return products.detail(id).orElseThrow(() ->
                new ApiException(NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm."));
    }
}
