# Yêu cầu chức năng

Mỗi yêu cầu có mã cố định `FR-<MODULE>-<số>`. Dùng mã này trong commit, PR, tên test và khi trao đổi với AI. Không đánh số lại; yêu cầu bị bỏ thì ghi "Đã bỏ", không xóa dòng.

- **Ưu tiên**: `P0` bắt buộc cho buổi demo (gắn trực tiếp đề gốc hoặc tiêu chí nghiệm thu) · `P1` nên có · `P2` có thì tốt.
- **Nguồn**: `ĐG` = đề gốc (kèm mục) · `ĐT` = đặc tả (kèm mục) · `N` = nhóm đề xuất.
- **Chắc chắn**: `Chốt` = đã rõ · `Giả định` = đang làm theo giả định ở [open-questions.md](open-questions.md) (ghi mã Q).
- Tình trạng cài đặt **không** ghi ở đây mà ở [../04-plan/status.md](../04-plan/status.md).

## HOME — Trang chủ và triển lãm số

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-HOME-01 | Trang chủ giới thiệu làng nghề: lịch sử, giá trị văn hóa, ảnh nổi bật. | P0 | ĐT 4.1 | Chốt |
| FR-HOME-02 | Hiển thị hộ tiêu biểu, sản phẩm và lô hàng nổi bật (do Admin chọn, xem FR-ADMIN-03). | P1 | ĐT 4.1 | Chốt |
| FR-HOME-03 | Điều hướng rõ đến: hộ gia đình, sản phẩm, lô hàng, bản đồ, tin tức. | P0 | ĐT 4.1 | Chốt |

## AUTH — Tài khoản và phân quyền

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-AUTH-01 | Đăng nhập, đăng xuất, xem thông tin tài khoản hiện tại. | P0 | ĐT 6 | Chốt |
| FR-AUTH-02 | Phân quyền theo 3 vai trò `BUYER`, `ARTISAN`, `ADMIN` ở cả API và giao diện (route guard). | P0 | ĐG 3, ĐT 6 | Chốt |
| FR-AUTH-03 | Admin cấp tài khoản cho hộ; hộ chưa được duyệt không đăng nhập được. | P0 | ĐG 3 | Chốt |
| FR-AUTH-04 | Buyer tự đăng ký tài khoản, có xác thực email và số điện thoại. | P0 | N | Chốt (Q-12); cách xác thực: Giả định Q-39 |
| FR-AUTH-05 | Khách xem nội dung công khai không cần đăng nhập; tạo yêu cầu mua sỉ thì phải đăng nhập. | P0 | ĐT 5 | Chốt (Q-01) |
| FR-AUTH-06 | Đổi mật khẩu của chính mình. | P2 | N | Chốt |

## WS — Hồ sơ hộ gia đình / xưởng

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-WS-01 | Mỗi hộ có trang hồ sơ công khai: tên, giới thiệu, lịch sử làm nghề, ảnh xưởng, liên hệ. | P0 | ĐG 3, ĐT 4.2 | Chốt |
| FR-WS-02 | Hộ tự sửa hồ sơ của mình (văn bản + bộ ảnh). | P0 | ĐG 3 | Chốt |
| FR-WS-03 | Hiển thị chứng nhận chất lượng (tên, đơn vị cấp, ảnh). | P1 | ĐG 3, ĐT 4.2 | Chốt |
| FR-WS-04 | Hồ sơ hộ lưu Gmail và Facebook làm thông tin xác minh; Admin dựa vào đó để duyệt. Hiển thị huy hiệu "Đã xác minh bởi Ban quản lý". Không lưu giấy tờ. | P1 | ĐT 4.2 | Chốt (Q-07) |
| FR-WS-05 | Hộ khai tọa độ, giờ mở cửa, có nhận khách tham quan/trải nghiệm hay không. | P0 | ĐG 3–4.3, ĐT 4.6 | Tọa độ: Chốt · Trạng thái mở cửa/nhận đơn: chờ Q-29 |
| FR-WS-06 | Danh sách hộ gia đình (công khai), có tìm theo tên. | P0 | ĐT 4.1 | Chốt |
| FR-WS-07 | Trang hồ sơ liệt kê sản phẩm và lô công khai của hộ. | P0 | ĐG 3 | Chốt |

