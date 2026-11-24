package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record ProductUpdateInput(
    @NotBlank @Size(max=160) String name,
    @NotNull Long categoryId,
    @Size(max=5000) String description,
    @DecimalMin(value="0", inclusive=false) BigDecimal referencePrice,
    Boolean priceVisible,
    List<@NotNull @Positive Long> tagIds,
    List<@NotNull @Positive Long> mediaIds
) {}
