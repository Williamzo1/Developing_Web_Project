# Cài đặt và chạy local — M1

Hướng dẫn từng bước đầy đủ ở [README](../../README.md), mục 2. Bản này ưu tiên IntelliJ + PostgreSQL + Vite, không cần Docker.

1. Cài JDK 17, Node 22 LTS, PostgreSQL 16. Tạo database mới trống, giữ DB cũ nguyên vẹn.
2. Open backend/pom.xml, Reload Maven, đặt các JDK trong IntelliJ là 17.
3. Run VCraftApplication với Working directory là backend và biến SPRING_PROFILES_ACTIVE=local, VCRAFT_DB_PASSWORD là mật khẩu DB của bạn. VCRAFT_DB_URL mặc định jdbc:postgresql://localhost:5432/vcraft_local, VCRAFT_DB_USER mặc định postgres. Có thể dùng file private theo mẫu ở backend thay cho password env.
4. Chờ Flyway V1–V3, JPA validate, seed; gọi http://localhost:8080/api/health/db.
5. Terminal khác vào frontend chạy npm ci rồi npm run dev, mở http://localhost:5173.

Không đặt local mặc định trong cấu hình chung. File .env không được Spring Boot tự đọc. Dùng profile demo riêng thì truyền đủ SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME và SPRING_DATASOURCE_PASSWORD; demo bật seed giống local. Không bật local/demo ở môi trường dữ liệu thật.

Tài khoản: buyer, admin, xuong-men-lam, xuong-dat-nung, mật khẩu demo trong README.

## Chạy bằng Docker Compose (cả ba dịch vụ)

Cần Docker Desktop (Windows: bật WSL2). Không cần cài JDK, Node hay PostgreSQL cho cách này.

1. Sao `.env.example` thành `.env` ở thư mục gốc, đặt `POSTGRES_PASSWORD`. `.env` bị git bỏ qua; Compose tự đọc file này.
2. `docker compose up -d --build` ở thư mục gốc. Lần đầu mất vài phút để tải dependency.
3. Mở http://localhost:5173 (Nginx phục vụ giao diện và chuyển `/api/`, `/media/` sang backend). Backend trực tiếp: http://localhost:8080.
   Nếu backend báo `password authentication failed for user "vcraft"`: volume DB đã được tạo với mật khẩu cũ, còn `.env` đang có mật khẩu khác (thường do sao đè `.env`). Chỉ đặt `POSTGRES_PASSWORD` khi chạy lần đầu; sau đó đổi mật khẩu trong DB bằng `ALTER USER`, hoặc xóa volume bằng `docker compose down -v` nếu không cần dữ liệu.
4. Dừng: `docker compose down`. Thêm `-v` để xóa cả dữ liệu DB và ảnh upload.

Compose chạy profile `local` nên có tài khoản và dữ liệu mẫu (xem README). Dữ liệu DB nằm ở volume `catalog_data`, ảnh upload ở `media_data`. Khi triển khai có tên miền, đặt `APP_BASE_URL` thành địa chỉ đó. Nginx cho phép body tới 60 MB để upload ảnh.

### Sao lưu và khôi phục (DB + ảnh)

Cần stack đang chạy (ít nhất `db`). Chạy ở thư mục gốc, bằng Windows PowerShell:

```powershell
.\scripts\backup.ps1
```

Tạo `backups\<yyyyMMdd-HHmmss>\` gồm `db.dump` (pg_dump) và `media.tgz` (volume ảnh); mặc định giữ 14 bản gần nhất (`-Keep N`). Thư mục `backups/` bị git bỏ qua. Khôi phục (ghi đè dữ liệu hiện tại, có hỏi xác nhận):

```powershell
.\scripts\restore.ps1 -From backups\20261124-103839
```

Để sao lưu định kỳ, dùng Task Scheduler gọi `powershell -File <đường dẫn>\scripts\backup.ps1`. Hãy chép `backups\` sang ổ/máy khác, vì cùng ổ đĩa với Docker thì không phải sao lưu thật. Đã kiểm vòng upload → backup → xóa → restore trên máy dev (2026-11-24).

CI (`.github/workflows/ci.yml`) build backend và frontend ở mỗi push và pull request.

| Việc | Lệnh | Thư mục |
|---|---|---|
| Backend dev | ./mvnw spring-boot:run hoặc .\mvnw.cmd spring-boot:run | backend |
| Backend build | ./mvnw clean package hoặc .\mvnw.cmd clean package | backend |
| Frontend | npm ci rồi npm run dev | frontend |
| Frontend build | npm run build | frontend |
| Kiểm tra Phase02 và M1 | python backend/tests/m1.py | gốc; DB test riêng, backend đã chạy |
| Postman | Import hai JSON, chọn vCraft local, mở folder M1 trong Collection | Postman |

Chỉ thay schema bằng migration mới V7 trở đi; không sửa checksum V1–V6, không bật Hibernate update, không baseline schema thủ công chưa đối chiếu. Trước khi chuyển dữ liệu cũ cần sao lưu và so schema. Các file Compose/Docker có sẵn được giữ như tài nguyên cũ tùy chọn; chưa kiểm tra Docker trong chặng này.

Backend báo giá: xem [quote-backend.md](quote-backend.md). Chạy terminal `.\run-local.cmd` ở backend để tự chọn profile local, không cần Edit Configurations; giữ file private ngang pom.xml.
