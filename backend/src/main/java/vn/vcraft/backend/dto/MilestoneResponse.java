package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record MilestoneResponse(
    Long id, String title, String note,
    LocalDate happenedOn,
    Long processStepId, String processStepName,
    List<MediaResponse> images
) {}
