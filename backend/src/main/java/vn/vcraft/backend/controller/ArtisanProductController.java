package vn.vcraft.backend.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/artisan/products")
public class ArtisanProductController {
    private final ProductService products;
    public ArtisanProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    List<ProductSummaryResponse> list(@AuthenticationPrincipal Actor a) { return products.listMine(a); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    ProductDetailResponse create(@AuthenticationPrincipal Actor a,@Valid @RequestBody ProductCreateInput i) { return products.create(a,i); }
    @GetMapping("/{id}")
    ProductDetailResponse get(@AuthenticationPrincipal Actor a,@PathVariable long id) { return products.getMine(a,id); }
    @PutMapping("/{id}")
    ProductDetailResponse update(@AuthenticationPrincipal Actor a,@PathVariable long id,@Valid @RequestBody ProductUpdateInput i) { return products.update(a,id,i); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void hide(@AuthenticationPrincipal Actor a,@PathVariable long id) { products.hide(a,id); }
    @PutMapping("/{id}/steps") @ResponseStatus(HttpStatus.NO_CONTENT)
    void steps(@AuthenticationPrincipal Actor a,@PathVariable long id,@RequestBody @NotNull @Valid List<@NotNull @Valid StepInput> i) { products.updateSteps(a,id,i); }
    @PutMapping("/{id}/tags") @ResponseStatus(HttpStatus.NO_CONTENT)
    void tags(@AuthenticationPrincipal Actor a,@PathVariable long id,@RequestBody @NotNull List<@NotNull @Positive Long> i) { products.updateTags(a,id,i); }

}
