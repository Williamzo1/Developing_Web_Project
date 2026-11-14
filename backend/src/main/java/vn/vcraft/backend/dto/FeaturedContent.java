package vn.vcraft.backend.dto;

import jakarta.validation.constraints.Positive;

public final class FeaturedContent {
    private FeaturedContent() {}
    public record Input(@Positive(message = "Mã xưởng không hợp lệ.") Long workshopId,
                        @Positive(message = "Mã sản phẩm không hợp lệ.") Long productId,
                        @Positive(message = "Mã lô không hợp lệ.") Long batchId) {}
    public record Item(long id, String code, String name, String imageUrl) {}
    public record Home(Item workshop, Item product, Item batch) {}
}
