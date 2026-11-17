# ADR-0002: Stack công nghệ và cơ chế xác thực

- Trạng thái: Stack — Đã chốt (đã có trong mã). Xác thực — Đề xuất (Q-16).
- Ngày: 2026-11-10 (ghi lại quyết định đã có trong repo)
- Người quyết định: Nhóm

## Bối cảnh

Đề cho tự do chọn công nghệ. Repo đã có mã chạy được với một stack cụ thể; ghi chú nhóm xác nhận lựa chọn này. Bản đánh giá v0 trong ghi chú nhóm đề xuất dùng JWT, trong khi mã hiện tại đã cài xác thực bằng token ngẫu nhiên lưu DB.

## Quyết định

**Stack (đã chốt):**

| Tầng | Công nghệ |
|---|---|
| Frontend | React 19, React Router 7, Vite 7, CSS thuần |
| Backend | Java 17, Spring Boot 4.1.2 (Web MVC, Security, Data JPA), Flyway |
| Database | PostgreSQL 16 |
| Đóng gói | Docker Compose (db, backend, frontend/Nginx) |
| Bản đồ | Leaflet + OpenStreetMap (dự kiến, khi làm FR-MAP) |

**Xác thực (đề xuất giữ nguyên):** token ngẫu nhiên do server cấp, DB lưu mã băm trong `account_sessions` kèm hạn; client gửi `Authorization: Bearer`. Không chuyển sang JWT.

## Phương án đã cân nhắc

- **JWT**: không cần tra DB mỗi request, nhưng thu hồi khi đăng xuất/khóa hộ phức tạp hơn; phải viết lại phần đang chạy và đang có test. Với quy mô bài tập, lợi ích không đáng kể.
- **Session cookie của Spring**: phải xử lý CSRF; cấu hình hiện tại đang stateless.

## Hệ quả

- Khóa hộ hoặc đăng xuất có hiệu lực ngay (filter kiểm trạng thái xưởng mỗi request).
- Frontend phải tự lưu token và gắn vào mọi request qua client API dùng chung.
- Các thư viện bổ sung chưa quyết (resize ảnh, sinh QR, validation, UI kit, công cụ test): quyết khi làm module tương ứng và ghi ADR mới.
