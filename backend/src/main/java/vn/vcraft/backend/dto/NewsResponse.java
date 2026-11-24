package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record NewsResponse(
    Long id, String slug, String title, String body,
    String kind, Instant eventStart, Instant eventEnd, String eventPlace,
    MediaResponse coverImage,
    Instant publishedAt, Instant createdAt
) {}
