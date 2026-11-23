# Tài liệu backend bổ sung

> Nguồn: 7 tài liệu Markdown được nhập nguyên văn từ `backend.zip` vào ngày 2026-11-23. Phần nội dung gốc không bị chỉnh sửa; README này là lớp điều hướng và ghi chú tương thích với snapshot `vCraft-Quote-backend.zip`.

## Mục đích và thứ tự đọc

| Tài liệu | Nội dung | Dùng khi |
|---|---|---|
| [Blueprint backend](design/vcraft-backend-blueprint.md) | Architecture review, stack, schema/migration, API, security, transaction, media, deployment và ánh xạ tiêu chí chấm | Xem bức tranh backend tổng thể; phải đọc ghi chú tương thích bên dưới trước khi áp dụng |
| [Giai đoạn B — Architecture Proposal](design/vcraft-backend-phase-b.md) | Kiến trúc, API, schema, bảo mật, triển khai và các điểm cần nhóm xác nhận | Chốt thiết kế giữa các module và phạm vi API |
| [Giai đoạn C — Detailed Backend Design](design/vcraft-backend-phase-c.md) | Thiết kế chi tiết theo module, migration và code mẫu M1–M3 | Triển khai từng module sau khi đã thống nhất nghiệp vụ và migration |
| [Kế hoạch thực thi](planning/vcraft-action-plan.md) | Công việc theo người phụ trách và thứ tự thực hiện, kèm checkpoint/test/demo | Chia việc và theo dõi triển khai nhóm |
| [Implementation Guide](implementation/vcraft-implementation-guide.md) | Repository, seed, frontend structure và một số implementation mẫu | Tra cứu code mẫu khi triển khai |
| [Ready to Copy](implementation/vcraft-ready-to-copy.md) | Các đoạn code/patch cụ thể, gồm sửa slug, SecurityConfig, repository và auth frontend | Dùng để đối chiếu/áp dụng từng thay đổi nhỏ; không thay toàn bộ file một cách máy móc |
| [Services & Tests](implementation/vcraft-services-and-tests.md) | Service implementations và script kiểm thử tích hợp M2 | Triển khai nghiệp vụ, bổ sung test và kiểm tra luồng đầu-cuối |

**Thứ tự khuyến nghị:** Blueprint → Giai đoạn B → xác nhận các điểm còn mở → Giai đoạn C → Kế hoạch thực thi. Hai tài liệu implementation và Ready to Copy là tài liệu tra cứu, không phải lệnh thay thế hàng loạt mã nguồn hiện có.

## Vị trí của bộ tài liệu này trong dự án

Đây là tài liệu thiết kế/triển khai bổ sung. Nó **không tự động trở thành yêu cầu đã chốt hoặc trạng thái đã triển khai**. Khi có mâu thuẫn, tiếp tục dùng:

- Câu trả lời đã xác nhận của giảng viên/nhóm và ADR trong `../decisions/` cho quyết định nghiệp vụ/kỹ thuật đã chốt.
- `../00-source/` và `../01-requirements/` cho nguồn yêu cầu và phạm vi chuẩn hóa.
- Controller/DTO hiện tại để xác định hợp đồng API đang chạy; migration Flyway hiện tại để xác định schema trong repo.
- `../04-plan/status.md` để xác định phần nào đã thực sự được triển khai.

Khi một đề xuất từ bộ này được nhóm duyệt, cập nhật câu hỏi tương ứng trong `../01-requirements/open-questions.md`, ghi ADR nếu là quyết định dài hạn, rồi cập nhật thiết kế/API/schema/status cùng PR. Không chỉ dựa vào việc một đề xuất xuất hiện trong tài liệu này để đánh dấu tính năng hoàn thành.

## Ghi chú tương thích đã đối chiếu với ZIP dự án

### 1. Migration V5–V7 — cần xác minh trước khi tạo

