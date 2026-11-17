package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;

public record NewArtisanRequest(
        @NotBlank(message = "Vui lòng nhập tên xưởng.")
        @Size(max = 160, message = "Tên xưởng tối đa 160 ký tự.") String workshopName,
        @NotNull(message = "Vui lòng nhập tên đăng nhập.")
        @Pattern(regexp = "[a-z0-9-]{3,60}", message = "Tên đăng nhập gồm 3–60 chữ thường, số hoặc dấu gạch ngang.")
        String username,
        @NotBlank(message = "Vui lòng nhập mật khẩu.")
        @Size(min = 10, message = "Mật khẩu có ít nhất 10 ký tự.")
        @Utf8Bytes(max = 72) String password
) {
    public NewArtisanRequest {
        if (workshopName != null) workshopName = workshopName.strip();
    }
}
