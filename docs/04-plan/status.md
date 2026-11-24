# Hiện trạng cài đặt

> Tài liệu **sống**: cập nhật trong cùng thay đổi mỗi khi một yêu cầu đổi tình trạng. Đây là nơi duy nhất ghi "đã làm đến đâu".
>

Ký hiệu: ✅ xong (API + giao diện) · 🟡 một phần · ⬜ chưa làm.

## Tóm tắt


## Cập nhật 2026-11-24 — THẮNG M1 Backend bước 1–10

Đã ghép hoàn chỉnh repository/service/controller/DTO cho auth đăng ký + OTP, media,
hồ sơ xưởng + lịch + chứng nhận, CRUD sản phẩm + công đoạn + tag, lô + mốc + QR + liên kết riêng,
news, admin và home. V5/V6 đã có trong ZIP đầu vào, giữ nguyên nội dung migration.
Mã nguồn được kiểm tra biên dịch và chạy HTTP với PostgreSQL. Xem kết quả cụ thể tại
`../03-engineering/m1-steps-1-10.md`. Chưa tuyên bố hoàn thành giao diện hoặc xác nhận SMTP thật.
Bước 11 trên máy nhóm vẫn cần cấu hình DB/SMTP thực tế và chạy build/test theo hướng dẫn.

## Theo module

| Module | Tình trạng | Đã có | Còn thiếu |
|---|---|---|---|
| HOME | 🟡 | API nội dung nổi bật động, trang chủ cũ | Giao diện tích hợp API |
| AUTH | 🟡 | Login/logout/me, đăng ký Buyer, OTP email, kiểm định dạng SĐT, khóa phiên hộ | Giao diện, SMTP thực tế, nghiệp vụ sau M1 |
| WS | 🟡 | API hồ sơ, ảnh, lịch, ngày nghỉ, chứng nhận và hồ sơ công khai | Giao diện M2 |
| PROD | 🟡 | API CRUD, slug/id, công đoạn, tag, lọc hộ/phân loại/tag và phân trang | Giao diện CRUD, tìm kiếm/sort tùy chọn |
| BATCH | 🟡 | API lô, mốc, QR, quyền chủ sở hữu, liên kết riêng/thu hồi | Landing page và giao diện M2 |
| MAP | 🟡 | API ghim, tọa độ và openNow | Bản đồ frontend |
| NEWS | 🟡 | API tin công khai và CRUD admin, ảnh bìa | Giao diện M3 |
| ADMIN | 🟡 | Cấp/duyệt/khóa hộ, thống kê thật, tag, hậu kiểm, nổi bật | Giao diện, báo cáo vi phạm |
| MEDIA | 🟡 | Upload nhiều ảnh, kiểm MIME/dung lượng, resize và phục vụ ảnh, kiểm chủ ảnh | UI upload, WebP cần codec JVM |

## Nợ kỹ thuật đã biết

| # | Vấn đề | Mức |
|---|---|---|
| 1 | Đã sửa: cấu hình chung không chứa mật khẩu, local bật tường minh | Xong M1 backend |
| 2 | Đã sửa: ApiErrorResponse chung cho MVC và Security | Xong M1 backend |
| 3 | Đã thêm Bean Validation tại DTO/service, constraint UTF-8 cho BCrypt | Xong M1 backend |
| 4 | Đã có `/api/products/page`; API cũ giữ mảng. URL chi tiết còn id số | Một phần |
| 5 | Frontend dồn trong một file `main.jsx`; chưa có `@vitejs/plugin-react`, chưa có client API/phiên đăng nhập | Trước khi 3 người cùng làm frontend |
| 6 | Truy cập dữ liệu lẫn JPA và JDBC | Chấp nhận; mã mới theo `architecture.md` |
| 7 | Chưa có CI, chưa có test tự động trong build | P1 |
| 8 | Đã cấu hình CORS trong SecurityFilterChain | M1 bước 9 |
| 9 | Compose chưa có `restart`, Nginx chưa đặt cache cho tệp tĩnh | P2 |
| 10 | README đã viết lại theo M1 backend, chạy local IntelliJ/React | Xong |

## Nhật ký thay đổi lớn

| Ngày | Thay đổi |
|---|---|
| 2026-11-10 | Dựng hệ thống tài liệu `docs/`, `AGENTS.md`, `CLAUDE.md`. Chưa đổi mã. |
| 2026-11-10 | Họp nhóm lần 1: chốt 18 câu hỏi mở (Q-01..12, 14, 15, 30..33, trừ Q-29 cần làm rõ). Thay đổi đáng chú ý: khớp yêu cầu mở theo **tag**; Buyer đăng ký có xác thực email + SĐT; **bắt buộc triển khai Internet** với dữ liệu như người dùng thật; hộ chọn công khai/ẩn giá; Buyer chỉ định một hộ. Phát sinh Q-37, Q-38, Q-39. Bảng "sẵn sàng cài đặt" ở `roadmap.md`. |
| 2026-11-10 | Chốt Q-18 (CSS thuần + CSS Modules) và Q-19 (hook tự viết + client API chung) — ADR-0003. Phần giao diện không còn bị chặn. |

| 2026-11-12 | Hoàn thành phần backend foundation của M1: lỗi JSON chung, Bean Validation, phân trang danh mục thật, quyền Buyer và cấu hình local. Không thay migration. MEDIA và phần frontend foundation chưa hoàn thành. |

| 2026-11-24 | Hoàn thiện THẮNG M1 Backend đến bước 10, bổ sung kiểm thử HTTP và tài liệu hợp đồng API. |
