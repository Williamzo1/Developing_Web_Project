package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record AdminStatsResponse(
    long totalWorkshops, long approvedWorkshops,
    long totalProducts, long totalBatches,
    long totalQuoteRequests, long totalBuyers
) {}
