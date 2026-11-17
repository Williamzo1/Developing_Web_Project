package vn.vcraft.backend.dto;

import java.time.Instant;
import vn.vcraft.backend.model.*;

public record QuoteRequestSummary(
    Long id,
    String title,
    RequestMode mode,
    RequestStatus status,
    Integer quantity,
    String unit,
    Instant biddingDeadline,
    Instant updatedAt,
    Long version) {}
