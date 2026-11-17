<!-- NGUỒN GỐC - KHÔNG SỬA. Trích tự động từ original/ghi-chu-nhom.docx (All in one.docx) ngày 2026-11-10; bảng bị dàn phẳng thành từng dòng. -->

API contract: ??  
Github workflow: ??  
Thiết kế ERD ??  
Thiết kế kiến trúc Backend. ??  
Thiết kế UI/UX và wireframe. ??  
Thống nhất coding conventions ??.  
Setup Spring Boot và Frontend. ??  
Setup database ??  
Cấu hình môi trường development ??  
Tạo skeleton project và API mẫu ??  
Danh sách feature cần hoàn thành ??  
Merge và testing ??  
Deploy??  
Viết slide ??  
  
1. Bản ZIP đã làm được gì so với đề?  
Yêu cầu trong đề  
Hiện trạng trong vCraft-v0(1).zip  
Triển lãm số và xem sản phẩm  
Đã có. Trang chủ chủ đề gốm Bát Tràng, danh mục và trang chi tiết. Danh mục và chi tiết lấy dữ liệu qua API. Có ảnh minh họa và giao diện thích ứng.  
Lọc danh mục theo hộ hoặc loại sản phẩm  
Có nền dữ liệu. Mỗi sản phẩm đã liên kết với xưởng và loại, giao diện cũng hiển thị hai thông tin này. API hiện trả danh sách chung, chưa có bộ lọc trên giao diện.  
Hồ sơ xưởng và đăng sản phẩm  
Có dữ liệu cơ bản. Đã có bảng workshops và products. Chưa có trang hồ sơ xưởng riêng, lịch sử gia đình, chứng nhận hoặc màn hình cho hộ tự đăng và sửa sản phẩm.  
Nhật ký sản xuất, lô và QR  
Chưa có. Chưa có bảng lô, mốc sản xuất hay trang truy xuất bằng QR.  
Bản đồ tương tác  
Chưa có. Chưa có tọa độ, ghim xưởng, giờ tham quan hoặc trạng thái mở cửa.  
Yêu cầu báo giá, chào giá và điều phối  
Chưa có. Chưa có luồng Buyer gửi yêu cầu, hộ chào giá, Buyer chọn và hộ cập nhật tiến độ.  
Tài khoản và quản trị  
Chưa có. Chưa có đăng nhập, phân quyền, duyệt hộ hoặc quản lý tin cộng đồng.  
Tải nhiều ảnh và tối ưu ảnh phía máy chủ  
Chưa có. Ảnh hiện tại là SVG có sẵn trong frontend/public/images/. Backend trả đường dẫn ảnh.  
 |  
Huy- Điều phối Frontend (UI/UX): Chịu trách nhiệm giao diện người dùng, xây dựng khung giao diện  
  
Thắng- Điều phối Backend & Database: Thiết kế cơ sở dữ liệu, viết các API, xử lý logic nghiệp vụ, bảo mật và quản lý server (Node.js, Python, Java...). Thiết kế khung backend  
  
Dương- Fullstack / Tích hợp (Người kết nối): Hỗ trợ code cả Frontend hoặc Backend khi một trong hai bên bị quá tải, kiểm thử (QA/Testing), và lo việc deploy (đưa web lên host/server).Cấu hình môi trường development  
  
Sau khi setup được cơ bản → Mỗi người sẽ được chia và làm theo feature  
  
