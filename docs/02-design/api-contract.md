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

## 2.1. Backend báo giá — đã cài đặt 2026-11-13

Các endpoint sau dùng token Phase02/M1 và lỗi JSON chung. `/api/quote-tags`, `/api/quote-categories` công khai trả mảng `{id,slug,name}` để form không hardcode ID. Các list request dùng PageResponse của M1, page bắt đầu 0, size mặc định 12/tối đa 50, sort id giảm dần.

| Method | URL | Quyền | Body / kết quả |
|---|---|---|---|
| POST | `/api/buyer/quote-requests` | Buyer | QuoteRequestInput → 201 QuoteRequestResponse (DRAFT) |
| GET | `/api/buyer/quote-requests?page=0&size=12` | Buyer | Chỉ của mình → PageResponse<QuoteRequestSummary> |
| GET | `/api/buyer/quote-requests/{id}` | Buyer chủ yêu cầu | QuoteRequestResponse với tất cả quote |
| PUT | `/api/buyer/quote-requests/{id}` | Buyer chủ yêu cầu | Chỉ DRAFT; QuoteRequestInput + version hiện tại |
| DELETE | `/api/buyer/quote-requests/{id}` | Buyer chủ yêu cầu | Chỉ DRAFT; JSON `{version}` → 204 |
| POST | `/api/buyer/quote-requests/{id}/send` | Buyer chủ yêu cầu | `{version}` của request → SENT |
| POST | `/api/buyer/quote-requests/{id}/cancel` | Buyer chủ yêu cầu | `{version}` của request; SENT/BIDDING → CANCELLED |
| GET | `/api/buyer/quote-requests/{id}/quotes` | Buyer chủ yêu cầu | Mảng QuoteResponse, dùng so sánh |
| POST | `/api/buyer/quote-requests/{id}/quotes/{quoteId}/accept` | Buyer chủ yêu cầu | `{version}` **của quote đang xem** → ACCEPTED |
| GET | `/api/buyer/quote-requests/{id}/events` | Buyer chủ yêu cầu | Mảng QuoteEventResponse với actor ID |
| GET | `/api/artisan/quote-requests?page=0&size=12` | Artisan được duyệt | Inbox riêng: targeted, open khớp tag đang SENT/BIDDING, hoặc mình từng báo giá |
| GET | `/api/artisan/quote-requests/{id}` | Artisan được xem | QuoteRequestResponse, chỉ quote của mình; buyerContact null trước khi mình báo giá |
| GET | `/api/artisan/quote-requests/{id}/quotes` | Artisan được xem | Mảng quote của chính mình (0 hoặc 1 dòng), không có đối thủ |
| POST | `/api/artisan/quote-requests/{id}/quotes` | Artisan đủ điều kiện | QuoteInput → 201; gửi lại WITHDRAWN cần version hiện tại |
| GET | `/api/artisan/quotes/{id}` | Artisan chủ báo giá | QuoteResponse; quote hộ khác → 404 |
| PUT | `/api/artisan/quotes/{id}` | Artisan chủ báo giá | QuoteInput + version quote; chỉ SUBMITTED khi request BIDDING |
| POST | `/api/artisan/quotes/{id}/withdraw` | Artisan chủ báo giá | `{version}` của quote → WITHDRAWN; request về SENT nếu không còn quote SUBMITTED |
| POST | `/api/artisan/quote-requests/{id}/start-production` | Chỉ hộ thắng | `{version}` của request; ACCEPTED → IN_PRODUCTION |
| POST | `/api/artisan/quote-requests/{id}/deliver` | Chỉ hộ thắng | `{version}` của request; IN_PRODUCTION → DELIVERED |
| GET | `/api/artisan/quote-requests/{id}/events` | Artisan được xem | Lịch sử ẩn actorAccountId của người khác; không có giá/note báo giá |
| PATCH | `/api/artisan/workshops/{id}/contact` | Artisan chủ xưởng | `{email,phone}`, cần ít nhất một kênh; → ContactResponse |

Admin không có quyền xem giao dịch qua các vùng Buyer/Artisan. Sai vai trò 403; tài nguyên riêng không tồn tại/không sở hữu 404.

### QuoteRequestInput

Ví dụ lấy categoryId/tagIds thực tế từ options trước khi gửi:

```json
{
  "mode":"OPEN",
  "categoryId":1,
  "title":"Mua sỉ 100 cốc gốm",
  "quantity":100,
  "unit":"chiếc",
  "spec":"Men lam thủ công, cần mẫu trước khi sản xuất",
  "note":"Liên hệ để thống nhất đóng gói",
  "desiredBy":"2099-12-31",
  "biddingDeadline":null,
  "tagIds":[1],
  "buyerContactName":"Người mua demo",
  "buyerContactEmail":"buyer@example.test",
  "buyerContactPhone":"0900000001"
}
```

