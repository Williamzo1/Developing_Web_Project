# ADR-0004 — Hợp đồng nền backend M1

Ngày: 2026-11-12. Trạng thái: đã áp dụng trong bản M1 backend theo phạm vi yêu cầu.

- Dùng Bean Validation của Spring Boot, không tự khai phiên bản Hibernate Validator. Kiểm tra tại DTO/controller và service; quyền sở hữu vẫn là nghiệp vụ service.
- Một ApiErrorResponse cho advice và Security; fieldErrors là object tên trường → thông báo tiếng Việt. Không đưa rejectedValue ra client.
- PageResponse dùng items/page/size/totalElements/totalPages/first/last. Endpoint mới /api/products/page có page từ 0, mặc định size 12, giới hạn 50, sort id ASC. Giữ API danh mục cũ trả mảng để không làm hỏng tích hợp Phase02. Muốn bỏ endpoint cũ phải thống nhất frontend trước.
- Buyer, Artisan và Admin có vùng URL riêng, không suy diễn Admin là Buyer. /api/buyer/me là endpoint kiểm tra quyền, không thay /api/me chung và chưa cài nghiệp vụ báo giá.
- Profile local bật tường minh, bí mật qua env hoặc file private gitignored. Flyway vẫn là cơ chế duy nhất quản lý schema; giữ nguyên V1–V3.

Giới hạn: chưa làm MEDIA, frontend foundation, deploy hoặc đổi cơ chế opaque token thành JWT. Spring Boot/Maven/Java giữ phiên bản source.
