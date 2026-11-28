package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record WorkshopProfileInput(
    @NotBlank @Size(max=160) String name,
    @Size(max=5000) String description,
    @Size(max=5000) String familyHistory,
    @Size(max=500) String address,
    @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
    @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
    @Email @Size(max=254) String contactEmail,
    @Pattern(regexp="^[0-9+\\-\\s]{7,20}$") String phone,
    @Size(max=500) String facebookUrl,
    Boolean acceptsVisitors,
    Long coverMediaId,
    List<@NotNull @Positive Long> mediaIds       // thứ tự ảnh xưởng
) {}
