# Tổng quan sản phẩm và phạm vi

> Trạng thái: **Đã chốt** ở mức định hướng (theo đặc tả sau khi hỏi giảng viên). Chi tiết còn mở nằm ở [open-questions.md](open-questions.md).

## 1. vCraft là gì

Nền tảng web **triển lãm số** cho một làng nghề truyền thống và **kết nối mua sỉ**:

- Mỗi hộ gia đình/nghệ nhân có không gian riêng để kể chuyện làm nghề, trưng bày sản phẩm, minh bạch quy trình sản xuất (truy xuất nguồn gốc qua QR).
- Buyer khám phá làng nghề (danh mục, bản đồ), gửi yêu cầu mua sỉ và nhận báo giá từ các hộ.
- Ban quản lý (Admin) xác minh hộ, quản lý tin tức/sự kiện, quảng bá.

Làng nghề nhóm chọn: **gốm Bát Tràng** (đã dùng trong mã và dữ liệu mẫu).

Bối cảnh: bài tập lớn INT3306 Fall 2026, nhóm 3 người, chấm bằng **demo trực tiếp** tại buổi vấn đáp (không cần slide/doc). Hệ quả: mọi thứ phải demo được ổn định; luồng demo quan trọng hơn độ phủ tính năng.

## 2. Nguyên tắc thiết kế

1. **Image-centric**: giao diện đẹp, giàu hình ảnh, có bản sắc làng nghề, responsive.
2. **Mua sỉ, không phải bán lẻ**: không giỏ hàng, không thanh toán.
3. **Nền tảng kết nối, không điều hành giao dịch**: các bên tự thương lượng, ký kết, thanh toán, giao nhận.
4. **Tách nội dung trưng bày khỏi thông tin giao dịch**: quy trình sản xuất gắn với loại sản phẩm (dùng chung); thông tin số lượng/giá/thời gian gắn với lô hàng.
5. **Riêng tư theo mặc định** cho yêu cầu mua sỉ, báo giá và lô riêng tư.
6. Mỗi hộ là một pháp nhân độc lập, tự chịu trách nhiệm; Admin **không** ký thay, **không** phân lô/điều phối đơn.

## 3. Trong phạm vi

| Module | Nội dung chính | Chi tiết |
|---|---|---|
| HOME | Trang chủ triển lãm số, điều hướng | FR-HOME |
| AUTH | Đăng nhập, phân quyền 3 vai trò, cấp/duyệt tài khoản hộ | FR-AUTH |
| WS | Hồ sơ hộ gia đình/xưởng | FR-WS |
| PROD | Loại sản phẩm + quy trình sản xuất dùng chung | FR-PROD |
| BATCH | Lô hàng, landing page lô, QR, công khai/riêng tư | FR-BATCH |
| QUOTE | Yêu cầu mua sỉ, báo giá, máy trạng thái | FR-QUOTE |
| MAP | Bản đồ tương tác với ghim xưởng | FR-MAP |
| NEWS | Tin tức, thông báo, sự kiện | FR-NEWS |
| ADMIN | Xác minh, nội dung nổi bật, báo cáo vi phạm, thống kê | FR-ADMIN |
| MEDIA | Upload nhiều ảnh, tối ưu/resize ảnh ở backend | FR-MEDIA |

Danh sách đầy đủ: [functional-requirements.md](functional-requirements.md).

## 4. Ngoài phạm vi (không làm)

- Admin nhận đơn rồi phân chia cho các hộ; Admin ký hợp đồng thay hộ.
- Quy trình bắt buộc quản lý hợp đồng, thanh toán, giao hàng, khiếu nại, thiếu/lỗi hàng.
- Điều phối nội bộ giữa nhiều hộ cùng làm một đơn.
- Giỏ hàng, cổng thanh toán, chữ ký số, chat thời gian thực (trừ khi có yêu cầu bổ sung).

## 5. Chỗ đặc tả lệch với đề gốc

Đặc tả (sau khi hỏi giảng viên) thu hẹp đề gốc ở vài điểm. Vì buổi vấn đáp vẫn có thể đối chiếu với **mục 4 – Yêu cầu kỹ thuật của đề gốc**, nhóm chọn cách dung hòa dưới đây. Các dòng "Đề xuất" cần nhóm/giảng viên xác nhận.

| Mã | Đề gốc | Đặc tả | Cách xử lý | Trạng thái |
|---|---|---|---|---|
| C-01 | BQL phân bổ đơn hàng lớn cho nhiều hộ | Admin không phân lô, không điều phối | Theo đặc tả. Không làm chức năng phân bổ. | Đã chốt |
| C-02 | Mỗi **lô** có mảng mốc thời gian + ảnh chứng minh | Quy trình dùng chung theo **loại sản phẩm**, không lặp lại cho từng lô | Làm cả hai tầng: quy trình dùng chung ở sản phẩm (bắt buộc) + mốc thời gian riêng của lô (ngày + ảnh, tùy chọn, ánh xạ vào công đoạn). Landing page lô ghép hai tầng. | Đề xuất (Q-10) |
| C-03 | State machine 6 trạng thái đến `Delivered`, có khóa bản ghi | Không quản lý bắt buộc sản xuất/giao hàng; "chấp nhận báo giá, nếu có" | Giữ đủ 6 trạng thái để đáp ứng 4.2, nhưng `In Production`/`Delivered` chỉ là mốc thông tin do hộ tự cập nhật, không kèm quản lý hợp đồng/thanh toán. Khóa bản ghi ở bước chấp nhận báo giá. | Đề xuất (Q-04, Q-11) |
| C-04 | QR mỗi lô → landing page kể toàn bộ câu chuyện | QR đến landing page lô "hoặc trang truy xuất phù hợp" | QR → landing page lô; trang này nhúng quy trình của loại sản phẩm. | Đề xuất (Q-06) |
| C-05 | Bản đồ cho biết xưởng nào **đang mở cửa** | Giờ mở cửa "có thể hiển thị nếu hộ cung cấp" | Hộ khai giờ mở cửa + cờ nhận khách tham quan; ghim thể hiện trạng thái mở/đóng. | Đề xuất |
| C-06 | Admin kiểm duyệt **và cấp** tài khoản hộ | Admin xác minh hộ | Tương thích. Giữ luồng hiện có: Admin cấp tài khoản → duyệt → hộ đăng nhập được. | Đã chốt |

## 6. Tiêu chí nghiệm thu (tự kiểm)

1. Khách khám phá được làng nghề: hồ sơ hộ, sản phẩm, quy trình, bản đồ.
2. Hộ tạo/cập nhật được lô hàng; landing page hiển thị đúng; quét QR ra đúng trang.
3. Quy trình sản xuất dùng chung cho các lô cùng loại.
4. Lô riêng tư không lộ cho người không có quyền.
5. Buyer tạo được yêu cầu mua sỉ (chỉ định hoặc mở) và chỉ thấy báo giá của mình.
6. Hộ phản hồi và gửi được báo giá; không thấy báo giá của hộ khác.
7. Admin xác minh hộ, đăng tin, quản lý sự kiện, xem thống kê; không có chức năng phân lô.
8. Giao diện đẹp, ảnh được tối ưu, các luồng demo chạy ổn định.
