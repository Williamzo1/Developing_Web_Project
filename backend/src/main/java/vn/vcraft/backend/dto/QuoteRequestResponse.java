package vn.vcraft.backend.dto;

import java.time.*;
import java.util.List;
import vn.vcraft.backend.model.*;

public record QuoteRequestResponse(
    Long id,
    Long categoryId,
    String title,
    Integer quantity,
    String unit,
    String spec,
    String note,
    LocalDate desiredBy,
    Instant biddingDeadline,
    RequestMode mode,
    Long targetWorkshopId,
    List<Long> tagIds,
    RequestStatus status,
    Long acceptedQuoteId,
    Long version,
    Instant createdAt,
    Instant updatedAt,
    ContactResponse buyerContact,
    List<QuoteResponse> quotes) {}
