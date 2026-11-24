package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record WorkshopDetailResponse(
    Long id, String slug, String name,
    String description, String familyHistory, String address,
    BigDecimal latitude, BigDecimal longitude,
    String contactEmail, String phone, String facebookUrl,
    Boolean acceptsVisitors, Boolean openNow,    // null = không có giờ khai báo
    MediaResponse coverImage,
    List<MediaResponse> images,
    List<CertificateResponse> certificates,
    List<WorkshopHourResponse> hours,
    List<ProductSummaryResponse> products,
    List<BatchSummaryResponse> publicBatches
) {}
