package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record StepInput(
    @NotBlank @Size(max=200) String name,
    @Size(max=2000) String description,
    List<@NotNull @Positive Long> mediaIds
) {}
