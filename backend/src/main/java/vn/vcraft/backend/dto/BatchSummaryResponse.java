package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record BatchSummaryResponse(
    Long id, String publicCode, String code, String name,
    String kind, Integer quantity, String unit,
    BigDecimal referencePrice,
    MediaResponse coverImage,
    LocalDate availableFrom
) {}
