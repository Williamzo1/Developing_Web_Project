package vn.vcraft.backend.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.service.WorkshopService;
import java.util.List;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    private final WorkshopService workshopService;

    public WorkshopController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }

    @GetMapping
    PageResponse<WorkshopSummaryResponse> list(@Valid @ModelAttribute PageQuery page) {
        return workshopService.listPublic(page);
    }

    @GetMapping("/map")
    List<MapPinResponse> map() {
        return workshopService.mapPins();
    }

    @GetMapping("/{slug}")
    WorkshopDetailResponse detail(@PathVariable String slug) {
        return workshopService.getBySlug(slug);
    }
}
