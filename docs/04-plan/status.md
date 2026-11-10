# Hiện trạng cài đặt

> Tài liệu **sống**: cập nhật trong cùng thay đổi mỗi khi một yêu cầu đổi tình trạng. Đây là nơi duy nhất ghi "đã làm đến đâu".
>
> Cập nhật lần cuối: **2026-11-10** — đối chiếu mã ở commit `b9f5a04` (README gọi là "Phase 02 MVC").

Ký hiệu: ✅ xong (API + giao diện) · 🟡 một phần · ⬜ chưa làm.

## Tóm tắt

Đã có nền backend (xác thực token, phân quyền 3 vai trò, cấp/duyệt hộ, sửa tên xưởng, đọc danh mục) và giao diện công khai tối thiểu (trang chủ, danh mục, chi tiết). **Bốn yêu cầu kỹ thuật của đề gốc (§4.1–4.4) đều chưa bắt đầu.**

## Theo module

| Module | Tình trạng | Đã có | Còn thiếu |
|---|---|---|---|
| HOME | 🟡 | Trang chủ chủ đề gốm Bát Tràng (tĩnh) | Nội dung nổi bật động, điều hướng đầy đủ |
| AUTH | 🟡 | API đăng nhập/đăng xuất/`me`; phân quyền theo vai trò; Admin cấp + duyệt hộ (FR-AUTH-01..03 phía API) | Toàn bộ giao diện đăng nhập, route guard; đăng ký Buyer (FR-AUTH-04) |
| WS | 🟡 | Bảng `workshops` (tên, slug, approved); API sửa tên xưởng | Hồ sơ đầy đủ, ảnh, chứng nhận, tọa độ, giờ mở cửa, trang công khai |
| PROD | 🟡 | `GET /api/products`, `GET /api/products/{id}`; trang danh mục + chi tiết | Lọc theo hộ/phân loại, tìm kiếm, phân trang, slug trên URL, CRUD của hộ, công đoạn sản xuất |
| BATCH | ⬜ | — | Tất cả (lô, mốc sản xuất, landing page, QR, riêng tư) |
| QUOTE | ⬜ | — | Tất cả (state machine, khóa bản ghi) |
| MAP | ⬜ | — | Tất cả |
| NEWS | ⬜ | — | Tất cả |
| ADMIN | 🟡 | API danh sách xưởng, cấp tài khoản, duyệt | Giao diện quản trị, khóa hộ, thống kê, nổi bật, báo cáo |
| MEDIA | ⬜ | Chỉ có handler lỗi 413; ảnh hiện là SVG tĩnh trong `frontend/public/images/` | Upload, resize, lưu trữ, phục vụ ảnh |

## Nợ kỹ thuật đã biết

| # | Vấn đề | Mức |
|---|---|---|
| 1 | `application.properties` chứa mật khẩu DB cục bộ dạng chữ và đặt cứng `spring.profiles.active=local` | Nên sửa sớm |
| 2 | Mã lỗi chưa thống nhất (`HTTP_<status>` từ service; 401/403 không có `message`) | Trước khi làm frontend có form |
| 3 | Chưa có `spring-boot-starter-validation`, validate viết tay trong service | Khi thêm endpoint ghi |
| 4 | `GET /api/products` chưa phân trang, định danh bằng id số | Cùng lúc làm FR-PROD-03..05 |
| 5 | Frontend dồn trong một file `main.jsx`; chưa có `@vitejs/plugin-react`, chưa có client API/phiên đăng nhập | Trước khi 3 người cùng làm frontend |
| 6 | Truy cập dữ liệu lẫn JPA và JDBC | Chấp nhận; mã mới theo `architecture.md` |
| 7 | Chưa có CI, chưa có test tự động trong build | P1 |
| 8 | Chưa cấu hình CORS (chỉ cần nếu frontend và backend khác origin) | Khi triển khai |
| 9 | Compose chưa có `restart`, Nginx chưa đặt cache cho tệp tĩnh | P2 |
| 10 | README gốc mô tả "Phase 02" và ghi tên thư mục cũ `vCraft-phase02-mvc/` | Cập nhật khi cấu trúc ổn định |

## Nhật ký thay đổi lớn

| Ngày | Thay đổi |
|---|---|
| 2026-11-10 | Dựng hệ thống tài liệu `docs/`, `AGENTS.md`, `CLAUDE.md`. Chưa đổi mã. |
| 2026-11-10 | Họp nhóm lần 1: chốt 18 câu hỏi mở (Q-01..12, 14, 15, 30..33, trừ Q-29 cần làm rõ). Thay đổi đáng chú ý: khớp yêu cầu mở theo **tag**; Buyer đăng ký có xác thực email + SĐT; **bắt buộc triển khai Internet** với dữ liệu như người dùng thật; hộ chọn công khai/ẩn giá; Buyer chỉ định một hộ. Phát sinh Q-37, Q-38, Q-39. Bảng "sẵn sàng cài đặt" ở `roadmap.md`. |
| 2026-11-10 | Chốt Q-18 (CSS thuần + CSS Modules) và Q-19 (hook tự viết + client API chung) — ADR-0003. Phần giao diện không còn bị chặn. |
