package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record HomeResponse(
    List<WorkshopSummaryResponse> featuredWorkshops,
    List<ProductSummaryResponse> featuredProducts,
    List<BatchSummaryResponse> featuredBatches,
    List<NewsResponse> latestNews
) {}
