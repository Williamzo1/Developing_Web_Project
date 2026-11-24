package vn.vcraft.backend.dto;

/**
 * Một ảnh đã upload. {@code width}/{@code height} là kích thước của bản lớn (đã xoay đúng hướng
 * chụp), dùng để giữ tỉ lệ khung ảnh trên giao diện.
 */
public record MediaResponse(
        long id, String thumbUrl, String mediumUrl, String largeUrl, int width, int height) {}
