package vn.vcraft.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record ManagedProduct(long id, String slug, String name, String category, String description,
                             BigDecimal price, boolean priceVisible, boolean hidden, boolean adminHidden,
                             List<ProcessStepInput> steps) {}