- Tạo: mode bắt buộc; còn lại có thể thiếu khi nháp. PUT là thay toàn bộ nội dung nháp, thêm version hiện tại; trường bỏ qua sẽ về null/empty.
- Gửi: cần category/title/quantity/unit/spec/desiredBy, tên liên hệ và email hoặc phone. OPEN cần >=1 tag; TARGETED cần targetWorkshopId của đúng một hộ đã duyệt, tag có thể rỗng. OPEN không nhận targetWorkshopId.
- title/name tối đa 160; unit 40; spec 4000; note 2000; tagIds <=20 không trùng; quantity số nguyên dương; email/phone đúng định dạng; tag/category/target tồn tại. desiredBy không trước ngày UTC khi gửi. Deadline ISO-8601 có múi giờ và ở tương lai khi gửi, có thể null.
- Client không gửi buyerAccountId/workshopId/status/acceptedQuoteId/totalPrice để tự quyết dữ liệu; service lấy chủ sở hữu từ Actor và tự chuyển trạng thái/tính giá.
- Chưa có mediaIds/ảnh tham khảo, chờ MEDIA.

### QuoteInput và VersionInput

```json
{"unitPrice":10000,"leadTimeDays":10,"note":"Chào giá riêng tư"}
```

Sửa hoặc gửi lại thêm `"version":0` (dùng giá trị thật đang xem). unitPrice >0, tối đa 13 chữ số nguyên/2 chữ số thập phân; leadTimeDays 1..3650; note <=2000. Backend tính totalPrice=unitPrice*quantity và kiểm tra tổng không vượt NUMERIC(15,2). Không coi dữ liệu này là hợp đồng/thanh toán.

Các action dùng `{ "version": 0 }`; riêng accept là **quoteVersion**, còn send/cancel/start/deliver/delete là **requestVersion**. Sau bất kỳ chỉnh sửa trạng thái/quote, phải dùng version mới server trả về hoặc GET lại. Không tự tăng version phía frontend.

### Response

QuoteRequestSummary: `{id,title,mode,status,quantity,unit,biddingDeadline,updatedAt,version}`. List không gửi giá hoặc contact.

QuoteRequestResponse: `{id,categoryId,title,quantity,unit,spec,note,desiredBy,biddingDeadline,mode,targetWorkshopId,tagIds,status,acceptedQuoteId,version,createdAt,updatedAt,buyerContact,quotes}`. Buyer thấy mọi quote; Artisan chỉ quote mình; acceptedQuoteId null với hộ không thắng; buyerContact null nếu hộ chưa báo giá.

QuoteResponse: `{id,requestId,workshopId,unitPrice,totalPrice,leadTimeDays,note,status,version,createdAt,updatedAt,workshopContact}`. ContactResponse là `{name,email,phone}`. QuoteStatus: SUBMITTED/WITHDRAWN/ACCEPTED/NOT_SELECTED.

QuoteEventResponse: `{id,actorAccountId,fromStatus,toStatus,note,createdAt}`; từ null sang DRAFT là sự kiện tạo. Note lịch sử chỉ mô tả hành động; nội dung note của quote không nằm trong lịch sử. Artisan không nhận ID actor khác.

### Lỗi nghiệp vụ mới

| HTTP | code | Frontend xử lý |
|---|---|---|
| 400 | REQUEST_INCOMPLETE | Hiển thị message chung (thiếu dữ liệu để gửi, ID tag/category sai, tổng tiền quá lớn...) |
| 400 | VALIDATION_FAILED | fieldErrors gắn ô nhập |
| 409 | STALE_DATA | Yêu cầu tải lại dữ liệu; không tự gửi lại thao tác chốt |
| 409 | INVALID_STATE_TRANSITION | Trạng thái đã đổi hoặc thao tác lặp, GET lại request |
| 409 | BIDDING_CLOSED | Hết hạn nhận báo giá mới/gửi lại |
| 409 | QUOTE_ALREADY_EXISTS | Chuyển sang sửa quote hiện có |
| 409 | CONCURRENT_UPDATE | Gặp lỗi lấy khóa/xung đột khóa, tải lại rồi thử sau |
| 409 | DATA_CONFLICT | Chốt ràng buộc DB; tải lại và đối chiếu trạng thái |

Quá hạn không tự đóng request; Buyer vẫn chọn quote đã có, hộ vẫn sửa/rút khi trạng thái cho phép. Hủy chỉ trước accept; không có hủy sau ACCEPTED. Không có API Admin phân bổ đơn.

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

### BUYER — `/api/buyer`

| Phương thức | Đường dẫn | FR |
|---|---|---|

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
