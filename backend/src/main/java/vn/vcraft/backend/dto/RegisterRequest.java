package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record RegisterRequest(
    @NotBlank @Size(min=3, max=60)
    @Pattern(regexp="^[a-z0-9-]{3,60}$", message="Chỉ dùng chữ thường, số và dấu gạch ngang.")
    String username,

    @NotBlank @Utf8Bytes(max=72)  // kiểm tra ≤72 byte cho BCrypt
    @Size(min=8, message="Mật khẩu tối thiểu 8 ký tự.")
    String password,

    @NotBlank @Size(max=160)
    String displayName,

    @NotBlank @Size(max=254) @Email(message="Email không hợp lệ.")
    String email,

    @NotBlank @Pattern(regexp="^[0-9+\\-\\s]{7,20}$", message="Số điện thoại không hợp lệ.")
    String phone
) {}
