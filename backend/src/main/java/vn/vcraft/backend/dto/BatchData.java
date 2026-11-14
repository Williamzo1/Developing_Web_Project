package vn.vcraft.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class BatchData {
    private BatchData() {}

    public enum Kind { IN_STOCK, MADE_TO_ORDER }
    public enum Visibility { PUBLIC, PRIVATE }

    public record Input(@Positive(message = "Vui lòng chọn sản phẩm.") long productId,
                        @NotBlank(message = "Vui lòng nhập mã lô.") @Size(max = 120, message = "Mã lô tối đa 120 ký tự.") String code,
                        @NotBlank(message = "Vui lòng nhập tên lô.") @Size(max = 160, message = "Tên lô tối đa 160 ký tự.") String name,
                        @Size(max = 10000, message = "Mô tả tối đa 10.000 ký tự.") String description,
                        @NotNull(message = "Vui lòng chọn loại lô.") Kind kind,
                        @Positive(message = "Số lượng phải lớn hơn 0.") int quantity,
                        @NotBlank(message = "Vui lòng nhập đơn vị.") @Size(max = 40, message = "Đơn vị tối đa 40 ký tự.") String unit,
                        @PositiveOrZero(message = "Giá không được âm.")
                        @Digits(integer = 13, fraction = 0, message = "Giá tối đa 13 chữ số nguyên.") BigDecimal price,
                        boolean priceVisible, @Size(max = 200, message = "Khả năng cung ứng tối đa 200 ký tự.") String supplyCapacity,
                        LocalDate availableFrom, @NotNull(message = "Vui lòng chọn phạm vi công khai.") Visibility visibility,
                        @Size(max = 10000, message = "Ghi chú tối đa 10.000 ký tự.") String partnerNote) {}

    public record Milestone(@NotBlank(message = "Vui lòng nhập tên mốc.")
                            @Size(max = 160, message = "Tên mốc tối đa 160 ký tự.") String title,
                            @Size(max = 10000, message = "Ghi chú tối đa 10.000 ký tự.") String note,
                            @NotNull(message = "Vui lòng chọn ngày của mốc.") LocalDate happenedOn) {}

    public record Milestones(@NotNull(message = "Vui lòng gửi danh sách mốc.")
                             @Size(max = 100, message = "Lô có tối đa 100 mốc.")
                             List<@Valid @NotNull(message = "Mốc không được để trống.") Milestone> items) {}

    public record Batch(long id, UUID publicCode, long productId, String productSlug, String productName,
                        String workshopSlug, String workshopName, String familyHistory, String code,
                        String name, String description, Kind kind, int quantity, String unit, BigDecimal price,
                        boolean priceVisible, String supplyCapacity, LocalDate availableFrom,
                        Visibility visibility, String partnerNote, boolean hidden) {}

    public record Details(Batch batch, List<Milestone> milestones, List<ProcessStepInput> steps) {}

    public record Share(String token) {}
}
