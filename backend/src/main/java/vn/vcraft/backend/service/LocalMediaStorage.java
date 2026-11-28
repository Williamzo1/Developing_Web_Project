package vn.vcraft.backend.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Lưu ảnh trên đĩa dưới thư mục gốc cố định; từ chối khóa thoát ra ngoài thư mục gốc. */
public class LocalMediaStorage implements MediaStorage {
  private final Path root;

  public LocalMediaStorage(Path root) {
    this.root = root.toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.root);
    } catch (IOException e) {
      throw new UncheckedIOException("Không tạo được thư mục ảnh " + this.root, e);
    }
  }

  public Path root() {
    return root;
  }

  @Override
  public void save(String key, byte[] data) {
    Path target = resolve(key);
    try {
      Files.createDirectories(target.getParent());
      Files.write(target, data, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    } catch (IOException e) {
      throw new UncheckedIOException("Không ghi được ảnh " + key, e);
    }
  }

  @Override
  public void delete(String key) {
    try {
      Files.deleteIfExists(resolve(key));
    } catch (IOException e) {
      throw new UncheckedIOException("Không xóa được ảnh " + key, e);
    }
  }

  private Path resolve(String key) {
    Path path = root.resolve(key).normalize();
    if (!path.startsWith(root) || path.equals(root)) {
      throw new IllegalArgumentException("Khóa ảnh không hợp lệ.");
    }
    return path;
  }
}
