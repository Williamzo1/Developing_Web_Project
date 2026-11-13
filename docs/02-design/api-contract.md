# Hợp đồng API

> Mục 2 mô tả API **đang có** (đối chiếu mã ngày 2026-11-12). Mục 3 là **đề xuất** cho các module chưa làm: chỉ là khung đường dẫn; khi cài đặt một endpoint, bổ sung request/response cụ thể vào đây trong cùng thay đổi và chuyển nó lên mục 2. Mã nguồn controller + DTO luôn là nguồn sự thật cuối cùng.

## 1. Quy ước chung

- Tiền tố `/api`. JSON UTF-8. Tên trường `camelCase`.
- Xác thực: `Authorization: Bearer <token>` (token lấy từ `POST /api/login`).
- Phân vùng theo vai trò: công khai `GET /api/...` · `/api/buyer/**` · `/api/artisan/**` · `/api/admin/**`.
- Tài nguyên công khai định danh bằng `slug` (lô dùng `publicCode`); thao tác quản lý dùng `id`.
- Phân trang đã cài đặt: `/api/products/page?page=0&size=12`; `page` 0..100000, `size` 1..50, mặc định 12. Response `{items,page,size,totalElements,totalPages,first,last}`, sắp `id ASC`. `/api/products` giữ mảng để tương thích; admin list chưa phân trang.
- Thời gian ISO-8601 có múi giờ; ngày `YYYY-MM-DD`; tiền là số nguyên VND.
- Giá bị hộ ẩn (`priceVisible = false`) thì response công khai trả `price: null` — không gửi giá về client rồi ẩn bằng giao diện.
- Ảnh trong response là đối tượng `{ "id", "thumbUrl", "mediumUrl", "largeUrl", "width", "height" }`. *(Đề xuất; hiện `ProductResponse` chỉ có `imageUrl`.)*

### Lỗi

Thân lỗi thống nhất: `{ "timestamp": "ISO-8601", "status": 400, "code": "VALIDATION_FAILED", "message": "Thông báo tiếng Việt", "path": "/api/login", "fieldErrors": {"username": "Vui lòng nhập tên đăng nhập."} }`.

`fieldErrors` là object rỗng khi lỗi không gắn với ô nhập. Không trả rejectedValue, mật khẩu, token hoặc stack trace. Controller advice và Security dùng chung DTO.

| HTTP | code | Trường hợp |
|---|---|---|
| 400 | VALIDATION_FAILED | Vi phạm ràng buộc DTO/service; query page/size ngoài giới hạn hoặc sai kiểu khi bind. fieldErrors có tên trường. |
| 400 | INVALID_INPUT | JSON lỗi, kiểu id sai, thiếu request value. |
| 401 | UNAUTHENTICATED | Thiếu/sai/hết hạn token; sai thông tin đăng nhập hoặc hộ chưa duyệt. |
| 403 | FORBIDDEN | Sai vai trò hoặc không sở hữu xưởng (giữ hành vi Phase02). |
| 404 | PRODUCT_NOT_FOUND | Không có sản phẩm theo id. |
| 404 | NOT_FOUND | Không có đường dẫn/tài nguyên khác. |
| 405 | METHOD_NOT_ALLOWED | Phương thức không được hỗ trợ sau khi qua Security; có Allow header. |
| 409 | DATA_CONFLICT | Trùng/vi phạm ràng buộc dữ liệu. |
| 413 | UPLOAD_TOO_LARGE | Đã có handler; chưa có API upload. |
| 415 | UNSUPPORTED_MEDIA_TYPE | Content-Type không được hỗ trợ. |
| 500 | INTERNAL_ERROR | Lỗi bất ngờ; nội dung chi tiết chỉ trong log server. |
| 503 | DATABASE_UNAVAILABLE | Health DB không kết nối được. |

Frontend rẽ nhánh theo status/code; đưa fieldErrors tới ô nhập, message vào thông báo chung. Không đổi mọi success response thành wrapper mới: login, Actor, product detail và legacy array giữ hợp đồng cũ.

## 2. API đang có

| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/api/health`, `/api/health/db` | Công khai | Kiểm tra dịch vụ và kết nối DB. |
| GET | `/api/products` | Công khai | Danh sách sản phẩm: mảng `ProductResponse { id, slug, name, workshop, category, description, imageUrl }`. Chưa lọc, chưa phân trang. |
| GET | `/api/products/page?page=0&size=12` | Công khai | PageResponse; ví dụ/trường/giới hạn ở mục 1. |
| GET | `/api/buyer/me` | BUYER | Actor của Buyer; Artisan/Admin → 403, Guest → 401. Endpoint kiểm tra vùng quyền; chưa là nghiệp vụ báo giá. |
| GET | `/api/products/{id}` | Công khai | Chi tiết theo `id` số. |
| POST | `/api/login` | Công khai | `{ username, password }` → `{ token, role }`. Sai thông tin → 401. Hộ chưa duyệt → 401. |
| GET | `/api/me` | Đã đăng nhập | `{ id, role, workshop }` (`workshop` là id xưởng, null nếu không phải ARTISAN). |
| POST | `/api/logout` | Đã đăng nhập | 204; token bị thu hồi. |
| GET | `/api/admin/workshops` | ADMIN | Danh sách xưởng kèm trạng thái duyệt. |
| POST | `/api/admin/workshops` | ADMIN | `{ workshopName, username, password }` → 201; tạo xưởng chờ duyệt + tài khoản hộ trong một giao dịch. |
| POST | `/api/admin/workshops/{id}/approve` | ADMIN | Duyệt xưởng. |
| PATCH | `/api/artisan/workshops/{id}` | ARTISAN (chủ xưởng) | `{ name }`; xưởng khác → 403. |

Collection Postman: `postman/`. Kiểm thử HTTP: `backend/tests/phase02.py`, `backend/tests/m1.py`.

## 3. API dự kiến (đề xuất)

### Công khai

| Phương thức | Đường dẫn | FR |
|---|---|---|
| POST | `/api/register` (đăng ký Buyer: username, mật khẩu, tên hiển thị, email, SĐT) · các endpoint xác thực email/SĐT — chờ Q-39 | FR-AUTH-04 |
| GET | `/api/home` (nội dung nổi bật) | FR-HOME-02 |
| GET | `/api/categories` · `/api/tags` | FR-PROD-03, FR-PROD-06 |
| GET | `/api/workshops?q=&page=` · `/api/workshops/{slug}` | FR-WS-01, 06, 07 |
| GET | `/api/workshops/map` (chỉ trường cần cho ghim: slug, tên, tọa độ, giờ mở cửa, `openNow`, ảnh thumb) | FR-MAP-01..03 |
| GET | `/api/products?workshop=&category=&tag=&q=&sort=&page=` · `/api/products/{slug}` (kèm công đoạn và lô công khai) | FR-PROD-03..05 |
| GET | `/api/batches?product=&workshop=&kind=&page=` | FR-BATCH-07 |
| GET | `/api/batches/{publicCode}` (landing page: thông tin lô + mốc thời gian + quy trình + ảnh; lô riêng tư cần `?token=`; sai → 404) | FR-BATCH-02, 03, 05, 06 |
| GET | `/api/batches/{publicCode}/qr.png` (lô công khai) — với lô riêng tư, QR chứa liên kết riêng nên chỉ chủ lô lấy được qua `/api/artisan/batches/{id}/qr.png` | FR-BATCH-04 |
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
| POST | `/quote-requests/{id}/send` (`{ mode, workshopId? }` — chỉ định một hộ hoặc mở) | FR-QUOTE-02 |
| POST | `/quote-requests/{id}/cancel` | FR-QUOTE-08 |
| GET | `/quote-requests/{id}/quotes` | FR-QUOTE-05 |
| POST | `/quote-requests/{id}/quotes/{quoteId}/accept` (`{ quoteUpdatedAt }`) | FR-QUOTE-06 |

### ADMIN — `/api/admin`

| Phương thức | Đường dẫn | FR |
|---|---|---|
| POST | `/workshops/{id}/suspend` | FR-ADMIN-01 |
| GET, POST | `/news` · PUT, DELETE `/news/{id}` | FR-NEWS-01 |
| GET | `/stats` | FR-ADMIN-02 |
| POST | `/products/{id}/hide` · `/batches/{id}/hide` (hậu kiểm) | FR-ADMIN-05 |
| GET, POST, PUT, DELETE | `/tags[/{id}]` | FR-ADMIN-06 |
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
