# Thuật ngữ

Dùng **đúng một tên** cho mỗi khái niệm trong mã, API, cơ sở dữ liệu và giao diện. Cột "Trong mã" là tên tiếng Anh bắt buộc dùng cho định danh; cột "Trên giao diện" là chữ hiển thị tiếng Việt.

| Trên giao diện | Trong mã | Nghĩa |
|---|---|---|
| Hộ gia đình / Nghệ nhân | `ARTISAN` (vai trò) | Tài khoản của một hộ làm nghề. Mỗi tài khoản Artisan gắn đúng một xưởng. |
| Xưởng / Hồ sơ hộ | `workshop` | Đơn vị sản xuất của một hộ; có trang hồ sơ công khai, tọa độ, giờ mở cửa. Trong hệ thống "hộ" và "xưởng" là một thực thể. |
| Người mua sỉ | `BUYER` (vai trò) | Tài khoản đối tác mua số lượng lớn. |
| Khách | guest (không có tài khoản) | Người xem nội dung công khai, gồm cả khách du lịch quét QR. |
| Ban quản lý | `ADMIN` (vai trò) | Hội làng nghề/chủ tịch xã; quản trị nền tảng. |
| Phân loại | `category` | Nhóm sản phẩm (cốc, bình, đĩa…). |
| Sản phẩm (loại sản phẩm) | `product` | Một **loại** sản phẩm của một xưởng, mang nội dung trưng bày và quy trình sản xuất dùng chung. Không phải từng món hàng. |
| Công đoạn | `process_step` | Một bước trong quy trình sản xuất của sản phẩm (nhào đất, vuốt, nung…): tên, mô tả, ảnh. |
| Lô hàng | `batch` | Một đợt hàng cụ thể của một sản phẩm: số lượng, giá tham khảo, thời gian, trạng thái hiển thị. Đơn vị gắn QR. |
| Mốc sản xuất | `batch_milestone` | Sự kiện có ngày + ảnh chứng minh của một lô (ngày nung lô này…). Tầng "nhật ký sản xuất" theo đề gốc. |
| Landing page lô | batch page | Trang công khai (hoặc riêng tư) của một lô, đích đến của QR. |
| Yêu cầu mua sỉ / Yêu cầu báo giá | `quote_request` | Nhu cầu do Buyer tạo: loại sản phẩm, số lượng, yêu cầu kỹ thuật, thời hạn. |
| Yêu cầu chỉ định | targeted request | Yêu cầu gửi cho các hộ Buyer chọn. |
| Yêu cầu mở | open request | Yêu cầu không chỉ định hộ; các hộ phù hợp chủ động chào giá. |
| Báo giá / Chào giá | `quote` | Phản hồi của một hộ cho một yêu cầu: giá + thời gian hoàn thành. Tránh dùng `bid` trong mã mới. |
| Xác minh / Duyệt hộ | `approved` (hiện có) | Admin xác nhận hộ thuộc làng nghề. Chưa duyệt thì không đăng nhập được. |
| Tin tức cộng đồng | `news_post` | Thông báo, tin, sự kiện do Admin đăng. |
| Ảnh / Tệp phương tiện | `media` | Ảnh đã upload cùng các biến thể đã resize. |
