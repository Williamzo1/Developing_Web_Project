# Hiện trạng cài đặt

> Tài liệu **sống**: cập nhật trong cùng thay đổi mỗi khi một yêu cầu đổi tình trạng. Đây là nơi duy nhất ghi "đã làm đến đâu".
>

Ký hiệu: ✅ xong (API + giao diện) · 🟡 một phần · ⬜ chưa làm.

## Tóm tắt

Đã có nền backend (xác thực token, phân quyền 3 vai trò, cấp/duyệt hộ, sửa tên xưởng, đọc danh mục), API máy trạng thái báo giá có khóa dòng PostgreSQL (§4.2), và giao diện công khai tối thiểu (trang chủ, danh mục, chi tiết). Schema V5 (hồ sơ hộ, media, chứng nhận, giờ mở cửa, công đoạn, lô, mốc) và V6 (tin tức) đã có migration, nhưng **chưa có service/controller/DTO** dùng chúng. Ảnh/lô/QR/bản đồ và toàn bộ giao diện báo giá chưa làm. Nền frontend chưa xong (prototype của Huy ở nhánh `developing/prototype_1`, chưa gộp).


## Theo module

| Module | Tình trạng | Đã có | Còn thiếu |
|---|---|---|---|
| HOME | 🟡 | Trang chủ chủ đề gốm Bát Tràng (tĩnh) | Nội dung nổi bật động, điều hướng đầy đủ |
| AUTH | 🟡 | API đăng nhập/đăng xuất/`me`; phân quyền theo vai trò; Admin cấp + duyệt hộ (FR-AUTH-01..03 phía API) | Toàn bộ giao diện đăng nhập, route guard; đăng ký Buyer (FR-AUTH-04) |
| WS | 🟡 | Bảng `workshops` (tên, slug, approved); API sửa tên xưởng; schema + entity `Workshop` đã mở rộng (V5: hồ sơ, tọa độ, chứng nhận, giờ mở cửa) | API hồ sơ đầy đủ, ảnh, trang công khai |
| PROD | 🟡 | `GET /api/products`, `GET /api/products/{id}`, `/api/products/page`; trang danh mục phân trang + chi tiết | Lọc theo hộ/phân loại, tìm kiếm, slug trên URL, CRUD của hộ, công đoạn sản xuất |
| BATCH | ⬜ | Chỉ có schema (`batches`, `batch_milestones` trong V5) | API, lô, mốc sản xuất, landing page, QR, riêng tư, giao diện |
| QUOTE | 🟡 | Backend nháp/gửi/hủy; báo giá/sửa/rút/chọn; row lock + version; tiến độ; lịch sử; deadline; liên hệ; matching tag tạm (V4) | Giao diện Buyer + Artisan, ảnh tham khảo (MEDIA), badge FR-QUOTE-10; quản trị tag/Q-38 còn mở |
| MAP | ⬜ | Chỉ có cột tọa độ trong schema V5 | API ghim bản đồ, giao diện Leaflet, trạng thái mở cửa (Q-29) |
| NEWS | ⬜ | Chỉ có schema `news_posts` (V6) | API, giao diện công khai và quản trị |
| ADMIN | 🟡 | API danh sách xưởng, cấp tài khoản, duyệt | Giao diện quản trị, khóa hộ, thống kê, nổi bật, báo cáo |
| MEDIA | ⬜ | Handler lỗi 413; schema `media`, `media_links` (V5); ảnh hiện là SVG tĩnh trong `frontend/public/images/` | Upload, resize, lưu trữ, phục vụ ảnh (chờ Q-13, Q-21) |

## Nợ kỹ thuật đã biết

| # | Vấn đề | Mức |
|---|---|---|
| 1 | Đã sửa: cấu hình chung không chứa mật khẩu, local bật tường minh | Xong M1 backend |
| 2 | Đã sửa: ApiErrorResponse chung cho MVC và Security | Xong M1 backend |
| 3 | Đã thêm Bean Validation tại DTO/service, constraint UTF-8 cho BCrypt | Xong M1 backend |
| 4 | Đã có `/api/products/page`; API cũ giữ mảng. URL chi tiết còn id số | Một phần |
| 5 | Frontend dồn trong một file `main.jsx`; chưa có `@vitejs/plugin-react`, chưa có client API/phiên đăng nhập. Prototype tách cấu trúc (auth, layout, `DataState`, `Pagination`) có ở `origin/developing/prototype_1`, chưa gộp | Trước khi 3 người cùng làm frontend |
| 6 | Truy cập dữ liệu lẫn JPA và JDBC | Chấp nhận; mã mới theo `architecture.md` |
| 7 | Đã có CI build backend + frontend (`.github/workflows/ci.yml`, chưa chạy trên GitHub). Chưa có test tự động trong build | P1 |
| 8 | Chưa cấu hình CORS (chỉ cần nếu frontend và backend khác origin) | Khi triển khai |
| 9 | Đã sửa: Compose có `restart`, healthcheck backend, volume ảnh; Nginx có cache, gzip, giới hạn body 60 MB, route `/media/` | Xong |
| 10 | README đã viết lại theo M1 backend, chạy local IntelliJ/React | Xong |

## Nhật ký thay đổi lớn

| Ngày | Thay đổi |
|---|---|
| 2026-11-10 | Dựng hệ thống tài liệu `docs/`, `AGENTS.md`, `CLAUDE.md`. Chưa đổi mã. |
| 2026-11-10 | Họp nhóm lần 1: chốt 18 câu hỏi mở (Q-01..12, 14, 15, 30..33, trừ Q-29 cần làm rõ). Thay đổi đáng chú ý: khớp yêu cầu mở theo **tag**; Buyer đăng ký có xác thực email + SĐT; **bắt buộc triển khai Internet** với dữ liệu như người dùng thật; hộ chọn công khai/ẩn giá; Buyer chỉ định một hộ. Phát sinh Q-37, Q-38, Q-39. Bảng "sẵn sàng cài đặt" ở `roadmap.md`. |
| 2026-11-10 | Chốt Q-18 (CSS thuần + CSS Modules) và Q-19 (hook tự viết + client API chung) — ADR-0003. Phần giao diện không còn bị chặn. |

| 2026-11-12 | Hoàn thành phần backend foundation của M1: lỗi JSON chung, Bean Validation, phân trang danh mục thật, quyền Buyer và cấu hình local. Không thay migration. MEDIA và phần frontend foundation chưa hoàn thành. |
| 2026-11-17 | Backend báo giá (migration V4): nháp/gửi/hủy yêu cầu, gửi/sửa/rút/chọn báo giá, khóa dòng + version, tiến độ In Production → Delivered, lịch sử trạng thái. Xem `../03-engineering/quote-backend.md`. Chưa có giao diện. |
| 2026-11-23 | Migration V5 (mở rộng schema lõi: hồ sơ hộ, media, chứng nhận, giờ mở cửa, công đoạn, lô, mốc) và V6 (tin tức); cập nhật entity `Workshop`, `Product`. Chưa có API cho các bảng mới. |
| 2026-11-24 | Dương: thêm CI, nâng cấp Compose/Nginx/Dockerfile backend (restart, healthcheck, volume ảnh, giới hạn upload, cache), thêm mục chạy Compose vào `setup.md`. Đã dựng và chạy thử Compose trên Windows. |
| 2026-11-24 | Đồng bộ lại `status.md` (khôi phục dòng QUOTE bị mất khi merge, ghi V5/V6) và thêm bảng phân công đề xuất vào `roadmap.md`. |
