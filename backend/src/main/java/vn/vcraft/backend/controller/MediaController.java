package vn.vcraft.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vcraft.backend.dto.MediaResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.MediaService;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    List<MediaResponse> upload(
            @AuthenticationPrincipal Actor actor,
            @RequestParam("files") List<MultipartFile> files) {
        return mediaService.upload(actor, files);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
        mediaService.delete(actor, id);
    }
}
