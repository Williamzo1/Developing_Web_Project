# Hợp đồng API

Cập nhật 2026-11-24: đã triển khai phạm vi THẮNG — M1 Backend, bước 1–10 trong `vcraft-action-plan.md`.
Controller và DTO trong `backend/src/main/java/vn/vcraft/backend/` là hợp đồng chi tiết của từng trường.
Luồng báo giá hiện có giữ nguyên, xem `../03-engineering/quote-backend.md`.

## Quy ước

- JSON UTF-8, tên trường camelCase. Token: `Authorization: Bearer <token>`.
- `PageQuery`: page từ 0 đến 100000, size từ 1 đến 50, mặc định page=0 và size=12.
- `PageResponse`: `{items,page,size,totalElements,totalPages,first,last}`.
- Các DTO mới dùng `referencePrice`, trả null khi `priceVisible=false`.
- `MediaResponse`: `{id,thumbUrl,mediumUrl,largeUrl,width,height}`.
- Không truyền id xưởng sở hữu trong input sản phẩm/lô. Backend lấy xưởng từ Actor.
- Tài nguyên riêng tư của người khác trả 404. Sai vai trò trả 403, thiếu/sai/hết hạn token trả 401.
- Hộ chưa duyệt hoặc bị khóa không đăng nhập được. Khóa hộ thu hồi mọi phiên và ẩn nội dung công khai của hộ.
- Lỗi giữ dạng `{timestamp,status,code,message,path,fieldErrors}`. DTO/query/list sai trả 400, trùng dữ liệu 409, quá dung lượng 413, sai MIME 415. Lỗi 500 không trả chi tiết nội bộ.

## API công khai

| Method | Path | Input / response |
|---|---|---|
| GET | `/api/health`, `/api/health/db` | Trạng thái dịch vụ / kết nối DB |
| POST | `/api/login` | LoginRequest → LoginResponse `{token,role}` |
| POST | `/api/register` | RegisterRequest `{username,password,displayName,email,phone}` → 201 RegisterResponse |
| POST | `/api/register/verify-email` | VerifyEmailRequest `{username,otp}` → 200 không body |
| POST | `/api/register/resend-otp` | ResendOtpRequest `{username}` → 200 không body |
| GET | `/api/home` | HomeResponse: tối đa 3 xưởng, 6 sản phẩm, 3 lô nổi bật và 5 tin mới |
| GET | `/api/quote-categories`, `/api/quote-tags` | Danh sách phân loại và tag hiện có |
| GET | `/api/workshops?page=&size=` | PageResponse<WorkshopSummaryResponse> |
| GET | `/api/workshops/map` | Mảng MapPinResponse, chỉ hộ công khai có tọa độ |
| GET | `/api/workshops/{slug}` | WorkshopDetailResponse, kèm sản phẩm/lô công khai (trang đầu) |
| GET | `/api/products` | Mảng ProductResponse cũ, chỉ nội dung công khai |
| GET | `/api/products/page?workshopId=&categoryId=&tagSlug=&page=&size=` | PageResponse<ProductSummaryResponse> |
| GET | `/api/products/{idOrSlug}` | ProductDetailResponse, chấp nhận cả id số và slug |
| GET | `/api/products/{slug}/steps` | Mảng StepResponse |
| GET | `/api/batches?productId=&workshopId=&kind=&page=&size=` | PageResponse<BatchSummaryResponse> |
| GET | `/api/batches/{publicCode}?token=` | BatchDetailResponse. Lô PRIVATE cần token đang có hiệu lực, sai/thu hồi trả 404 |
| GET | `/api/batches/{publicCode}/qr.png` | PNG của lô PUBLIC, lô PRIVATE/ẩn/bị khóa trả 404 |
| GET | `/api/news?kind=&page=&size=` | PageResponse<NewsResponse>, chỉ tin đã xuất bản |
| GET | `/api/news/{slug}` | NewsResponse đã xuất bản |
| GET | `/media/**` | Ảnh tĩnh đã resize |

`ProductSummaryResponse` và `ProductDetailResponse` có thêm alias `workshop`, `category`, `imageUrl`
để frontend cũ tiếp tục hiển thị được. `/api/products/page` giữ metadata phân trang và bổ sung dữ liệu mới.
Slug không tồn tại trả 404 PRODUCT_NOT_FOUND, thay cho lỗi parse id 400 trước đây.
`kind` của lô: IN_STOCK hoặc MADE_TO_ORDER. `visibility`: PUBLIC hoặc PRIVATE.
`kind` của tin: MATERIAL_PRICE, POWER_CUT, POLICY, EVENT hoặc OTHER.

## API có xác thực

