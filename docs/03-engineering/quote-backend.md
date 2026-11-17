# Luồng báo giá backend — sau M1

Ngày 2026-11-13. Nguồn triển khai: bản vCraft-M1-backend.zip. Giữ Spring Boot 4.1.2, Maven 3.9.11, Java 17.0.12; dùng JPA cho bảng nghiệp vụ và JDBC trong repository tra tag/liên hệ. Không có giao diện báo giá trong bản này.

## 1. Chạy local, không cần Edit Configurations

1. Giải nén ZIP thành thư mục mới, mở backend/pom.xml trong IntelliJ và Reload Maven.
2. Copy backend/application-local-private.properties.example thành backend/application-local-private.properties, nằm **ngang pom.xml**, ngoài src/resources.
3. Điền VCRAFT_DB_URL, VCRAFT_DB_USER, VCRAFT_DB_PASSWORD đúng PostgreSQL của bạn. Database đã chạy M1 và có Flyway V1–V3 dùng tiếp được; ứng dụng tự chạy V4. Nếu DB từng tạo tay, không có Flyway history, hãy sao lưu và dùng DB mới trống; không xóa DB cũ hoặc bật baseline bừa.
4. Trong terminal tại backend chạy `.\run-local.cmd` (Windows) hoặc `sh ./run-local.sh` (Linux). Script tự đặt profile local và Working directory về backend; cần JDK 17/JAVA_HOME đúng. Không cần Run → Edit Configurations. Có thể dùng cách Run Configuration trong README nếu muốn chạy nút Run IntelliJ; hai cách dùng cùng cấu hình backend.
5. Gọi GET http://localhost:8080/api/health/db. Mở React như trước: tại frontend, npm ci rồi npm run dev. UI hiện chỉ có trang chủ/danh mục/chi tiết; báo giá kiểm thử bằng Postman.

File private đặt trong resources theo cách đã tự chỉnh ở máy cũ thì cần chuyển ra backend cho bản ZIP mới này, vì import dùng `file:./application-local-private.properties`. Không copy password/token vào ZIP, Git hoặc Postman Environment.

## 2. Tài khoản và dữ liệu

Profile local/demo giữ buyer, admin, xuong-men-lam, xuong-dat-nung; thêm buyer-other để kiểm tra Buyer không đọc yêu cầu của nhau. Mật khẩu demo: VcraftDemo17!.

Seed thêm ba tag gom-thu-cong, men-lam, dat-nung và product_tags cho hai hộ hiện có; không seed yêu cầu/báo giá để tránh giả hoàn thành trạng thái. Liên hệ hộ là địa chỉ example.test và số demo, không phải thông tin thật. Hộ có API PATCH /api/artisan/workshops/{id}/contact để cập nhật email/phone của chính mình.

Giả định Q-38: tag seed cố định, khớp đúng ít nhất một tag; category vẫn riêng. Điều kiện khớp nằm **duy nhất trong QuoteMatchingRepository.eligibilityPredicate()**, dùng lại cho danh sách và quyền tham gia. Chưa làm CRUD tag/Admin UI hoặc matching gần đúng. Rút báo giá tạm theo gợi ý Q-31 đã ghi trong tài liệu gốc.

## 3. Luồng chính và quyền

| Thao tác | Ai thực hiện | Điều kiện/kết quả |
|---|---|---|
| Tạo/sửa/xóa nháp | Buyer chủ yêu cầu | DRAFT; sửa/xóa gửi version hiện tại. Nháp có thể thiếu nội dung. Xóa trả 204, xóa cả tag/events của nháp. |
| Gửi | Buyer chủ yêu cầu | DRAFT, đủ category/title/quantity/unit/spec/desiredBy, tên và email hoặc phone liên hệ; OPEN có tag; TARGETED đúng một hộ đã duyệt; deadline nếu có phải ở tương lai. → SENT |
| Gửi báo giá | Hộ đã duyệt, được chỉ định hoặc khớp tag | SENT/BIDDING, chưa hết hạn; tối đa một dòng báo giá/hộ/yêu cầu. → BIDDING |
| Sửa báo giá | Hộ sở hữu | SUBMITTED, request BIDDING, version đúng. Tăng version; tổng giá backend tính unitPrice × quantity. |
| Rút/gửi lại | Hộ sở hữu | Rút khi còn BIDDING; cuối cùng rút thì request về SENT. Gửi lại dùng chính dòng WITHDRAWN và version mới; quá hạn không gửi lại được. |
| Chấp nhận | Buyer chủ yêu cầu | BIDDING, đúng quote của request, SUBMITTED và **quote version** đúng. Chọn một, phần còn lại NOT_SELECTED. → ACCEPTED |
| Bắt đầu sản xuất | Chỉ hộ có quote ACCEPTED | Request ACCEPTED + request version đúng. → IN_PRODUCTION |
| Đã giao | Chỉ hộ có quote ACCEPTED | Request IN_PRODUCTION + request version đúng. → DELIVERED |
| Hủy | Buyer chủ yêu cầu | SENT/BIDDING + request version đúng; các SUBMITTED → NOT_SELECTED; → CANCELLED. Nháp dùng xóa, không dùng hủy. |

