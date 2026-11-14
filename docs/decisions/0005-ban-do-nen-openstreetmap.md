# ADR-0005 — Bản đồ nền OpenStreetMap

- Trạng thái: Đã áp dụng
- Ngày: 2026-11-14

## Bối cảnh

FR-MAP-01 và FR-MAP-04 đã rõ. Leaflet là định hướng trong ADR-0002. Workspace hiện chưa có thư viện này và yêu cầu của dự án giới hạn việc thêm thư viện.

## Quyết định

Dùng React với tile chuẩn OpenStreetMap cho kéo, phóng to, chọn ghim và chọn tọa độ. Ghim lấy từ API của vCraft. Attribution hiển thị trên bản đồ. Request tile dùng chính sách referrer chỉ gửi origin, theo chính sách tile của OpenStreetMap.

## Hệ quả

Bản đồ nền cần Internet. Khi tile không tải được, giao diện vẫn có ghim theo tọa độ và danh sách xưởng. Phần ảnh và giờ mở cửa phụ thuộc MEDIA và Q-29.

[Chính sách tile OpenStreetMap](https://operations.osmfoundation.org/policies/tiles/)
