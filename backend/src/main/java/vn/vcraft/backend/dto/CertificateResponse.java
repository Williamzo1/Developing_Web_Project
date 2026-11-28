package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record CertificateResponse(
    Long id, String name, String issuer, LocalDate issuedOn, Instant createdAt, List<MediaResponse> images) {
    public CertificateResponse(Long id,String name,String issuer,LocalDate issuedOn,Instant createdAt) {
        this(id,name,issuer,issuedOn,createdAt,List.of());
    }
}
