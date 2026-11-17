package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;

public record VersionInput(
    @NotNull(message = "Vui lòng nhập trường bắt buộc.")
        @PositiveOrZero(message = "Giá trị không được âm.")
        Long version) {}
