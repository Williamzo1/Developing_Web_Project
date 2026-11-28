package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record ProductDetailResponse(
    Long id, String slug, String name,
    String workshopSlug, String workshopName,
    String categoryName,
    String description,
    BigDecimal referencePrice,  // null nếu price_visible=false
    Boolean priceVisible,
    List<String> tags,
    List<MediaResponse> images,
    List<StepResponse> steps,
    List<BatchSummaryResponse> publicBatches,
    String imageUrl
) {
    @com.fasterxml.jackson.annotation.JsonProperty("workshop") public String workshop() { return workshopName; }
    @com.fasterxml.jackson.annotation.JsonProperty("category") public String category() { return categoryName; }
}