| Quyền | Method | Path | Input / response |
|---|---|---|---|
| Đăng nhập | GET | `/api/me` | Actor `{id,role,workshop}` |
| Đăng nhập | POST | `/api/logout` | 204, thu hồi token |
| BUYER | GET | `/api/buyer/me` | Actor của Buyer |
| Đăng nhập | POST | `/api/media` | Multipart `files` (nhiều tệp), tối đa 10 tệp × 5 MB → 201 mảng MediaResponse |
| Chủ ảnh | DELETE | `/api/media/{id}` | 204, ảnh còn liên kết/được chọn làm bìa trả 409 |
| ARTISAN | GET, PUT | `/api/artisan/workshop` | GET hồ sơ mình, PUT WorkshopProfileInput → WorkshopDetailResponse |
| ARTISAN | PATCH | `/api/artisan/workshops/{id}` | WorkshopUpdateRequest `{name}` → WorkshopResponse cũ |
| ARTISAN | PATCH | `/api/artisan/workshops/{id}/contact` | WorkshopContactInput → ContactResponse |
| ARTISAN | PUT | `/api/artisan/workshop/hours` | Mảng WorkshopHourInput → 204, thay toàn bộ lịch |
| ARTISAN | POST | `/api/artisan/workshop/closures` | WorkshopClosureInput → 201 |
| ARTISAN | DELETE | `/api/artisan/workshop/closures/{id}` | 204 |
| ARTISAN | GET, POST | `/api/artisan/workshop/certificates` | GET mảng CertificateResponse, POST CertificateInput → 201 |
| ARTISAN | DELETE | `/api/artisan/workshop/certificates/{id}` | 204 |
| ARTISAN | GET, POST | `/api/artisan/products` | GET mảng ProductSummaryResponse, POST ProductCreateInput → 201 ProductDetailResponse |
| ARTISAN | GET, PUT, DELETE | `/api/artisan/products/{id}` | GET chi tiết, PUT ProductUpdateInput, DELETE ẩn → 204 |
| ARTISAN | PUT | `/api/artisan/products/{id}/steps` | Mảng StepInput thay toàn bộ công đoạn → 204 |
| ARTISAN | PUT | `/api/artisan/products/{id}/tags` | Mảng id tag → 204 |
| ARTISAN | GET, POST | `/api/artisan/batches` | GET mảng BatchSummaryResponse, POST BatchCreateInput → 201 BatchDetailResponse |
| ARTISAN | GET, PUT | `/api/artisan/batches/{id}` | GET chi tiết, PUT BatchUpdateInput |
| ARTISAN | PUT | `/api/artisan/batches/{id}/milestones` | Mảng MilestoneInput → 204, công đoạn phải thuộc sản phẩm của lô |
| ARTISAN | POST, DELETE | `/api/artisan/batches/{id}/share-token` | POST `{shareUrl}`, DELETE thu hồi → 204 |
| ARTISAN | GET | `/api/artisan/batches/{id}/qr.png` | PNG, lô PRIVATE cần tạo share-token trước (chưa có → 409) |
| ADMIN | GET, POST | `/api/admin/workshops` | GET mảng WorkshopResponse, POST NewArtisanRequest → 201 |
| ADMIN | POST | `/api/admin/workshops/{id}/approve` | WorkshopResponse |
| ADMIN | POST | `/api/admin/workshops/{id}/lock` | LockInput `{reason}` → 204 |
| ADMIN | POST | `/api/admin/workshops/{id}/unlock` | 204 |
| ADMIN | GET | `/api/admin/stats` | AdminStatsResponse, đếm dữ liệu thật |
| ADMIN | GET, POST | `/api/admin/tags` | GET mảng TagResponse, POST TagInput → 201 |
| ADMIN | PUT, DELETE | `/api/admin/tags/{id}` | PUT TagInput → TagResponse, DELETE → 204 (tag đang được tham chiếu có thể trả 409) |
| ADMIN | POST | `/api/admin/products/{id}/hide`, `/api/admin/batches/{id}/hide` | 204 |
| ADMIN | PUT | `/api/admin/featured` | FeaturedInput `{workshopIds,productIds,batchIds}` → 204, thay toàn bộ lựa chọn |
| ADMIN | GET, POST | `/api/admin/news` | GET PageResponse<NewsResponse>, POST NewsCreateInput → 201 |
| ADMIN | PUT, DELETE | `/api/admin/news/{id}` | PUT NewsCreateInput → NewsResponse, DELETE → 204 |

Giờ dùng HH:mm, thứ 0=CN đến 6=T7. Giờ mở phải trước giờ đóng, mỗi thứ một khung giờ.
`openNow` tính theo Asia/Ho_Chi_Minh, null khi chưa có lịch cho ngày hiện tại. Ngày nghỉ riêng trả false.
`mediaIds` là danh sách có thứ tự, ảnh phải thuộc tài khoản đang thao tác. PUT các input tạo/sửa
sản phẩm và lô là thay thế các trường dữ liệu, không phải PATCH từng trường.

QR/shareUrl dẫn tới `${app.frontend-url}/batches/{publicCode}` và thêm `?token=` cho lô riêng.
Frontend landing page thuộc M2, chưa được xây trong lần cập nhật này.

## Backend báo giá — đã cài đặt 2026-11-13

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


## Giới hạn và phần sau M1 bước 10

Q-29, Q-38, Q-39 đã chốt ngày 2026-11-28 đúng như cài đặt: lịch theo tuần/ngày nghỉ,
Admin quản lý tag song song phân loại, OTP email và kiểm định dạng SĐT. Chưa có OTP SMS.
Upload chỉ nhận JPEG/PNG theo nội dung, không nhận WebP. Bộ xử lý ImageVariants xuất ba biến thể JPEG
320/800/1600 px theo cạnh dài, không phóng to, xoay EXIF, bỏ metadata và đưa nền trong suốt về trắng.
Upload trả URL tương đối /media/..., width/height là kích thước biến thể lớn.
Ảnh gốc tối đa 30 megapixel. /media/** có cache immutable, tên tệp UUID.
Một tệp không hợp lệ làm hủy cả request; lỗi ghi đĩa/DB dọn các tệp đã tạo.
Ảnh đang gắn thực thể/bìa xưởng không được xóa (409). Không giả vờ gửi OTP thành công: lỗi SMTP được log, tài khoản vẫn được tạo
để có thể gửi lại khi cấu hình mail hoạt động. Không log mã OTP.

Tìm kiếm theo tên, sắp xếp tùy chọn, route `/api/categories`, `/api/tags`, báo cáo vi phạm,
đổi mật khẩu và giao diện M2/M3 nằm ngoài phạm vi lần này.
Postman: thư mục `M1 steps 1–10` trong collection hiện có. Kiểm thử: `m1.py`, `m1_extension.py`, `quotes.py`.
