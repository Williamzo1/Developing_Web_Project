package vn.vcraft.backend.dto;

public record WorkshopProfile(String slug, String name, String description, String familyHistory,
                              String address, String phone, String contactEmail, String facebookUrl,
                              Double latitude, Double longitude, boolean approved) {}
