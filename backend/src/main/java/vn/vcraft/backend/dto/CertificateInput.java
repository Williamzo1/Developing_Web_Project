package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record CertificateInput(
    @NotBlank @Size(max=200) String name,
    @Size(max=200) String issuer,
    LocalDate issuedOn,
    List<@NotNull @Positive Long> mediaIds
) {}