Phase 1 – Requirement AnalysisTuần 1  
Thống nhất yêu cầu và chức năng.  
Xác định actors, use cases.  
Viết user stories.  
Chia chức năng thành các modules.  
Phase 2 – System DesignTuần 1–2  
Thiết kế ERD.  
Thiết kế kiến trúc Backend.  
Thiết kế API contract.  
Thiết kế UI/UX và wireframe.  
Thống nhất coding conventions.  
Phase 3 – Environment SetupTuần 2  
Tạo GitHub repository.  
Setup Spring Boot và Frontend.  
Setup database.  
Cấu hình môi trường development.  
Tạo skeleton project và API mẫu.  
Phase 4 – Parallel DevelopmentTuần 3–5  
Mỗi người phát triển feature của mình.  
Tạo branch riêng.  
Viết unit tests.  
Tích hợp từng feature thường xuyên.  
Review code chéo.  
Phase 5 – Integration & TestingTuần 6 - Integration testing. - Kiểm tra API và database. - Kiểm tra các luồng nghiệp vụ end-to-end. - Fix bugs.  
Phase 6 – Deployment & DocumentationTuần 7–8  
Deploy website.  
Viết tài liệu kỹ thuật.  
Chuẩn bị báo cáo và demo.  
  
UX/UI:  
React  
  
Backend:  
SpingBoot(java)  
ProgesSQL  
  
Deployment:  
Docker  
  
https://github.com/Williamzo1/Developing_Web_Project.git  
Đánh giá vCraft v0 – INT3306  
Môn: INT3306 – Phát triển ứng dụng web | Stack: React 19 · Spring Boot 4.1.2 · PostgreSQL 16  
  
Tổng quan điểm mạnh  
Cấu trúc dự án sạch, rõ ràng (frontend / backend / postman / docker).  
Docker Compose hoàn chỉnh, chạy được ngay từ ZIP.  
Flyway migration đúng quy trình, Hibernate ddl-auto=validate an toàn.  
Custom hook useCatalog xử lý loading / error / retry đúng cách.  
Responsive layout, hỗ trợ 390 px mobile.  
Error boundary API trả JSON có code chuẩn.  
  
1. Phân tích theo Tiêu chí đề bài  
1.1 Chức năng vai trò  
Vai trò  
Yêu cầu  
Trạng thái  
Artisan – Quản lý hồ sơ xưởng  
Lịch sử gia đình, hình ảnh, chứng nhận  
❌ Chưa có  
Artisan – Nhật ký sản xuất  
Mảng mốc thời gian + ảnh theo lô  
❌ Chưa có  
Artisan – Báo giá (Bidding)  
Nhận & phản hồi Quote Request  
❌ Chưa có  
Buyer – Danh mục sản phẩm  
Xem theo hộ / phân loại  
✅ Có (read-only, không lọc)  
Buyer – Bản đồ làng nghề  
Interactive Map, pin xưởng  
❌ Chưa có  
Buyer – Tạo yêu cầu báo giá  
Quotation State Machine  
❌ Chưa có  
Admin – Kiểm duyệt tài khoản  
Phê duyệt hộ gia đình  
❌ Chưa có  
Admin – Tin tức cộng đồng  
Thông báo, chính sách  
❌ Chưa có  
→ Chỉ 1/8 chức năng nghiệp vụ được thực hiện (đọc danh mục).  
1.2 Yêu cầu kỹ thuật  
Yêu cầu kỹ thuật  
Trạng thái  
4.1 Nhật ký sản xuất & QR code lô sản phẩm  
❌ Chưa có schema, chưa có UI  
4.2 Quotation State Machine (6 trạng thái)  
❌ Chưa có  
4.3 Bản đồ số (Leaflet / Google Maps, pin tọa độ)  
❌ Chưa có  
4.4 Upload ảnh thực, Image Optimization backend  
❌ Chỉ có SVG tĩnh, không có upload  
 |  
