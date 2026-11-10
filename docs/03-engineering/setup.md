# Cài đặt và chạy

Yêu cầu: JDK 17, Node.js 22.12+, PostgreSQL 16 (hoặc Docker Desktop).

## Cách 1 — Docker Compose (toàn bộ hệ thống)

Từ thư mục gốc repo:

```bash
cp .env.example .env
```

```bash
docker compose up --build
```

- Frontend: http://localhost:5173 · Backend: http://localhost:8080/api/health/db
- Xóa sạch dữ liệu để seed lại: `docker compose down -v`

## Cách 2 — Chạy rời để phát triển

1. **Database**: tạo database `vcraft_local` trong PostgreSQL 16. Thông tin kết nối ở `backend/src/main/resources/application.properties` (có thể ghi đè bằng biến môi trường `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
2. **Backend** (thư mục `backend/`):

   ```bash
   ./mvnw spring-boot:run
   ```

   Trên PowerShell dùng `.\mvnw.cmd spring-boot:run`, hoặc chạy `VCraftApplication` trong IntelliJ. Flyway tự áp migration; profile `local` tự nạp dữ liệu mẫu.
3. **Frontend** (thư mục `frontend/`):

   ```bash
   npm ci
   ```

   ```bash
   npm run dev
   ```

   Mở http://localhost:5173. Vite proxy `/api` sang cổng 8080.

## Tài khoản mẫu (profile `local`/`demo`)

`buyer`, `admin`, `xuong-men-lam`, `xuong-dat-nung` — mật khẩu demo chung xem ở `README.md` gốc / `DemoAccounts.java`. Chỉ dùng cho môi trường cục bộ.

## Lệnh thường dùng

| Việc | Lệnh | Chạy ở |
|---|---|---|
| Biên dịch + đóng gói backend | `./mvnw -B package` | `backend/` |
| Build frontend | `npm run build` | `frontend/` |
| Kiểm thử HTTP (cần backend đang chạy profile local) | `python backend/tests/phase02.py` | gốc repo |
| Postman | import 2 file trong `postman/` | — |

## Lưu ý

- Hibernate chạy `ddl-auto=validate`: entity lệch schema thì backend không khởi động — đây là chủ ý.
- Sửa một migration đã chạy sẽ làm Flyway báo lệch checksum. Ở máy cá nhân có thể xóa database rồi tạo lại; trên nhánh chính thì **không** sửa migration cũ.
- Các lệnh trên lấy từ README và cấu hình của repo; nếu một lệnh không chạy trên máy bạn, sửa tài liệu này ngay khi tìm ra cách đúng.