Chuyển sai trạng thái hoặc gửi lặp: 409 INVALID_STATE_TRANSITION. Version cũ: 409 STALE_DATA. Không tìm thấy/không sở hữu tài nguyên riêng: 404. Sai vùng vai trò: 403; thiếu xác thực: 401.

Buyer xem mọi báo giá trong yêu cầu của mình. Artisan chỉ nhận báo giá của chính mình ở cả endpoint detail/list; không lộ acceptedQuoteId của hộ thắng khác. Số tiền/note của đối thủ không có trong response. Lịch sử Artisan được ẩn actorAccountId của người khác; note lịch sử chỉ ghi hành động, không chứa nội dung báo giá. Admin không được xem giao dịch; không thêm API phân bổ đơn hay ký hợp đồng.

Liên hệ: Buyer nhận liên hệ xưởng trên từng QuoteResponse; Artisan chỉ nhận buyerContact sau khi đã gửi báo giá, trước đó null. Buyer contact là snapshot do Buyer nhập trong yêu cầu nháp; chưa xác minh email/SĐT (đăng ký/xác minh là module khác).

## 4. Hạn chót

biddingDeadline là Instant ISO-8601 có Z/offset, có thể null; thời điểm >= deadline chặn báo giá mới/gửi lại. Sửa/rút báo giá đã có vẫn được trước ACCEPTED; Buyer vẫn chấp nhận sau hạn. Không có scheduler tự đóng yêu cầu. desiredBy là ngày mong muốn, không phải hạn nhận báo giá; khi gửi không được trước ngày UTC hiện tại.

## 5. Giao dịch, khóa dòng và version

Mọi ghi dữ liệu qua QuoteRequestService, transaction READ_COMMITTED. Mọi chuyển trạng thái qua transition() và bảng TRANSITIONS; trạng thái đầu DRAFT được tạo cùng event khởi tạo.

1. Khóa yêu cầu cha bằng QuoteRequestRepository.lockById(), @Lock(PESSIMISTIC_WRITE).
2. Kiểm tra chủ sở hữu/vai trò, trạng thái và version thích hợp.
3. Đọc/sửa báo giá con sau khi đã có khóa cha.
4. Ghi trạng thái, acceptedQuoteId và event trong cùng transaction; exception rollback toàn bộ.
5. Unique index một phần quotes_one_accepted_per_request chặn hai quote ACCEPTED ngay tại PostgreSQL. Composite FK bảo đảm acceptedQuoteId thuộc đúng request.

Thứ tự khóa luôn cha trước, rồi con. Khi nhận quoteId ở endpoint sửa/rút, bước đầu chỉ query scalar requestId, không nạp quote entity cũ trước lúc chờ khóa. Nhờ vậy dữ liệu được đọc lại sau transaction trước commit. Không dùng synchronized Java: hai instance backend vẫn cần cùng khóa database.

@Version trên QuoteRequest và Quote là chốt phát hiện dữ liệu cũ; client cũng gửi version. Body `{version: ...}` trong accept dùng **version báo giá**, còn send/cancel/start/deliver/delete dùng **version yêu cầu**. Sửa/gửi/rút báo giá có thể làm requestVersion đổi, nên Buyer phải GET lại trước hủy. Version không phải token và không phải số tiền.

Request thứ hai chờ khóa, sau khi được cấp khóa sẽ thấy ACCEPTED và trả 409; không tự chọn tiếp báo giá khác. Đây là hành vi gửi lặp được kiểm thử, không phải tự động retry.

## 6. File cần học

