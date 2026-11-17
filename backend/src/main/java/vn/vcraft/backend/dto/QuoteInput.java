package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record QuoteInput(
    @PositiveOrZero(message = "Giá trị không được âm.") Long version,
    @NotNull(message = "Vui lòng nhập trường bắt buộc.")
        @DecimalMin(value = "0", inclusive = false, message = "Đơn giá phải lớn hơn 0.")
        @Digits(
            integer = 13,
            fraction = 2,
            message = "Đơn giá tối đa 13 chữ số nguyên và 2 chữ số thập phân.")
        BigDecimal unitPrice,
    @NotNull(message = "Vui lòng nhập trường bắt buộc.")
        @Min(value = 1, message = "Số ngày ít nhất là 1.")
        @Max(value = 3650, message = "Số ngày tối đa là 3650.")
        Integer leadTimeDays,
    @Size(max = 2000, message = "Tối đa 2000 ký tự.") String note) {}
