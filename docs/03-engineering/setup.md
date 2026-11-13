# Cài đặt và chạy local — M1

Hướng dẫn từng bước đầy đủ ở [README](../../README.md), mục 2. Bản này ưu tiên IntelliJ + PostgreSQL + Vite, không cần Docker.

1. Cài JDK 17, Node 22 LTS, PostgreSQL 16. Tạo database mới trống, giữ DB cũ nguyên vẹn.
2. Open backend/pom.xml, Reload Maven, đặt các JDK trong IntelliJ là 17.
3. Run VCraftApplication với Working directory là backend và biến SPRING_PROFILES_ACTIVE=local, VCRAFT_DB_PASSWORD là mật khẩu DB của bạn. VCRAFT_DB_URL mặc định jdbc:postgresql://localhost:5432/vcraft_local, VCRAFT_DB_USER mặc định postgres. Có thể dùng file private theo mẫu ở backend thay cho password env.
4. Chờ Flyway V1–V3, JPA validate, seed; gọi http://localhost:8080/api/health/db.
5. Terminal khác vào frontend chạy npm ci rồi npm run dev, mở http://localhost:5173.

Không đặt local mặc định trong cấu hình chung. File .env không được Spring Boot tự đọc. Dùng profile demo riêng thì truyền đủ SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME và SPRING_DATASOURCE_PASSWORD; demo bật seed giống local. Không bật local/demo ở môi trường dữ liệu thật.

Tài khoản: buyer, admin, xuong-men-lam, xuong-dat-nung, mật khẩu demo trong README.

| Việc | Lệnh | Thư mục |
|---|---|---|
| Backend dev | ./mvnw spring-boot:run hoặc .\mvnw.cmd spring-boot:run | backend |
| Backend build | ./mvnw clean package hoặc .\mvnw.cmd clean package | backend |
| Frontend | npm ci rồi npm run dev | frontend |
| Frontend build | npm run build | frontend |
| Kiểm tra Phase02 và M1 | python backend/tests/m1.py | gốc; DB test riêng, backend đã chạy |
| Postman | Import hai JSON, chọn vCraft local, mở folder M1 trong Collection | Postman |

Chỉ thay schema bằng migration mới V4 trở đi; không sửa checksum V1–V3, không bật Hibernate update, không baseline schema thủ công chưa đối chiếu. Trước khi chuyển dữ liệu cũ cần sao lưu và so schema. Các file Compose/Docker có sẵn được giữ như tài nguyên cũ tùy chọn; chưa kiểm tra Docker trong chặng này.
