package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record ProductSummaryResponse(
    Long id, String slug, String name,
    String workshopName, String categoryName,
    BigDecimal referencePrice,  // null nếu ẩn giá
    MediaResponse coverImage,
    String description, String imageUrl
) {
    public ProductSummaryResponse(Long id,String slug,String name,String workshopName,String categoryName,
            BigDecimal referencePrice,MediaResponse coverImage) {
        this(id,slug,name,workshopName,categoryName,referencePrice,coverImage,null,
                coverImage==null ? null:coverImage.thumbUrl());
    }
    @com.fasterxml.jackson.annotation.JsonProperty("workshop") public String workshop() { return workshopName; }
    @com.fasterxml.jackson.annotation.JsonProperty("category") public String category() { return categoryName; }
}
