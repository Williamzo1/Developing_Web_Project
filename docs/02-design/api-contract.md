# Hợp đồng API

> Mục 2 mô tả API **đang có** (đối chiếu mã ngày 2026-11-14). Mục 3 là **đề xuất** cho các module chưa làm: chỉ là khung đường dẫn; khi cài đặt một endpoint, bổ sung request/response cụ thể vào đây trong cùng thay đổi và chuyển nó lên mục 2. Mã nguồn controller + DTO luôn là nguồn sự thật cuối cùng.

## 1. Quy ước chung

- Tiền tố `/api`. JSON UTF-8. Tên trường `camelCase`.
- Xác thực: `Authorization: Bearer <token>` (token lấy từ `POST /api/login`).
- Phân vùng theo vai trò: công khai `GET /api/...` · `/api/buyer/**` · `/api/artisan/**` · `/api/admin/**`.
- Tài nguyên công khai định danh bằng `slug` (lô dùng `publicCode`); thao tác quản lý dùng `id`.
- Phân trang đã cài đặt: `/api/products/page?page=0&size=12`; `page` 0..100000, `size` 1..50, mặc định 12. Response `{items,page,size,totalElements,totalPages,first,last}`, mặc định sắp `id ASC`. `/api/products` giữ mảng để tương thích; admin list chưa phân trang.
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
| GET | `/api/products` | Công khai | Danh sách sản phẩm: mảng `ProductResponse { id, slug, name, workshop, category, description, imageUrl, workshopSlug, price }`. Endpoint mảng giữ tương thích. |
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

### Các endpoint bổ sung

| Phương thức và đường dẫn | Hợp đồng |
|---|---|
| GET `/api/categories` | Mảng `{slug,name}`. |
| GET `/api/products/page` | Thêm `q` (tên chứa chuỗi, bỏ qua hoa/thường), `workshop`, `category` (slug), `sort=id\|name-asc\|name-desc`. |
| GET `/api/products/by-slug/{slug}`, `/api/products/by-slug/{slug}/steps` | Chi tiết hoặc mảng `{name,description}` có thứ tự. Endpoint chi tiết theo ID cũ vẫn dùng được. Giá ẩn trả `price: null`. |
| GET `/api/workshops?q=&page=&size=`, `/api/workshops/{slug}` | Trang `{slug,name}` của hộ đã duyệt hoặc hồ sơ đầy đủ. Hồ sơ có `name,description,familyHistory,address,phone,contactEmail,facebookUrl,latitude,longitude,slug,approved`. |
| GET, PUT `/api/artisan/workshop` | Hồ sơ xưởng của phiên. PUT nhận các trường hồ sơ trừ `slug,approved`, tọa độ đi theo cặp hoặc cả hai để trống. |
| POST `/api/admin/workshops/{id}/suspend` | Đặt `approved=false`, thu hồi phiên của hộ, trả `WorkshopResponse`. |
| GET, POST `/api/artisan/products` · GET, PUT `/api/artisan/products/{id}` | Danh sách phân trang hoặc sản phẩm của xưởng. POST/PUT gửi `{name,category,description,price,priceVisible,hidden}`, `category` là slug. Trả thêm `id,slug,adminHidden,steps`; danh sách không tải công đoạn. |
| PUT `/api/artisan/products/{id}/steps` | `{steps:[{name,description}]}` thay toàn bộ quy trình theo thứ tự gửi. |
| POST `/api/admin/products/{id}/hide` | 204, cờ hậu kiểm riêng với cờ ẩn của hộ. |
| GET `/api/batches?product=&workshop=&kind=&page=&size=` · GET `/api/batches/{publicCode}?token=` | Danh sách công khai lọc theo slug sản phẩm/xưởng và loại lô. Chi tiết trả `{batch,milestones,steps}`; lô riêng tư yêu cầu liên kết hợp lệ. |
| GET, POST `/api/artisan/batches` · GET, PUT `/api/artisan/batches/{id}` | Danh sách phân trang hoặc chi tiết. Ghi nhận `{productId,code,name,description,kind,quantity,unit,price,priceVisible,supplyCapacity,availableFrom,visibility,partnerNote}`. Loại `IN_STOCK\|MADE_TO_ORDER`, hiển thị `PUBLIC\|PRIVATE`. |
| PUT `/api/artisan/batches/{id}/milestones` | Thay danh sách mốc bằng `{items:[{title,note,happenedOn}]}`. |
| POST, DELETE `/api/artisan/batches/{id}/share-token` | POST trả `{token}` mới và thu hồi mã cũ; ghép vào `/lo/{publicCode}?token=...`. DELETE trả 204. |
| GET `/api/admin/batches/{id}` · POST `/api/admin/batches/{id}/hide` | Xem nội dung để hậu kiểm hoặc ẩn (204). |
| GET, POST `/api/buyer/quote-requests` · GET, PUT, DELETE `/api/buyer/quote-requests/{id}` | Danh sách yêu cầu của Buyer hoặc `{request,quotes,events}`. Ghi nháp `{title,category,quantity,unit,spec,desiredBy,note,biddingDeadline,version}`; `category` là slug, PUT cần phiên bản nháp. DELETE nháp trả 204. |
| POST `/api/buyer/quote-requests/{id}/send`, `.../{id}/cancel` | Gửi chỉ định một xưởng bằng `{workshop}` (slug), hoặc hủy. Trả chi tiết mới. |
| GET `/api/buyer/quote-requests/{id}/quotes` · POST `/api/buyer/quote-requests/{id}/quotes/{quoteId}/accept` | Liệt kê báo giá hoặc chấp nhận bằng `{quoteUpdatedAt}`. Đồng thời/dữ liệu cũ trả 409. |
| GET `/api/artisan/quote-requests[/{id}]` · PUT `.../{id}/quote` | Xưởng đọc yêu cầu được chỉ định và báo giá của mình. Gửi/sửa bằng `{unitPrice,totalPrice,leadTimeDays,note}`. |
| POST `/api/artisan/quote-requests/{id}/start-production`, `.../{id}/deliver` | Xưởng được chọn cập nhật tiến độ. |
| GET `/api/news?kind=&page=&size=` · GET `/api/news/{slug}` | Tin công khai, phân trang và lọc, chi tiết theo slug. |
| GET, POST `/api/admin/news` · GET, PUT `/api/admin/news/{id}` | Quản lý tin, ghi `{title,body,kind,eventStart,eventEnd,eventPlace,hidden}`. Loại `MATERIAL_PRICE\|POWER_CUT\|POLICY\|EVENT`. |
| GET `/api/admin/stats` | Tổng số `{workshops,products,batches,quoteRequests}`. |

