# 00-source — Tài liệu nguồn (bất biến)

Thư mục này lưu **nguyên văn** các tài liệu đầu vào. Không sửa nội dung ở đây; mọi diễn giải, chuẩn hóa, quyết định nằm ở `01-requirements/` và `decisions/`.

| File | Nguồn | Vai trò |
|---|---|---|
| [de-goc.md](de-goc.md) | `original/de-goc.docx` | Đề bài chính thức của giảng viên. Là căn cứ chấm điểm khi vấn đáp. |
| [dac-ta-yeu-cau.md](dac-ta-yeu-cau.md) | `original/dac-ta-yeu-cau.docx` | Đặc tả nhóm tổng hợp **sau khi hỏi giảng viên**. Thu hẹp/làm rõ đề gốc. |
| [ghi-chu-nhom.md](ghi-chu-nhom.md) | `original/ghi-chu-nhom.docx` ("All in one") | Ghi chú nội bộ: phân vai, các phase, stack, bản đánh giá vCraft v0. Phần đánh giá mô tả bản **v0**, đã cũ so với mã hiện tại (xem `04-plan/status.md`). |

Ghi chú:

- `de-goc.md` được chép tay, giữ nguyên câu chữ. Hai file còn lại trích tự động nên bảng bị dàn phẳng thành từng dòng; cần đối chiếu chính xác thì mở file `.docx` gốc.
- Khi giảng viên trả lời thêm câu hỏi: **không** sửa file ở đây, mà ghi vào `01-requirements/open-questions.md` (đổi trạng thái câu hỏi) và nếu là quyết định lớn thì thêm ADR ở `decisions/`.
- Thứ tự ưu tiên khi các nguồn mâu thuẫn: xem [ADR-0001](../decisions/0001-thu-tu-uu-tien-nguon-yeu-cau.md).
