# vCraft — M1 Backend Foundation

## 1. Thay đổi trong bản này

- Lỗi API thống nhất `{timestamp,status,code,message,path,fieldErrors}`, kể cả 401/403 từ Spring Security. Không trả stack trace, mật khẩu hoặc token trong lỗi.
- Bean Validation tại DTO và service cho đăng nhập, cấp tài khoản hộ, sửa tên xưởng. Mật khẩu giới hạn **72 byte UTF-8** để phù hợp BCrypt; mật khẩu tạo mới ít nhất 10 ký tự và không chỉ chứa khoảng trắng.
- Phân trang thật tại PostgreSQL qua JPA: `GET /api/products/page?page=0&size=12`. React danh mục dùng endpoint này. Endpoint cũ `/api/products` vẫn trả mảng để giữ tương thích.
- `/api/buyer/**` chỉ cho BUYER; endpoint `GET /api/buyer/me` giúp kiểm tra quyền trước khi làm nghiệp vụ báo giá. ADMIN không tự động có quyền BUYER. `/api/me` vẫn dùng chung cho mọi tài khoản đã xác thực.
- Tách cấu hình chung và profile local; bỏ mật khẩu DB khỏi mã dùng chung, không tự bật local. Giữ nguyên migration V1–V3, không thêm bảng cho tính năng chưa làm.

## 2. Chạy local từ bản ZIP

Cần **JDK 17** (không chỉ JRE), Node 22 LTS hoặc Node >=20.19, PostgreSQL đang chạy, IntelliJ và Postman. Lần đầu Maven/npm cần Internet tải dependency.

### Bước 1 — PostgreSQL

Dùng pgAdmin tạo database **mới và trống** `vcraft_local` bằng tài khoản của bạn. Nếu tên này đã có schema tự tạo tay, dùng tên mới như `vcraft_m1` và đổi URL bên dưới. Không xóa database/volume cũ, không tự bật `baseline-on-migrate` để bỏ qua khác biệt. Nếu muốn giữ dữ liệu cũ, sao lưu rồi đối chiếu cấu trúc với V1–V3 trước khi lập kế hoạch chuyển dữ liệu.

Không chạy SQL tạo bảng bằng tay. Spring Boot chạy Flyway V1–V3, sau đó JPA kiểm tra schema; profile local nạp seed và tài khoản mẫu idempotent.

### Bước 2 — Backend trong IntelliJ

1. Open `backend/pom.xml` dưới dạng Maven project và Reload Maven.
2. Đặt Project SDK, Module SDK, Maven Runner JRE và Run Configuration JRE đều là **JDK 17**.
3. Mở `VCraftApplication`. Trong Run → Edit Configurations, đặt **Working directory** là đường dẫn thư mục `backend`.
4. Profile `local` **không tự bật**. Trong cùng cửa sổ đó, điền Environment variables:

```text
SPRING_PROFILES_ACTIVE=local
```

5. Khai mật khẩu database theo **một trong hai** cách:
   - **File riêng (khuyên dùng):** chép `backend/application-local-private.properties.example` thành `backend/application-local-private.properties`, sửa `VCRAFT_DB_PASSWORD` (và URL/user nếu máy bạn khác mặc định). File này đã nằm trong `.gitignore`, không commit.
   - **Biến môi trường:** thêm vào Environment variables ở bước 4: `SPRING_PROFILES_ACTIVE=local;VCRAFT_DB_PASSWORD=MAT_KHAU_CUA_BAN` (tùy chọn thêm `VCRAFT_DB_URL`, `VCRAFT_DB_USER`).
6. Run; đợi log `Started VCraftApplication` và seed chạy xong. Kiểm tra `http://localhost:8080/api/health/db` trả `200 {"status":"UP"}`.

**Chạy bằng PowerShell thay cho IntelliJ** (đang ở `backend/`):

```powershell
$env:SPRING_PROFILES_ACTIVE = "local"
$env:VCRAFT_DB_PASSWORD = "MAT_KHAU_CUA_BAN"   # bỏ dòng này nếu đã dùng file riêng
.\mvnw.cmd spring-boot:run
```


Lỗi thường gặp khi khởi động:

| Thông báo | Nguyên nhân |
|---|---|
| `Failed to configure a DataSource: 'url' attribute is not specified` | Chưa bật profile: thiếu `SPRING_PROFILES_ACTIVE=local`. |
| `password authentication failed` | Sai mật khẩu, hoặc đã bật `local` nhưng chưa khai mật khẩu ở file riêng lẫn biến môi trường. |
| `Connection refused` | PostgreSQL chưa chạy hoặc sai cổng/URL. |

