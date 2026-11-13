package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.WorkshopResponse;
import vn.vcraft.backend.dto.WorkshopUpdateRequest;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.WorkshopService;

@RestController
@RequestMapping("/api/artisan/workshops")
class ArtisanWorkshopController {
    private final WorkshopService workshops;

    ArtisanWorkshopController(WorkshopService workshops) {
        this.workshops = workshops;
    }

    @PatchMapping("/{id}")
    WorkshopResponse rename(@AuthenticationPrincipal Actor actor, @PathVariable long id,
                            @Valid @RequestBody WorkshopUpdateRequest input) {
        return workshops.rename(actor, id, input);
    }
}
