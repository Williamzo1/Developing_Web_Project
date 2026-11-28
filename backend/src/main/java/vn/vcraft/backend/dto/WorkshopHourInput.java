package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record WorkshopHourInput(
    @NotNull @Min(0) @Max(6) Integer dayOfWeek,
    @NotBlank @Pattern(regexp="^([01][0-9]|2[0-3]):[0-5][0-9]$",
                       message="Định dạng giờ phải là HH:mm") String openTime,
    @NotBlank @Pattern(regexp="^([01][0-9]|2[0-3]):[0-5][0-9]$",
                       message="Định dạng giờ phải là HH:mm") String closeTime
) {}
