package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record TagInput(
    @NotBlank @Size(max=120)
    @Pattern(regexp="^[a-z0-9-]+$", message="Slug chỉ dùng chữ thường, số và dấu gạch ngang")
    String slug,
    @NotBlank @Size(max=120) String name
) {}
