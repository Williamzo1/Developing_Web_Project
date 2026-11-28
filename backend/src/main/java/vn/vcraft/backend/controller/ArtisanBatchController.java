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
@RequestMapping("/api/artisan/batches")
public class ArtisanBatchController {
    private final BatchService batches;
    public ArtisanBatchController(BatchService batches) {
        this.batches = batches;
    }

    @GetMapping
    List<BatchSummaryResponse> list(@AuthenticationPrincipal Actor a) { return batches.listMine(a); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    BatchDetailResponse create(@AuthenticationPrincipal Actor a,@Valid @RequestBody BatchCreateInput i) { return batches.create(a,i); }
    @GetMapping("/{id}")
    BatchDetailResponse get(@AuthenticationPrincipal Actor a,@PathVariable long id) { return batches.getMine(a,id); }
    @PutMapping("/{id}")
    BatchDetailResponse update(@AuthenticationPrincipal Actor a,@PathVariable long id,@Valid @RequestBody BatchUpdateInput i) { return batches.update(a,id,i); }
    @PutMapping("/{id}/milestones") @ResponseStatus(HttpStatus.NO_CONTENT)
    void milestones(@AuthenticationPrincipal Actor a,@PathVariable long id,@RequestBody @NotNull @Valid List<@NotNull @Valid MilestoneInput> i) { batches.updateMilestones(a,id,i); }
    @PostMapping("/{id}/share-token")
    Map<String,String> share(@AuthenticationPrincipal Actor a,@PathVariable long id) { return Map.of("shareUrl",batches.generateShareToken(a,id)); }
    @DeleteMapping("/{id}/share-token") @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(@AuthenticationPrincipal Actor a,@PathVariable long id) { batches.revokeShareToken(a,id); }
    @GetMapping(value="/{id}/qr.png",produces="image/png")
    ResponseEntity<byte[]> qr(@AuthenticationPrincipal Actor a,@PathVariable long id) throws Exception {
        return ResponseEntity.ok().header("Cache-Control","no-store").body(batches.generateArtisanQr(a,id));
    }

}
