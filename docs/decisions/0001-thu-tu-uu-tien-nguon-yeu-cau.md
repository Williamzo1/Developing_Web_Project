# ADR-0001: Thứ tự ưu tiên giữa các nguồn yêu cầu

- Trạng thái: Đã chốt (về thứ tự); các điểm dung hòa cụ thể còn chờ xác nhận, xem bên dưới
- Ngày: 2026-11-10
- Người quyết định: Nhóm (theo thông tin đã hỏi giảng viên); cần cả nhóm xác nhận lại

## Bối cảnh

Có ba nguồn yêu cầu và chúng mâu thuẫn ở vài điểm:

1. **Đề gốc** của giảng viên: có Admin phân bổ đơn hàng, nhật ký sản xuất theo từng lô, state machine đến `Delivered`.
2. **Đặc tả** nhóm tổng hợp sau khi hỏi giảng viên: thu hẹp vai trò Admin, quy trình dùng chung theo loại sản phẩm, nền tảng không điều hành giao dịch; còn 9 câu hỏi mở.
3. **Ghi chú nhóm**: phân vai, stack, bản đánh giá v0 với đề xuất kỹ thuật (JWT, tên bảng `bids`…).

Bài được chấm bằng demo vấn đáp, người chấm có thể đối chiếu trực tiếp với mục 4 "Yêu cầu kỹ thuật" của đề gốc.

## Quyết định

Khi các nguồn mâu thuẫn, ưu tiên theo thứ tự:

1. Câu trả lời đã chốt của giảng viên/nhóm ghi trong `01-requirements/open-questions.md` và các ADR.
2. Đặc tả (`00-source/dac-ta-yeu-cau.md`) — về **phạm vi nghiệp vụ và vai trò**.
3. Đề gốc (`00-source/de-goc.md`) — về **mọi thứ đặc tả không nói tới**, và riêng **mục 4 Yêu cầu kỹ thuật vẫn phải đáp ứng đầy đủ** trừ khi có câu trả lời chốt nói khác.
4. Ghi chú nhóm — chỉ để tham khảo; không phải yêu cầu.

Ở những điểm đặc tả làm nhẹ một yêu cầu kỹ thuật của đề gốc, chọn phương án **thỏa cả hai** (bảng C-01..C-06 trong `01-requirements/overview.md`) cho đến khi giảng viên xác nhận.

Các tài liệu trong `01-requirements/` là bản đã chuẩn hóa theo thứ tự này và là thứ được dùng hằng ngày; `00-source/` chỉ để đối chiếu.

## Phương án đã cân nhắc

- **Chỉ theo đặc tả**: gọn hơn, nhưng có nguy cơ mất điểm ở §4.1 (mốc thời gian theo lô) và §4.2 (state machine đủ 6 trạng thái, khóa bản ghi) nếu người chấm bám đề gốc.
- **Chỉ theo đề gốc**: bỏ qua những gì giảng viên đã làm rõ; phải làm chức năng Admin phân bổ đơn mà giảng viên đã nói không cần.

## Hệ quả

- Mô hình dữ liệu có cả công đoạn dùng chung (`process_steps`) lẫn mốc theo lô (`batch_milestones`); state machine giữ đủ 6 trạng thái nhưng hai trạng thái cuối chỉ mang tính thông tin.
- Cần hỏi giảng viên Q-04, Q-06, Q-10, Q-11 sớm; nếu câu trả lời cho phép bỏ bớt thì phạm vi giảm, không cần làm lại.
- Phải chuẩn bị lời giải thích khi vấn đáp cho phần không làm (C-01).
