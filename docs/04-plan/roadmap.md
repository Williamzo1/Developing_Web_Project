# Lộ trình

> Trạng thái: **Đề xuất**. Thứ tự dựa trên phụ thuộc kỹ thuật và trọng số của đề gốc; mốc thời gian cụ thể và người phụ trách do nhóm điền. Kế hoạch 8 tuần gốc của nhóm nằm ở `00-source/ghi-chu-nhom.md`.

## Phân vai (theo ghi chú nhóm)

| Thành viên | Vai trò |
|---|---|
| Huy | Điều phối frontend, UI/UX, khung giao diện |
| Thắng | Điều phối backend và database, khung backend |
| Dương | Fullstack/tích hợp, QA, môi trường phát triển, triển khai |

Sau khi có nền, chia việc **theo tính năng** (mỗi người làm trọn cả API lẫn giao diện của tính năng mình nhận).

### Việc tiếp theo của từng người (cập nhật 2026-11-28)

Chia luồng M2 theo Q-25 (đã chốt). Backend của cả ba luồng phần lớn đã có, nên phần việc chính là giao diện và phần backend còn thiếu.

| Thành viên | Việc tiếp theo |
|---|---|
| Huy | (1) **Chặn cả nhóm:** gộp prototype `developing/prototype_1`, hoàn tất nền frontend (tách `main.jsx`, client API, `useApi`/`useMutation`, AuthContext, route guard, bố cục 3 khu vực, design tokens, đăng nhập/đăng ký + nhập OTP). (2) Wireframe 5 màn hình trọng điểm. (3) Luồng **A. Triển lãm**: trang chủ, danh sách/hồ sơ hộ, danh mục có lọc/tìm, chi tiết sản phẩm + công đoạn. |
| Thắng | (1) Luồng **C. Báo giá**: giao diện Buyer + Artisan, badge FR-QUOTE-10, ảnh tham khảo. (2) Backend còn thiếu: tìm kiếm theo tên/sort, đổi mật khẩu (FR-AUTH-06), báo cáo vi phạm (FR-ADMIN-04, cắt đầu tiên theo Q-35), nhãn "đang nhận đơn" (Q-29, migration mới). (3) Giữ `api-contract.md` và Postman khớp mã. |
| Dương | (1) Review và gộp PR M1 vào `master`; chạy `m1.py`, `m1_extension.py`, `quotes.py`, `phase02.py`, `media.py` với SMTP thật. (2) Seed tối thiểu ở `db/seed/` + nhập ảnh demo (Q-24). (3) **Triển khai VPS** + tên miền + HTTPS (Q-23), lịch sao lưu DB + ảnh; đưa test vào CI. (4) Luồng **B. Truy xuất + bản đồ**: landing page lô `/batches/{publicCode}`, giao diện quản lý lô/mốc/QR, bản đồ Leaflet. |

M3 (tin tức, giao diện quản trị, UI upload ảnh): ai xong luồng M2 trước nhận. Mốc thời gian: đếm ngược từ ngày thi (Q-26) — điền khi biết ngày.

## Giai đoạn

### M0 — Chốt tài liệu (đang làm)

- [ ] Nhóm đọc và duyệt `01-requirements/`, đặc biệt bảng lệch đề gốc/đặc tả trong `overview.md`.
- [x] Họp nhóm lần 1 (2026-11-10): đã chốt 18 câu nghiệp vụ (nhóm A và phần lớn nhóm B).
- [x] Chốt toàn bộ câu hỏi mở còn lại theo gợi ý (2026-11-28), gồm Q-29, Q-38, Q-39, Q-13, Q-23, Q-16, Q-17, Q-25, Q-26.
- [ ] Gửi giảng viên các điểm nhóm làm khác/diễn giải đề gốc §4.1–4.2 (bản nháp tin nhắn có sẵn trong `open-questions.md`).
- [x] Quy trình review/merge và dùng AI (Q-27, Q-28) đã chốt; `conventions.md`, `git-workflow.md` áp dụng như hiện có.
- [ ] Wireframe 5 màn hình trọng điểm (`02-design/ui-ux.md`).

