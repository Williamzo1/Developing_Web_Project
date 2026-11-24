package vn.vcraft.backend.service;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.vcraft.backend.dto.MediaResponse;
import vn.vcraft.backend.exception.ApiException;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.repository.MediaRepository;
import vn.vcraft.backend.storage.MediaStorage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");
    private static final Tika TIKA = new Tika();

    @Value("${media.max-file-size-mb:5}")
    private int maxFileSizeMb;

    @Value("${media.max-files-per-request:10}")
    private int maxFilesPerRequest;

    private final MediaStorage storage;
    private final MediaRepository mediaRepo;

    public MediaService(MediaStorage storage, MediaRepository mediaRepo) {
        this.storage = storage;
        this.mediaRepo = mediaRepo;
    }

    @Transactional
    public List<MediaResponse> upload(Actor actor, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "Vui lòng chọn ít nhất một ảnh.");
        }
        if (files.size() > maxFilesPerRequest) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "Tối đa " + maxFilesPerRequest + " ảnh mỗi lần.");
        }

        List<MediaResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(processOne(actor, file));
        }
        return results;
    }

    private MediaResponse processOne(Actor actor, MultipartFile file) {
        // 1. Kiểm tra kích thước
        long maxBytes = (long) maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD_TOO_LARGE",
                    "Ảnh phải nhỏ hơn " + maxFileSizeMb + " MB.");
        }

        // 2. Validate MIME bằng Tika (magic bytes)
        try {
            byte[] bytes = file.getBytes();
            String detectedType = TIKA.detect(bytes);
            if (!ALLOWED_TYPES.contains(detectedType)) {
                throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "UNSUPPORTED_MEDIA_TYPE",
                        "Chỉ chấp nhận ảnh JPEG, PNG hoặc WebP.");
            }

            // 3. Đọc ảnh gốc
            BufferedImage original;
            try (var stream = ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))) {
                var readers=ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_INPUT","Định dạng ảnh chưa được bộ giải mã hỗ trợ.");
                var reader=readers.next();
                try {
                    reader.setInput(stream);
                    if ((long)reader.getWidth(0)*reader.getHeight(0)>40_000_000L)
                        throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_INPUT","Ảnh vượt giới hạn 40 triệu điểm ảnh.");
                    original=reader.read(0);
                } finally { reader.dispose(); }
            }
            if (original == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT",
                        "Không thể đọc file ảnh.");
            }
            int origW = original.getWidth(), origH = original.getHeight();

            // 4. Sinh 3 biến thể
            String ext = detectedType.equals("image/png") ? "png" : "jpg";
            String baseName = "img." + ext;
            String outputFormat = ext.equals("png") ? "png" : "jpg";

            String pathThumb  = storage.store(resize(original, 240,  outputFormat), "thumb_"  + baseName);
            String pathMedium = storage.store(resize(original, 800,  outputFormat), "medium_" + baseName);
            String pathLarge  = storage.store(resize(original, 1600, outputFormat), "large_"  + baseName);

            // 5. Lưu metadata vào DB (trong @Transactional của caller)
            long mediaId = mediaRepo.create(
                    actor.id(),
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "image",
                    detectedType,
                    (int) file.getSize(),
                    origW, origH,
                    pathThumb, pathMedium, pathLarge
            );

            return new MediaResponse(mediaId,
                    storage.toUrl(pathThumb),
                    storage.toUrl(pathMedium),
                    storage.toUrl(pathLarge),
                    origW, origH);

        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "Lỗi xử lý ảnh.");
        }
    }

    private byte[] resize(BufferedImage src, int maxWidth, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(src)
                .width(Math.min(src.getWidth(), maxWidth))  // không phóng to ảnh nhỏ
                .outputFormat(format)
                .outputQuality(0.85)
                .toOutputStream(out);
        return out.toByteArray();
    }

    @Transactional
    public void delete(Actor actor, long mediaId) {
        var media = mediaRepo.findById(mediaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                        "Không tìm thấy ảnh."));

        if (media.ownerAccountId() != actor.id()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài nguyên.");
        }

        // Kiểm tra ảnh chưa được gắn vào thực thể nào
        if (mediaRepo.isLinked(mediaId)) {
            throw new ApiException(HttpStatus.CONFLICT, "DATA_CONFLICT",
                    "Ảnh đang được sử dụng, không thể xóa.");
        }

        mediaRepo.delete(mediaId);
        storage.delete(media.pathThumb());
        storage.delete(media.pathMedium());
        storage.delete(media.pathLarge());
    }
}
