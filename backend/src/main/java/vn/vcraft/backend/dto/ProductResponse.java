package vn.vcraft.backend.dto;

import java.math.BigDecimal;

public record ProductResponse(
        long id, String slug, String name, String workshop, String category,
        String description, String imageUrl, String workshopSlug, BigDecimal price
) {}