| File | Vai trò |
|---|---|
| controller/BuyerQuoteController.java | Endpoint phía Buyer, nhận DTO/Actor, không truy vấn DB |
| controller/ArtisanQuoteController.java | Inbox, gửi/sửa/rút quote và cập nhật tiến độ |
| controller/QuoteOptionsController.java | Category/tag options công khai cho form |
| service/QuoteRequestService.java | Một nơi xử lý máy trạng thái, quyền sở hữu, deadline, transaction, mapping DTO riêng tư |
| model/QuoteRequest.java, Quote.java | Entity có @Version; không trả entity trực tiếp ra API |
| model/RequestStatus.java, RequestMode.java, QuoteStatus.java | Các enum được phép |
| model/QuoteRequestEvent.java | Lịch sử ai/lúc nào/từ trạng thái nào sang trạng thái nào |
| repository/QuoteRequestRepository.java | JPA lock và danh sách Buyer |
| repository/QuoteRepository.java | CRUD quote và scalar parent lookup |
| repository/QuoteEventRepository.java | Lịch sử theo thứ tự ID |
| repository/QuoteMatchingRepository.java | Một predicate Q-38; inbox phân trang SQL, tra tags/category/contact |
| dto/QuoteRequestInput.java, QuoteInput.java, VersionInput.java | Dữ liệu ghi và Bean Validation |
| dto/QuoteRequestResponse.java, QuoteResponse.java, QuoteRequestSummary.java, QuoteEventResponse.java, ContactResponse.java | Hợp đồng JSON; summary không chứa giá hoặc thông tin liên hệ |
| dto/QuoteTagResponse.java, QuoteCategoryResponse.java, WorkshopContactInput.java | Option cho form và cập nhật liên hệ hộ |
| config/DemoQuoteData.java, db/seed/quotes.sql | Seed local/demo sau catalog/accounts |
| db/migration/V4__quote_workflow.sql | Schema duy nhất của chặng; V1–V3 không đổi |
| tests/quotes.py | Test HTTP, quyền riêng tư, state, deadline, song song và assertion DB tùy chọn |

## 7. Demo Postman và kiểm thử

Import hai JSON trong postman, chọn vCraft local. Folder **Quote Backend — Luồng báo giá** nằm trong Collection. Chạy từ trên xuống: đăng nhập → lấy ID tag/category → tạo nháp → gửi → hai hộ báo giá → sửa → Buyer chọn → hộ thắng sản xuất/giao → lịch sử. Scripts tự lưu requestId/requestVersion/quoteId/quoteVersion. Không lấy token_hash từ DB.

Từ gốc dự án, backend đang chạy với DB test riêng:

```powershell
python backend/tests/m1.py
python backend/tests/quotes.py
```

Để có cả kiểm tra SQL và chứng minh chờ khóa bằng kết nối riêng:

```powershell
python -m pip install -r backend/tests/quote_requirements.txt
$env:VCRAFT_TEST_DB_URL = "postgresql://postgres@localhost:5432/vcraft_quote_test"
$env:VCRAFT_TEST_DB_PASSWORD = "YOUR_DB_PASSWORD"
python backend/tests/quotes.py
```

DB_URL này phải trỏ **cùng database backend đang chạy**. Script tạo nhiều request/quote và dùng transaction SQL thử vi phạm unique rồi rollback; không chạy trên DB dữ liệu thật. Nếu không có DB_URL, test HTTP vẫn chạy và in SKIP cho kiểm tra SQL/giữ khóa, không báo PASS thay cho phần bỏ qua.

Test chính: 10 vòng hai accept khác nhau, mỗi vòng đúng 200/409; accept vs sửa/rút; cancel vs submit; gửi trùng và sửa nháp đồng thời; một kết nối giữ khóa cha khiến hai request HTTP phải chờ; unique index độc lập ngăn ACCEPTED thứ hai. Xem kết quả chạy thực tế trong quote-verification.md.

Truy vấn demo pgAdmin:

```sql
SELECT id,status,accepted_quote_id,version FROM quote_requests ORDER BY id;
SELECT id,request_id,workshop_id,status,version FROM quotes ORDER BY request_id,id;
SELECT request_id,COUNT(*) FROM quotes WHERE status='ACCEPTED' GROUP BY request_id HAVING COUNT(*)>1;
SELECT request_id,actor_account_id,from_status,to_status,created_at FROM quote_request_events ORDER BY request_id,id;
SELECT version,success FROM flyway_schema_history ORDER BY installed_rank;
```

Query HAVING phải rỗng. Các API giữ mã lỗi chuẩn của M1; thêm STALE_DATA, INVALID_STATE_TRANSITION, BIDDING_CLOSED, QUOTE_ALREADY_EXISTS, REQUEST_INCOMPLETE, CONCURRENT_UPDATE. REQUEST_INCOMPLETE hiển thị message chung; VALIDATION_FAILED dùng fieldErrors.

## 8. Phần chưa hoàn thiện

- Ảnh tham khảo của yêu cầu chờ MEDIA; FR-QUOTE-01 mới xong phần dữ liệu văn bản. Không thêm mediaIds giả hoặc nhận URL ảnh không kiểm quyền.
- CRUD tag/Q-38, badge FR-QUOTE-10, UI báo giá/đăng nhập, đăng ký/xác minh Buyer, liên kết lô/QR, thanh toán, hợp đồng và giao hàng thực tế không thuộc chặng này.
- Có API tiến độ nhưng chưa có UI; không đánh dấu module QUOTE hoàn thành cả frontend/backend.
- Hộ tự điều phối ngoài nền tảng; BQL không tham gia transaction. Câu hỏi giảng viên về phân bổ đơn lớn trong tài liệu gốc vẫn giữ nguyên.
