# Bản sửa lỗi cú pháp — 2026-11-18

Nguồn: vCraft-Quote-backend (2).zip do bạn gửi. Chỉ khôi phục mã bị mất và sửa cách nạp cấu hình local; không đổi Spring Boot 4.1.2, Maven Wrapper 3.9.11, Java 17, API hoặc migration.

## Lỗi tìm thấy

WorkshopRepository.java có SQL SELECT/INSERT nằm trực tiếp trong thân hàm, không còn lời gọi JdbcTemplate và chuỗi Java bao quanh. Hai hàm approve/rename trả kết quả nhưng bị rỗng; hàm updateContact được service gọi cũng đã mất. Bản đầu Maven báo 26 lỗi cú pháp liên tiếp từ file này.

Đã khôi phục:

- list(): SELECT xưởng/tài khoản, map ResultSet sang WorkshopResponse.
- createPending(): INSERT tham số hóa, trả xưởng chưa duyệt.
- approve(): UPDATE trạng thái duyệt, trả Optional.
- rename(): UPDATE tên xưởng đã duyệt, trả Optional.
- updateContact(): UPDATE email/phone của xưởng đã duyệt, trả ContactResponse.

Quyền sở hữu và vai trò vẫn được kiểm tra ở service/Security như source của bạn. Không đưa SQL ra controller và không bỏ các kiểm tra nghiệp vụ để làm build chạy qua.

## Cấu hình local

application-local.properties bị thiếu import file private, nên file password riêng trong resources không được nạp tự động. Đã thêm import hai vị trí file ngoài resources, để chạy khi Working directory là backend hoặc thư mục gốc.

Sau khi giải nén:

1. Copy backend/application-local-private.properties.example thành backend/application-local-private.properties (ngang pom.xml).
2. Điền URL/user/password PostgreSQL của bạn. Nếu dùng file private từ project cũ, chuyển/copy file đó từ src/main/resources ra backend. Không gửi mật khẩu trong chat.
3. Open backend/pom.xml bằng IntelliJ, Reload Maven, chọn JDK 17. Bấm Run VCraftApplication hoặc chạy `.\run-local.cmd` tại backend. Giữ profile local mà source của bạn đã bật sẵn.
4. Gọi GET http://localhost:8080/api/health/db.

ZIP không chứa mật khẩu thật. Không đổi database cũ hoặc tự chạy SQL tạo lại schema; Flyway giữ V1–V4 nguyên vẹn. Build và test trong lần sửa này dùng PostgreSQL mới tách biệt.

## Kết quả kiểm tra thực tế

| Bước | Kết quả |
|---|---|
| Backend Maven clean package | PASS, Zulu JDK 17.0.12, Maven 3.9.11, 62 source Java compile release 17, executable jar tạo thành công |
| Frontend npm ci + npm run build | PASS, Vite 7.2.2; cảnh báo use client từ React Router không chặn build |
| Python test files, JSON, pom.xml | Parse hợp lệ |
| JavaScript trong Postman | node --check hợp lệ, 36 script |
| Khởi động trên PostgreSQL 16.15 thật | PASS, Flyway V1–V4 và JPA validate |
| Phase02 và M1 | PASS: đăng nhập/role/owner, cấp/duyệt hộ, sửa xưởng, token, validation và phân trang |
| Backend báo giá | PASS: 147 kiểm tra HTTP qua helper, cùng các ca cạnh tranh riêng |
| Khóa dòng/đồng thời | PASS: 10 vòng hai accept có đúng một 200 và một 409; accept/edit, accept/withdraw, cancel/submit, gửi trùng và sửa nháp đồng thời; giữ khóa bằng kết nối PG riêng và kiểm tra unique index |
| Import cấu hình private | PASS khi khởi động từ backend và từ thư mục gốc; health/db trả UP, không dựa vào datasource env để né việc import file |

Maven build không có JUnit test; kiểm tra nghiệp vụ là các script HTTP/SQL đã chạy riêng nêu trên. Chưa kiểm tra GUI IntelliJ Windows hoặc render UI trong trình duyệt.

## Đóng gói

Không đóng .git, .idea, *.iml, target, node_modules, dist, log, database hoặc file private chứa mật khẩu. Cấu hình IDE và module sẽ được IntelliJ tạo lại từ pom.xml khi import. Các file source khác, chỉnh sửa frontend của bạn và toàn bộ SQL migration được giữ nguyên.
