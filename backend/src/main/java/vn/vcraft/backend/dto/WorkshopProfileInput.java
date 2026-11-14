package vn.vcraft.backend.dto;

import jakarta.validation.constraints.*;

public record WorkshopProfileInput(
        @NotBlank(message = "Vui lòng nhập tên xưởng.")
        @Size(max = 160, message = "Tên xưởng tối đa 160 ký tự.") String name,
        @Size(max = 10000, message = "Giới thiệu tối đa 10.000 ký tự.") String description,
        @Size(max = 10000, message = "Lịch sử tối đa 10.000 ký tự.") String familyHistory,
        @Size(max = 300, message = "Địa chỉ tối đa 300 ký tự.") String address,
        @Size(max = 40, message = "Số điện thoại tối đa 40 ký tự.") String phone,
        @Email(message = "Email không hợp lệ.")
        @Size(max = 254, message = "Email tối đa 254 ký tự.") String contactEmail,
        @Pattern(regexp = "^$|https://(?:www\\.)?facebook\\.com/[^\\s]+",
                message = "Dùng liên kết https://www.facebook.com/ hợp lệ.")
        @Size(max = 500, message = "Liên kết Facebook tối đa 500 ký tự.") String facebookUrl,
        @DecimalMin(value = "-90", message = "Vĩ độ tối thiểu -90.")
        @DecimalMax(value = "90", message = "Vĩ độ tối đa 90.") Double latitude,
        @DecimalMin(value = "-180", message = "Kinh độ tối thiểu -180.")
        @DecimalMax(value = "180", message = "Kinh độ tối đa 180.") Double longitude
) {
    @AssertTrue(message = "Nhập cả vĩ độ và kinh độ hoặc để cả hai trống.")
    public boolean isCoordinatePairValid() {
        return (latitude == null) == (longitude == null)
                && (latitude == null || (Double.isFinite(latitude) && Double.isFinite(longitude)));
    }
}
