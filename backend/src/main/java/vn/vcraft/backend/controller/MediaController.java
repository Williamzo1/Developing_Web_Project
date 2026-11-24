package vn.vcraft.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vcraft.backend.dto.MediaResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.MediaService;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaService media;
    public MediaController(MediaService media) { this.media = media; }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    List<MediaResponse> upload(@AuthenticationPrincipal Actor actor,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return media.upload(actor, files);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Actor actor, @PathVariable long id) { media.delete(actor, id); }
}
