# Tài liệu vCraft

Bộ tài liệu này là **nguồn sự thật** của dự án cho cả thành viên nhóm lẫn các phiên AI (Claude, Codex…). Đọc [../AGENTS.md](../AGENTS.md) trước nếu bạn là AI.

## Bản đồ tài liệu

| Thư mục | Trả lời câu hỏi | File |
|---|---|---|
| `00-source/` | Giảng viên và nhóm đã viết nguyên văn những gì? (bất biến) | [de-goc](00-source/de-goc.md) · [dac-ta-yeu-cau](00-source/dac-ta-yeu-cau.md) · [ghi-chu-nhom](00-source/ghi-chu-nhom.md) |
| `01-requirements/` | Phải làm **cái gì**, cho ai, điều gì chưa rõ? | [overview](01-requirements/overview.md) · [glossary](01-requirements/glossary.md) · [roles-permissions](01-requirements/roles-permissions.md) · [functional-requirements](01-requirements/functional-requirements.md) · [non-functional](01-requirements/non-functional.md) · [open-questions](01-requirements/open-questions.md) · [traceability](01-requirements/traceability.md) |
| `02-design/` | Làm **như thế nào**? | [architecture](02-design/architecture.md) · [data-model](02-design/data-model.md) · [api-contract](02-design/api-contract.md) · [quote-state-machine](02-design/quote-state-machine.md) · [ui-ux](02-design/ui-ux.md) |
| `03-engineering/` | Chạy, viết mã, cộng tác, kiểm thử ra sao? | [setup](03-engineering/setup.md) · [conventions](03-engineering/conventions.md) · [git-workflow](03-engineering/git-workflow.md) · [testing](03-engineering/testing.md) |
| `04-plan/` | Đã làm đến đâu, làm gì tiếp, demo thế nào? | [status](04-plan/status.md) · [roadmap](04-plan/roadmap.md) · [demo-script](04-plan/demo-script.md) |
| `decisions/` | **Vì sao** quyết định như vậy? | [danh sách ADR](decisions/README.md) |

## Đọc gì cho việc gì

| Việc | Đọc (theo thứ tự) |
|---|---|
| Mới vào dự án | `01-requirements/overview.md` → `glossary.md` → `04-plan/status.md` |
| Làm một tính năng | Dòng FR trong `functional-requirements.md` → `open-questions.md` (các Q liên quan) → `roles-permissions.md` → phần tương ứng trong `data-model.md` và `api-contract.md` → `conventions.md` |
| Làm luồng báo giá | Thêm `02-design/quote-state-machine.md` |
| Làm giao diện | `02-design/ui-ux.md` + `glossary.md` |
| Đổi schema | `02-design/data-model.md` mục 3 |
| Chạy dự án | `03-engineering/setup.md` |
| Chuẩn bị vấn đáp | `04-plan/demo-script.md` + `01-requirements/traceability.md` |

## Nhãn độ chắc chắn

Mọi tài liệu ghi rõ mức chắc chắn; đừng coi một đề xuất là điều đã chốt.

- **Đã chốt** — đã thống nhất hoặc đã có trong mã.
- **Đề xuất** — phương án hợp lý do người soạn tài liệu đưa ra, chờ nhóm/giảng viên duyệt.
- **Giả định Q-xx** — đang tạm làm theo giả định của câu hỏi mở tương ứng.
- **Hiện trạng** — mô tả mã đang có tại ngày ghi trong tài liệu.

## Nguồn sự thật khi có mâu thuẫn

| Về | Nguồn sự thật |
|---|---|
| Yêu cầu | `01-requirements/` (đã chuẩn hóa theo [ADR-0001](decisions/0001-thu-tu-uu-tien-nguon-yeu-cau.md)) |
| Schema | File migration Flyway > `data-model.md` |
| API đang có | Controller + DTO > `api-contract.md` |
| Tình trạng cài đặt | `04-plan/status.md` |
| Điều chưa rõ | `01-requirements/open-questions.md` |

Phát hiện tài liệu lệch với mã: sửa tài liệu (hoặc mã) ngay trong thay đổi đó, đừng để nguyên.

## Khi nào cập nhật gì

| Bạn vừa… | Cập nhật |
|---|---|
| Hoàn thành/đổi tình trạng một FR | `04-plan/status.md` |
| Thêm/sửa endpoint | `02-design/api-contract.md` (chuyển từ "dự kiến" sang "đang có"), Postman |
| Thêm migration | `02-design/data-model.md` |
| Nhận câu trả lời của giảng viên / nhóm chốt một điểm | `open-questions.md` → sửa nhãn "Giả định" trong `functional-requirements.md` → tài liệu thiết kế liên quan |
| Gặp điểm mơ hồ mới | Thêm dòng vào `open-questions.md` kèm giả định |
| Quyết định có ảnh hưởng lâu dài (thư viện, cấu trúc, phạm vi) | ADR mới trong `decisions/` |
| Thêm/đổi yêu cầu | `functional-requirements.md` (mã mới, không đánh số lại) + `traceability.md` nếu liên quan đề gốc |
| Đổi cách chạy/lệnh | `03-engineering/setup.md` và mục lệnh trong `AGENTS.md` |

## Quy tắc viết tài liệu

- Mỗi thông tin chỉ ở **một** nơi; nơi khác liên kết tới, không chép lại.
- Viết tiếng Việt, ngắn, dùng bảng và mã định danh (FR-, NFR-, Q-, C-, ADR-) để tham chiếu được.
- Không tạo file tài liệu mới ngoài cấu trúc này khi chưa cần; ưu tiên bổ sung vào file sẵn có.
- Không sửa `00-source/`.
