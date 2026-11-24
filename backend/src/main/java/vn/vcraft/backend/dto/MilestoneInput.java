package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record MilestoneInput(
    @NotBlank @Size(max=200) String title,
    @Size(max=2000) String note,
    @NotNull LocalDate happenedOn,
    Long processStepId,         // nullable — liên kết với công đoạn sản phẩm
    List<@NotNull @Positive Long> mediaIds
) {}
