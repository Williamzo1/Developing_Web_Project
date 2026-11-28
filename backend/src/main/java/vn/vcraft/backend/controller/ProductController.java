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
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService products;
    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    List<ProductResponse> list() { return products.list(); }
    @GetMapping("/page")
    PageResponse<ProductSummaryResponse> page(@Valid @ModelAttribute PageQuery q,
            @RequestParam(required=false) Long workshopId,@RequestParam(required=false) Long categoryId,
            @RequestParam(required=false) String tagSlug) { return products.pageFiltered(q,workshopId,categoryId,tagSlug); }
    @GetMapping("/{idOrSlug}")
    ProductDetailResponse detail(@PathVariable String idOrSlug) { return products.detailByIdOrSlug(idOrSlug); }
    @GetMapping("/{slug}/steps")
    List<StepResponse> steps(@PathVariable String slug) { return products.getSteps(slug); }

}
