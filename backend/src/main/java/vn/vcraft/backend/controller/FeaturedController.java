package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.FeaturedContent.*;
import vn.vcraft.backend.service.FeaturedService;

@RestController
@RequestMapping("/api")
class FeaturedController {
    private final FeaturedService featured;

    FeaturedController(FeaturedService featured) { this.featured = featured; }

    @GetMapping("/home")
    Home published() { return featured.published(); }

    @GetMapping("/admin/featured")
    Input selected() { return featured.selected(); }

    @PutMapping("/admin/featured")
    Input save(@Valid @RequestBody Input input) { return featured.save(input); }
}
