package vn.vcraft.backend.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ProductInput(
        @NotBlank(message = "Vui lòng nhập tên sản phẩm.")
        @Size(max = 160, message = "Tên tối đa 160 ký tự.") String name,
        @NotBlank(message = "Vui lòng chọn phân loại.")
        @Size(max = 120, message = "Mã phân loại tối đa 120 ký tự.") String category,
        @Size(max = 10000, message = "Mô tả tối đa 10.000 ký tự.") String description,
        @PositiveOrZero(message = "Giá không được âm.")
        @Digits(integer = 13, fraction = 0, message = "Giá phải là số nguyên VND tối đa 13 chữ số.") BigDecimal price,
        boolean priceVisible, boolean hidden
) {}