## PROD — Sản phẩm và quy trình sản xuất

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-PROD-01 | Hộ tạo/sửa/ẩn loại sản phẩm của mình: tên, mô tả, phân loại, bộ ảnh, giá. Hộ chọn **công khai hoặc ẩn giá**; ẩn thì hiển thị "Liên hệ báo giá". | P0 | ĐG 3, ĐT 4.3 | Chốt (Q-30) |
| FR-PROD-02 | Mỗi sản phẩm có quy trình sản xuất gồm nhiều công đoạn có thứ tự; mỗi công đoạn có tên, mô tả, ảnh. | P0 | ĐG 3–4.1, ĐT 4.3 | Chốt |
| FR-PROD-03 | Danh mục sản phẩm công khai, lọc theo **hộ** và theo **phân loại**. | P0 | ĐG 3 | Chốt |
| FR-PROD-04 | Tìm kiếm theo tên, sắp xếp, phân trang danh mục. | P1 | ĐT 6 | Chốt |
| FR-PROD-05 | Trang chi tiết sản phẩm: ảnh, mô tả, hộ sản xuất, quy trình, các lô công khai. URL dùng slug. | P0 | ĐT 4.3 | Chốt |
| FR-PROD-06 | Sản phẩm gắn nhiều **tag**; tag dùng để khớp yêu cầu mở với hộ (FR-QUOTE-03) và làm bộ lọc phụ. | P0 | N (Q-03) | Chốt là có tag; cách tổ chức: Giả định Q-38 |

## BATCH — Lô hàng, truy xuất nguồn gốc, QR

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-BATCH-01 | Hộ tạo/sửa lô của một sản phẩm: mã/tên lô, mô tả, ảnh, số lượng, giá tham khảo, khả năng cung ứng, thời gian dự kiến, loại (có sẵn / làm theo đơn). Giá của lô cũng chọn được công khai hoặc ẩn. | P0 | ĐT 4.4 | Chốt (Q-30) |
| FR-BATCH-02 | Mỗi lô có landing page riêng: thông tin lô + câu chuyện hộ + quy trình của sản phẩm. | P0 | ĐG 4.1, ĐT 4.4 | Chốt |
| FR-BATCH-03 | Lô có các **mốc sản xuất** riêng (ngày + ghi chú + ảnh chứng minh), hiển thị thành dòng thời gian trên landing page. | P0 | ĐG 4.1 | Chốt (Q-10) |
| FR-BATCH-04 | Hệ thống tự sinh **mã QR** cho mỗi lô; quét ra landing page của lô. Mọi sản phẩm cùng lô dùng chung QR. Hộ tải được ảnh QR để in hoặc gửi cho khách. QR của lô riêng tư mã hóa liên kết riêng (FR-BATCH-06). | P0 | ĐG 4.1 | Chốt (Q-06) |
| FR-BATCH-05 | Lô ở chế độ công khai hoặc riêng tư. Lô riêng tư không xuất hiện trong danh sách/tìm kiếm công khai. | P0 | ĐT 4.4, 5 | Chốt |
| FR-BATCH-06 | Lô riêng tư xem được qua liên kết riêng có mã bí mật do chủ lô tạo, tự gửi cho người mình muốn và thu hồi được. | P0 | ĐT 5 | Chốt (Q-02, Q-06) |
| FR-BATCH-07 | Danh sách lô công khai, có lọc theo sản phẩm/hộ/loại lô. | P1 | ĐT 4.4, 6 | Chốt |
| FR-BATCH-08 | Lô có một hộ chịu trách nhiệm; có thể ghi chú (văn bản) các hộ cùng tham gia. Không có chức năng điều phối. | P2 | ĐT 4.4 | Chốt (Q-05, Q-14) |

## QUOTE — Yêu cầu mua sỉ và báo giá

Chi tiết trạng thái và khóa bản ghi: [../02-design/quote-state-machine.md](../02-design/quote-state-machine.md).

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-QUOTE-01 | Buyer tạo yêu cầu ở trạng thái nháp: phân loại và **tag** sản phẩm, số lượng, yêu cầu kỹ thuật, thời hạn mong muốn, ghi chú, ảnh tham khảo (tùy chọn). | P0 | ĐG 3, ĐT 4.5 | Chốt |
| FR-QUOTE-02 | Buyer gửi yêu cầu: **chỉ định một hộ**, hoặc để **mở**. Hộ nhận đơn muốn rủ thêm hộ khác thì tự thu xếp ngoài nền tảng. | P0 | ĐT 4.5 | Chốt (Q-14, theo diễn giải ghi ở đó) |
| FR-QUOTE-03 | Yêu cầu mở hiển thị cho các hộ có sản phẩm mang tag trùng với tag của yêu cầu. | P0 | ĐG 3 ("các hộ phù hợp") | Chốt (Q-03); chi tiết tag: Giả định Q-38 |
| FR-QUOTE-04 | Hộ xem danh sách yêu cầu mình được tham gia và gửi báo giá: đơn giá/tổng giá, thời gian hoàn thành, ghi chú. Mỗi hộ tối đa một báo giá còn hiệu lực cho một yêu cầu; được sửa khi yêu cầu còn nhận báo giá (rút báo giá: tạm theo gợi ý Q-31). | P0 | ĐG 3–4.2, ĐT 4.5 | Chốt (Q-31) |
| FR-QUOTE-05 | Buyer xem và so sánh các báo giá gửi cho yêu cầu của mình. Hộ không thấy báo giá của hộ khác. | P0 | ĐT 4.5, 5 | Chốt |
| FR-QUOTE-06 | Buyer chấp nhận đúng một báo giá; các báo giá còn lại tự chuyển sang "không được chọn"; yêu cầu ngừng nhận báo giá. Thao tác phải an toàn khi có truy cập đồng thời. | P0 | ĐG 4.2 | Chốt (Q-04) |
| FR-QUOTE-07 | Sau khi được chấp nhận, hộ tự cập nhật `Đang sản xuất` → `Đã giao` như mốc thông tin. Không có hợp đồng/thanh toán. | P0 | ĐG 4.2 | Chốt (Q-11) |
| FR-QUOTE-08 | Buyer hủy yêu cầu trước khi chấp nhận báo giá. | P1 | N | Chốt |
| FR-QUOTE-09 | Hiển thị thông tin liên hệ của hai bên cho nhau sau khi có báo giá, để tự thương lượng ngoài nền tảng. | P1 | ĐT 4.5 | Chốt |
| FR-QUOTE-10 | Hộ thấy số yêu cầu mới trong trang quản lý (badge). Không gửi email thông báo. | P1 | N | Chốt (Q-08) |
| FR-QUOTE-12 | Yêu cầu có hạn chót nhận báo giá tùy chọn; quá hạn thì không nhận báo giá mới. | P1 | N | Chốt (Q-15) |
| FR-QUOTE-11 | Lịch sử chuyển trạng thái của yêu cầu (ai, lúc nào, từ đâu sang đâu). | P1 | N (phục vụ ĐG 4.2) | Chốt |

