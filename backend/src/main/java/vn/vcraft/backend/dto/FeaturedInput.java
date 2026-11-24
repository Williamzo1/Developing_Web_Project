package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record FeaturedInput(
    List<@NotNull @Positive Long> workshopIds,
    List<@NotNull @Positive Long> productIds,
    List<@NotNull @Positive Long> batchIds
) {}
