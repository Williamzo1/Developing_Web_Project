# THẮNG — M1 Backend đến hết bước 10

Bản cập nhật ngày 2026-11-24, dựa trên `vCraft-Quote-backend.zip` người dùng cung cấp.
Lịch sử Git trong ZIP có HEAD `83e80c3cdc74a94c9d70a25226a570aebbb5b081`.
ZIP đã chứa các thay đổi chưa commit, nên nội dung ZIP được dùng làm mốc đối chiếu thực tế.
Không commit, push hoặc sửa mã frontend trong lần cập nhật này.

## Đã làm

| Bước | Kết quả |
|---|---|
| 1–5 | Giữ các dependency, V5/V6 và entity có sẵn. Bổ sung identity, constructor/setter của entity để service tạo được dữ liệu. Không sửa migration cũ. |
| 6 | Giữ storage/SlugUtils, sửa tương thích Java 17, giới hạn slug và kiểm tra đường dẫn lưu trữ. |
| 7 | Ghép imports/method repository, bỏ method phiên trùng, giữ rename/contact cũ, thêm tag/filter và sửa chuyển đổi Instant ở JDBC. |
| 8 | Hoàn thiện Auth/Otp, Product, Batch, Workshop, Admin, Home và Media service. Tạo sản phẩm có workshop/category thật, tag và liên kết ảnh có kiểm chủ sở hữu. Lô có create/update, mốc, landing, QR, tạo/thu hồi token. |
| 9 | Đủ controller công khai/artisan/admin và auth, CORS trong SecurityFilterChain, phục vụ ảnh tĩnh. Các handler gọi service, không gọi repository trực tiếp. |
| 10 | Tạo đầy đủ DTO trong danh sách action plan, gồm cả ProductUpdateInput và BatchUpdateInput, có kiểm dữ liệu đầu vào. |

`ArtisanWorkshopProfileController` chứa 8 endpoint hồ sơ dưới `/api/artisan/workshop`.
Hai endpoint cũ `/api/artisan/workshops/{id}` và `/{id}/contact` vẫn nằm ở controller cũ.
Cách này giữ đường dẫn cũ đồng thời tránh ghép nhầm tiền tố `/workshops/workshop` trong đoạn mã mẫu.

Một số đoạn tham chiếu chứa dấu `...`, method chưa có hoặc tên bean khác dự án.
Đã thay các đoạn đó bằng mã thực thi, giữ phân lớp controller → service → repository.
Tài nguyên riêng tư của hộ khác trả 404, theo AGENTS.md.
Các response sản phẩm mới vẫn có workshop/category/imageUrl để frontend cũ đọc được.

## Cách chạy trên máy nhóm

Dùng JDK 17 trở lên với đầy đủ JDK. Cấu hình DB qua
`backend/application-local-private.properties.example` như README hiện có.
Không đưa mật khẩu thật vào Git. Nếu có DB cũ, sao lưu trước khi chạy Flyway.

```bash
cd backend
# Windows dùng .\mvnw.cmd thay cho bash mvnw
bash mvnw -B package
bash mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Từ một terminal khác, ở thư mục gốc dự án và với DB thử:

```bash
python backend/tests/m1.py
python backend/tests/m1_extension.py
python backend/tests/quotes.py
```

`m1_extension.py` tạo sản phẩm/lô/tin/tag, thử khóa rồi mở khóa hộ và thay lựa chọn nổi bật.
Chỉ chạy trên dữ liệu local/demo dùng cho kiểm thử. Các trường hợp sai quyền, input xấu,
ẩn giá, lô riêng tư, token thu hồi, QR, ảnh, OTP, news và phân quyền đều có kiểm tra.
Bộ `m1.py`/`phase02.py` được sửa đúng hai kỳ vọng đã đổi: sai chủ xưởng trả 404,
slug sản phẩm không tồn tại trả 404 thay vì lỗi parse số 400.

Postman: dùng collection/environment hiện có, mở folder `M1 steps 1–10` (54 request mới).
Đăng nhập bằng folder cũ để lấy token, chạy request tạo trước các request phụ thuộc id.
Không chạy toàn bộ folder như một kịch bản tuần tự vì có các thao tác ẩn/khóa/thu hồi.

## Kiểm thử thực tế

- Biên dịch toàn bộ source với source=17, target=17: đạt.
- Maven package: BUILD SUCCESS, không có JUnit test nên HTTP suite được chạy riêng.
- JAR do Maven tạo đã được khởi động và kiểm tra với PostgreSQL 16.6 tạm.
- Flyway và Hibernate được kiểm tra qua khởi động ứng dụng, giữ ddl-auto=validate.
- Bộ Phase02, M1, M1 extension và quote workflow: xem kết quả thật trong
  `verification/m1-http-results.txt` và `verification/m1-build-results.txt`.
- Kiểm thử OTP thành công dùng SMTP tạm chỉ trong test, không thêm endpoint bỏ qua xác thực.
- Các kiểm tra DB/held-lock tùy chọn của quotes.py chưa chạy. Kiểm tra HTTP đồng thời đã chạy.

Môi trường chạy ở đây có JDK 21 nhưng thiếu ct.sym cho `--release 17`.
Vì vậy lệnh đóng gói thực tế dùng các tùy chọn sau, không sửa java.version trong pom.xml:

```bash
bash mvnw -B -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 package
```

Trên máy có JDK đầy đủ, dùng `bash mvnw -B package` thông thường.
Không khẳng định đã chạy ứng dụng trực tiếp trên runtime JDK 17 hoặc đã build frontend.

## Giả định và phần tiếp theo

Theo tài liệu tham chiếu đang dùng, Q-29 dùng giờ tuần/ngày nghỉ, Q-38 dùng tag do Admin
quản lý song song categories, Q-39 dùng OTP email và kiểm định dạng điện thoại.
Đây không phải quyết định nghiệp vụ mới thay nhóm. Không có OTP SMS.
OTP không được log. Nếu SMTP lỗi, đăng ký vẫn lưu tài khoản để có thể gửi lại.
Phải cấu hình MAIL_USERNAME/MAIL_PASSWORD và thử SMTP thực tế khi làm bước 11.

JPEG/PNG đã thử upload/resize/đọc lại. WebP cần ImageIO reader tương ứng,
không thêm dependency codec ngoài danh sách được yêu cầu.
QR/shareUrl dùng app.frontend-url và đường dẫn `/batches/{publicCode}`.
Trang landing frontend là việc M2, chưa có trong frontend hiện tại.
Tìm theo tên, sort tùy chọn, báo cáo vi phạm, đổi mật khẩu và UI các module chưa được làm.
Hồ sơ xưởng/sản phẩm chứa trang đầu các mục liên quan, lấy tiếp qua API phân trang.

Chỉ đánh dấu đã làm đến bước 10. Nhóm cần kiểm bước 11 bằng cấu hình DB/SMTP thật
trước khi chuyển sang M2 theo action plan.

## Nội dung ZIP

ZIP chứa toàn bộ mã nguồn và tài liệu dự án đã cập nhật.
Không kèm .git, target, node_modules, cache Python hoặc cấu hình private chứa thông tin máy cá nhân.
Các file .example, Maven wrapper và script chạy dự án được giữ lại.
Danh sách source/tài liệu khác so với ZIP đầu vào nằm trong `m1-changed-files.txt`.

SHA-256 ZIP đầu vào: `7786e34f640f42cca6eaee7c986f740ee98fc47c3819af7d7a62e5f57b400c99`.
