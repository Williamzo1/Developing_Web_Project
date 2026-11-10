# Kịch bản demo vấn đáp

> Trạng thái: **Bản nháp mục tiêu** — mô tả luồng mà sản phẩm cuối phải chạy được; dùng làm định nghĩa "xong" ở mức hệ thống. Hầu hết các bước **chưa** chạy được (xem `status.md`). Cập nhật khi tính năng hoàn thành.

Bài được chấm bằng demo trực tiếp, không slide. Kịch bản đi theo một câu chuyện xuyên suốt và chạm đủ 4 yêu cầu kỹ thuật của đề gốc.

## Chuẩn bị

- Hệ thống chạy bằng Docker Compose với dữ liệu seed: ≥ 5 hộ có tọa độ (có hộ mở, hộ đóng), vài chục đến vài trăm sản phẩm có ảnh thật, ≥ 2 lô có mốc sản xuất, tin tức mẫu, một yêu cầu đang nhận báo giá với ≥ 2 báo giá.
- Điện thoại cùng mạng với máy demo; `APP_BASE_URL` trỏ đúng địa chỉ để quét QR được. In sẵn một mã QR.
- Mở sẵn 3 trình duyệt/hồ sơ: Buyer, Artisan, Admin.

## Luồng

| # | Vai | Thao tác | Chứng minh |
|---|---|---|---|
| 1 | Khách | Trang chủ → danh sách hộ → hồ sơ một hộ (lịch sử, ảnh xưởng, chứng nhận) | §3 Artisan, §4.4 image-centric |
| 2 | Khách | Danh mục: lọc theo hộ, theo phân loại; cuộn danh sách dài | §3 Buyer, §4.4 tối ưu ảnh (mở tab Network cho thấy thumbnail nhỏ) |
| 3 | Khách | Bản đồ: ghim xưởng, lọc "đang mở cửa", bấm ghim xem popup → hồ sơ | §4.3 |
| 4 | Admin | Cấp tài khoản hộ mới → hộ chưa đăng nhập được → duyệt → đăng nhập được | §3 Admin |
| 5 | Artisan | Sửa hồ sơ, tải nhiều ảnh cùng lúc; tạo sản phẩm với các công đoạn | §3 Artisan, §4.4 upload |
| 6 | Artisan | Tạo lô, thêm mốc sản xuất có ngày + ảnh; hệ thống sinh QR | §4.1 |
| 7 | Khách (điện thoại) | Quét QR → landing page kể lại quá trình làm ra lô | §4.1 |
| 8 | Artisan | Chuyển lô sang riêng tư → khách không còn thấy; mở bằng liên kết riêng thì thấy | Đặc tả §5 |
| 9 | Buyer | Tạo yêu cầu "5.000 cốc in logo trong 1 tháng" → lưu nháp → gửi (mở) | §4.2 Draft → Sent |
| 10 | 2 Artisan | Mỗi hộ thấy yêu cầu, gửi báo giá; hộ này không thấy báo giá hộ kia | §4.2 Bidding, đặc tả §5 |
| 11 | Buyer | So sánh báo giá → chấp nhận một báo giá. Thử chấp nhận báo giá khác ở tab thứ hai → bị từ chối | §4.2 Accepted + **khóa bản ghi** |
| 12 | Artisan được chọn | Cập nhật Đang sản xuất → Đã giao; Buyer thấy tiến trình | §4.2 In Production → Delivered |
| 13 | Admin | Đăng tin "lịch cúp điện"; xem thống kê; chọn lô nổi bật lên trang chủ | §3 Admin |

## Câu hỏi vấn đáp cần chuẩn bị

- Vì sao không có chức năng BQL phân bổ đơn hàng? → Đã làm rõ với giảng viên: mỗi hộ là pháp nhân độc lập, BQL không điều phối (C-01).
- Khóa bản ghi cài thế nào, chống tình huống gì? → `02-design/quote-state-machine.md` mục 3.
- Ảnh được tối ưu ra sao? → Các biến thể resize ở backend, danh sách chỉ tải thumbnail, lazy loading.
- QR sinh ở đâu, mã hóa gì? Lô riêng tư bảo vệ thế nào?
- Phân quyền kiểm ở đâu? → Vai trò ở `SecurityConfig`, quyền sở hữu ở service.
- Mỗi thành viên cần giải thích được phần mã mình phụ trách, kể cả phần có AI hỗ trợ.