- Snapshot `vCraft-Quote-backend.zip` hiện có đúng các migration `V1` đến `V4`; **không có file `V5__expand_core.sql` dạng placeholder** trong cây mã nguồn được gửi.
- Phần mở đầu Blueprint mô tả một placeholder V5 đã tồn tại và đưa ra phương án đổi số migration nếu V5 đã được chạy. Chi tiết đó không khớp với snapshot ZIP này. Có thể môi trường DB riêng đã có lịch sử khác; vì vậy trước khi tạo migration mới, chạy `SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;` trên từng DB liên quan.
- Các tài liệu đều hướng đến mở rộng schema bằng migration mới và không sửa migration đã áp dụng. Hãy thống nhất một chuỗi phiên bản duy nhất theo lịch sử DB thực tế; không tự tạo V6/V7 chỉ vì đoạn hướng dẫn có nhắc đến placeholder V5.

### 2. Các xác nhận nhóm ngày 2026-11-21

Giai đoạn B ghi căn cứ vào “8 câu xác nhận của nhóm ngày 2026-11-21”, nhưng `../01-requirements/open-questions.md` trong ZIP dự án vẫn còn một số câu kỹ thuật/nghiệp vụ đánh dấu `_chưa_` (ví dụ Q-13, Q-16, Q-17, Q-20, Q-21, Q-23, Q-29, Q-38, Q-39). Cần đưa **nội dung chính xác** của các câu đã chốt vào tài liệu câu hỏi mở và ADR phù hợp trước khi xem chúng là quyết định chính thức. Không suy đoán 8 câu đó từ các đoạn thiết kế.

### 3. API cũ và API đề xuất mới

- API hiện tại của Artisan workshop đang dùng `PATCH /api/artisan/workshops/{id}` và `PATCH /api/artisan/workshops/{id}/contact`; Product detail đang dùng `GET /api/products/{id}`.
- Giai đoạn B/C đề xuất thêm hoặc chuyển sang `PUT /api/artisan/workshop` và hỗ trợ product detail theo `idOrSlug`/slug. Giai đoạn B cũng nhắc giữ API cũ trong giai đoạn chuyển tiếp hoặc phối hợp cập nhật frontend.
- Khi triển khai, kiểm tra tất cả controller, Postman collection và frontend caller. Ưu tiên tương thích ngược: thêm route mới trước, kiểm thử cả route cũ và mới, chỉ loại route cũ sau khi đã thống nhất và cập nhật client.

### 4. Slug khi cấp tài khoản Artisan

Trong snapshot dự án, `WorkshopService.provision()` đang truyền `input.username()` làm slug khi tạo workshop. `vcraft-ready-to-copy.md` đề xuất sinh slug từ tên xưởng bằng `SlugUtils` và kiểm tra trùng qua repository. Đây là điểm phù hợp để sửa, nhưng cần thêm utility/query tương ứng và kiểm thử trường hợp tên tiếng Việt, slug trùng, username trùng trước khi merge.

### 5. Media — giới hạn và quyền upload cần thống nhất

- `open-questions.md` hiện gợi ý tối đa 8 MB mỗi ảnh; các tài liệu bổ sung thiết kế giới hạn 5 MB mỗi ảnh và tối đa 10 ảnh/request.
- Giai đoạn B có chỗ liệt kê Buyer được upload ảnh tham khảo, đồng thời nêu đây là điểm cần xác nhận về quyền upload. Chưa nên mở quyền chỉ dựa trên bảng API đề xuất.
- Chốt giới hạn dung lượng, MIME được phép, số ảnh, quyền theo role, ownership và nơi lưu trữ vào yêu cầu/ADR trước khi triển khai endpoint upload.

### 6. Đoạn code “thay toàn bộ file”

Một số tài liệu implementation ghi rõ thay toàn bộ `SecurityConfig`, repository hoặc service. Không chép đè thẳng nếu chưa so sánh với snapshot hiện tại: phải giữ các endpoint báo giá, role guard, quy tắc sở hữu, xử lý lỗi và hành vi concurrency đã có. Đặc biệt, module Quote trong snapshot đã có migration V4 và các controller/service riêng; các phần này không được vô tình loại bỏ khi tích hợp code mới.

## Phạm vi nhập vào ZIP

- Đã nhập 7 tài liệu Markdown ở các thư mục `design/`, `planning/`, `implementation/`.
- Không chép bất kỳ file Java/SQL/config nào từ `backend.zip`, vì ZIP nguồn chỉ chứa tài liệu Markdown.
- Không thay mã nguồn backend, frontend, migration, Postman hoặc trạng thái triển khai trong lần bổ sung này.
