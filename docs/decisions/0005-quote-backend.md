# ADR-0005 — Backend báo giá

Ngày 2026-11-13. Đã áp dụng trong phạm vi backend được yêu cầu.

Giữ máy trạng thái/giới hạn một hộ chịu trách nhiệm đã chốt; mọi action khóa request cha bằng JPA PESSIMISTIC_WRITE trong transaction READ_COMMITTED trước khi đọc/sửa quote. @Version trên hai entity; accept gửi version quote; unique index một phần là chốt database. Composite FK bảo đảm quote được chọn thuộc request. Gửi lặp trả 409.

Q-38 chưa chốt: tạm theo gợi ý tag seed cố định và trùng ít nhất một tag; điều kiện duy nhất tại QuoteMatchingRepository.eligibilityPredicate(). Không làm CRUD tag. Q-31 rút báo giá tạm theo gợi ý trong tài liệu đã có; gửi lại reuse một dòng, cần version của dòng WITHDRAWN. Sau deadline chỉ cấm gửi mới/gửi lại; sửa/rút/accept vẫn được khi trạng thái cho phép.

FR-QUOTE-09: liên hệ Buyer nhập snapshot trên nháp, hộ chỉ thấy sau khi đã báo giá; workshop có email/phone và endpoint cập nhật của chính hộ. Không triển khai xác thực liên hệ trong chặng này. Hộ khác không xem giá/note của nhau; Artisan history ẩn actor ID của người khác.

V4 thêm quote/tag/contact; V1–V3 bất biến. Ảnh tham khảo chờ MEDIA, không giả lập media; UI, badge và Admin tag vẫn chưa làm. Script run-local đặt profile/working directory giúp chạy local không cần Edit Configurations và không đóng bí mật vào resources.
