package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.CatalogOption;
import vn.vcraft.backend.dto.CatalogQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.dto.WorkshopProfile;
import vn.vcraft.backend.dto.WorkshopProfileInput;
import vn.vcraft.backend.dto.WorkshopPin;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.WorkshopService;

@RestController
class WorkshopProfileController {
    private final WorkshopService workshops;

    WorkshopProfileController(WorkshopService workshops) {
        this.workshops = workshops;
    }

    @GetMapping("/api/workshops")
    PageResponse<CatalogOption> publicList(@Valid @ModelAttribute CatalogQuery query) {
        return workshops.publicList(query);
    }

    @GetMapping("/api/map/workshops")
    List<WorkshopPin> mapPins() {
        return workshops.mapPins();
    }

    @GetMapping("/api/workshops/{slug}")
    WorkshopProfile publicProfile(@PathVariable String slug) {
        return workshops.publicProfile(slug);
    }

    @GetMapping("/api/artisan/workshop")
    WorkshopProfile ownProfile(@AuthenticationPrincipal Actor actor) {
        return workshops.ownProfile(actor);
    }

    @PutMapping("/api/artisan/workshop")
    WorkshopProfile updateProfile(@AuthenticationPrincipal Actor actor, @Valid @RequestBody WorkshopProfileInput input) {
        return workshops.updateProfile(actor, input);
    }
}