Nếu wrapper báo `'powershell' is not recognized`, kiểm tra PATH Windows có `C:\Windows\System32\WindowsPowerShell\v1.0\`; hoặc chạy Maven goal `spring-boot:run` trong cửa sổ Maven của IntelliJ. Không đổi Spring Boot chỉ để sửa lỗi PATH.

### Bước 3 — React ở terminal thứ hai

```powershell
cd frontend
npm ci
npm run dev
```

Mở `http://localhost:5173`. React chạy bằng Vite, backend chạy riêng bằng IntelliJ ở cổng 8080. Vite proxy `/api` đến backend nên không cần tắt CORS trên trình duyệt. Dừng server bằng Ctrl+C. File `.env.example` là mẫu tham khảo; Spring Boot không tự đọc file `.env`.

### Bước 4 — Postman

Import **file JSON Collection** và **file JSON Environment** trong `postman/`, chọn môi trường **vCraft local**. Folder **Phase 02 — Tài khoản và quyền xưởng** và **M1 — Backend foundation** là folder bên trong Collection của Postman, không phải thư mục trên ổ đĩa.

Chạy folder M1 từ trên xuống: ba request đăng nhập lưu `buyerToken`, `artisanToken`, `adminToken`; tiếp đó kiểm tra phân trang, lỗi nhập liệu, 404, Buyer 200/Artisan 403/Admin 403/Guest 401. Token được lấy từ phản hồi đăng nhập, không lấy `token_hash` trong DB. Chạy folder Phase02 để chứng minh quyền sở hữu, cấp/duyệt hộ và đăng xuất. Token lưu trong Postman khi chạy trên máy; file environment bàn giao không chứa token.

| Tên đăng nhập | Vai trò | Mật khẩu demo |
|---|---|---|
| buyer | BUYER | VcraftDemo17! |
| admin | ADMIN | VcraftDemo17! |
| xuong-men-lam | ARTISAN | VcraftDemo17! |
| xuong-dat-nung | ARTISAN | VcraftDemo17! |

Chỉ tài khoản demo trên profile local/demo. Không dùng mật khẩu này cho dữ liệu thật.

## 3. Cấu trúc file và trách nhiệm MVC

| Đường dẫn dưới backend/src/main/java/vn/vcraft/backend/ | Trách nhiệm |
|---|---|
| controller/ | Nhận HTTP, bind DTO, `@Valid`, gọi service; không truy vấn DB trực tiếp ở endpoint mới. BuyerController cung cấp endpoint kiểm tra vùng Buyer. |
| dto/ApiErrorResponse.java | Hợp đồng lỗi chung cho advice và Security. |
| dto/LoginRequest.java, NewArtisanRequest.java, WorkshopUpdateRequest.java | Ràng buộc Bean Validation trên dữ liệu ghi. |
| dto/PageQuery.java, PageResponse.java | Bind page/size có giá trị mặc định; DTO phản hồi phân trang, không lộ Spring Page/entity. PageQuery dùng JavaBean để bind query parameters. |
| service/ | Nghiệp vụ, quyền sở hữu, giao dịch; `@Validated` kiểm tra đầu vào khi service được gọi qua Spring proxy. |
| repository/ | Truy vấn JPA/JDBC hiện có; ProductRepository phân trang và count ở DB, fetch workshop/category trong cùng truy vấn. |
| model/ | Entity và Actor (principal đã xác thực). |
| validation/Utf8Bytes.java, Utf8BytesValidator.java | Constraint riêng đo số byte UTF-8 của mật khẩu, không chỉ số ký tự. |
| exception/ApiException.java, ApiErrors.java | Lỗi nghiệp vụ có mã; một advice chuyển lỗi MVC/service/DB thành JSON an toàn. |
| security/SecurityErrorWriter.java | Ghi cùng JSON cho lỗi Security xảy ra trước controller. |
| security/BearerTokenFilter.java, BearerTokens.java | Đọc Bearer token, hash rồi tra phiên, tạo Actor. Không đổi cơ chế token sang JWT. |
| config/SecurityConfig.java | Phân vai theo tiền tố URL; kiểm tra sở hữu vẫn ở service. |
| config/DemoData.java, DemoAccounts.java | Nạp dữ liệu mẫu khi bật local/demo; không tạo lại schema. |

