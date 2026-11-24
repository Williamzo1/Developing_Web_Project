package vn.vcraft.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class LocalFileStorage implements MediaStorage {

    @Value("${media.storage-path:/tmp/vcraft-media}")
    private String storagePath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String store(byte[] data, String filename) {
        if (filename == null || !filename.matches("[a-zA-Z0-9_.-]+"))
            throw new IllegalArgumentException("Tên tệp không hợp lệ.");
        try {
            LocalDate now = LocalDate.now();
            // Tổ chức theo năm/tháng để tránh thư mục quá nhiều file
            Path dir = Path.of(storagePath,
                    String.valueOf(now.getYear()),
                    String.format("%02d", now.getMonthValue()));
            Files.createDirectories(dir);

            String stored = UUID.randomUUID().toString().replace("-","") + "_" + filename;
            Files.write(dir.resolve(stored), data);

            return now.getYear() + "/" + String.format("%02d", now.getMonthValue())
                    + "/" + stored;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path root=Path.of(storagePath).toAbsolutePath().normalize();
            Path target=root.resolve(relativePath).normalize();
            if (!target.startsWith(root)) throw new IllegalArgumentException("Đường dẫn không hợp lệ.");
            Files.deleteIfExists(target);
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(LocalFileStorage.class).warn("Không xóa được tệp ảnh.");
        }
    }

    @Override
    public String toUrl(String relativePath) {
        return baseUrl + "/media/" + relativePath;
    }
}
