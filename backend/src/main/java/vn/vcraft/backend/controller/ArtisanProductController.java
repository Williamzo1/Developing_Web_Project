package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.ProductService;

@RestController
@RequestMapping("/api/artisan/products")
class ArtisanProductController {
    private final ProductService products;

    ArtisanProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    PageResponse<ManagedProduct> list(@AuthenticationPrincipal Actor actor, @Valid @ModelAttribute PageQuery query) {
        return products.ownPage(actor, query);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ManagedProduct create(@AuthenticationPrincipal Actor actor, @Valid @RequestBody ProductInput input) {
        return products.create(actor, input);
    }

    @GetMapping("/{id}")
    ManagedProduct detail(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
        return products.ownDetail(actor, id);
    }

    @PutMapping("/{id}")
    ManagedProduct update(@AuthenticationPrincipal Actor actor, @PathVariable long id,
                          @Valid @RequestBody ProductInput input) {
        return products.update(actor, id, input);
    }

    @PutMapping("/{id}/steps")
    ManagedProduct steps(@AuthenticationPrincipal Actor actor, @PathVariable long id,
                         @Valid @RequestBody ProcessStepsInput input) {
        return products.setSteps(actor, id, input.steps());
    }
}
