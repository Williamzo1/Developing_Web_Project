# Hiện trạng cài đặt

> Tài liệu **sống**: cập nhật trong cùng thay đổi mỗi khi một yêu cầu đổi tình trạng. Đây là nơi duy nhất ghi "đã làm đến đâu".
>
> Cập nhật lần cuối: **2026-11-14** — nền frontend và các luồng nghiệp vụ đã rõ.

Ký hiệu: ✅ xong (API + giao diện) · 🟡 một phần · ⬜ chưa làm.

## Tóm tắt

Đã nối đăng nhập/phân quyền với SPA. Có luồng báo giá chỉ định đi từ nháp đến giao hàng và luồng truy xuất lô qua trang công khai/liên kết riêng. Ảnh upload, QR, giờ mở cửa trên bản đồ, ghép tag và đăng ký Buyer còn phụ thuộc quyết định của nhóm.

## Theo module

| Module | Tình trạng | Đã có | Còn thiếu |
|---|---|---|---|
| HOME | ✅ | Giới thiệu làng nghề, nội dung Admin chọn, điều hướng tới hộ, danh mục, lô, bản đồ, tin tức | — |
| AUTH | 🟡 | Đăng nhập, đăng xuất, /me, phân quyền 3 vai trò, Admin cấp tài khoản hộ, đổi mật khẩu | Đăng ký/xác thực Buyer (Q-39) |
| WS | 🟡 | Hồ sơ công khai và tìm theo tên, văn bản/liên hệ/tọa độ, huy hiệu duyệt, liên kết tới sản phẩm và lô của hộ | Ảnh, chứng nhận, giờ mở cửa |
| PROD | 🟡 | Lọc theo hộ/phân loại, tìm theo tên, sắp xếp, phân trang, slug; tạo/sửa/ẩn, giá, công đoạn văn bản | Bộ ảnh và tag |
| BATCH | 🟡 | Tạo/sửa, danh sách lọc, mốc sản xuất, landing page, giá, liên kết riêng tạo/thu hồi, API hậu kiểm | Ảnh và QR (Q-20) |
| QUOTE | 🟡 | Nháp/gửi cho một hộ/hủy, gửi/sửa/chấp nhận báo giá, tiến độ, lịch sử, khóa dòng, hạn nhận | Yêu cầu mở (Q-38), ảnh tham khảo, liên hệ Buyer, báo số yêu cầu mới (Q-41), rút báo giá (Q-31) |
| MAP | 🟡 | Bản đồ tương tác, ghim xưởng đã duyệt và chọn tọa độ trên hồ sơ | Giờ/trạng thái mở cửa (Q-29) và ảnh trong thông tin ghim |
| NEWS | 🟡 | Đăng/sửa/ẩn, danh sách lọc, trang chi tiết và thời gian/địa điểm sự kiện | Ảnh bìa |
| ADMIN | 🟡 | Giao diện cấp/duyệt/khóa hộ, tin tức, thống kê, chọn nội dung nổi bật và hậu kiểm sản phẩm/lô | Báo cáo (Q-40) |
| MEDIA | ⬜ | Ảnh minh họa tĩnh và lazy loading | Upload, resize, lưu trữ và phục vụ ảnh |

## Nợ kỹ thuật đã biết

| # | Vấn đề | Mức |
|---|---|---|
| 1 | Đã sửa: cấu hình chung không chứa mật khẩu, local bật tường minh | Xong M1 backend |
| 2 | Đã sửa: ApiErrorResponse chung cho MVC và Security | Xong M1 backend |
| 3 | Đã thêm Bean Validation tại DTO/service, constraint UTF-8 cho BCrypt | Xong M1 backend |
| 4 | Đã có `/api/products/page`; API cũ giữ mảng. URL chi tiết còn id số | Một phần |
| 5 | Frontend có client API, hook và phiên dùng chung. Còn một số màn hình danh mục trong `main.jsx`. | Một phần |
| 6 | Truy cập dữ liệu lẫn JPA và JDBC | Chấp nhận; mã mới theo `architecture.md` |
| 7 | Chưa có CI, chưa có test tự động trong build | P1 |
| 8 | Chưa cấu hình CORS (chỉ cần nếu frontend và backend khác origin) | Khi triển khai |
| 9 | Compose chưa có `restart`, Nginx chưa đặt cache cho tệp tĩnh | P2 |
| 10 | README đã viết lại theo M1 backend, chạy local IntelliJ/React | Xong |

## Nhật ký thay đổi lớn

| Ngày | Thay đổi |
|---|---|
| 2026-11-10 | Dựng hệ thống tài liệu `docs/`, `AGENTS.md`, `CLAUDE.md`. Chưa đổi mã. |
| 2026-11-10 | Họp nhóm lần 1: chốt 18 câu hỏi mở (Q-01..12, 14, 15, 30..33, trừ Q-29 cần làm rõ). Thay đổi đáng chú ý: khớp yêu cầu mở theo **tag**; Buyer đăng ký có xác thực email + SĐT; **bắt buộc triển khai Internet** với dữ liệu như người dùng thật; hộ chọn công khai/ẩn giá; Buyer chỉ định một hộ. Phát sinh Q-37, Q-38, Q-39. Bảng "sẵn sàng cài đặt" ở `roadmap.md`. |
| 2026-11-10 | Chốt Q-18 (CSS thuần + CSS Modules) và Q-19 (hook tự viết + client API chung) — ADR-0003. Phần giao diện không còn bị chặn. |

| 2026-11-12 | Hoàn thành phần backend foundation của M1: lỗi JSON chung, Bean Validation, phân trang danh mục thật, quyền Buyer và cấu hình local. Không thay migration. MEDIA và phần frontend foundation chưa hoàn thành. |

| 2026-11-14 | Thêm nền frontend, hồ sơ xưởng, sản phẩm/lô, luồng báo giá chỉ định, bản đồ, tin tức, thống kê và nội dung nổi bật. |
