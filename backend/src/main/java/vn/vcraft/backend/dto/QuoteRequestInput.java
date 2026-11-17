package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;
import vn.vcraft.backend.model.RequestMode;

public record QuoteRequestInput(
    @PositiveOrZero(message = "Giá trị không được âm.") Long version,
    @Positive(message = "Giá trị phải lớn hơn 0.") Long categoryId,
    @Size(max = 160, message = "Tối đa 160 ký tự.") String title,
    @Positive(message = "Giá trị phải lớn hơn 0.") Integer quantity,
    @Size(max = 40, message = "Tối đa 40 ký tự.") String unit,
    @Size(max = 4000, message = "Tối đa 4000 ký tự.") String spec,
    @Size(max = 2000, message = "Tối đa 2000 ký tự.") String note,
    LocalDate desiredBy,
    Instant biddingDeadline,
    @NotNull(message = "Vui lòng nhập trường bắt buộc.") RequestMode mode,
    @Positive(message = "Giá trị phải lớn hơn 0.") Long targetWorkshopId,
    @Size(max = 20, message = "Tối đa 20 tag.")
        List<
                @NotNull(message = "Vui lòng nhập trường bắt buộc.")
                @Positive(message = "Giá trị phải lớn hơn 0.") Long>
            tagIds,
    @Size(max = 160, message = "Tối đa 160 ký tự.") String buyerContactName,
    @Email(message = "Email không hợp lệ.") @Size(max = 254, message = "Tối đa 254 ký tự.")
        String buyerContactEmail,
    @Pattern(regexp = "[+0-9 ().-]{7,30}", message = "Số điện thoại không hợp lệ.")
        String buyerContactPhone) {}
