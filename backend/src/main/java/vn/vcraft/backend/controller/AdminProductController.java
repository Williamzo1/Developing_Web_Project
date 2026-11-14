package vn.vcraft.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.service.ProductService;

@RestController
@RequestMapping("/api/admin/products")
class AdminProductController {
    private final ProductService products;

    AdminProductController(ProductService products) {
        this.products = products;
    }

    @PostMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void hide(@PathVariable long id) {
        products.hideByAdmin(id);
    }
}
