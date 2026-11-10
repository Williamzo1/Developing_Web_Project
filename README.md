# vCraft Phase 02 MVC

> **Tài liệu dự án** (yêu cầu, thiết kế, quy ước, hiện trạng, lộ trình): xem [docs/README.md](docs/README.md). Hướng dẫn cho AI agent: [AGENTS.md](AGENTS.md).

Bài tập lớn INT3306, chủ đề gốm Bát Tràng. Bản này tiếp nối vCraft Phase 02, tổ chức lại backend Spring MVC theo lớp. Chức năng gồm danh mục công khai, đăng nhập và đăng xuất, Admin cấp và duyệt tài khoản hộ, Artisan sửa tên xưởng của mình. Giao diện React hiện phục vụ trang chủ, danh mục và chi tiết. Chức năng tài khoản và quản trị được thử bằng Postman.

## 1. Cấu trúc thư mục

Backend là REST API. Controller trả DTO dưới dạng JSON. React đảm nhiệm phần hiển thị trong trình duyệt. Service chứa xử lý nghiệp vụ. Repository thực hiện truy cập PostgreSQL.

```text
vCraft-phase02-mvc/
├── backend/
│   ├── pom.xml, mvnw, mvnw.cmd                 Spring Boot 4.1.2, Java 17, Maven Wrapper
│   ├── src/main/java/vn/vcraft/backend/
│   │   ├── VCraftApplication.java              Điểm khởi động và quét component
│   │   ├── config/
│   │   │   ├── SecurityConfig.java            Tuyến công khai, quyền vai trò, cấu hình filter
│   │   │   ├── DemoData.java                  Chạy SQL mẫu theo profile
│   │   │   └── DemoAccounts.java              Tạo tài khoản mẫu sau dữ liệu xưởng
│   │   ├── controller/
│   │   │   ├── AuthController.java            Đăng nhập, thông tin tài khoản, đăng xuất
│   │   │   ├── AdminWorkshopController.java   Danh sách xưởng, cấp tài khoản, duyệt hộ
│   │   │   ├── ArtisanWorkshopController.java Sửa tên xưởng
│   │   │   ├── ProductController.java         Danh mục và chi tiết sản phẩm
│   │   │   └── HealthController.java          Kiểm tra HTTP và kết nối database
│   │   ├── service/
│   │   │   ├── AuthService.java               Kiểm tra mật khẩu và vòng đời token
│   │   │   ├── WorkshopService.java           Validation, giao dịch cấp hộ, quyền sở hữu
│   │   │   └── ProductService.java            Đọc danh mục và chuyển entity thành DTO
│   │   ├── repository/
│   │   │   ├── AccountRepository.java        Truy vấn tài khoản và token qua JDBC
│   │   │   ├── WorkshopRepository.java       Truy vấn và cập nhật xưởng qua JDBC
│   │   │   └── ProductRepository.java        Truy vấn sản phẩm qua Spring Data JPA
│   │   ├── model/
│   │   │   ├── Actor.java                    Danh tính đã xác thực
│   │   │   ├── Product.java                  Entity sản phẩm
│   │   │   ├── Category.java                 Entity loại sản phẩm
│   │   │   └── Workshop.java                 Entity xưởng cho luồng đọc danh mục
│   │   ├── dto/
│   │   │   ├── LoginRequest.java              Dữ liệu đăng nhập
│   │   │   ├── LoginResponse.java             Token và vai trò trả về
│   │   │   ├── NewArtisanRequest.java          Dữ liệu cấp tài khoản hộ
│   │   │   ├── WorkshopUpdateRequest.java      Dữ liệu sửa tên xưởng
│   │   │   ├── WorkshopResponse.java         Phản hồi thao tác xưởng
│   │   │   └── ProductResponse.java          Phản hồi danh mục và chi tiết
│   │   ├── security/
│   │   │   ├── BearerTokenFilter.java         Nhận diện người dùng qua token
│   │   │   └── BearerTokens.java              Đọc header và băm token
│   │   └── exception/ApiErrors.java          Chuyển lỗi nghiệp vụ thành HTTP và JSON
│   ├── src/main/resources/
│   │   ├── application.properties           Thiết lập Spring và kiểm tra schema JPA
│   │   └── db/
│   │       ├── migration/                    Flyway V1, V2 và V3
│   │       └── seed/catalog.sql              Dữ liệu danh mục cho local/demo
│   └── tests/phase02.py                      Kiểm thử HTTP, quyền và duyệt hộ
├── frontend/
│   ├── package.json, package-lock.json        Thư viện React và Vite
│   ├── vite.config.js                        Chuyển tiếp /api sang cổng 8080
│   ├── src/main.jsx, src/styles.css           Router, component và giao diện
│   └── public/images/                        Ảnh minh họa mẫu
├── postman/                                  Collection và Environment mẫu
└── README.md
```

