package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public final class QuoteData {
    private QuoteData() {}

    public record Draft(
            @NotBlank(message = "Vui lòng nhập tiêu đề.") @Size(max = 160, message = "Tiêu đề tối đa 160 ký tự.") String title,
            @NotBlank(message = "Vui lòng chọn phân loại.") @Size(max = 120, message = "Mã phân loại tối đa 120 ký tự.") String category,
            @Positive(message = "Số lượng phải lớn hơn 0.") int quantity,
            @NotBlank(message = "Vui lòng nhập đơn vị.") @Size(max = 40, message = "Đơn vị tối đa 40 ký tự.") String unit,
            @NotBlank(message = "Vui lòng nhập yêu cầu kỹ thuật.") @Size(max = 10000, message = "Yêu cầu tối đa 10.000 ký tự.") String spec,
            LocalDate desiredBy, @Size(max = 10000, message = "Ghi chú tối đa 10.000 ký tự.") String note,
            OffsetDateTime biddingDeadline, Long version) {}

    public record Target(@NotBlank(message = "Vui lòng chọn một xưởng.") @Size(max = 120, message = "Mã xưởng tối đa 120 ký tự.") String workshop) {}

    public record Offer(
            @NotNull(message = "Vui lòng nhập đơn giá.") @PositiveOrZero(message = "Đơn giá không được âm.")
            @Digits(integer = 13, fraction = 0, message = "Đơn giá tối đa 13 chữ số.") BigDecimal unitPrice,
            @NotNull(message = "Vui lòng nhập tổng giá.") @PositiveOrZero(message = "Tổng giá không được âm.")
            @Digits(integer = 13, fraction = 0, message = "Tổng giá tối đa 13 chữ số.") BigDecimal totalPrice,
            @Positive(message = "Thời gian hoàn thành phải lớn hơn 0.") int leadTimeDays,
            @Size(max = 10000, message = "Ghi chú tối đa 10.000 ký tự.") String note) {}

    public record Selection(@NotNull(message = "Thiếu thời điểm cập nhật báo giá.") OffsetDateTime quoteUpdatedAt) {}

    public enum State { DRAFT, SENT, BIDDING, ACCEPTED, IN_PRODUCTION, DELIVERED, CANCELLED }

    public record Request(long id, long buyerId, String category, String title, int quantity, String unit,
                          String spec, LocalDate desiredBy, String note, OffsetDateTime biddingDeadline,
                          Long targetWorkshopId, String targetWorkshop, String targetName, State status, Long acceptedQuoteId, long version) {}

    public record Quote(long id, String workshop, BigDecimal unitPrice, BigDecimal totalPrice, int leadTimeDays,
                        String note, String status, OffsetDateTime updatedAt,
                        String phone, String contactEmail, String facebookUrl) {}

    public record Event(State fromStatus, State toStatus, OffsetDateTime createdAt, String actorRole) {}

    public record Details(Request request, List<Quote> quotes, List<Event> events) {}
}
