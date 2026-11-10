# Truy vết: đề gốc → yêu cầu

Mục đích: bảo đảm không câu nào trong **đề gốc** bị bỏ sót, vì buổi vấn đáp đối chiếu với đề gốc. Tình trạng cài đặt xem [../04-plan/status.md](../04-plan/status.md).

| Đề gốc | Nội dung | Yêu cầu đáp ứng | Ghi chú |
|---|---|---|---|
| §2 | Triển lãm số, không gian riêng mỗi hộ | FR-HOME-01..03, FR-WS-01 | |
| §2 | Sản phẩm độc bản, minh bạch quy trình | FR-PROD-02, FR-BATCH-02..04 | |
| §2 | BQL phân bổ đơn hàng lớn cho nhiều hộ | — | **Không làm**, theo đặc tả (C-01). Chuẩn bị câu trả lời khi vấn đáp: đã xác nhận với giảng viên. |
| §3 Artisan | Hồ sơ xưởng: lịch sử, ảnh, chứng nhận | FR-WS-01..03 | |
| §3 Artisan | Đăng sản phẩm kèm nhật ký sản xuất | FR-PROD-01..02, FR-BATCH-03 | |
| §3 Artisan | Nhận và chào giá yêu cầu báo giá | FR-QUOTE-04 | |
| §3 Buyer | Danh mục theo hộ hoặc phân loại | FR-PROD-03 | |
| §3 Buyer | Bản đồ, biết xưởng nào đang mở cửa | FR-MAP-01..03, FR-WS-05 | |
| §3 Buyer | Tạo yêu cầu báo giá, gửi đến các hộ phù hợp | FR-QUOTE-01..03 | |
| §3 Admin | Kiểm duyệt và cấp tài khoản hộ | FR-AUTH-03, FR-ADMIN-01 | |
| §3 Admin | Quản lý tin tức cộng đồng | FR-NEWS-01..02 | |
| §4.1 | Mỗi lô có mảng mốc thời gian + ảnh chứng minh | FR-BATCH-03 | Giả định Q-10 |
| §4.1 | Tự sinh QR mỗi lô → landing page kể toàn bộ quy trình | FR-BATCH-02, FR-BATCH-04 | Giả định Q-06 |
| §4.2 | State machine 6 trạng thái | FR-QUOTE-01..07, FR-QUOTE-11 | Giả định Q-04, Q-11 |
| §4.2 | Khóa bản ghi, cập nhật trạng thái phức tạp | FR-QUOTE-06, NFR-DATA-01 | Điểm kỹ thuật cần demo được |
| §4.3 | Tích hợp API bản đồ, ghim theo tọa độ, popup | FR-MAP-01..02 | Leaflet |
| §4.4 | Giao diện đẹp, image-centric | NFR-UI-01..02 | |
| §4.4 | Upload nhiều ảnh, resize/tối ưu ở backend | FR-MEDIA-01..03, NFR-PERF-01 | |

## Yêu cầu chỉ có trong đặc tả (không có trong đề gốc)

FR-BATCH-05..08 (lô công khai/riêng tư, nhiều hộ), FR-QUOTE-02 (chỉ định/mở), FR-QUOTE-05 (báo giá riêng tư giữa các hộ), FR-ADMIN-02..04 (thống kê, nổi bật, báo cáo vi phạm), FR-NEWS-03 (sự kiện).
