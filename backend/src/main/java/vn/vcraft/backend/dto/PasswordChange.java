package vn.vcraft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.vcraft.backend.validation.Utf8Bytes;

public record PasswordChange(
        @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại.")
        @Utf8Bytes(max = 72, message = "Mật khẩu hiện tại vượt quá 72 byte UTF-8.") String currentPassword,
        @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
        @Size(min = 10, message = "Mật khẩu mới cần ít nhất 10 ký tự.")
        @Utf8Bytes(max = 72, message = "Mật khẩu mới vượt quá 72 byte UTF-8.") String newPassword
) {}
