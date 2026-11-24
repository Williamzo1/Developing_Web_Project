package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONTENT_TOO_LARGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.vcraft.backend.dto.MediaResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.MediaRepository;

/**
 * Upload và xóa ảnh (FR-MEDIA-01, 02, 04). Một lần upload là tất cả-hoặc-không: mọi tệp được
 * kiểm tra và resize trong bộ nhớ trước khi ghi đĩa; nếu ghi đĩa hoặc ghi DB lỗi thì các tệp đã
 * ghi bị xóa lại.
 */
@Service
public class MediaService {
  private static final Logger log = LoggerFactory.getLogger(MediaService.class);
  static final String URL_PREFIX = "/media/";

  private final MediaRepository media;
  private final MediaStorage storage;
  private final TransactionTemplate tx;
  private final long maxFileBytes;
  private final int maxFiles;

  public MediaService(
          MediaRepository media,
          MediaStorage storage,
          TransactionTemplate tx,
          @Value("${media.max-file-size-mb:5}") int maxFileMb,
          @Value("${media.max-files-per-request:10}") int maxFiles) {
    this.media = media;
    this.storage = storage;
    this.tx = tx;
    this.maxFileBytes = maxFileMb * 1024L * 1024L;
    this.maxFiles = maxFiles;
  }

  private record Prepared(String name, int size, ImageVariants.Result image, String base) {}

  public List<MediaResponse> upload(Actor actor, List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      throw new ApiException(BAD_REQUEST, "NO_FILES", "Vui lòng chọn ít nhất một ảnh.");
    }
    if (files.size() > maxFiles) {
      throw new ApiException(
              BAD_REQUEST, "TOO_MANY_FILES", "Mỗi lần chỉ tải tối đa " + maxFiles + " ảnh.");
    }
    LocalDate today = LocalDate.now();
    String folder = "%d/%02d/".formatted(today.getYear(), today.getMonthValue());
    List<Prepared> prepared = new ArrayList<>();
    for (MultipartFile file : files) {
      if (file.isEmpty()) {
        throw new ApiException(BAD_REQUEST, "EMPTY_FILE", "Có tệp rỗng trong danh sách tải lên.");
      }
      if (file.getSize() > maxFileBytes) {
        throw new ApiException(
                CONTENT_TOO_LARGE,
                "UPLOAD_TOO_LARGE",
                "Mỗi ảnh tối đa " + maxFileBytes / (1024 * 1024) + " MB.");
      }
      byte[] data = read(file);
      prepared.add(
              new Prepared(
                      cleanName(file.getOriginalFilename()),
                      data.length,
                      ImageVariants.create(data),
                      folder + UUID.randomUUID()));
    }

    List<String> written = new ArrayList<>();
    try {
      for (Prepared p : prepared) {
        write(written, p.base() + "-" + ImageVariants.THUMB + ".jpg", p.image().thumb());
        write(written, p.base() + "-" + ImageVariants.MEDIUM + ".jpg", p.image().medium());
        write(written, p.base() + "-" + ImageVariants.LARGE + ".jpg", p.image().large());
      }
      return tx.execute(status -> prepared.stream().map(p -> insert(actor, p)).toList());
    } catch (RuntimeException e) {
      written.forEach(this::deleteQuietly);
      throw e;
    }
  }

  /** Chỉ người tải lên được xóa; ảnh của người khác hoặc không có → 404. */
  public void delete(Actor actor, long id) {
    MediaRepository.Stored stored =
            tx.execute(
                    status -> {
                      var found =
                              media
                                      .findOwned(id, actor.id())
                                      .orElseThrow(
                                              () ->
                                                      new ApiException(
                                                              NOT_FOUND, "NOT_FOUND", "Không tìm thấy ảnh."));
                      media.delete(id);
                      return found;
                    });
    deleteQuietly(stored.pathThumb());
    deleteQuietly(stored.pathMedium());
    deleteQuietly(stored.pathLarge());
  }

  private MediaResponse insert(Actor actor, Prepared p) {
    String thumb = p.base() + "-" + ImageVariants.THUMB + ".jpg";
    String medium = p.base() + "-" + ImageVariants.MEDIUM + ".jpg";
    String large = p.base() + "-" + ImageVariants.LARGE + ".jpg";
    long id =
            media.insert(
                    actor.id(),
                    p.name(),
                    p.image().contentType(),
                    p.size(),
                    p.image().width(),
                    p.image().height(),
                    thumb,
                    medium,
                    large);
    return new MediaResponse(
            id,
            URL_PREFIX + thumb,
            URL_PREFIX + medium,
            URL_PREFIX + large,
            p.image().width(),
            p.image().height());
  }

  private void write(List<String> written, String key, byte[] data) {
    storage.save(key, data);
    written.add(key);
  }

  private void deleteQuietly(String key) {
    try {
      storage.delete(key);
    } catch (RuntimeException e) {
      log.warn("Không xóa được tệp ảnh {}", key, e);
    }
  }

  private static byte[] read(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException e) {
      throw new ApiException(BAD_REQUEST, "INVALID_IMAGE", "Không đọc được tệp tải lên.");
    }
  }

  /** Tên gốc chỉ để hiển thị/ghi vết, không bao giờ dùng làm đường dẫn tệp. */
  static String cleanName(String raw) {
    if (raw == null) {
      return "image";
    }
    String name = raw.substring(Math.max(raw.lastIndexOf('/'), raw.lastIndexOf('\\')) + 1);
    name = name.replaceAll("\\p{Cntrl}", "").strip();
    if (name.length() > 255) {
      name = name.substring(0, 255);
    }
    return name.isEmpty() ? "image" : name;
  }
}