2. Đánh giá Frontend  
Điểm tốt  
Custom hook useCatalog – AbortController chống memory leak, retry state hợp lý.  
Routing SPA – React Router v7, slug tiếng Việt (/san-pham/:id), 404 page riêng.  
CSS thuần – Không dùng thư viện nặng, biến CSS root nhất quán, clamp() responsive.  
Accessibility cơ bản – role="status", role="alert", aria-label nav, lang="vi" trên <html>.  
Image fallback – ProductImage xử lý onError, hiện placeholder thay vì ảnh vỡ.  
encodeURIComponent – An toàn khi truyền id vào URL fetch.  
Vấn đề cần cải thiện  
2.1 Routing dùng id số thay vì slug  
/san-pham/1   ← hiện tại  
/san-pham/coc-men-lam  ← nên dùng slug  
Schema đã có cột slug, backend trả về slug trong ProductResponse, nhưng frontend không dùng. URL không thân thiện SEO, lộ ID nội bộ.  
Sửa: Thêm endpoint GET /api/products/slug/{slug} ở backend, đổi Link sang product.slug, đổi useParams thành { slug }.  
2.2 Không có phân loại / lọc danh mục  
Đề yêu cầu "xem theo Hộ gia đình hoặc theo Phân loại". Hiện chỉ hiển thị flat list toàn bộ sản phẩm, không có filter, không có nhóm theo category hay workshop.  
2.3 Không có hệ thống xác thực / phân quyền UI  
Không có trang đăng nhập, không có guard route, không có context lưu user session. Ba vai trò (Artisan, Buyer, Admin) không được phân biệt trên UI.  
2.4 State management thiếu khi app mở rộng  
useCatalog đủ cho read-only, nhưng khi thêm form, auth, cart, quote — cần Context API hoặc Zustand. Hiện không có chuẩn bị.  
2.5 Không có meta tag SEO / Open Graph  
index.html chỉ có <title> cứng. Thiếu <meta name="description">, không có Open Graph, không có favicon thực.  
2.6 Vite config thiếu plugin React  
// vite.config.js hiện tại  
export default defineConfig({  
  server: { proxy: { '/api': 'http://localhost:8080' } },  
});  
Không có @vitejs/plugin-react. Dự án chạy được vì dùng React 19 RSC transform mode, nhưng thiếu HMR đầy đủ và JSX transform tường minh.  
  
3. Đánh giá Backend  
Điểm tốt  
Spring Boot 4.1.2 + Java 17 – Stack hiện đại, đúng cấu hình đề bài.  
JPA với join fetch – ProductRepository dùng JPQL join fetch tránh N+1 query.  
open-in-view=false – Tránh lazy loading ngoài transaction, thực hành tốt.  
Record DTO – ProductResponse là Java record, bất biến, gọn.  
Error handling có cấu trúc – ApiError(code, message) nhất quán, 404 trả PRODUCT_NOT_FOUND.  
Health endpoints – /api/health và /api/health/db dùng JdbcTemplate, không gây lỗi khi DB down.  
Profile local/demo – Seed data tách biệt, production không bị nhiễm.  
Vấn đề cần cải thiện  
3.1 Không có Authentication & Authorization  
Không có Spring Security, không có JWT/session, không có phân quyền. Mọi endpoint đều public. Với 3 vai trò trong đề, đây là thiếu sót nghiêm trọng.  
Hướng sửa:  
<dependency>  
  <groupId>org.springframework.boot</groupId>  
  <artifactId>spring-boot-starter-security</artifactId>  
</dependency>  
Kết hợp JWT (JJWT) hoặc Spring Session, tạo bảng users với role ENUM('ARTISAN','BUYER','ADMIN').  
3.2 Không có CORS configuration  
Không có @CrossOrigin hay WebMvcConfigurer cấu hình CORS. Trong Compose, Nginx proxy nên OK, nhưng khi dev riêng lẻ hoặc deploy khác origin sẽ bị block.  
3.3 Toàn bộ API là read-only (GET)  
Không có POST, PUT, DELETE. Không thể tạo sản phẩm, không thể cập nhật, không thể quản lý bất kỳ thực thể nào. Đề yêu cầu luồng nghiệp vụ phức tạp (bidding, state machine).  
3.4 Không có Input Validation  
Không có @Valid, @NotBlank, jakarta.validation. Khi có POST endpoint, không có gì ngăn dữ liệu xấu vào database.  
3.5 Package-private class hạn chế test  
ProductController, Product, Category, Workshop đều package-private (không có public). Điều này tốt cho encapsulation nhưng khiến không viết unit test từ ngoài package được. Nên dùng public cho Controller hoặc dùng @VisibleForTesting.  
3.6 Không có Pagination  
findCatalog() trả toàn bộ danh sách. Khi có hàng trăm/ngàn sản phẩm, endpoint này sẽ OOM. Cần Pageable.  
// Nên đổi thành:  
Page<ProductResponse> list(@PageableDefault(size=20) Pageable pageable);  
3.7 Không có logging  
Không có @Slf4j, không có cấu hình logging level. Khó debug khi có lỗi production.  
  
