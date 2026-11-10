# Vai trò và phân quyền

> Trạng thái: khung vai trò **Đã chốt**; các ô có dấu `*` phụ thuộc câu hỏi mở (ghi mã Q).

## Vai trò

| Vai trò | Cách có tài khoản | Ghi chú |
|---|---|---|
| Khách (guest) | Không cần | Xem toàn bộ nội dung công khai, quét QR. *(giả định Q-01)* |
| `BUYER` | Tự đăng ký *(giả định Q-12)* | Tạo yêu cầu mua sỉ, xem báo giá gửi cho mình. |
| `ARTISAN` | Admin cấp, phải được duyệt mới đăng nhập được (đã có trong mã) | Gắn đúng 1 xưởng. Chỉ thao tác trên dữ liệu của xưởng mình. |
| `ADMIN` | Tạo sẵn bằng seed | Quản trị nền tảng; không tham gia giao dịch. |

## Ma trận quyền

Ký hiệu: ✔ được phép · ✖ không · "của mình" = chỉ bản ghi thuộc sở hữu.

| Tài nguyên / hành động | Khách | BUYER | ARTISAN | ADMIN |
|---|---|---|---|---|
| Xem trang chủ, hồ sơ hộ, sản phẩm, quy trình, bản đồ, tin tức | ✔ | ✔ | ✔ | ✔ |
| Xem lô **công khai** / landing page qua QR | ✔ | ✔ | ✔ | ✔ |
| Xem lô **riêng tư** | qua liên kết riêng* (Q-02) | qua liên kết riêng* | của mình | ✔ (mục đích kiểm duyệt) |
| Sửa hồ sơ xưởng | ✖ | ✖ | của mình | ✖ (chỉ duyệt/ẩn) |
| Tạo/sửa/ẩn sản phẩm, công đoạn | ✖ | ✖ | của mình | ẩn khi vi phạm |
| Tạo/sửa lô, mốc sản xuất, đổi công khai/riêng tư | ✖ | ✖ | của mình | ẩn khi vi phạm |
| Upload ảnh | ✖ | ✖* | ✔ | ✔ |
| Tạo/sửa/gửi/hủy yêu cầu mua sỉ | ✖ | của mình | ✖ | ✖ |
| Xem yêu cầu mua sỉ | ✖ | của mình | yêu cầu chỉ định mình + yêu cầu mở phù hợp* (Q-03) | chỉ số liệu tổng hợp* |
| Gửi/sửa/rút báo giá | ✖ | ✖ | của mình | ✖ |
| Xem báo giá | ✖ | các báo giá gửi cho yêu cầu của mình | **chỉ** báo giá của mình | ✖* |
| Chấp nhận báo giá | ✖ | yêu cầu của mình* (Q-04) | ✖ | ✖ |
| Cập nhật `In Production`/`Delivered` | ✖ | ✖ | hộ có báo giá được chấp nhận* (Q-11) | ✖ |
| Cấp tài khoản hộ, duyệt/xác minh hộ | ✖ | ✖ | ✖ | ✔ |
| Đăng/sửa/ẩn tin tức, sự kiện | ✖ | ✖ | ✖ | ✔ |
| Chọn nội dung nổi bật | ✖ | ✖ | ✖ | ✔ |
| Gửi báo cáo vi phạm | ✔* | ✔ | ✔ | — |
| Xử lý báo cáo vi phạm, xem thống kê | ✖ | ✖ | ✖ | ✔ |

## Quy tắc bắt buộc khi cài đặt

1. **Kiểm tra quyền sở hữu ở tầng service**, không chỉ theo vai trò ở `SecurityConfig`. Mẫu hiện có: `WorkshopService` so ID xưởng trong URL với xưởng của người đăng nhập, sai → `403`.
2. **Không lộ sự tồn tại** của tài nguyên riêng tư: lô riêng tư, yêu cầu, báo giá của người khác trả `404` (không phải `403`) cho người không có quyền.
3. Báo giá của hộ A **không bao giờ** xuất hiện trong response trả cho hộ B, kể cả số lượng báo giá chi tiết theo hộ.
4. Admin không có endpoint nào tạo/sửa/chấp nhận yêu cầu hay báo giá.
5. Mỗi endpoint mới phải có ít nhất một ca kiểm thử "sai chủ sở hữu" (xem `03-engineering/testing.md`).
