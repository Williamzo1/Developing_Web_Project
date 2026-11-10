# Kiến trúc

> Phần "Hiện trạng" mô tả mã **đang có** (đã đối chiếu ngày 2026-11-10). Phần "Định hướng" là **đề xuất** cho các module chưa làm.

## 1. Tổng thể

```text
Trình duyệt ── React SPA (Vite) ──/api──▶ Spring Boot REST API ──▶ PostgreSQL 16
                    │                          │
                    └── /media (ảnh tĩnh) ◀────┴── thư mục ảnh đã resize (volume)
```

- **Frontend**: React 19, React Router 7, Vite 7, CSS thuần. Khi dev, Vite proxy `/api` sang `localhost:8080`. Khi chạy Compose, Nginx phục vụ bản build và proxy `/api`.
- **Backend**: Spring Boot 4.1.2, Java 17, Spring MVC + Spring Security + Spring Data JPA + JDBC, Flyway. Chỉ trả JSON, không render HTML.
- **Database**: PostgreSQL 16. Schema do Flyway quản lý, Hibernate chỉ `validate`.
- **Triển khai**: Docker Compose gồm `db`, `backend`, `frontend`.

Lý do chọn stack: [ADR-0002](../decisions/0002-stack-va-xac-thuc.md).

## 2. Backend — hiện trạng

Gói gốc `vn.vcraft.backend`, chia theo **lớp**:

| Gói | Trách nhiệm | Quy tắc |
|---|---|---|
| `controller` | Ánh xạ HTTP ↔ DTO. | Không chứa nghiệp vụ, không gọi repository trực tiếp. |
| `service` | Nghiệp vụ, validate, giao dịch (`@Transactional`), kiểm tra quyền sở hữu. | Ném `ResponseStatusException` kèm thông báo tiếng Việt. |
| `repository` | Truy cập dữ liệu. | Hiện dùng lẫn JPA (`ProductRepository`) và JDBC (`AccountRepository`, `WorkshopRepository`). |
| `model` | Entity JPA và `Actor` (danh tính đã xác thực: `id`, `role`, `workshop`). | |
| `dto` | Java record cho request/response. | Không trả entity ra ngoài. |
| `security` | `BearerTokenFilter`, `BearerTokens`. | |
| `config` | `SecurityConfig`, seed dữ liệu demo theo profile `local`/`demo`. | |
| `exception` | `ApiErrors` (`@RestControllerAdvice`) chuyển lỗi thành JSON `{code, message}`. | |

Xác thực hiện có: đăng nhập trả token ngẫu nhiên; DB chỉ lưu mã băm token trong `account_sessions` kèm hạn; mỗi request gửi `Authorization: Bearer <token>`; `BearerTokenFilter` nạp `Actor` vào SecurityContext; đăng xuất xóa phiên. Phân quyền theo đường dẫn trong `SecurityConfig` (`/api/admin/**` → ADMIN, `/api/artisan/**` → ARTISAN), quyền sở hữu kiểm ở service.

## 3. Backend — định hướng (đề xuất)

1. **Giữ kiến trúc phân lớp hiện có**, thêm tiền tố tên theo module để dễ tìm (`BatchController`, `BatchService`, `BatchRepository`…). Chỉ chuyển sang chia gói theo module nếu cả nhóm thống nhất; không tự ý tái cấu trúc.
2. **Truy cập dữ liệu**: mã mới dùng Spring Data JPA; dùng JDBC/native query khi cần khóa dòng hoặc truy vấn tổng hợp. Không viết lại phần JDBC đang chạy nếu không có lý do.
3. **Đường dẫn theo vai trò** (khớp `SecurityConfig` sẵn có):
   - công khai: `GET /api/...`
   - `/api/buyer/**` → BUYER (cần thêm vào `SecurityConfig`)
   - `/api/artisan/**` → ARTISAN
   - `/api/admin/**` → ADMIN