## Sẵn sàng cài đặt (đánh giá ngày 2026-11-10)

> **Lịch sử.** Từ 2026-11-28 mọi câu hỏi mở đã chốt và backend M1 đã có phần lớn các việc dưới đây; xem hiện trạng ở `status.md`.

Dựa trên các câu đã chốt. "Sẵn sàng" = không còn câu hỏi mở nào ảnh hưởng tới schema hay hành vi của phần đó. Cập nhật bảng này mỗi khi chốt thêm câu hỏi.

### Làm được ngay — không vướng gì

| # | Việc | FR | Ghi chú |
|---|---|---|---|
| 1 | Nền backend: validation, chuẩn hóa mã lỗi, khung phân trang, thêm `/api/buyer/**` vào `SecurityConfig`, dọn `application.properties` | — | Mọi việc khác dựa vào. Giữ cấu trúc gói hiện tại (Q-17 chưa chốt, nhưng đây là hiện trạng nên không phát sinh việc làm lại). |
| 3 | Sản phẩm phía backend: CRUD của hộ, công đoạn sản xuất, lọc theo hộ/phân loại, tìm kiếm, phân trang, chi tiết theo slug, giá + cờ công khai/ẩn giá, Admin ẩn sản phẩm | FR-PROD-01..05, FR-ADMIN-05 | Chưa gồm tag (Q-38) và bộ ảnh (Q-13). |
| 4 | Lô phía backend: CRUD, mốc sản xuất (ngày + ghi chú), công khai/riêng tư, liên kết riêng tạo/thu hồi, endpoint landing page | FR-BATCH-01..03, 05..08 | Ảnh của mốc chờ MEDIA. |
| 5 | Hồ sơ hộ phía backend: giới thiệu, lịch sử, địa chỉ, liên hệ, Gmail + Facebook, tọa độ; danh sách hộ; endpoint ghim bản đồ (tọa độ + tóm tắt) | FR-WS-01, 02, 04, 06, 07, FR-MAP-01, 02 | Chưa gồm giờ mở cửa/trạng thái (Q-29). |
| 6 | Tin tức và thống kê phía backend | FR-NEWS-01..03, FR-ADMIN-01, 02 | |
| 7 | Sinh mã QR cho lô | FR-BATCH-04 | Q-20 chưa chốt nhưng mức ảnh hưởng nhỏ; làm ở backend theo gợi ý, đổi sau không tốn công. |
| 8 | **Nền frontend** (Q-18, Q-19 đã chốt): tách `main.jsx` theo cấu trúc thư mục, client API, `useApi`/`useMutation`, AuthContext, route guard theo vai trò, bố cục 3 khu vực, design tokens, trang đăng nhập | FR-AUTH-01, 02 | Làm trước khi ba người cùng viết giao diện. |
| 9 | Giao diện cho các phần backend đã sẵn sàng ở trên (danh mục, chi tiết sản phẩm, hồ sơ hộ, trang lô, bản đồ ghim + popup, báo giá, tin tức, quản trị) | theo từng module | Sau việc 8. Chỗ cần ảnh tạm dùng ảnh tĩnh cho đến khi có MEDIA. Nên có wireframe các màn hình trọng điểm trước. |

### Làm được một phần — vướng một câu hỏi

| Việc | Vướng | Phần làm được trước |
|---|---|---|
| Khớp yêu cầu mở theo tag; tag trên sản phẩm | Q-38 | Bảng `tags` và các bảng nối gần như chắc chắn cần; chờ chốt danh sách cố định hay tự do. |
| Đăng ký Buyer | Q-39 | Form + API đăng ký lưu email/SĐT; bước xác thực để sau. |
| Trạng thái "đang mở" trên bản đồ | Q-29 | Ghim + popup làm trước. |

