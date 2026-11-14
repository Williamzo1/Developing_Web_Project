# vCraft — Hướng dẫn cho AI agent

File này là điểm vào cho mọi phiên AI (Claude Code đọc qua `CLAUDE.md`, Codex và các công cụ khác đọc trực tiếp). Giữ file ngắn; chi tiết nằm trong `docs/`.

## Dự án

vCraft: nền tảng web **triển lãm số làng nghề gốm Bát Tràng và kết nối mua sỉ**. Bài tập lớn INT3306 (Phát triển ứng dụng web), nhóm 3 người, chấm bằng **demo trực tiếp**. Ba vai trò: `ARTISAN` (hộ/xưởng), `BUYER` (mua sỉ), `ADMIN` (ban quản lý). Bốn trọng tâm kỹ thuật: truy xuất nguồn gốc theo lô + QR, máy trạng thái báo giá có khóa bản ghi, bản đồ tương tác, upload + tối ưu ảnh; giao diện đẹp, giàu hình ảnh là tiêu chí chấm.

Không phải sàn bán lẻ: không giỏ hàng, không thanh toán, Admin không phân bổ/điều phối đơn.

## Cấu trúc repo

```text
backend/    Spring Boot 4.1.2, Java 17 — gói vn.vcraft.backend (controller/service/repository/model/dto/security/config/exception)
            src/main/resources/db/migration  Flyway (đang ở V3; file kế tiếp là V4)
            tests/phase02.py, tests/m1.py    kiểm thử HTTP
frontend/   React 19 + React Router 7 + Vite 7, CSS thuần (hiện dồn trong src/main.jsx)
postman/    Collection + environment
docs/       Tài liệu — xem docs/README.md
compose.yaml, .env.example   Docker Compose: db (PostgreSQL 16), backend, frontend (Nginx)
```

## Lệnh

| Việc | Lệnh | Thư mục |
|---|---|---|
| Chạy local | PostgreSQL + IntelliJ backend (profile local) + Vite, xem README | — |
| Chạy backend | `./mvnw spring-boot:run` (PowerShell: `.\mvnw.cmd spring-boot:run`) | `backend/` |
| Build backend | `./mvnw -B package` | `backend/` |
| Chạy frontend | `npm ci` rồi `npm run dev` (http://localhost:5173) | `frontend/` |
| Build frontend | `npm run build` | `frontend/` |
| Kiểm thử HTTP | `python backend/tests/m1.py` (chạy cả `phase02.py`; backend phải đang chạy với profile `local` trên database thử) | gốc |

Chi tiết và tài khoản mẫu: `docs/03-engineering/setup.md`.

## Trước khi làm việc

1. Đọc `docs/04-plan/status.md` để biết hiện trạng.
2. Tìm yêu cầu liên quan trong `docs/01-requirements/functional-requirements.md` (mã `FR-…`) và các câu hỏi mở nó phụ thuộc trong `docs/01-requirements/open-questions.md`.
3. Đọc phần thiết kế tương ứng trong `docs/02-design/` và `docs/03-engineering/conventions.md`.

Bảng "Đọc gì cho việc gì" ở `docs/README.md` chỉ đúng file cần đọc cho từng loại việc. Không cần đọc hết `docs/`.

## Quy tắc bắt buộc

**Yêu cầu**

- Không tự bịa yêu cầu. Thứ tự ưu tiên nguồn: câu trả lời đã chốt → đặc tả → đề gốc (mục 4 kỹ thuật vẫn phải đáp ứng) → ghi chú nhóm. Xem `docs/decisions/0001-thu-tu-uu-tien-nguon-yeu-cau.md`.
- Tài liệu có nhãn **Đề xuất** / **Giả định Q-xx** chưa phải quyết định. Được phép làm theo để không bị chặn, nhưng phải nói rõ với người dùng mình đang dựa vào giả định nào, và cài đặt sao cho dễ đổi.
- Gặp điểm mơ hồ chưa có trong `open-questions.md`: thêm câu hỏi mới kèm giả định đề xuất và báo người dùng. Nếu lựa chọn ảnh hưởng schema hoặc khó đảo ngược, **hỏi trước khi làm**.
- Không sửa `docs/00-source/`.

**Mã**

- Dùng đúng thuật ngữ ở `docs/01-requirements/glossary.md` (`workshop`, `product`, `batch`, `quote_request`, `quote`…). Định danh tiếng Anh; chữ trên giao diện và thông báo lỗi tiếng Việt.
- Backend theo lớp `controller → service → repository`; DTO là `record`; không trả entity ra API.
- Phân quyền: vai trò theo tiền tố đường dẫn trong `SecurityConfig`; **quyền sở hữu kiểm ở service** bằng `Actor`. Không tin id chủ sở hữu do client gửi. Tài nguyên riêng tư của người khác trả 404.
- Báo giá của một hộ không được lộ cho hộ khác qua bất kỳ endpoint nào.
- Schema chỉ đổi bằng **file Flyway mới**; không sửa migration đã có; entity phải khớp (`ddl-auto=validate`). Dữ liệu mẫu để ở `db/seed/`, idempotent.
- Mọi chuyển trạng thái của yêu cầu báo giá đi qua một service duy nhất, trong giao dịch, có khóa dòng — theo `docs/02-design/quote-state-machine.md`.
- Frontend: gọi API qua client dùng chung; dùng biến CSS, không viết cứng màu; mỗi màn hình có trạng thái tải/rỗng/lỗi; responsive từ 390 px.
- Không thêm thư viện, không đổi cấu trúc thư mục, không tái cấu trúc ngoài phạm vi được giao khi chưa được đồng ý.
- Không commit bí mật. Không tự commit/push/merge khi chưa được yêu cầu.

**Tài liệu đi cùng mã** — trong cùng một thay đổi:

- Xong/đổi tình trạng tính năng → `docs/04-plan/status.md`.
- Thêm/sửa endpoint → `docs/02-design/api-contract.md` (+ Postman).
- Thêm migration → `docs/02-design/data-model.md`.
- Quyết định lâu dài → ADR mới trong `docs/decisions/`.

Bảng đầy đủ: `docs/README.md` mục "Khi nào cập nhật gì".

## Định nghĩa "xong" cho một tính năng

- [ ] Đáp ứng dòng FR tương ứng, cả API lẫn giao diện (trừ khi được giao chỉ một phía).
- [ ] Backend và frontend build được.
- [ ] Có kiểm thử: đường chính, 401/403, **sai chủ sở hữu**, đầu vào xấu (`docs/03-engineering/testing.md`); đã chạy và báo kết quả thật.
- [ ] Giao diện thử ở 390 px và màn hình rộng; chữ tiếng Việt.
- [ ] Tài liệu liên quan đã cập nhật.
- [ ] Báo cáo cuối nêu rõ: đã làm gì, dựa trên giả định nào, phần nào chưa kiểm được.
