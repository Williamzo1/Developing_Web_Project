package vn.vcraft.backend.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.BatchData.*;
import vn.vcraft.backend.dto.PageQuery;
import vn.vcraft.backend.dto.PageResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.BatchService;

@RestController
@RequestMapping("/api")
class BatchController {
    private static final String ARTISAN = "/artisan/batches";
    private static final String ADMIN = "/admin/batches";
    private final BatchService batches;

    BatchController(BatchService batches) { this.batches = batches; }

    @GetMapping("/batches")
    PageResponse<Batch> list(@Valid @ModelAttribute PageQuery page,
                             @RequestParam(required = false) String product,
                             @RequestParam(required = false) String workshop,
                             @RequestParam(required = false) Kind kind) {
        return batches.published(page, product, workshop, kind);
    }

    @GetMapping("/batches/{code}")
    ResponseEntity<Details> publicDetail(@PathVariable UUID code, @RequestParam(required = false) String token) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("Referrer-Policy", "no-referrer")
                .body(batches.publicDetail(code, token));
    }

    @GetMapping(ARTISAN)
    PageResponse<Batch> ownList(@AuthenticationPrincipal Actor actor, @Valid @ModelAttribute PageQuery page) {
        return batches.ownList(actor, page);
    }

    @GetMapping(ARTISAN + "/{id}")
    Details ownDetail(@AuthenticationPrincipal Actor actor, @PathVariable long id) { return batches.ownDetail(actor, id); }

    @PostMapping(ARTISAN)
    @ResponseStatus(HttpStatus.CREATED)
    Details create(@AuthenticationPrincipal Actor actor, @Valid @RequestBody Input input) {
        return batches.create(actor, input);
    }

    @PutMapping(ARTISAN + "/{id}")
    Details update(@AuthenticationPrincipal Actor actor, @PathVariable long id, @Valid @RequestBody Input input) {
        return batches.update(actor, id, input);
    }

    @PutMapping(ARTISAN + "/{id}/milestones")
    Details milestones(@AuthenticationPrincipal Actor actor, @PathVariable long id, @Valid @RequestBody Milestones input) {
        return batches.milestones(actor, id, input);
    }

    @PostMapping(ARTISAN + "/{id}/share-token")
    ResponseEntity<Share> share(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(batches.share(actor, id));
    }

    @DeleteMapping(ARTISAN + "/{id}/share-token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(@AuthenticationPrincipal Actor actor, @PathVariable long id) { batches.revoke(actor, id); }

    @GetMapping(ADMIN + "/{id}")
    Details inspect(@PathVariable long id) { return batches.adminDetail(id); }

    @PostMapping(ADMIN + "/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void hide(@PathVariable long id) { batches.hide(id); }
}
