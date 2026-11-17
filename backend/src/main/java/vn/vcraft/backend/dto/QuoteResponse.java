package vn.vcraft.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import vn.vcraft.backend.model.QuoteStatus;

public record QuoteResponse(
    Long id,
    Long requestId,
    Long workshopId,
    BigDecimal unitPrice,
    BigDecimal totalPrice,
    Integer leadTimeDays,
    String note,
    QuoteStatus status,
    Long version,
    Instant createdAt,
    Instant updatedAt,
    ContactResponse workshopContact) {}
