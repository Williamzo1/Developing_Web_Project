# Lộ trình

> Trạng thái: **Đề xuất**. Thứ tự dựa trên phụ thuộc kỹ thuật và trọng số của đề gốc; mốc thời gian cụ thể và người phụ trách do nhóm điền. Kế hoạch 8 tuần gốc của nhóm nằm ở `00-source/ghi-chu-nhom.md`.

## Phân vai (theo ghi chú nhóm)

| Thành viên | Vai trò |
|---|---|
| Huy | Điều phối frontend, UI/UX, khung giao diện |
| Thắng | Điều phối backend và database, khung backend |
| Dương | Fullstack/tích hợp, QA, môi trường phát triển, triển khai |

Sau khi có nền, chia việc **theo tính năng** (mỗi người làm trọn cả API lẫn giao diện của tính năng mình nhận).

## Giai đoạn

### M0 — Chốt tài liệu (đang làm)

- [ ] Nhóm đọc và duyệt `01-requirements/`, đặc biệt bảng lệch đề gốc/đặc tả trong `overview.md`.
- [ ] Họp nhóm theo `01-requirements/open-questions.md`: chốt các câu 🔴 (Q-13, Q-16, Q-17, Q-18, Q-19, Q-25, Q-26) trước khi code.
- [ ] Gửi giảng viên các câu nhóm A (bản nháp tin nhắn có sẵn trong file đó; ưu tiên Q-04, Q-06, Q-10, Q-11 vì ảnh hưởng schema).
- [ ] Duyệt `conventions.md`, `git-workflow.md` (Q-27, Q-28).
- [ ] Wireframe 5 màn hình trọng điểm (`02-design/ui-ux.md`).

### M1 — Nền tảng dùng chung (chặn mọi việc khác; làm trước khi chia song song)

- [ ] Frontend: tách cấu trúc thư mục, client API, AuthContext, route guard, bố cục 3 khu vực, design tokens, đăng nhập/đăng ký (FR-AUTH-01, 02, 04).
- [ ] Backend: validation, chuẩn hóa lỗi, khung phân trang, `/api/buyer/**` trong `SecurityConfig`, dọn `application.properties`.
- [ ] **MEDIA**: upload nhiều ảnh + resize + phục vụ ảnh (FR-MEDIA-01..03) — làm sớm vì mọi module nội dung đều cần.
- [ ] Seed dữ liệu demo có ảnh thật.

### M2 — Ba luồng song song (mỗi người một luồng)

| Luồng | Nội dung | FR |
|---|---|---|
| A. Triển lãm | Hồ sơ hộ, danh sách hộ, sản phẩm + công đoạn, danh mục có lọc/tìm/phân trang, trang chủ | FR-WS, FR-PROD, FR-HOME |
| B. Truy xuất + bản đồ | Lô, mốc sản xuất, landing page, QR, công khai/riêng tư; bản đồ Leaflet | FR-BATCH, FR-MAP |
| C. Báo giá | Yêu cầu mua sỉ, báo giá, state machine, khóa bản ghi, giao diện Buyer + Artisan | FR-QUOTE |

Phụ thuộc: B cần `products` và tọa độ xưởng từ A → thống nhất migration cho `workshops`/`products` ngay đầu M2. C chỉ cần `categories`, `workshops`, `accounts`.

### M3 — Quản trị và hoàn thiện

- [ ] Tin tức (FR-NEWS), giao diện quản trị, thống kê, nổi bật (FR-ADMIN).
- [ ] Các mục P1/P2 còn lại, đánh bóng giao diện, responsive.

### M4 — Tích hợp, kiểm thử, demo

- [ ] Chạy trọn `demo-script.md` trên máy sạch bằng Docker Compose.
- [ ] Kiểm phân quyền/riêng tư toàn hệ thống; kiểm QR bằng điện thoại thật.
- [ ] Triển khai (nếu Q-09 yêu cầu hoặc kịp thời gian).
- [ ] Tập demo, chuẩn bị câu trả lời cho các điểm lệch đề gốc (C-01..C-06).

## Thứ tự ưu tiên khi thiếu thời gian

1. Bốn yêu cầu kỹ thuật đề gốc §4.1–4.4 chạy được từ đầu đến cuối (mọi FR `P0`).
2. Giao diện đẹp cho các màn hình công khai và landing page lô.
3. `P1`, rồi `P2`.
