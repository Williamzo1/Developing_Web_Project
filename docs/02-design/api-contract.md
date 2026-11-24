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

## Giới hạn và phần sau M1 bước 10

Các giả định Q-29, Q-38, Q-39 vẫn chưa phải quyết định mới của nhóm: lịch theo tuần/ngày nghỉ,
Admin quản lý tag song song phân loại, OTP email và kiểm định dạng SĐT. Chưa có OTP SMS.
JPEG/PNG đã kiểm thử. WebP chỉ xử lý được nếu JVM có ImageIO reader tương ứng, cấu hình hiện tại
không thêm codec mới. Không giả vờ gửi OTP thành công: lỗi SMTP được log, tài khoản vẫn được tạo
để có thể gửi lại khi cấu hình mail hoạt động. Không log mã OTP.

Tìm kiếm theo tên, sắp xếp tùy chọn, route `/api/categories`, `/api/tags`, báo cáo vi phạm,
đổi mật khẩu và giao diện M2/M3 nằm ngoài phạm vi lần này.
Postman: thư mục `M1 steps 1–10` trong collection hiện có. Kiểm thử: `m1.py`, `m1_extension.py`, `quotes.py`.
