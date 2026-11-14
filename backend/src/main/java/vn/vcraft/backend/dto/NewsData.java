package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public final class NewsData {
    private NewsData() {}

    public enum Kind { MATERIAL_PRICE, POWER_CUT, POLICY, EVENT }

    public record Input(
            @NotBlank(message = "Vui lòng nhập tiêu đề.") @Size(max = 160, message = "Tiêu đề tối đa 160 ký tự.") String title,
            @NotBlank(message = "Vui lòng nhập nội dung.") @Size(max = 50000, message = "Nội dung tối đa 50.000 ký tự.") String body,
            @NotNull(message = "Vui lòng chọn loại tin.") Kind kind, OffsetDateTime eventStart,
            OffsetDateTime eventEnd, @Size(max = 300, message = "Địa điểm tối đa 300 ký tự.") String eventPlace,
            boolean hidden) {
        @AssertTrue(message = "Sự kiện cần thời gian, địa điểm và giờ kết thúc hợp lệ.")
        public boolean isEventValid() {
            if (kind != Kind.EVENT) return eventStart == null && eventEnd == null && (eventPlace == null || eventPlace.isBlank());
            return eventStart != null && eventPlace != null && !eventPlace.isBlank()
                    && (eventEnd == null || !eventEnd.isBefore(eventStart));
        }
    }

    public record Summary(long id, String slug, String title, Kind kind, boolean hidden, OffsetDateTime publishedAt) {}

    public record News(long id, String slug, String title, String body, Kind kind, OffsetDateTime eventStart,
                       OffsetDateTime eventEnd, String eventPlace, boolean hidden, OffsetDateTime publishedAt) {}
}