Các file mới/sửa ngoài Java: `backend/pom.xml` (starter-validation), `backend/src/main/resources/application*.properties`, mẫu cấu hình private, `backend/tests/m1.py`, `frontend/src/main.jsx` và `styles.css`, Postman, tài liệu API/status/roadmap. Giữ mã frontend hiện có, chưa tái cấu trúc frontend trong chặng backend này.

## 4. Workflow chạy

**Khởi động:** cấu hình local → kết nối PostgreSQL → Flyway migrate → JPA validate → seed local/demo → nhận HTTP.

**Đọc danh mục:** React gọi `/api/products/page` → Security cho GET công khai → controller bind và validate PageQuery → service tạo PageRequest → repository query có limit/offset + count → DTO PageResponse → React cập nhật danh sách và nút trang.

**Ghi dữ liệu:** Bearer filter nhận diện Actor → Security kiểm vai trò → controller `@Valid` DTO → service kiểm sở hữu/nghiệp vụ → repository → PostgreSQL. Vi phạm input trả 400 và `fieldErrors`; thiếu/sai token 401; sai vai trò hoặc sửa xưởng khác 403. Luồng sở hữu xưởng giữ HTTP 403 của Phase02; các tài nguyên riêng tư sau này theo quy tắc 404 trong docs.

**Lỗi:** lỗi controller/service qua ApiErrors; lỗi Security qua SecurityErrorWriter; cả hai dùng ApiErrorResponse. React hiển thị `message`, ánh xạ `fieldErrors` tới từng ô nhập; không dựa vào việc tách chuỗi message.

## 5. Hợp đồng nhanh cho frontend

```http
GET /api/products/page?page=0&size=12
```

```json
{"items":[],"page":0,"size":12,"totalElements":0,"totalPages":0,"first":true,"last":true}
```

`page` từ 0 đến 100000; `size` mặc định 12, từ 1 đến 50. Sắp xếp cố định `id ASC`. Trang vượt dữ liệu trả 200 với items rỗng. Chưa hỗ trợ sort/filter; không tự suy ra đây là CRUD sản phẩm hoàn chỉnh.

Ví dụ lỗi nhập liệu:

```json
{"timestamp":"2026-11-12T00:00:00Z","status":400,"code":"VALIDATION_FAILED","message":"Vui lòng kiểm tra dữ liệu nhập.","path":"/api/login","fieldErrors":{"username":"Vui lòng nhập tên đăng nhập.","password":"Vui lòng nhập mật khẩu."}}
```

Danh sách đầy đủ: `docs/02-design/api-contract.md`; chi tiết chặng: `docs/03-engineering/m1-backend.md`.

## 6. SQL, kiểm thử và bàn giao

- Migration chính thức: `backend/src/main/resources/db/migration/`; giữ nguyên V1–V3. Schema mới dùng **V4__...sql**. Không sửa file đã áp dụng.
- Seed: `backend/src/main/resources/db/seed/`, chỉ local/demo. JPA `validate`, SQL init `never`. Kiểm tra DB: `SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;`, `SELECT COUNT(*) FROM products;`, `SELECT COUNT(*) FROM accounts;`. Khởi động lại không tăng số bản ghi seed; bộ test HTTP chủ động tạo thêm hộ với username riêng, đó không phải seed bị trùng.
- Build backend: tại backend chạy `.\mvnw.cmd clean package` (Linux `./mvnw clean package`). Build frontend: `npm ci` rồi `npm run build` trong frontend.
- Backend đang chạy với DB **test riêng**, từ gốc chạy `python backend/tests/m1.py`. Test này chạy cả Phase02, tạo/duyệt thêm một hộ để kiểm tra; không chạy trên dữ liệu thật.
- Kết quả kiểm tra thực tế và giới hạn môi trường: `docs/03-engineering/m1-verification.md`.

Lỗi thường gặp: `password authentication failed` → kiểm tra user/password đúng PostgreSQL; `database does not exist` → tạo DB trống đúng tên; Flyway báo schema non-empty/no history → đổi sang DB mới và giữ DB cũ; cổng 8080 bận → dừng phiên backend cũ; React lỗi tải → gọi health/db trước và kiểm tra backend 8080; Java release không đúng → kiểm tra cả Maven Runner JRE, không chỉ Project SDK.

Các phần chưa hoàn thành: upload/resize ảnh, CRUD danh mục đầy đủ, đăng ký Buyer, giao diện đăng nhập/quản trị, lô/QR, báo giá, bản đồ, tin tức, triển khai Internet. Không đánh dấu toàn bộ M1 hoặc đề bài là hoàn thành.