4. **Validation**: thêm `spring-boot-starter-validation`, dùng `@Valid` trên DTO; bổ sung handler trong `ApiErrors` trả `INVALID_INPUT` kèm danh sách trường lỗi.
5. **Lỗi**: chuẩn hóa mã lỗi nghiệp vụ (xem `api-contract.md` mục Lỗi). Hiện `ApiErrors` trả `HTTP_<status>` cho `ResponseStatusException` và 401/403 chỉ có `code`; cần thống nhất dần.
6. **Ảnh** (FR-MEDIA): `POST` multipart → kiểm tra loại/dung lượng → sinh biến thể (thumbnail ~320 px, vừa ~800 px, lớn ~1600 px) bằng thư viện resize (ví dụ Thumbnailator) → lưu qua interface `MediaStorage` (cài đặt mặc định: đĩa cục bộ, giả định Q-13) → ghi bảng `media`. Ảnh được phục vụ như tệp tĩnh có cache dài hạn.
7. **QR** (FR-BATCH-04): mỗi lô có `public_code` ngẫu nhiên, không đoán được. QR mã hóa URL `{APP_BASE_URL}/lo/{public_code}`. `APP_BASE_URL` là biến môi trường để điện thoại quét được khi demo trong LAN. Sinh ảnh QR ở backend (ví dụ ZXing) qua endpoint trả PNG, hoặc ở frontend — chọn một khi làm module, ghi vào ADR.
8. **Luồng báo giá**: toàn bộ chuyển trạng thái đi qua **một** service (`QuoteRequestService`) với bảng chuyển trạng thái tường minh; chi tiết khóa bản ghi ở [quote-state-machine.md](quote-state-machine.md).
9. **Phân trang**: endpoint danh sách nhận `page`, `size` và trả `{items, page, size, total}`.

## 4. Frontend — hiện trạng

- Toàn bộ nằm trong `frontend/src/main.jsx` (~140 dòng) và `styles.css`: hook `useCatalog` (fetch + abort + thử lại), các trang `Home`, `Catalog`, `Detail`, route `/`, `/san-pham`, `/san-pham/:id`.
- Chưa có: đăng nhập, lưu phiên, route guard, form, `@vitejs/plugin-react`.

## 5. Frontend — định hướng (đề xuất)

```text
frontend/src/
├── main.jsx            điểm vào, router
├── api/                client fetch dùng chung (gắn token, xử lý lỗi {code,message}), hàm gọi theo module
├── auth/               AuthContext, RequireRole (route guard), trang đăng nhập/đăng ký
├── components/         thành phần dùng chung (ảnh, thẻ, phân trang, form, trạng thái tải/rỗng/lỗi)
├── features/<module>/  trang và thành phần riêng của từng module (workshops, products, batches, quotes, map, news, admin)
└── styles/             biến CSS (design tokens) và style chung
```

- Trạng thái: React Context cho phiên đăng nhập; dữ liệu server lấy bằng hook fetch. Chỉ thêm thư viện quản lý state khi thật sự cần và cả nhóm đồng ý.
- Bản đồ: Leaflet + OpenStreetMap (không cần khóa API).
- Tuyến đường tiếng Việt không dấu, xem `ui-ux.md`.
- Thêm thư viện mới (UI kit, form, state…) là quyết định của nhóm → ghi ADR trước khi cài.

## 6. Cấu hình và môi trường

- Compose đọc `.env` (mẫu: `.env.example`): `POSTGRES_*`, `SPRING_PROFILES_ACTIVE`, `BACKEND_PORT`, `FRONTEND_PORT`.
- Chạy backend trực tiếp thì dùng `application.properties` (đang trỏ `localhost:5432/vcraft_local`, người dùng `postgres`). **Nợ kỹ thuật**: file này đang chứa mật khẩu DB cục bộ dạng chữ; nên chuyển sang biến môi trường có giá trị mặc định.
- Profile `local`/`demo` nạp `db/seed/catalog.sql` rồi tạo tài khoản mẫu (`DemoData`, `DemoAccounts`).
- Biến sẽ cần thêm: `APP_BASE_URL` (QR), `MEDIA_DIR` (thư mục ảnh).