4. Đánh giá Database  
Điểm tốt  
Flyway migration – V1, V2 rõ ràng, có thể rollback lịch sử.  
Foreign key constraints – workshop_id, category_id có REFERENCES, đảm bảo toàn vẹn.  
UNIQUE trên slug – Mỗi bảng có slug UNIQUE, tránh trùng lặp.  
GENERATED BY DEFAULT AS IDENTITY – Dùng SQL standard thay vì SERIAL PostgreSQL-specific.  
Seed idempotent – ON CONFLICT (slug) DO NOTHING cho phép chạy lại không lỗi.  
Named volume – Dữ liệu được persist qua docker compose down/up.  
Vấn đề cần cải thiện  
4.1 Schema quá tối giản so với đề bài  
Schema hiện tại:  
workshops (id, slug, name)  
categories (id, slug, name)  
products (id, workshop_id, category_id, slug, name, description, image_path)  
Schema cần có thêm theo đề:  
-- Người dùng & xác thực  
CREATE TABLE users (  
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,  
  email VARCHAR(255) NOT NULL UNIQUE,  
  password_hash VARCHAR(255) NOT NULL,  
  role VARCHAR(20) NOT NULL CHECK (role IN ('ARTISAN','BUYER','ADMIN')),  
  approved_at TIMESTAMPTZ  
);  
  
-- Hồ sơ xưởng  
CREATE TABLE workshop_profiles (  
  workshop_id BIGINT PRIMARY KEY REFERENCES workshops(id),  
  family_history TEXT,  
  map_lat DOUBLE PRECISION,  
  map_lng DOUBLE PRECISION,  
  is_open BOOLEAN DEFAULT FALSE  
);  
  
-- Lô sản phẩm & nhật ký sản xuất  
CREATE TABLE batches (  
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,  
  product_id BIGINT NOT NULL REFERENCES products(id),  
  qr_code VARCHAR(255) UNIQUE,  
  created_at TIMESTAMPTZ DEFAULT NOW()  
);  
  
CREATE TABLE batch_logs (  
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,  
  batch_id BIGINT NOT NULL REFERENCES batches(id),  
  event_name VARCHAR(160) NOT NULL,  
  event_at TIMESTAMPTZ NOT NULL,  
  image_path VARCHAR(255)  
);  
  
-- Quotation State Machine  
CREATE TABLE quote_requests (  
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,  
  buyer_id BIGINT NOT NULL REFERENCES users(id),  
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',  
  description TEXT,  
  quantity INT,  
  created_at TIMESTAMPTZ DEFAULT NOW()  
);  
  
