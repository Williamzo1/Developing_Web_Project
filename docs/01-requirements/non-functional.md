# Yêu cầu phi chức năng

| Mã | Yêu cầu | Cách kiểm |
|---|---|---|
| NFR-UI-01 | Image-centric, có bản sắc gốm Bát Tràng, phân cấp nội dung rõ. | Duyệt chéo trong nhóm theo `02-design/ui-ux.md`. |
| NFR-UI-02 | Responsive từ 390 px (điện thoại) đến màn hình máy tính. Landing page lô ưu tiên điện thoại vì được mở bằng cách quét QR. | Thử ở 390, 768, 1280 px. |
| NFR-UI-03 | Toàn bộ chữ trên giao diện và thông báo lỗi bằng tiếng Việt. | Rà soát. |
| NFR-UI-04 | Có trạng thái đang tải, rỗng, lỗi (kèm thử lại) cho mọi màn hình gọi API. | Rà soát. |
| NFR-PERF-01 | Danh sách hàng trăm sản phẩm vẫn mượt: phân trang phía server, chỉ tải thumbnail, lazy loading. | Seed ≥ 300 sản phẩm, đo trang danh mục. |
| NFR-PERF-02 | Không có truy vấn N+1 ở các endpoint danh sách. | Xem log SQL khi phát triển. |
| NFR-SEC-01 | Mật khẩu băm BCrypt; token phiên chỉ lưu mã băm trong DB (đã có). | Xem mã. |
| NFR-SEC-02 | Mọi endpoint ghi dữ liệu kiểm tra vai trò **và** quyền sở hữu. | Test "sai chủ sở hữu". |
| NFR-SEC-03 | Dữ liệu riêng tư (lô riêng tư, yêu cầu, báo giá) không lộ qua bất kỳ endpoint công khai hay endpoint của vai trò khác. | Test phân quyền. |
| NFR-SEC-04 | Validate đầu vào ở backend; upload chỉ nhận ảnh (JPEG/PNG/WebP), có giới hạn dung lượng. | Test đầu vào xấu. |
| NFR-SEC-05 | Không commit bí mật (mật khẩu DB thật, khóa API). Cấu hình qua biến môi trường. | Rà soát trước khi merge. |
| NFR-DATA-01 | Toàn vẹn khi truy cập đồng thời ở luồng báo giá: không thể có hai báo giá cùng được chấp nhận cho một yêu cầu. | Test gửi song song. |
| NFR-DATA-02 | Mọi thay đổi schema qua Flyway migration; không sửa migration đã merge. | Review. |
| NFR-OPS-01 | Chạy được toàn bộ hệ thống bằng một lệnh Docker Compose kèm dữ liệu mẫu đủ để demo. | Chạy trên máy sạch. |
| NFR-OPS-02 | Dữ liệu demo (seed) đủ phong phú: nhiều hộ, sản phẩm có ảnh thật, lô có QR, yêu cầu ở nhiều trạng thái. | Chạy kịch bản `04-plan/demo-script.md`. |
| NFR-OPS-03 | Triển khai lên Internet. | Giả định Q-09: không bắt buộc; ưu tiên chạy ổn định trên máy demo. QR cần URL gốc cấu hình được để điện thoại quét được trong mạng LAN. |