## 2. Workflow xử lý

| Luồng | Các bước |
|---|---|
| Khởi động | `VCraftApplication` → kết nối PostgreSQL → Flyway áp dụng V1–V3 → JPA kiểm tra schema → profile `local` hoặc `demo` nạp danh mục rồi tài khoản mẫu. |
| Xem danh mục | React Fetch `/api/products` → Vite proxy → `ProductController` → `ProductService` → `ProductRepository` → PostgreSQL. Service chuyển entity thành `ProductResponse` và controller trả JSON. |
| Đăng nhập | `AuthController` → `AuthService` → `AccountRepository`. Service kiểm tra mật khẩu bằng BCrypt, cấp token và chỉ lưu mã băm token trong database. |
| Request đã đăng nhập | `BearerTokenFilter` đọc header, kiểm tra phiên còn hạn và trạng thái xưởng, đưa danh tính vào SecurityContext. `SecurityConfig` kiểm tra vai trò theo đường dẫn rồi chuyển request vào controller. |
| Admin cấp và duyệt hộ | `AdminWorkshopController` → `WorkshopService` → `WorkshopRepository` và `AccountRepository`. Tạo xưởng chờ duyệt và tạo tài khoản diễn ra trong cùng một giao dịch. Admin duyệt xưởng để hộ đăng nhập. |
| Artisan sửa xưởng | `ArtisanWorkshopController` → `WorkshopService`. Service so ID trong URL với ID xưởng của người đăng nhập. Khớp thì `WorkshopRepository` cập nhật tên trong PostgreSQL, sai thì trả 403. |

## 3. Chạy local theo thứ tự

### Bước 1 — PostgreSQL

Bật PostgreSQL 16. Trong pgAdmin tạo database `vcraft_local`, thay đổi mật khẩu trong phần application.properties

### Bước 2 — Backend bằng IntelliJ

1. Mở thư mục `backend/` chứa `pom.xml`, chọn JDK 17 và **Reload Maven**.
2. Chạy `VCraftApplication.java`. Chờ log `Started VCraftApplication`, mở http://localhost:8080/api/health/db và kiểm tra `{"status":"UP"}`.

### Bước 3 — Frontend bằng Vite

Giữ Spring Boot chạy ở cổng 8080. Mở terminal riêng tại thư mục gốc, dùng Node.js 22.12+ và chạy:

```powershell
cd frontend
npm ci
npm run dev
```
nếu không được thì mở terminal trong intellij và mở frontend và nhập
```
npm ci
npm run dev
```

Mở http://localhost:5173 hoặc địa chỉ Vite in ra. Kiểm tra trang chủ → danh mục → chi tiết.

### Bước 4 — Demo quyền trong Postman

Import Collection và Environment trong `postman/`, chọn môi trường **vCraft local**, chạy folder **Phase 02 — Tài khoản và quyền xưởng** theo thứ tự. Tài khoản mẫu gồm `buyer`, `admin`, `xuong-men-lam`, `xuong-dat-nung`, cùng mật khẩu demo `VcraftDemo17!`.

1. Đăng nhập hai hộ và gọi GET /api/me để lấy ID hai xưởng.
2. Dùng token hộ A sửa xưởng A, mong đợi 200. Giữ nguyên token và sửa xưởng B, mong đợi 403.
3. Admin cấp tài khoản hộ mới, mong đợi 201. Hộ đăng nhập trước duyệt nhận 401. Admin duyệt, hộ đăng nhập thành công.
4. Đăng xuất, mong đợi 204. Dùng lại token đó ở GET /api/me, mong đợi 401.

## 4. Kiểm thử

Từ thư mục gốc, chạy `python backend/tests/phase02.py` khi backend đang chạy profile local. Script tạo một xưởng thử và khôi phục tên xưởng mẫu sau khi kiểm tra quyền.

Kết quả lần tái cấu trúc ghi ở cuối README. Môi trường phát triển chính vẫn là PostgreSQL 16. Trong môi trường kiểm thử dùng PGlite qua JDBC với cấu hình khóa Flyway cấp phiên và tắt server-prepared statements chỉ cho phép thử.

- PASS biên dịch Java 17 bằng ECJ và khởi động Spring Boot 4.1.2.
- PASS Flyway V1–V3 trên database thử trống và JPA validate.
- PASS ca HTTP cho đăng nhập, phân quyền vai trò, sở hữu xưởng, duyệt hộ và thu hồi token.
- PASS danh mục, chi tiết sản phẩm, lỗi 400 và 404.
- Chưa chạy lại IntelliJ/Maven, PostgreSQL 16 chuẩn hoặc Postman GUI trong lượt tái cấu trúc. Dùng hướng dẫn local ở trên để kiểm tra trên máy nhóm.
