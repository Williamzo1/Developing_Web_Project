package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record BatchDetailResponse(
    Long id,
    String publicCode,
    String code,
    String name,
    String description,
    String kind,
    Integer quantity,
    String unit,
    BigDecimal referencePrice,  // null nếu ẩn giá
    String supplyCapacity,
    LocalDate availableFrom,
    String visibility,
    // Thông tin sản phẩm + xưởng (câu chuyện)
    ProductSummaryResponse product,
    WorkshopSummaryResponse workshop,
    // Quy trình sản xuất của sản phẩm (tầng 1)
    List<StepResponse> productSteps,
    // Mốc theo lô (tầng 2)
    List<MilestoneResponse> milestones,
    List<MediaResponse> images,
    Instant createdAt, Instant updatedAt
) {}
