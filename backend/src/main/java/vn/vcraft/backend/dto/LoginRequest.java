package vn.vcraft.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import vn.vcraft.backend.validation.Utf8Bytes;

public record LoginRequest(
        @NotNull(message = "Vui lòng nhập tên đăng nhập.")
        @Pattern(regexp = "[a-z0-9-]{3,60}", message = "Tên đăng nhập gồm 3–60 chữ thường, số hoặc dấu gạch ngang.")
        String username,
        @NotBlank(message = "Vui lòng nhập mật khẩu.")
        @Utf8Bytes(max = 72) String password
) {}
