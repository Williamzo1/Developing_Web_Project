# Hiện trạng cài đặt

> Tài liệu **sống**: cập nhật trong cùng thay đổi mỗi khi một yêu cầu đổi tình trạng. Đây là nơi duy nhất ghi "đã làm đến đâu".
>
> Cập nhật lần cuối: **2026-11-28** — gộp backend M1 vào `master`, chốt câu hỏi mở.

Ký hiệu: ✅ xong (API + giao diện) · 🟡 một phần · ⬜ chưa làm.

## Tóm tắt

**Backend M1 gần đủ** (nhánh của Thắng, 2026-11-24, gộp cùng phần của Dương): xác thực token + phân quyền 3 vai trò, đăng ký Buyer + OTP email, hồ sơ hộ + giờ mở cửa + chứng nhận, CRUD sản phẩm + công đoạn + tag, lô + mốc + QR + liên kết riêng, tin tức, quản trị, trang chủ, máy trạng thái báo giá có khóa dòng (§4.2), upload + resize ảnh (§4.4). Chi tiết: `../03-engineering/m1-steps-1-10.md`. **Frontend gần như chưa có**: vẫn dồn trong `main.jsx`, prototype của Huy (`developing/prototype_1`) chưa gộp. Chưa thử SMTP thật, chưa triển khai Internet.

Ngày 2026-11-28 nhóm chốt toàn bộ câu hỏi mở còn lại (Q-13, 16, 17, 20–29, 34–39) theo gợi ý; không còn câu nào ở trạng thái "Mở".

## Theo module

| Module | Tình trạng | Đã có | Còn thiếu |
|---|---|---|---|
| HOME | 🟡 | API nội dung nổi bật động, trang chủ cũ | Giao diện tích hợp API |
| AUTH | 🟡 | Login/logout/me, đăng ký Buyer, OTP email, kiểm định dạng SĐT, khóa phiên hộ | Giao diện, SMTP thực tế, nghiệp vụ sau M1 |
| WS | 🟡 | API hồ sơ, ảnh, lịch, ngày nghỉ, chứng nhận và hồ sơ công khai | Giao diện M2 |
| PROD | 🟡 | API CRUD, slug/id, công đoạn, tag, lọc hộ/phân loại/tag và phân trang | Giao diện CRUD, tìm kiếm/sort tùy chọn |
| BATCH | 🟡 | API lô, mốc, QR, quyền chủ sở hữu, liên kết riêng/thu hồi | Landing page và giao diện M2 |
| QUOTE | 🟡 | Backend nháp/gửi/hủy; báo giá/sửa/rút/chọn; row lock + version; tiến độ; lịch sử; deadline; liên hệ; khớp yêu cầu mở theo tag (Q-38) | Giao diện Buyer + Artisan, ảnh tham khảo, badge FR-QUOTE-10 |
| MAP | 🟡 | API ghim, tọa độ và openNow (Q-29) | Bản đồ Leaflet ở frontend; nhãn "đang nhận đơn" (Q-29, cần migration mới) |
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
| 5 | Frontend dồn trong một file `main.jsx`; chưa có `@vitejs/plugin-react`, chưa có client API/phiên đăng nhập. Prototype tách cấu trúc có ở `origin/developing/prototype_1`, chưa gộp | Trước khi 3 người cùng làm frontend |
| 6 | Truy cập dữ liệu lẫn JPA và JDBC | Chấp nhận; mã mới theo `architecture.md` |
| 7 | Đã có CI build backend + frontend (`.github/workflows/ci.yml`). Chưa có test tự động trong build | P1 |
| 8 | Đã cấu hình CORS trong SecurityFilterChain | M1 bước 9 |
| 9 | Đã sửa: Compose có `restart`, healthcheck backend, volume ảnh; Nginx có cache, gzip, giới hạn body 60 MB, route `/media/` | Xong |
| 10 | README đã viết lại theo M1 backend, chạy local IntelliJ/React | Xong |

## Nhật ký thay đổi lớn

| Ngày | Thay đổi |
|---|---|
| 2026-11-10 | Dựng hệ thống tài liệu `docs/`, `AGENTS.md`, `CLAUDE.md`. Chưa đổi mã. |
| 2026-11-10 | Họp nhóm lần 1: chốt 18 câu hỏi mở (Q-01..12, 14, 15, 30..33, trừ Q-29 cần làm rõ). Thay đổi đáng chú ý: khớp yêu cầu mở theo **tag**; Buyer đăng ký có xác thực email + SĐT; **bắt buộc triển khai Internet** với dữ liệu như người dùng thật; hộ chọn công khai/ẩn giá; Buyer chỉ định một hộ. Phát sinh Q-37, Q-38, Q-39. Bảng "sẵn sàng cài đặt" ở `roadmap.md`. |
| 2026-11-10 | Chốt Q-18 (CSS thuần + CSS Modules) và Q-19 (hook tự viết + client API chung) — ADR-0003. Phần giao diện không còn bị chặn. |
| 2026-11-12 | Hoàn thành phần backend foundation của M1: lỗi JSON chung, Bean Validation, phân trang danh mục thật, quyền Buyer và cấu hình local. Không thay migration. MEDIA và phần frontend foundation chưa hoàn thành. |
| 2026-11-13 | Thêm V4 và backend báo giá; Postman, test HTTP và PostgreSQL đồng thời; giữ M1. Xem quote-verification.md về kết quả thật. |
| 2026-11-17 | Backend báo giá (migration V4): nháp/gửi/hủy yêu cầu, gửi/sửa/rút/chọn báo giá, khóa dòng + version, tiến độ In Production → Delivered, lịch sử trạng thái. Xem `../03-engineering/quote-backend.md`. Chưa có giao diện. |
| 2026-11-20 | Khôi phục tài liệu báo giá, Postman và phần font bị mất ở commit merge `ed29e0b` (lấy lại từ `b39455b` và `3f1230d`). Không đổi mã backend. |
| 2026-11-23 | Migration V5 (hồ sơ hộ, media, chứng nhận, giờ mở cửa, công đoạn, lô, mốc) và V6 (tin tức); cập nhật entity `Workshop`, `Product`. |
| 2026-11-24 | Dương: CI, nâng cấp Compose/Nginx/Dockerfile backend; MEDIA backend (upload nhiều ảnh, resize 3 cỡ, EXIF, lưu đĩa); `scripts/backup.ps1` + `restore.ps1`. |
| 2026-11-24 | Hoàn thiện THẮNG M1 Backend đến bước 10, bổ sung kiểm thử HTTP và tài liệu hợp đồng API. |
| 2026-11-28 | Gộp nhánh M1 của Thắng với `backend_v1`; sửa lại `status.md` sau gộp. Nhóm chốt toàn bộ câu hỏi mở còn lại theo gợi ý; cập nhật các chỗ "Giả định Q-xx" trong `01-requirements/`, `02-design/`, ADR-0002, ADR-0005. |
