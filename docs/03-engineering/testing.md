# Kiểm thử

## Hiện trạng

- `backend/tests/phase02.py`: script Python gọi HTTP vào backend đang chạy (profile `local`), kiểm đăng nhập, phân quyền vai trò, quyền sở hữu xưởng, duyệt hộ, thu hồi token, danh mục. Địa chỉ đổi bằng biến `VCRAFT_TEST_URL`.
- `postman/`: collection + environment để thử tay.
- Chưa có: test JUnit trong `backend/src/test`, test frontend, CI.

## Chiến lược (đề xuất)

Ưu tiên theo giá trị cho buổi demo, không chạy theo độ phủ:

| Tầng | Công cụ | Dùng cho |
|---|---|---|
| Kiểm thử HTTP đầu-cuối trên backend thật + PostgreSQL thật | Tiếp tục kiểu script `backend/tests/*.py` (mỗi module một file) hoặc chuyển sang test tích hợp Spring — nhóm chọn một, ghi ADR | Phân quyền, quyền riêng tư, luồng báo giá, upload ảnh |
| Unit test | JUnit (cần thêm `spring-boot-starter-test`) | Logic thuần: bảng chuyển trạng thái, tính `openNow` từ giờ mở cửa, validate |
| Thử tay theo kịch bản | `04-plan/demo-script.md` | Giao diện, QR bằng điện thoại thật, responsive |

## Ca kiểm bắt buộc cho mỗi endpoint mới

1. Đường chính thành công.
2. Chưa đăng nhập → 401. Sai vai trò → 403.
3. **Sai chủ sở hữu** (hộ A thao tác dữ liệu hộ B; Buyer A đọc yêu cầu của Buyer B) → 403 hoặc 404 theo `roles-permissions.md`.
4. Đầu vào xấu → 400 với `code` đúng.
5. Với dữ liệu riêng tư: xác nhận nó **không xuất hiện** trong các endpoint danh sách/công khai.

Riêng luồng báo giá: xem danh sách ở `02-design/quote-state-machine.md` mục 5, gồm ca hai request chấp nhận song song.

## Quy tắc

- Test phải tự dọn hoặc dùng dữ liệu có tên ngẫu nhiên để chạy lại nhiều lần được (script hiện tại đã làm vậy).
- Test hỏng thì sửa nguyên nhân; không xóa hay nới lỏng assertion để cho qua.
- Khi báo "đã xong", ghi rõ đã chạy những kiểm thử nào và kết quả; phần nào chưa kiểm được thì nói thẳng.
