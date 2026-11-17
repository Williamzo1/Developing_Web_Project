# Kiểm thử backend báo giá

Ngày 2026-11-13, source vCraft-Quote-backend. Không tác động database của người dùng. Kết quả dưới đây đã chạy thật, không suy ra từ đọc code.

| Kiểm tra | Kết quả | Môi trường/bằng chứng |
|---|---|---|
| Backend clean package | PASS | Zulu JDK **17.0.12**, Maven **3.9.11**, Spring Boot **4.1.2**, javac release 17; jar thực thi tạo thành công. Maven không có JUnit test; không coi package là kiểm thử nghiệp vụ. |
| Frontend build | PASS | npm ci theo lockfile và Vite 7.2.2 build, 35 modules. Giữ UI danh mục từ M1, chưa có UI báo giá. Cảnh báo use client của React Router không chặn build. |
| PostgreSQL thật | PASS | PostgreSQL **16.15 native Linux**, kết nối JDBC, transaction và khóa dòng thật; không dùng H2/PGlite cho chặng này. Server/port/database thử riêng, dừng sau test. |
| Khởi tạo trên DB trống | PASS | V1–V4 migrate và JPA validate, local seed hoạt động. |
| Nâng cấp database M1 | PASS | Khởi động jar M1 trên DB mới để có V1–V3 + seed + một token Buyer; dừng M1, chạy jar mới, chỉ thêm V4; giữ IDs/nội dung sản phẩm, tài khoản/password hash cũ và token vẫn gọi /api/me thành công. Không xóa DB hoặc baseline. |
| Hồi quy Phase02 và M1 | PASS | scripts m1.py/phase02.py: đăng nhập, vai trò, sở hữu xưởng, cấp/duyệt, logout, lỗi chuẩn, validation, phân trang, vùng Buyer. |
| Luồng báo giá | PASS | quotes.py: **147 kiểm tra HTTP qua helper**, cộng các request cạnh tranh; nháp/sửa/xóa, gửi, targeted/open tag, báo giá/sửa/rút/gửi lại, chọn, sản xuất/giao, hủy, liên hệ, privacy, lỗi/version, lịch sử và deadline. |
| Hai accept song song | PASS | 10 vòng, mỗi vòng hai HTTP request khác nhau cùng Buyer/cùng request/hai quote: đúng một 200 và một 409 INVALID_STATE_TRANSITION; DB chỉ một ACCEPTED, một NOT_SELECTED. |
| Tranh chấp khác | PASS | accept vs edit; accept vs withdraw; cancel vs submit; gửi quote trùng đồng thời; hai PUT nháp dùng cùng version. Chỉ kết quả hợp lệ commit, thao tác cũ trả 409. |
| Chứng minh chờ khóa | PASS | Một kết nối PostgreSQL giữ SELECT ... FOR UPDATE dòng cha; hai HTTP accept được phát đồng thời vẫn chưa hoàn thành khi giữ khóa; release/commit thì đúng một 200 và một 409. |
| Chốt chặn database | PASS | Query kiểm tra không có request hai quote ACCEPTED; accepted_quote_id thuộc đúng request; chỉ một event ACCEPTED; thử UPDATE thủ công quote thua thành ACCEPTED bị SQLSTATE 23505 từ index quotes_one_accepted_per_request, rollback ca thử. |
| Khởi động lại | PASS | Trước/sau restart cùng DB: accounts=6, products=3, workshops=3, tags=3, product_tags=6, requests=21, request_tags=20, quotes=29, events=94, sessions=10; Flyway 4 bản đều success. Số tài khoản/hộ/request gồm dữ liệu do script tạo; mọi số lượng giữ nguyên qua restart. |
| Collection Postman qua Newman | PASS | Hai folder báo giá chạy thật trên backend/PostgreSQL: 29 requests, 36 assertions, 0 lỗi. Không xuất environment chứa token vào ZIP. |
| Postman UI / IntelliJ Windows / UI báo giá | CHƯA CHẠY | Đã chạy Collection bằng CLI, chưa thao tác GUI Windows/Postman hoặc demo giao diện báo giá. |

Công cụ build/PG/JDK được tải riêng phục vụ test; không thêm chúng hoặc cấu hình proxy/truststore của môi trường vào ZIP. Không đổi phiên bản Spring Boot để né lỗi môi trường.

## Chạy lại trên máy nhóm

Xem quote-backend.md mục 7. Bộ HTTP dùng Python chuẩn; phần SQL/giữ khóa cần cài quote_requirements.txt và VCRAFT_TEST_DB_URL/DB_PASSWORD trỏ cùng DB backend. Database nên là dữ liệu test riêng. Không có DB_URL thì script in SKIP cho phần DB, không báo PASS cho bước chưa chạy.

Postman có folder Quote Backend — Luồng báo giá và Quote Backend — Rút và hủy. Chạy luồng chính trước rồi folder rút/hủy. Kiểm thử song song/expired deadline dùng script quotes.py, không dựa vào việc bấm Postman tuần tự.

Giới hạn chức năng: chưa có UI báo giá, ảnh tham khảo/MEDIA, quản trị tag, badge, đăng ký/xác minh Buyer hoặc liên kết lô; hợp đồng/thanh toán/giao hàng do hai bên tự quản lý. Q-38 và phần rút của Q-31 vẫn theo giả định đã ghi, không tự coi là quyết định mới của nhóm.
