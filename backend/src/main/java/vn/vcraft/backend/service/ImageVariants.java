package vn.vcraft.backend.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import net.coobird.thumbnailator.Thumbnails;
import vn.vcraft.backend.exception.ApiException;

/**
 * Kiểm tra một ảnh upload và sinh ba biến thể JPEG (FR-MEDIA-02): thumbnail 320 px, vừa 800 px,
 * lớn 1600 px theo cạnh dài. Không phóng to ảnh nhỏ. Loại tệp xác định theo nội dung (không tin
 * Content-Type hay đuôi tệp). Ảnh được xoay theo EXIF, dán lên nền trắng nếu có kênh trong suốt,
 * và mọi metadata (EXIF, GPS) bị loại khi mã hóa lại.
 */
final class ImageVariants {
  static final int THUMB = 320;
  static final int MEDIUM = 800;
  static final int LARGE = 1600;
  /** Chặn "bom giải nén": ảnh nhỏ về dung lượng nhưng khổng lồ về điểm ảnh. */
  static final long MAX_PIXELS = 30_000_000L;

  private static final float QUALITY = 0.8f;

  static {
    // Giải mã từ bộ nhớ, không tạo tệp tạm.
    ImageIO.setUseCache(false);
  }

  record Result(
          String contentType, int width, int height, byte[] thumb, byte[] medium, byte[] large) {}

  private ImageVariants() {}

  static Result create(byte[] data) {
    String contentType = detect(data);
    BufferedImage large = fit(flatten(decode(data, contentType)), LARGE);
    BufferedImage medium = fit(large, MEDIUM);
    BufferedImage thumb = fit(medium, THUMB);
    return new Result(
            contentType,
            large.getWidth(),
            large.getHeight(),
            encode(thumb),
            encode(medium),
            encode(large));
  }

  /** Đọc đầu tệp để biết định dạng và kích thước mà không giải mã toàn bộ ảnh. */
  private static String detect(byte[] data) {
    try (ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(data))) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (!readers.hasNext()) {
        throw unsupported();
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(in, true, true);
        String contentType =
                switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
                  case "jpeg", "jpg" -> "image/jpeg";
                  case "png" -> "image/png";
                  default -> null;
                };
        if (contentType == null) {
          throw unsupported();
        }
        long width = reader.getWidth(0);
        long height = reader.getHeight(0);
        if (width <= 0 || height <= 0 || width * height > MAX_PIXELS) {
          throw new ApiException(
                  BAD_REQUEST,
                  "IMAGE_DIMENSIONS_TOO_LARGE",
                  "Ảnh quá lớn về kích thước (tối đa 30 megapixel).");
        }
        return contentType;
      } finally {
        reader.dispose();
      }
    } catch (IOException | RuntimeException e) {
      if (e instanceof ApiException api) {
        throw api;
      }
      throw invalid();
    }
  }

  private static BufferedImage decode(byte[] data, String contentType) {
    try {
      BufferedImage image =
              "image/jpeg".equals(contentType)
                      // Thumbnailator đọc EXIF Orientation và xoay ảnh khi giải mã.
                      ? Thumbnails.of(new ByteArrayInputStream(data)).scale(1.0).asBufferedImage()
                      : ImageIO.read(new ByteArrayInputStream(data));
      if (image == null) {
        throw invalid();
      }
      return image;
    } catch (IOException | RuntimeException e) {
      if (e instanceof ApiException api) {
        throw api;
      }
      throw invalid();
    }
  }

  /** JPEG không có kênh trong suốt: đưa mọi ảnh về RGB, nền trắng. */
  private static BufferedImage flatten(BufferedImage image) {
    if (image.getType() == BufferedImage.TYPE_3BYTE_BGR
            || image.getType() == BufferedImage.TYPE_INT_RGB) {
      return image;
    }
    BufferedImage rgb =
            new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = rgb.createGraphics();
    try {
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
      g.drawImage(image, 0, 0, null);
    } finally {
      g.dispose();
    }
    return rgb;
  }

  /** Thu nhỏ cho cạnh dài không vượt {@code size}; ảnh nhỏ hơn giữ nguyên. */
  private static BufferedImage fit(BufferedImage image, int size) {
    if (Math.max(image.getWidth(), image.getHeight()) <= size) {
      return image;
    }
    try {
      return Thumbnails.of(image).size(size, size).asBufferedImage();
    } catch (IOException | RuntimeException e) {
      throw invalid();
    }
  }

  private static byte[] encode(BufferedImage image) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Thumbnails.of(image).scale(1.0).outputFormat("jpg").outputQuality(QUALITY).toOutputStream(out);
      return out.toByteArray();
    } catch (IOException | RuntimeException e) {
      throw invalid();
    }
  }

  private static ApiException unsupported() {
    return new ApiException(
            UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_IMAGE_TYPE",
            "Chỉ hỗ trợ ảnh JPEG hoặc PNG.");
  }

  private static ApiException invalid() {
    return new ApiException(
            BAD_REQUEST, "INVALID_IMAGE", "Tệp ảnh bị hỏng hoặc không đọc được.");
  }
}
