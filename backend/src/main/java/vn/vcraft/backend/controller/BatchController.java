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
@RequestMapping("/api/batches")
public class BatchController {
    private final BatchService batches;
    public BatchController(BatchService batches) {
        this.batches = batches;
    }

    @GetMapping
    PageResponse<BatchSummaryResponse> list(@Valid @ModelAttribute PageQuery q,
            @RequestParam(required=false) Long productId,@RequestParam(required=false) Long workshopId,
            @RequestParam(required=false) String kind) { return batches.listPublic(q,productId,workshopId,kind); }
    @GetMapping("/{publicCode}")
    BatchDetailResponse detail(@PathVariable String publicCode,@RequestParam(required=false) String token) { return batches.getLandingPage(publicCode,token); }
    @GetMapping(value="/{publicCode}/qr.png",produces="image/png")
    ResponseEntity<byte[]> qr(@PathVariable String publicCode) throws Exception {
        return ResponseEntity.ok().header("Cache-Control","no-store").body(batches.generatePublicQr(publicCode));
    }

}
