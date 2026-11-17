package vn.vcraft.backend.dto;

import java.time.Instant;
import vn.vcraft.backend.model.RequestStatus;

public record QuoteEventResponse(
    Long id,
    Long actorAccountId,
    RequestStatus fromStatus,
    RequestStatus toStatus,
    String note,
    Instant createdAt) {}
