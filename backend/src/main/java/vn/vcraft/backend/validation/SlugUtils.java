package vn.vcraft.backend.validation;

import java.text.Normalizer;
import java.util.function.Predicate;

public final class SlugUtils {

    private SlugUtils() {}

    /**
     * Chuyển chuỗi tiếng Việt thành slug URL-safe.
     * Ví dụ: "Gốm Bát Tràng" → "gom-bat-trang"
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return "";
        // 1. Normalize Unicode NFD để tách dấu ra
        String normalized = Normalizer.normalize(input.strip(), Normalizer.Form.NFD);
        // 2. Xóa combining marks (dấu thanh, dấu mũ...)
        String noMark = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // 3. Xử lý đặc biệt: đ → d, Đ → D (không bị NFD tách)
        String noDdash = noMark.replace("đ", "d").replace("Đ", "D");
        // 4. Lowercase, thay ký tự không phải a-z0-9 bằng dấu gạch ngang
        String slug = noDdash.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? "item" : slug;
    }

    /**
     * Sinh slug duy nhất bằng cách thêm hậu tố nếu trùng.
     * Caller cung cấp Predicate kiểm tra xem slug đã tồn tại chưa.
     */
    public static String uniqueSlug(String base, Predicate<String> exists) {
        String candidate = toSlug(base);
        if (candidate.isEmpty()) candidate = "item";
        if (candidate.length()>100) candidate=candidate.substring(0,100).replaceAll("-+$", "");
        if (!exists.test(candidate)) return candidate;
        for (int i = 2; i <= 999; i++) {
            String with = candidate + "-" + i;
            if (!exists.test(with)) return with;
        }
        // Fallback: thêm timestamp (không xảy ra trong thực tế)
        return candidate + "-" + System.currentTimeMillis();
    }
}