## MAP — Bản đồ làng nghề

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-MAP-01 | Trang bản đồ tương tác hiển thị ghim theo tọa độ của các xưởng đã duyệt có tọa độ. | P0 | ĐG 4.3 | Chốt |
| FR-MAP-02 | Bấm ghim hiện popup: tên hộ, ảnh, mô tả ngắn, giờ mở cửa, liên kết đến hồ sơ. | P0 | ĐG 4.3 | Chốt |
| FR-MAP-03 | Phân biệt trực quan xưởng **đang mở cửa đón khách** và xưởng đóng; có bộ lọc "đang mở". | P0 | ĐG 3 | Chờ Q-29 (cách xác định "đang mở") |
| FR-MAP-04 | Hộ chọn tọa độ bằng cách bấm trên bản đồ trong trang sửa hồ sơ. | P1 | N | Chốt |

## NEWS — Tin tức cộng đồng

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-NEWS-01 | Admin đăng/sửa/ẩn tin: tiêu đề, nội dung, ảnh bìa, loại (giá nguyên liệu, lịch cúp điện, chính sách, sự kiện/hội chợ). | P0 | ĐG 3, ĐT 4.7 | Chốt |
| FR-NEWS-02 | Trang danh sách và chi tiết tin công khai, lọc theo loại. | P0 | ĐG 3 | Chốt |
| FR-NEWS-03 | Tin loại sự kiện có thời gian và địa điểm. | P2 | ĐT 4.7 | Chốt |

## ADMIN — Quản trị nền tảng

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-ADMIN-01 | Danh sách hộ kèm trạng thái duyệt; duyệt hoặc khóa hộ. | P0 | ĐG 3, ĐT 4.7 | Chốt |
| FR-ADMIN-02 | Thống kê tổng quan: số hộ, sản phẩm, lô, yêu cầu mua sỉ (chỉ số tổng, không lộ nội dung riêng tư). | P1 | ĐT 4.7 | Chốt |
| FR-ADMIN-03 | Chọn hộ/sản phẩm/lô nổi bật cho trang chủ. | P1 | ĐT 4.7 | Chốt |
| FR-ADMIN-04 | Người dùng báo cáo nội dung vi phạm; Admin xem và ẩn nội dung. | P2 | ĐT 4.7 | Chốt |
| FR-ADMIN-05 | Nội dung của hộ hiển thị ngay, không duyệt trước; Admin **hậu kiểm** và ẩn sản phẩm/lô vi phạm. | P1 | N | Chốt (Q-32) |
| FR-ADMIN-06 | Admin quản lý danh sách tag. | P1 | N | Giả định Q-38 |

## MEDIA — Ảnh

| Mã | Yêu cầu | Ưu tiên | Nguồn | Chắc chắn |
|---|---|---|---|---|
| FR-MEDIA-01 | Upload nhiều ảnh trong một lần cho hồ sơ, sản phẩm, công đoạn, lô, mốc sản xuất, tin tức. | P0 | ĐG 4.4 | Chốt |
| FR-MEDIA-02 | Backend kiểm tra loại tệp và dung lượng, tạo các biến thể đã resize/nén (thumbnail, cỡ vừa, cỡ lớn). | P0 | ĐG 4.4 | Chốt |
| FR-MEDIA-03 | Trang danh sách chỉ tải thumbnail; dùng lazy loading và kích thước ảnh cố định để không giật layout. | P0 | ĐG 4.4 | Chốt |
| FR-MEDIA-04 | Sắp xếp lại, chọn ảnh bìa, xóa ảnh trong bộ ảnh. | P1 | N | Chốt |
