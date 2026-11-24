package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;
import vn.vcraft.backend.validation.Utf8Bytes;
import java.util.List;
import java.math.BigDecimal;
import java.time.*;

public record RegisterResponse(
    Long id,
    String username,
    String displayName,
    String email,
    boolean emailVerified,
    String message
) {}
