# Nhật ký quyết định (ADR)

Mỗi quyết định có ảnh hưởng lâu dài (phạm vi, kiến trúc, thư viện, cách diễn giải yêu cầu) ghi thành một file ngắn để người và AI vào sau hiểu **vì sao**, không phải đoán.

## Khi nào viết ADR

- Chọn/đổi công nghệ, thư viện, cấu trúc thư mục.
- Chốt một câu hỏi mở có ảnh hưởng schema hoặc luồng nghiệp vụ.
- Chủ động làm khác đề gốc hoặc đặc tả.

Không cần ADR cho chi tiết cài đặt đọc mã là thấy.

## Cách viết

File `NNNN-ten-ngan.md`, số tăng dần, theo mẫu:

```markdown
# ADR-NNNN: Tiêu đề

- Trạng thái: Đề xuất | Đã chốt | Thay thế bởi ADR-XXXX
- Ngày: YYYY-MM-DD
- Người quyết định: ...

## Bối cảnh
## Quyết định
## Phương án đã cân nhắc
## Hệ quả
```

ADR đã chốt thì không sửa nội dung; muốn đổi thì viết ADR mới và đánh dấu cái cũ "Thay thế bởi".

## Danh sách

| ADR | Tiêu đề | Trạng thái |
|---|---|---|
| [0001](0001-thu-tu-uu-tien-nguon-yeu-cau.md) | Thứ tự ưu tiên giữa các nguồn yêu cầu | Đã chốt |
| [0002](0002-stack-va-xac-thuc.md) | Stack công nghệ và cơ chế xác thực | Stack: Đã chốt · Xác thực: Đề xuất |
| [0003](0003-frontend-style-va-du-lieu.md) | Frontend: CSS thuần + CSS Modules, hook tự viết để lấy dữ liệu | Đã chốt |

| [0004](0004-backend-foundation.md) | Hợp đồng lỗi, validation, phân trang và cấu hình M1 backend | Đã áp dụng |
| [0005](0005-ban-do-nen-openstreetmap.md) | Bản đồ nền OpenStreetMap | Đã áp dụng |
