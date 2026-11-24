package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record MapPinResponse(
    Long id, String slug, String name,
    java.math.BigDecimal latitude, java.math.BigDecimal longitude,
    MediaResponse thumbnail,
    Boolean acceptsVisitors, Boolean openNow,
    String address
) {}
