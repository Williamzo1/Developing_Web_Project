# M1 — Backend Foundation

Phạm vi: chuẩn hóa lỗi, Bean Validation, khung phân trang, quyền riêng Buyer, cấu hình local. Bản này không thay đổi schema; không làm MEDIA hoặc tái cấu trúc frontend. Hướng dẫn chạy, file và workflow: README gốc. Hợp đồng đầy đủ: `../02-design/api-contract.md`.

| Phần | Thực hiện | Kiểm tra |
|---|---|---|
| Lỗi | ApiErrorResponse + ApiErrors + SecurityErrorWriter | 400/401/403/404/405/409/415 cùng fields; log 500 không ra client |
| Bean Validation | starter-validation; @Valid DTO; @Validated service; Utf8Bytes | Username sai, tên xưởng trắng/quá dài, password hơn 72 byte UTF-8 |
| Phân trang | PageQuery bind mặc định; PageResponse generic; JPA Pageable + join fetch + countQuery | Trang 0/1 không trùng, size 0/51 bị từ chối, trang rỗng, legacy array |
| Quyền Buyer | Matcher /api/buyer/** và BuyerController /me | Buyer 200; Guest/token sai 401; Artisan/Admin 403 |
| Local | application.properties chung; application-local.properties; file private mẫu | Không còn mật khẩu thật và không bật local mặc định; Flyway rồi JPA validate |

Quy ước mặc định 12/tối đa 50 thay cho đề xuất cũ 20/100 để chốt hợp đồng nhỏ, phù hợp trang danh mục hiện tại. Đây là giới hạn kỹ thuật, không đổi nghiệp vụ/schema. Sort cố định id tăng dần; chưa triển khai filter/sort tùy ý. API cũ `/api/products` giữ nguyên vì frontend/caller cũ có thể đang dùng; khi bỏ API này cần thống nhất nhóm trước.

Phối hợp: frontend dùng items và metadata trang, ánh xạ fieldErrors theo tên DTO; database giữ V1–V3 nguyên vẹn, V4 dành cho thay đổi schema tiếp theo. API mới có input ghi phải dùng DTO + @Valid và service kiểm quyền; không chỉ ẩn nút trên React. @Validated có hiệu lực khi gọi bean qua proxy Spring, không áp dụng cho self-invocation.

Bằng chứng chấm: Bean Validation hỗ trợ xử lý nhập liệu 5%; Buyer isolation và không lộ lỗi hỗ trợ an ninh 5%; phân trang JSON/fetch không reload hỗ trợ hiệu năng 10%; DTO và phân lớp hỗ trợ phong cách 5%; Page query JPA hỗ trợ tiêu chí CSDL 5%. Đây không phải cam kết đạt điểm tối đa hay hoàn thành toàn bộ các tiêu chí.

Chưa làm: MEDIA (còn phụ thuộc quyết định lưu ảnh), frontend AuthContext/guards, đăng ký Buyer, CRUD mới, báo giá, lô, QR, bản đồ, Internet deploy. Không tick toàn bộ M1 trong roadmap.