| GET `/api/map/workshops` | Ghim `{slug,name,description,latitude,longitude}` của xưởng đã duyệt có tọa độ. |
| GET `/api/home` · GET, PUT `/api/admin/featured` | Trang chủ trả `{workshop,product,batch}`, mỗi mục công khai có `{id,code,name,imageUrl}`. Admin gửi/nhận `{workshopId,productId,batchId}` (ID hoặc null). |
| PUT `/api/password` | Tài khoản đang đăng nhập gửi `{currentPassword,newPassword}`; trả 204 và thu hồi các phiên của chính tài khoản đó. |

Sai chủ sở hữu trả 404 ở các tài nguyên mới. PATCH đổi tên xưởng cũ tiếp tục theo hợp đồng Phase 02.

Collection Postman: `postman/`. Kiểm thử HTTP: `backend/tests/phase02.py`, `backend/tests/m1.py`.

## 3. API dự kiến (đề xuất)

| Phương thức | Đường dẫn | FR hoặc phụ thuộc |
|---|---|---|
| POST | `/api/register` và các endpoint xác thực email/SĐT | FR-AUTH-04, Q-39 |
| GET | `/api/tags`, thêm tham số tag vào danh mục | FR-PROD-06, Q-38 |
| GET | `/api/batches/{publicCode}/qr.png` và `/api/artisan/batches/{id}/qr.png` | FR-BATCH-04, Q-20 |
| POST, PUT, DELETE | `/api/artisan/workshop/certificates[/{id}]` | FR-WS-03 và ảnh chứng nhận |
| GET, POST | Mở yêu cầu và khớp yêu cầu theo tag trong API báo giá | FR-QUOTE-02, 03, Q-38 |
| DELETE | `/api/artisan/quote-requests/{id}/quote` | Phần rút báo giá chưa chốt của Q-31 |
| GET, POST, PUT, DELETE | `/api/admin/tags[/{id}]` | FR-ADMIN-06, Q-38 |
| POST | `/api/reports` | FR-ADMIN-04, Q-40 |
| GET, POST | `/api/admin/reports` · `/api/admin/reports/{id}/resolve` | FR-ADMIN-04, Q-40 |

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