CREATE TABLE bids (  
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,  
  request_id BIGINT NOT NULL REFERENCES quote_requests(id),  
  artisan_id BIGINT NOT NULL REFERENCES users(id),  
  price NUMERIC(15,2),  
  delivery_days INT,  
  status VARCHAR(20) DEFAULT 'PENDING'  
);  
4.2 Thiếu index hiệu năng  
Chỉ có PRIMARY KEY và UNIQUE index. Với query thực tế cần thêm:  
CREATE INDEX idx_products_workshop ON products(workshop_id);  
CREATE INDEX idx_products_category ON products(category_id);  
CREATE INDEX idx_batch_logs_batch ON batch_logs(batch_id);  
4.3 Workshop thiếu thông tin tọa độ cho bản đồ  
Bảng workshops chỉ có slug và name. Không có latitude, longitude, is_open – các trường bắt buộc cho tính năng bản đồ theo đề 4.3.  
4.4 Không lưu metadata ảnh  
image_path chỉ là VARCHAR(255). Khi có upload thực, cần thêm image_width, image_height, image_size_bytes, image_format để hỗ trợ Image Optimization (đề 4.4).  
4.5 Thiếu audit fields  
Các bảng không có created_at, updated_at. Khó debug, không trace được lịch sử thay đổi.  
  
5. DevOps & Cấu hình  
Điểm tốt  
Compose với healthcheck trên DB, depends_on: condition: service_healthy đảm bảo thứ tự khởi động.  
Nginx phục vụ SPA đúng cách với try_files $uri $uri/ /index.html.  
.env.example chuẩn, không commit credential.  
Bind 127.0.0.1 trên port host, không expose ra internet khi dev.  
Điểm cần cải thiện  
Backend Dockerfile chưa xem được nhưng nếu chỉ dùng JAR, cần multi-stage build để giảm image size.  
Nginx không có cache header cho assets (JS, CSS, ảnh).  
Không có restart: unless-stopped trong Compose – khi container crash không tự restart.  
Chưa có CI/CD – Không có GitHub Actions hay pipeline build.  
  
6. Tóm tắt & Ưu tiên cải thiện  
Mức độ Nghiêm trọng (theo đề bài)  
#  
Vấn đề  
Mức độ  
Ảnh hưởng điểm  
1  
Chưa có Authentication/Authorization  
🔴 Nghiêm trọng  
Rất cao  
2  
Chưa có Quotation State Machine  
🔴 Nghiêm trọng  
Rất cao  
3  
Chưa có Nhật ký sản xuất & QR code  
🔴 Nghiêm trọng  
Cao  
4  
Chưa có Bản đồ số (Map + Pins)  
🔴 Nghiêm trọng  
Cao  
5  
Chưa có Upload ảnh thực & Image Optimization  
🟠 Quan trọng  
Cao  
6  
Chỉ có GET, không có POST/PUT/DELETE  
🟠 Quan trọng  
Cao  
7  
Schema DB thiếu ~6 bảng so với yêu cầu  
🟠 Quan trọng  
Cao  
8  
Routing dùng ID thay slug  
🟡 Trung bình  
Thấp  
9  
Không có filter/group danh mục  
🟡 Trung bình  
Trung bình  
10  
Thiếu Pagination  
🟡 Trung bình  
Trung bình  
11  
Thiếu CORS config, Input Validation  
🟡 Trung bình  
Thấp  
12  
Không có audit fields (created_at)  
🟢 Nhỏ  
Thấp  
13  
Vite thiếu plugin React tường minh  
🟢 Nhỏ  
Rất thấp  
Lộ trình đề xuất (3 sprint)  
Sprint 1 – Nền tảng (1 tuần)  
Thêm bảng users, Spring Security + JWT  
Đăng ký / đăng nhập, guard route frontend  
Thêm POST /api/products, POST /api/workshops  
Input validation với @Valid  
Sprint 2 – Nghiệp vụ cốt lõi (1 tuần)  
Bảng batches, batch_logs, UI nhật ký sản xuất  
QR code generation (thư viện zxing)  
Bảng quote_requests, bids, API state machine  
Tích hợp Leaflet.js + tọa độ xưởng trên bản đồ  
Sprint 3 – Media & Hoàn thiện (1 tuần)  
Upload ảnh thực (Multipart), lưu file server hoặc S3  
Image resize backend (Thumbnailator hoặc ImageIO)  
Pagination danh mục  
Filter theo category, workshop  
UI Admin dashboard  
  
