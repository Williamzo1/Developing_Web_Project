# Kết quả kiểm thử M1 Backend

Ngày: 2026-11-12. Kiểm thử trên source của bản ZIP này, không dùng database của người dùng.

| Kiểm tra | Kết quả thực tế | Bằng chứng / giới hạn |
|---|---|---|
| Backend build | PASS | Maven 3.9.11 `clean package`, Zulu JDK 17.0.12, Spring Boot 4.1.2; 36 source files compile bằng javac release 17, tạo executable jar. Chưa có JUnit tests: Maven build không được coi là bộ kiểm thử nghiệp vụ. |
| Frontend build | PASS | npm ci theo lockfile; npm run build, Vite 7.2.2, 35 modules. Có cảnh báo use client từ React Router, không chặn build. |
| Khởi động jar trên DB trống | PASS trong môi trường bên dưới | Jar thật chạy bằng JDK 17.0.12, Flyway V1–V3 thành công, JPA validate; local seed có 3 sản phẩm. |
| Hồi quy Phase02 | PASS | Đăng nhập, token/me, vai trò, hai xưởng riêng, sửa xưởng đúng/sai chủ, cấp tài khoản, duyệt, duplicate conflict, đăng xuất thu hồi token. |
| M1 | PASS | Script m1.py chạy cả Phase02; kiểm tra 25 ca hợp đồng lỗi, thêm các assertion phân trang/giới hạn/không trùng trang, giới hạn byte UTF-8, Buyer 200 và Artisan/Admin 403, Guest/token sai 401; API danh mục cũ vẫn trả mảng. |
| Khởi động lại cùng DB | PASS | Trước/sau restart: flyway_schema_history=3, workshops=3, products=3, accounts=5, account_sessions=4; version 1/2/3 đều success=true. Số hộ/tài khoản gồm một hộ do script tạo sau seed. Số bản ghi không đổi sau restart; bộ Phase02/M1 chạy lại thành công. |
| HTTP frontend/backend | PASS | Vite proxy gọi /api/products/page trả JSON thật; mở URL /san-pham/1 trực tiếp trả SPA HTML 200. Không phải kiểm tra render trong trình duyệt. |
| PostgreSQL 16 native của thành viên | CHƯA CHẠY | Môi trường này không cài PostgreSQL server/psql hoặc Docker. Dùng PGlite (PostgreSQL 18.6 chạy WASM) qua TCP và JDBC PostgreSQL trong database mới tách biệt. Đây không thay thế xác minh trên PostgreSQL 16 native. |
| IntelliJ Windows, Postman UI, mobile/browser | CHƯA CHẠY | Đã cung cấp lệnh, JSON Collection/Environment và script HTTP; chưa thao tác GUI IntelliJ/Postman hoặc nghiệm thu hiển thị responsive. |

Maven ban đầu bị DNS/proxy và thiếu JDK; đã cài JDK 17.0.12 riêng cho kiểm thử, cấu hình proxy/truststore riêng của môi trường để build thành công. Không thay pom/version và không đưa cấu hình proxy/truststore đó vào ZIP. Runtime PGlite dùng cấu hình khóa Flyway phù hợp môi trường WASM; cấu hình này không được đưa vào project. Không có kiểm thử đồng thời/giao dịch chốt báo giá trong M1.

## Tự xác minh trên PostgreSQL local

1. Tạo database trống mới, ví dụ vcraft_m1_test; bật local và điền URL/user/password theo README.
2. Run backend; kiểm tra /api/health/db. Từ gốc chạy `python backend/tests/m1.py` (script có tạo dữ liệu hộ test, không dùng DB dữ liệu thật).
3. Trong pgAdmin chạy `SELECT version,success FROM flyway_schema_history ORDER BY installed_rank;`, đếm workshops/products/accounts. Tắt và chạy lại backend, so số bản ghi **trước khi** chạy lại script test. Seed không nhân đôi; mỗi lần chạy test sẽ tạo một hộ mới riêng.
4. Chạy React rồi làm trang chủ → danh mục → chi tiết; mở trực tiếp URL chi tiết. Muốn nhìn rõ nút phân trang với 3 seed, có thể thử API size=1 trong Postman; UI mặc định 12 sản phẩm/trang.
5. Import hai file Postman; chọn vCraft local, chạy folder M1 rồi Phase02. Không copy token_hash từ database.

Các log nội bộ không được đóng vào ZIP vì có thể chứa thông tin phiên. ZIP chứa source + script + tài liệu và dữ liệu mẫu; không chứa target, node_modules, dist, database, .git hay file private.
