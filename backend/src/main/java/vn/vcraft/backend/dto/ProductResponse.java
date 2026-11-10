package vn.vcraft.backend.dto;

public record ProductResponse(
        long id, String slug, String name, String workshop, String category,
        String description, String imageUrl
) {}
