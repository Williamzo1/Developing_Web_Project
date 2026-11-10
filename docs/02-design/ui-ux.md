# Giao diện và trải nghiệm

> Trạng thái: **Đề xuất**. Chưa có wireframe; nhóm (người điều phối frontend) duyệt trước khi làm hàng loạt. Giao diện là tiêu chí chấm trực tiếp (đề gốc §4.4), không phải phần trang trí.

## 1. Định hướng

- **Image-centric**: ảnh là nội dung chính; chữ hỗ trợ. Thẻ sản phẩm/hộ/lô lấy ảnh làm trọng tâm, tỉ lệ khung cố định.
- **Bản sắc gốm Bát Tràng**: bảng màu lấy từ men lam, đất nung, men ngà; kiểu chữ có chân cho tiêu đề, không chân cho nội dung. Giá trị cụ thể khai báo một lần bằng biến CSS trong `:root` (đã có trong `frontend/src/styles.css`) — component không viết màu/cỡ chữ trực tiếp.
- **Kể chuyện**: hồ sơ hộ, trang sản phẩm và landing page lô trình bày như một câu chuyện có trình tự (con người → quy trình → sản phẩm), không phải bảng thông số.
- Tránh giao diện kiểu sàn thương mại điện tử: không giỏ hàng, không nút "Mua ngay"; lời kêu gọi hành động là **"Gửi yêu cầu báo giá"** và **"Xem xưởng trên bản đồ"**.

## 2. Bản đồ màn hình và tuyến đường

Đường dẫn tiếng Việt không dấu (đã có `/san-pham`).

| Đường dẫn | Màn hình | Quyền | FR |
|---|---|---|---|
| `/` | Trang chủ triển lãm | Công khai | FR-HOME |
| `/ho-gia-dinh` · `/ho-gia-dinh/:slug` | Danh sách hộ · Hồ sơ hộ | Công khai | FR-WS |
| `/san-pham` · `/san-pham/:slug` | Danh mục (lọc hộ/phân loại) · Chi tiết + quy trình | Công khai | FR-PROD |
| `/lo-hang` | Danh sách lô công khai | Công khai | FR-BATCH-07 |
| `/lo/:publicCode` | **Landing page lô** (đích của QR) | Công khai / liên kết riêng | FR-BATCH-02..06 |
| `/ban-do` | Bản đồ làng nghề | Công khai | FR-MAP |
| `/tin-tuc` · `/tin-tuc/:slug` | Tin tức | Công khai | FR-NEWS |
| `/dang-nhap` · `/dang-ky` | Đăng nhập · Đăng ký Buyer | Công khai | FR-AUTH |
| `/mua-si/yeu-cau` · `/mua-si/yeu-cau/moi` · `/mua-si/yeu-cau/:id` | Yêu cầu của tôi · Tạo · Chi tiết + so sánh báo giá | BUYER | FR-QUOTE |
| `/xuong` | Bảng điều khiển của hộ | ARTISAN | — |
| `/xuong/ho-so` | Sửa hồ sơ, tọa độ, giờ mở cửa, chứng nhận | ARTISAN | FR-WS |
| `/xuong/san-pham[/:id]` | Quản lý sản phẩm + công đoạn | ARTISAN | FR-PROD |
| `/xuong/lo-hang[/:id]` | Quản lý lô, mốc sản xuất, QR, công khai/riêng tư | ARTISAN | FR-BATCH |
| `/xuong/yeu-cau[/:id]` | Yêu cầu nhận được, gửi báo giá, cập nhật tiến độ | ARTISAN | FR-QUOTE |
| `/quan-tri` | Thống kê | ADMIN | FR-ADMIN-02 |
| `/quan-tri/ho-gia-dinh` | Cấp tài khoản, duyệt hộ | ADMIN | FR-AUTH-03, FR-ADMIN-01 |
| `/quan-tri/tin-tuc` | Quản lý tin | ADMIN | FR-NEWS-01 |
| `/quan-tri/noi-bat` · `/quan-tri/bao-cao` | Nổi bật · Báo cáo vi phạm | ADMIN | FR-ADMIN-03, 04 |

Ba khu vực có bố cục riêng: **công khai** (header + footer giàu hình ảnh), **khu quản lý của hộ/buyer** và **khu quản trị** (thanh bên, gọn, thiên về thao tác).

## 3. Màn hình trọng điểm

1. **Landing page lô** — mở từ điện thoại sau khi quét QR, thiết kế mobile-first: ảnh lô → tên, hộ sản xuất (kèm huy hiệu xác minh) → dòng thời gian mốc sản xuất của lô (ngày + ảnh) → quy trình của sản phẩm → câu chuyện hộ → nút xem xưởng trên bản đồ / gửi yêu cầu báo giá.
2. **Bản đồ** — bản đồ chiếm phần lớn màn hình; ghim hai kiểu (đang mở / đóng — ý nghĩa chờ Q-29); popup có ảnh, tên, giờ mở cửa, liên kết hồ sơ; bộ lọc "Đang mở cửa".
3. **Danh mục sản phẩm** — lưới ảnh, bộ lọc hộ + phân loại, tìm kiếm, phân trang; chỉ tải thumbnail.
4. **So sánh báo giá (Buyer)** — bảng/thẻ các báo giá: hộ, giá, thời gian; nút chấp nhận có hộp xác nhận; thanh tiến trình 6 trạng thái.
5. **Trình sửa quy trình và mốc sản xuất (Artisan)** — thêm/xóa/sắp xếp công đoạn, kéo thả nhiều ảnh, xem trước.

## 4. Quy tắc cho mọi màn hình

- Ba trạng thái bắt buộc: đang tải (khung xương), rỗng (có hướng dẫn hành động tiếp theo), lỗi (thông báo + nút thử lại).
- Ảnh: luôn có `width`/`height` hoặc `aspect-ratio`, `loading="lazy"`, `alt` có nghĩa; danh sách dùng thumbnail, chi tiết dùng cỡ vừa/lớn; có ảnh thay thế khi lỗi (đã có `ProductImage`).
- Form: nhãn rõ ràng, lỗi hiển thị cạnh trường bằng tiếng Việt, vô hiệu hóa nút khi đang gửi, hành động không hoàn tác được phải xác nhận.
- Responsive: thử ở 390, 768, 1280 px. Vùng bấm tối thiểu 44 px trên di động.
- Trợ năng cơ bản: HTML ngữ nghĩa, điều hướng được bằng bàn phím, tương phản đủ, `lang="vi"`.
- Mọi chữ hiển thị bằng tiếng Việt; dùng đúng thuật ngữ ở `01-requirements/glossary.md`.
- Định dạng: tiền `1.250.000 ₫`, ngày `dd/MM/yyyy`. Giá bị hộ ẩn hiển thị "Liên hệ báo giá".

## 5. Việc còn thiếu

- [ ] Wireframe cho 5 màn hình trọng điểm.
- [ ] Chốt bảng màu, kiểu chữ, bộ ảnh thật (tự chụp hoặc có giấy phép sử dụng — ghi nguồn).
- [ ] Quyết định có dùng thư viện UI/CSS hay tiếp tục CSS thuần (ghi ADR).
