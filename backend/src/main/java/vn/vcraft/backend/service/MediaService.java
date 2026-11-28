package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import vn.vcraft.backend.dto.MediaResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.MediaRepository;

/** Ghép liên kết ảnh M1 với bộ xử lý JPEG/PNG từ nhánh chung. */
@Service
public class MediaService {
    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private final MediaStorage storage;
    private final MediaRepository media;
    @Value("${media.max-file-size-mb:5}") private int maxFileSizeMb;
    @Value("${media.max-files-per-request:10}") private int maxFilesPerRequest;

    public MediaService(MediaStorage storage, MediaRepository media) {
        this.storage = storage;
        this.media = media;
    }
    private record Prepared(String name, int inputSize, ImageVariants.Result image) {}

    @Transactional
    public List<MediaResponse> upload(Actor actor, List<MultipartFile> files) {
        if (files == null || files.isEmpty())
            throw new ApiException(BAD_REQUEST, "NO_FILES", "Vui lòng chọn ít nhất một ảnh.");
        if (files.size() > maxFilesPerRequest)
            throw new ApiException(BAD_REQUEST, "TOO_MANY_FILES", "Tối đa " + maxFilesPerRequest + " ảnh mỗi lần.");
        // Kiểm tra tất cả ảnh trước khi ghi DB/đĩa.
        List<Prepared> prepared = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty())
                throw new ApiException(BAD_REQUEST, "EMPTY_FILE", "Tệp ảnh không được rỗng.");
            if (file.getSize() > (long) maxFileSizeMb * 1024 * 1024)
                throw new ApiException(PAYLOAD_TOO_LARGE, "UPLOAD_TOO_LARGE", "Ảnh vượt giới hạn " + maxFileSizeMb + " MB.");
            try {
                byte[] bytes = file.getBytes();
                prepared.add(new Prepared(safeName(file.getOriginalFilename()), bytes.length, ImageVariants.create(bytes)));
            } catch (IOException ex) {
                throw new ApiException(BAD_REQUEST, "INVALID_IMAGE", "Không đọc được tệp ảnh.");
            }
        }
        List<String> written = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) cleanup(written);
            }
        });
        List<MediaResponse> result = new ArrayList<>();
        try {
            for (Prepared p : prepared) {
                var image = p.image();
                LocalDate date = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                String prefix = "%d/%02d/%s".formatted(date.getYear(), date.getMonthValue(), UUID.randomUUID());
                String thumb = prefix + "-320.jpg", medium = prefix + "-800.jpg", large = prefix + "-1600.jpg";
                save(thumb, image.thumb(), written);
                save(medium, image.medium(), written);
                save(large, image.large(), written);
                long id = media.create(actor.id(), p.name(), "image/jpeg", p.inputSize(), image.width(), image.height(), thumb, medium, large);
                // URL tương đối hoạt động qua cả backend trực tiếp và Nginx.
                result.add(new MediaResponse(id, "/media/" + thumb, "/media/" + medium, "/media/" + large, image.width(), image.height()));
            }
            return result;
        } catch (RuntimeException ex) {
            cleanup(written);
            throw ex;
        }
    }
    private void save(String key, byte[] data, List<String> written) {
        written.add(key); // dọn cả tệp ghi dở nếu save thất bại
        storage.save(key, data);
    }
    private String safeName(String original) {
        if (original == null || original.isBlank()) return "image";
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
        if (name.isBlank()) return "image";
        return name.substring(0, Math.min(255, name.length()));
    }
    private void cleanup(List<String> keys) {
        for (String key : keys) {
            try { storage.delete(key); }
            catch (RuntimeException ex) { log.warn("Không xóa được biến thể ảnh khi dọn tệp."); }
        }
    }
    @Transactional
    public void delete(Actor actor, long id) {
        var row = media.findById(id).filter(m -> m.ownerAccountId() == actor.id())
                .orElseThrow(() -> new ApiException(NOT_FOUND, "NOT_FOUND", "Không tìm thấy ảnh."));
        if (media.isLinked(id))
            throw new ApiException(CONFLICT, "DATA_CONFLICT", "Ảnh đang được sử dụng, không thể xóa.");
        media.delete(id);
        List<String> keys = List.of(row.pathThumb(), row.pathMedium(), row.pathLarge());
        // Chỉ xóa tệp khi DB đã commit.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { cleanup(keys); }
        });
    }
}