### Chưa nên bắt đầu

| Việc | Vướng |
|---|---|
| Upload + resize ảnh (FR-MEDIA) | Q-13 (lưu ở đâu) phụ thuộc Q-23 (nơi đặt máy chủ); Q-21 (kích thước, giới hạn). Nên chốt sớm vì mọi module nội dung cần ảnh. |
| Triển khai Internet (NFR-OPS-03) | Q-23. |
| Xác thực email/SĐT | Q-39. |
| Bình luận | Q-37 (gợi ý: không làm). |

### M1 — Nền tảng dùng chung (chặn mọi việc khác; làm trước khi chia song song)

- [ ] Frontend: tách cấu trúc thư mục, client API, AuthContext, route guard, bố cục 3 khu vực, design tokens, đăng nhập/đăng ký (FR-AUTH-01, 02, 04).
- [x] Backend: validation, chuẩn hóa lỗi, khung phân trang, `/api/buyer/**` trong `SecurityConfig`, dọn `application.properties` — bản M1 backend ngày 2026-11-12; xem `../03-engineering/m1-backend.md`. MEDIA, nền frontend và deploy chưa hoàn thành.
- [x] **MEDIA backend**: upload nhiều ảnh + resize + phục vụ ảnh (FR-MEDIA-01..03) — 2026-11-24, theo Q-13/Q-21 (đã chốt 2026-11-28). Còn giao diện upload và gắn ảnh vào từng module.
- [ ] Seed tối thiểu (tài khoản mẫu, phân loại, tag, vài hộ/sản phẩm).
- [ ] **Triển khai lên Internet ngay sau M1** (bắt buộc — Q-09) để nhóm nhập dữ liệu thật dần qua giao diện; thiết lập sao lưu DB + ảnh.

### M2 — Ba luồng song song (mỗi người một luồng)

| Luồng | Nội dung | FR |
|---|---|---|
| A. Triển lãm | Hồ sơ hộ, danh sách hộ, sản phẩm + công đoạn, danh mục có lọc/tìm/phân trang, trang chủ | FR-WS, FR-PROD, FR-HOME |
| B. Truy xuất + bản đồ | Lô, mốc sản xuất, landing page, QR, công khai/riêng tư; bản đồ Leaflet | FR-BATCH, FR-MAP |
| C. Báo giá | Yêu cầu mua sỉ, báo giá, state machine, khóa bản ghi, giao diện Buyer + Artisan | FR-QUOTE |

Phụ thuộc: B cần `products` và tọa độ xưởng từ A → thống nhất migration cho `workshops`/`products` ngay đầu M2. C cần `categories`, `workshops`, `accounts`, và `tags` + `product_tags` (từ A) cho việc khớp yêu cầu mở.

### M3 — Quản trị và hoàn thiện

- [ ] Tin tức (FR-NEWS), giao diện quản trị, thống kê, nổi bật (FR-ADMIN).
- [ ] Các mục P1/P2 còn lại, đánh bóng giao diện, responsive.

### M4 — Tích hợp, kiểm thử, demo

- [ ] Chạy trọn `demo-script.md` trên máy sạch bằng Docker Compose.
- [ ] Kiểm phân quyền/riêng tư toàn hệ thống; kiểm QR bằng điện thoại thật.
- [ ] Bản triển khai Internet ổn định, có đủ dữ liệu "người dùng thật" và bản sao lưu mới nhất; chạy trọn kịch bản trên bản này bằng mạng di động.
- [ ] Tập demo, chuẩn bị câu trả lời cho các điểm lệch đề gốc (C-01..C-06).

## Thứ tự ưu tiên khi thiếu thời gian

1. Bốn yêu cầu kỹ thuật đề gốc §4.1–4.4 chạy được từ đầu đến cuối (mọi FR `P0`).
2. Giao diện đẹp cho các màn hình công khai và landing page lô.
3. `P1`, rồi `P2`.
