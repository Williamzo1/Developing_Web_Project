# Quy ước lập trình

> Trạng thái: **Đề xuất** dựa trên mã hiện có — nhóm duyệt một lần rồi coi như chốt. Quy tắc chung: **làm theo phong cách của mã xung quanh** trước, tài liệu này sau.

## Ngôn ngữ

| Nơi | Ngôn ngữ |
|---|---|
| Định danh trong mã, tên bảng/cột, đường dẫn API, tên nhánh | Tiếng Anh |
| Chữ trên giao diện, `message` lỗi của API, đường dẫn trang frontend (`/san-pham`) | Tiếng Việt |
| Tài liệu, commit, PR, comment trong mã | Tiếng Việt (thuật ngữ kỹ thuật giữ tiếng Anh) |

Tên khái niệm lấy từ [glossary.md](../01-requirements/glossary.md): `workshop`, `product`, `batch`, `quote_request`, `quote`…

## Backend (Java 17, Spring Boot)

- Phân lớp `controller → service → repository`; controller không gọi repository, không chứa nghiệp vụ.
- DTO là `record` trong `dto/`, đặt tên `XxxRequest` / `XxxResponse`. Không trả entity JPA ra API.
- Tiêm phụ thuộc qua constructor. Không dùng Lombok (repo chưa có).
- Nghiệp vụ ghi dữ liệu nằm trong phương thức service có `@Transactional`.
- Kiểm quyền: vai trò ở `SecurityConfig` theo tiền tố đường dẫn; **quyền sở hữu ở service** bằng `Actor` lấy từ `@AuthenticationPrincipal`. Không tin `workshopId`/`buyerId` do client gửi.
- Lỗi: ném `ResponseStatusException` (hoặc exception nghiệp vụ có mã lỗi khi đã chuẩn hóa) với thông báo tiếng Việt; không trả stack trace; không `catch` rồi bỏ qua.
- Validate đầu vào ở biên (DTO + service) trước khi chạm DB.
- Truy vấn danh sách: phân trang, tránh N+1 (`join fetch` hoặc projection).
- Schema chỉ đổi qua migration Flyway mới (xem `02-design/data-model.md` mục 3).
- Log bằng SLF4J ở service cho sự kiện nghiệp vụ quan trọng (chuyển trạng thái, duyệt hộ); không log mật khẩu, token.

## Frontend (React 19, Vite)

- Component hàm + hooks. Một component một file, tên `PascalCase.jsx`; hook `useXxx.js`.
- Gọi API qua **một** client dùng chung (gắn token, đọc `{code, message}`), không `fetch` rải rác trong component. Đọc dữ liệu bằng `useApi`, ghi bằng `useMutation`; không thêm thư viện quản lý dữ liệu/state (ADR-0003).
- Style của component viết trong file CSS Modules cùng tên (`ProductCard.jsx` + `ProductCard.module.css`); không dùng style inline trừ giá trị tính động.
- Không viết cứng màu, cỡ chữ, khoảng cách: dùng biến CSS trong `:root`.
- Mỗi màn hình gọi API có đủ trạng thái tải / rỗng / lỗi.
- Route cần đăng nhập bọc trong guard theo vai trò; ẩn nút không có nghĩa là đã phân quyền — backend luôn kiểm lại.
- Không thêm thư viện mới khi chưa thống nhất trong nhóm (ghi ADR).

## Chung

- Không commit bí mật, `.env`, thư mục build, ảnh upload thử.
- Thay đổi nhỏ, đúng phạm vi yêu cầu; không tái cấu trúc tiện tay trong PR tính năng.
- Mã mới đi kèm kiểm thử tương ứng (xem [testing.md](testing.md)) và cập nhật tài liệu bị ảnh hưởng (xem `docs/README.md` mục "Khi nào cập nhật gì").
