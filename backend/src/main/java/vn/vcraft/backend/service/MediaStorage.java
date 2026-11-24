package vn.vcraft.backend.service;

/**
 * Nơi lưu tệp ảnh (Q-13: mặc định là đĩa cục bộ). Khóa là đường dẫn tương đối dạng
 * {@code 2026/11/<uuid>-320.jpg}; cài đặt khác (S3, Cloudinary…) chỉ cần thay lớp này.
 */
public interface MediaStorage {
  void save(String key, byte[] data);

  /** Xóa nếu có; không báo lỗi khi tệp không tồn tại. */
  void delete(String key);
}
