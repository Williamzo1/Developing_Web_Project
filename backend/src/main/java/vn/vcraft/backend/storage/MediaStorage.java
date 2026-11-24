package vn.vcraft.backend.storage;

public interface MediaStorage {
    /** Lưu dữ liệu ảnh; trả đường dẫn tương đối (vd: "2024/11/abc123_thumb.jpg"). */
    String store(byte[] data, String filename);
    /** Xóa file theo đường dẫn tương đối. */
    void delete(String relativePath);
    /** Chuyển đường dẫn tương đối thành URL công khai đầy đủ. */
    String toUrl(String relativePath);
}
