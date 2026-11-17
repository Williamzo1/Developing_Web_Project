# ADR-0003: Frontend — cách viết style và cách lấy dữ liệu

- Trạng thái: Đã chốt
- Ngày: 2026-11-10
- Người quyết định: Nhóm (chốt Q-18 và Q-19 theo gợi ý trong `01-requirements/open-questions.md`)

## Bối cảnh

Frontend hiện là React 19 + Vite, CSS thuần, toàn bộ trong `src/main.jsx` với một hook `useCatalog`. Trước khi ba người cùng viết giao diện cần thống nhất hai thứ, vì đổi giữa chừng là viết lại: cách viết style và cách lấy/ghi dữ liệu server. Giao diện là tiêu chí chấm trực tiếp (đề gốc §4.4) và mỗi thành viên phải giải thích được mã của mình khi vấn đáp.

## Quyết định

1. **Style: CSS thuần + biến CSS + CSS Modules.**
   - Design tokens (màu, kiểu chữ, khoảng cách, bo góc, đổ bóng) khai báo một lần trong `:root`.
   - Style riêng của component viết trong `Ten.module.css` đặt cạnh component.
   - Style nền (reset, kiểu chữ, bố cục chung) đặt trong `src/styles/`.
   - Không dùng Tailwind, không dùng bộ component có sẵn (MUI, Ant Design…).
2. **Dữ liệu: hook tự viết + một client API dùng chung.**
   - `api/client`: bọc `fetch`, gắn `Authorization`, chuyển phản hồi lỗi `{code, message}` thành exception có `code`; xử lý 401 (xóa phiên, về trang đăng nhập).
   - `useApi(path, params)`: đọc dữ liệu, trả `{ data, loading, error, reload }`, hủy request khi component rời trang.
   - `useMutation(fn)`: ghi dữ liệu, trả `{ run, pending, error }`; sau khi thành công, nơi gọi tự `reload` dữ liệu liên quan.
   - Phiên đăng nhập (token, vai trò) giữ trong React Context.
   - Không dùng TanStack Query, Redux, Zustand.

## Phương án đã cân nhắc

- **Tailwind CSS**: viết nhanh, nhưng thêm công cụ và cú pháp phải học; giao diện cần bản sắc riêng nên vẫn phải tự thiết kế.
- **Bộ component có sẵn**: nhanh cho trang quản trị, nhưng khu công khai dễ ra giao diện đại trà, khó đạt yêu cầu image-centric có bản sắc làng nghề.
- **TanStack Query**: giảm mã lặp cho bộ nhớ đệm và tải lại sau khi ghi; đổi lại thêm một thư viện phải hiểu và giải thích. Quy mô dự án chưa cần bộ nhớ đệm phía client.
- **Redux/Zustand**: không có trạng thái toàn cục nào ngoài phiên đăng nhập.

## Hệ quả

- Ít phụ thuộc, không thêm bước build; mọi dòng mã frontend đều do nhóm viết và giải thích được.
- Phải tự viết các thành phần giao diện cơ bản (nút, ô nhập, hộp thoại, phân trang, thẻ ảnh) — làm một lần trong `components/` ở giai đoạn nền frontend.
- Không có bộ nhớ đệm: mỗi lần vào trang là một lần gọi API. Chấp nhận được; nếu về sau thấy chậm thì bổ sung trong `useApi`, không đổi nơi gọi.
- Sau khi ghi dữ liệu phải nhớ gọi `reload` cho danh sách liên quan; đây là chỗ dễ sót, cần kiểm khi review.
- Muốn đổi một trong hai quyết định này phải viết ADR mới thay thế.
