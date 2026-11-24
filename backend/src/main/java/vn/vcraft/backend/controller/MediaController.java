package vn.vcraft.backend.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vcraft.backend.dto.MediaResponse;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.MediaService;

/** Mọi vai trò đã đăng nhập đều được tải ảnh (Buyer dùng cho ảnh tham khảo của yêu cầu báo giá). */
@RestController
@RequestMapping("/api/media")
class MediaController {
  private final MediaService media;

  MediaController(MediaService media) {
    this.media = media;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  List<MediaResponse> upload(
          @AuthenticationPrincipal Actor actor,
          @RequestParam(value = "files", required = false) List<MultipartFile> files) {
    return media.upload(actor, files);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@AuthenticationPrincipal Actor actor, @PathVariable long id) {
    media.delete(actor, id);
  }
}
