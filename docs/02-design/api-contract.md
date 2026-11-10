# Hợp đồng API

> Mục 2 mô tả API **đang có** (đối chiếu mã ngày 2026-11-10). Mục 3 là **đề xuất** cho các module chưa làm: chỉ là khung đường dẫn; khi cài đặt một endpoint, bổ sung request/response cụ thể vào đây trong cùng thay đổi và chuyển nó lên mục 2. Mã nguồn controller + DTO luôn là nguồn sự thật cuối cùng.

## 1. Quy ước chung

- Tiền tố `/api`. JSON UTF-8. Tên trường `camelCase`.
- Xác thực: `Authorization: Bearer <token>` (token lấy từ `POST /api/login`).
- Phân vùng theo vai trò: công khai `GET /api/...` · `/api/buyer/**` · `/api/artisan/**` · `/api/admin/**`.
- Tài nguyên công khai định danh bằng `slug` (lô dùng `publicCode`); thao tác quản lý dùng `id`.
- Danh sách có phân trang: tham số `page` (từ 0), `size` (mặc định 20, tối đa 100) → `{ "items": [...], "page": 0, "size": 20, "total": 137 }`. *(Đề xuất; `GET /api/products` hiện trả mảng thuần.)*
- Thời gian ISO-8601 có múi giờ; ngày `YYYY-MM-DD`; tiền là số nguyên VND.
- Ảnh trong response là đối tượng `{ "id", "thumbUrl", "mediumUrl", "largeUrl", "width", "height" }`. *(Đề xuất; hiện `ProductResponse` chỉ có `imageUrl`.)*

### Lỗi

Thân lỗi: `{ "code": "MÃ_LỖI", "message": "Thông báo tiếng Việt cho người dùng" }`.

| HTTP | `code` | Khi nào | Hiện trạng |
|---|---|---|---|
| 400 | `INVALID_INPUT` | Sai định dạng/validate | Có (dữ liệu không đọc được); lỗi validate ở service hiện trả `HTTP_400` |
| 401 | `UNAUTHENTICATED` | Thiếu/sai/hết hạn token | Có (chỉ `code`, chưa có `message`) |
| 403 | `FORBIDDEN` | Sai vai trò hoặc sai chủ sở hữu | Có (chỉ `code`); từ service hiện trả `HTTP_403` |
| 404 | `NOT_FOUND` | Không tồn tại **hoặc** không có quyền biết là tồn tại | Hiện trả `HTTP_404` |
| 409 | `DATA_CONFLICT` | Trùng/vi phạm ràng buộc | Có |
| 409 | `INVALID_STATE_TRANSITION`, `STALE_DATA` | Luồng báo giá | Đề xuất |
| 413 | `UPLOAD_TOO_LARGE` | Ảnh quá lớn | Có handler |

Việc cần làm: thay `HTTP_<status>` bằng mã có nghĩa và thêm `message` cho 401/403. Frontend chỉ rẽ nhánh theo `code`, hiển thị `message`.

## 2. API đang có

| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/health`, `/api/health/db` | Công khai | Kiểm tra dịch vụ và kết nối DB. |
| GET | `/api/products` | Công khai | Danh sách sản phẩm: mảng `ProductResponse { id, slug, name, workshop, category, description, imageUrl }`. Chưa lọc, chưa phân trang. |
| GET | `/api/products/{id}` | Công khai | Chi tiết theo `id` số. |
| POST | `/api/login` | Công khai | `{ username, password }` → `{ token, role }`. Sai thông tin → 401. Hộ chưa duyệt → 401. |
| GET | `/api/me` | Đã đăng nhập | `{ id, role, workshop }` (`workshop` là id xưởng, null nếu không phải ARTISAN). |
| POST | `/api/logout` | Đã đăng nhập | 204; token bị thu hồi. |
| GET | `/api/admin/workshops` | ADMIN | Danh sách xưởng kèm trạng thái duyệt. |
| POST | `/api/admin/workshops` | ADMIN | `{ workshopName, username, password }` → 201; tạo xưởng chờ duyệt + tài khoản hộ trong một giao dịch. |
| POST | `/api/admin/workshops/{id}/approve` | ADMIN | Duyệt xưởng. |
| PATCH | `/api/artisan/workshops/{id}` | ARTISAN (chủ xưởng) | `{ name }`; xưởng khác → 403. |

Collection Postman: `postman/`. Kiểm thử HTTP: `backend/tests/phase02.py`.

## 3. API dự kiến (đề xuất)

### Công khai

| Phương thức | Đường dẫn | FR |
|---|---|---|
| POST | `/api/register` (đăng ký Buyer) | FR-AUTH-04 |
| GET | `/api/home` (nội dung nổi bật) | FR-HOME-02 |
| GET | `/api/categories` | FR-PROD-03 |
| GET | `/api/workshops?q=&page=` · `/api/workshops/{slug}` | FR-WS-01, 06, 07 |
| GET | `/api/workshops/map` (chỉ trường cần cho ghim: slug, tên, tọa độ, giờ mở cửa, `openNow`, ảnh thumb) | FR-MAP-01..03 |
| GET | `/api/products?workshop=&category=&q=&sort=&page=` · `/api/products/{slug}` (kèm công đoạn và lô công khai) | FR-PROD-03..05 |
| GET | `/api/batches?product=&workshop=&kind=&page=` | FR-BATCH-07 |
| GET | `/api/batches/{publicCode}` (landing page; lô riêng tư cần `?token=`; sai → 404) | FR-BATCH-02, 05, 06 |
| GET | `/api/batches/{publicCode}/qr.png` | FR-BATCH-04 |
| GET | `/api/news?kind=&page=` · `/api/news/{slug}` | FR-NEWS-02 |
| POST | `/api/reports` | FR-ADMIN-04 |

Đổi `GET /api/products/{id}` sang slug là thay đổi phá vỡ: sửa frontend và Postman cùng lúc.

### ARTISAN — `/api/artisan`

Mọi thao tác ngầm định trên xưởng của người đăng nhập; không nhận `workshopId` từ client.

| Phương thức | Đường dẫn | FR |
|---|---|---|
| GET, PUT | `/workshop` (hồ sơ của tôi) | FR-WS-02, 05 |
| POST, PUT, DELETE | `/workshop/certificates[/{id}]` | FR-WS-03 |
| GET, POST | `/products` · GET, PUT, DELETE `/products/{id}` | FR-PROD-01 |
| PUT | `/products/{id}/steps` (ghi cả danh sách công đoạn theo thứ tự) | FR-PROD-02 |
| GET, POST | `/batches` · GET, PUT `/batches/{id}` | FR-BATCH-01, 05 |
| PUT | `/batches/{id}/milestones` | FR-BATCH-03 |
| POST, DELETE | `/batches/{id}/share-token` | FR-BATCH-06 |
| GET | `/quote-requests?tab=open|targeted|mine` · `/quote-requests/{id}` | FR-QUOTE-03, 04, 10 |
| PUT, DELETE | `/quote-requests/{id}/quote` (gửi/sửa, rút báo giá của tôi) | FR-QUOTE-04 |
| POST | `/quote-requests/{id}/start-production` · `/quote-requests/{id}/deliver` | FR-QUOTE-07 |

### BUYER — `/api/buyer`

| Phương thức | Đường dẫn | FR |
|---|---|---|
| GET, POST | `/quote-requests` · GET, PUT, DELETE `/quote-requests/{id}` (sửa/xóa chỉ khi `DRAFT`) | FR-QUOTE-01 |
| POST | `/quote-requests/{id}/send` (`{ mode, workshopIds? }`) | FR-QUOTE-02 |
| POST | `/quote-requests/{id}/cancel` | FR-QUOTE-08 |
| GET | `/quote-requests/{id}/quotes` | FR-QUOTE-05 |
| POST | `/quote-requests/{id}/quotes/{quoteId}/accept` (`{ quoteUpdatedAt }`) | FR-QUOTE-06 |

### ADMIN — `/api/admin`

| Phương thức | Đường dẫn | FR |
|---|---|---|
| POST | `/workshops/{id}/suspend` | FR-ADMIN-01 |
| GET, POST | `/news` · PUT, DELETE `/news/{id}` | FR-NEWS-01 |
| GET | `/stats` | FR-ADMIN-02 |
| PUT | `/featured` | FR-ADMIN-03 |
| GET | `/reports` · POST `/reports/{id}/resolve` | FR-ADMIN-04 |

### Ảnh (ARTISAN, ADMIN, BUYER cho ảnh tham khảo)

| Phương thức | Đường dẫn | FR |
|---|---|---|
| POST | `/api/media` (multipart, nhiều tệp trường `files`) → mảng đối tượng ảnh | FR-MEDIA-01, 02 |
| DELETE | `/api/media/{id}` (chỉ người tải lên) | FR-MEDIA-04 |
| GET | `/media/**` (tệp tĩnh, không qua `/api`) | FR-MEDIA-03 |

Gắn ảnh vào thực thể bằng cách gửi `mediaIds: [..]` theo thứ tự trong request tạo/sửa của thực thể đó.

## 4. Quy trình thay đổi API

1. Sửa tài liệu này trước (hoặc cùng lúc) với mã; người làm frontend và backend thống nhất trên tài liệu.
2. Cập nhật collection Postman và test HTTP.
3. Thay đổi phá vỡ phải ghi rõ trong mô tả PR.
