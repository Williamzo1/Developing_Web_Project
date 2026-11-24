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
@RequestMapping("/api/artisan/workshop")
public class ArtisanWorkshopProfileController {
    private final WorkshopService workshopService;
    public ArtisanWorkshopProfileController(WorkshopService workshopService) {
        this.workshopService = workshopService;
    }
// Thêm vào ArtisanWorkshopController.java (giữ PATCH /{id} và PATCH /{id}/contact cũ)

@GetMapping("")
WorkshopDetailResponse myWorkshop(@AuthenticationPrincipal Actor actor) {
    return workshopService.getMyWorkshop(actor);
}

@PutMapping("")
WorkshopDetailResponse updateProfile(
        @AuthenticationPrincipal Actor actor,
        @Valid @RequestBody WorkshopProfileInput input) {
    return workshopService.updateProfile(actor, input);
}

@PutMapping("/hours")
@ResponseStatus(HttpStatus.NO_CONTENT)
void updateHours(
        @AuthenticationPrincipal Actor actor,
        @RequestBody @NotNull @Valid List<@NotNull @Valid WorkshopHourInput> hours) {
    workshopService.updateHours(actor, hours);
}

@PostMapping("/closures")
@ResponseStatus(HttpStatus.CREATED)
void addClosure(
        @AuthenticationPrincipal Actor actor,
        @Valid @RequestBody WorkshopClosureInput input) {
    workshopService.addClosure(actor, input);
}

@DeleteMapping("/closures/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
void deleteClosure(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    workshopService.deleteClosure(actor, id);
}

@GetMapping("/certificates")
List<CertificateResponse> certificates(@AuthenticationPrincipal Actor actor) {
    return workshopService.getCertificates(actor);
}

@PostMapping("/certificates")
@ResponseStatus(HttpStatus.CREATED)
CertificateResponse addCertificate(
        @AuthenticationPrincipal Actor actor,
        @Valid @RequestBody CertificateInput input) {
    return workshopService.addCertificate(actor, input);
}

@DeleteMapping("/certificates/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
void deleteCertificate(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    workshopService.deleteCertificate(actor, id);
}

}
