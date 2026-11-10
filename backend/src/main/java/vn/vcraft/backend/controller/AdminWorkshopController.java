package vn.vcraft.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.NewArtisanRequest;
import vn.vcraft.backend.dto.WorkshopResponse;
import vn.vcraft.backend.service.WorkshopService;

@RestController
@RequestMapping("/api/admin/workshops")
class AdminWorkshopController {
    private final WorkshopService workshops;

    AdminWorkshopController(WorkshopService workshops) {
        this.workshops = workshops;
    }

    @GetMapping
    List<WorkshopResponse> list() {
        return workshops.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    WorkshopResponse provision(@RequestBody NewArtisanRequest input) {
        return workshops.provision(input);
    }

    @PostMapping("/{id}/approve")
    WorkshopResponse approve(@PathVariable long id) {
        return workshops.approve(id);
    }
}
