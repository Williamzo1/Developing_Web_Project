package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;

public record WorkshopContactInput(
    @Email(message = "Email không hợp lệ.") @Size(max = 254, message = "Tối đa 254 ký tự.")
        String email,
    @Pattern(regexp = "[+0-9 ().-]{7,30}", message = "Số điện thoại không hợp lệ.") String phone) {}
