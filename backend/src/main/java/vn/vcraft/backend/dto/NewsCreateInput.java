package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record NewsCreateInput(
    @NotBlank @Size(max=300) String title,
    @NotBlank String body,
    @NotNull @Pattern(regexp="MATERIAL_PRICE|POWER_CUT|POLICY|EVENT|OTHER") String kind,
    Instant eventStart, Instant eventEnd,
    @Size(max=300) String eventPlace,
    boolean publish,            // true = published_at = now()
    Long coverMediaId
) {}
