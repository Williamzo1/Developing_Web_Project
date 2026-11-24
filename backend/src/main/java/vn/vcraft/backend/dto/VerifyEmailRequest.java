package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record VerifyEmailRequest(
    @NotBlank String username,
    @NotBlank @Pattern(regexp="[0-9]{6}", message="OTP phải là 6 chữ số.") String otp
) {}
