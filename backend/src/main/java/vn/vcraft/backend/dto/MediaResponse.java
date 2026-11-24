package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record MediaResponse(long id,
    String thumbUrl, String mediumUrl, String largeUrl,
    int width, int height) {}
