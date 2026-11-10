package vn.vcraft.backend.controller;

import vn.vcraft.backend.dto.ProductResponse;
import vn.vcraft.backend.service.ProductService;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    ResponseEntity<?> detail(@PathVariable long id) {
        return products.detail(id)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(product))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new ApiError("PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm.")));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> invalidId() {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_PRODUCT_ID", "Mã sản phẩm phải là số nguyên."));
    }

    record ApiError(String code, String message) {}
}
