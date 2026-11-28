package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record BatchCreateInput(
    @NotNull Long productId,
    @NotBlank @Size(max=120) String code,
    @Size(max=200) String name,
    @Size(max=5000) String description,
    @NotNull @Pattern(regexp="IN_STOCK|MADE_TO_ORDER") String kind,                         // IN_STOCK | MADE_TO_ORDER
    @Min(1) Integer quantity,
    @Size(max=40) String unit,
    @DecimalMin(value="0", inclusive=false) BigDecimal referencePrice,
    Boolean priceVisible,
    @Size(max=200) String supplyCapacity,
    LocalDate availableFrom,
    @Pattern(regexp="PUBLIC|PRIVATE") String visibility,                            // PUBLIC | PRIVATE
    @Size(max=1000) String partnerNote,
    List<@NotNull @Positive Long> mediaIds
) {}
