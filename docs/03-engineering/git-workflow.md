# Quy trình Git

> Trạng thái: **Đề xuất** (ghi chú nhóm đang để "Github workflow: ??"). Nhóm duyệt rồi chốt.

Repo: https://github.com/Williamzo1/Developing_Web_Project — nhánh chính: `master`.

## Nhánh

- `master` luôn chạy được và demo được. Không commit trực tiếp tính năng lên `master`.
- Mỗi việc một nhánh ngắn hạn tách từ `master`:
  - `feat/<module>-<mo-ta>` — ví dụ `feat/batch-landing-page`
  - `fix/<mo-ta>` · `docs/<mo-ta>` · `chore/<mo-ta>`
- Rebase/merge `master` vào nhánh thường xuyên để tránh xung đột lớn, nhất là số thứ tự migration Flyway.

## Commit

Dạng `<loại>(<module>): <mô tả ngắn>` kèm mã yêu cầu khi có:

```text
feat(batch): sinh mã QR cho lô hàng (FR-BATCH-04)
fix(quote): chặn chấp nhận hai báo giá đồng thời (FR-QUOTE-06)
docs: chốt Q-06 theo trả lời của giảng viên
```

Loại: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`. Module: `auth`, `ws`, `prod`, `batch`, `quote`, `map`, `news`, `admin`, `media`, `home`, `infra`.

## Pull request

1. Mở PR vào `master`; mô tả: làm gì, FR nào, cách tự kiểm, ảnh chụp màn hình nếu đổi giao diện, có thay đổi phá vỡ API/schema không.
2. Ít nhất một thành viên khác xem qua trước khi merge.
3. Danh sách kiểm trước khi merge:
   - [ ] Backend build được, frontend build được.
   - [ ] Test liên quan chạy qua; có ca kiểm quyền cho endpoint mới.
   - [ ] Migration mới có số không trùng; không sửa migration cũ.
   - [ ] Đã cập nhật `docs/` bị ảnh hưởng (API, data model, status, open-questions).
   - [ ] Không có bí mật hay tệp rác.

## Làm việc với AI

- AI không tự commit, push hay merge khi chưa được yêu cầu.
- Mỗi phiên AI làm một việc có phạm vi rõ (một FR hoặc một nhóm FR cùng module) trên nhánh riêng.
- Người yêu cầu chịu trách nhiệm đọc diff trước khi mở PR.
