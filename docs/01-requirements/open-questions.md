# Câu hỏi mở — tài liệu họp nhóm

Đây là nơi **duy nhất** theo dõi những điều chưa rõ. Mỗi câu có bối cảnh, các phương án, **gợi ý** (của người soạn tài liệu — Claude) và chỗ để nhóm ghi kết quả.

## Cách dùng

- **Gợi ý cũng là giả định làm việc**: cho đến khi nhóm chốt, mã và tài liệu thiết kế tạm đi theo gợi ý để không bị chặn. Gợi ý không phải quyết định.
- Khi chốt một câu: điền dòng **Nhóm chốt** (phương án, ngày, ai chốt), đổi trạng thái trong bảng mục lục thành `Đã chốt`, rồi sửa các chỗ ghi "Giả định Q-xx" trong `functional-requirements.md` và tài liệu thiết kế liên quan trong cùng một lần thay đổi. Quyết định lớn thì thêm ADR.
- Câu đã chốt giữ lại để tra cứu, không xóa, không đánh số lại.
- AI gặp điểm mơ hồ mới: thêm câu hỏi (mã kế tiếp, hiện là **Q-37**) kèm gợi ý, rồi báo người dùng.
- Nhãn mức ảnh hưởng: 🔴 chặn schema/kiến trúc — chốt trước khi code · 🟡 ảnh hưởng một module · 🟢 chốt lúc nào cũng được.

## Đề xuất thứ tự bàn

1. **Buổi 1 (trước khi code):** nhóm A (thống nhất câu hỏi gửi giảng viên) → các câu 🔴 của nhóm B, C → nhóm E (chia việc, mốc thời gian).
2. **Buổi 2 (khi có wireframe):** nhóm D và các câu 🟡 còn lại.
3. Các câu 🟢: người phụ trách module tự đề xuất, nhóm xác nhận qua chat.

## Mục lục

| Mã | Vấn đề | Nhóm | Mức | Gợi ý ngắn | Trạng thái |
|---|---|---|---|---|---|
| Q-01 | Khách chưa đăng nhập xem được gì | A | 🟡 | Xem công khai tự do; đăng nhập để gửi yêu cầu | Mở |
| Q-03 | Yêu cầu mở hiển thị cho hộ nào | A | 🟡 | Hộ có sản phẩm cùng phân loại | Mở |
| Q-04 | Có nút chấp nhận báo giá không | A | 🔴 | Có | Mở |
| Q-05 | Nhiều hộ cùng cung ứng một lô | A | 🟢 | Một hộ chịu trách nhiệm + ghi chú | Mở |
| Q-06 | QR dẫn tới đâu | A | 🔴 | Landing page lô | Mở |
| Q-07 | Xác minh pháp nhân lưu gì | A | 🟢 | Chỉ trạng thái + ngày duyệt | Mở |
| Q-09 | Có bắt buộc triển khai Internet | A | 🟡 | Không bắt buộc | Mở |
| Q-10 | Lô có mốc sản xuất riêng không | A | 🔴 | Có, hai tầng | Mở |
| Q-11 | Giữ `In Production`/`Delivered` không | A | 🔴 | Giữ, dạng mốc thông tin | Mở |
| Q-02 | Cách xem lô riêng tư | B | 🟡 | Liên kết riêng có mã bí mật | Mở |
| Q-08 | Kênh thông báo | B | 🟢 | Badge trong trang quản lý | Mở |
| Q-12 | Buyer đăng ký thế nào | B | 🟡 | Tự đăng ký, không xác minh email | Mở |
| Q-14 | Chỉ định một hay nhiều hộ | B | 🟡 | Nhiều hộ | Mở |
| Q-15 | Hạn chót nhận báo giá | B | 🟡 | Trường tùy chọn, kiểm lúc gửi | Mở |
| Q-29 | "Đang mở cửa" xác định thế nào | B | 🟡 | Giờ theo thứ + công tắc tạm đóng | Mở |
| Q-30 | Hiển thị giá sản phẩm/lô | B | 🟡 | Giá tham khảo tùy chọn, trống thì "Liên hệ" | Mở |
| Q-31 | Sửa/rút/từ chối báo giá | B | 🟡 | Sửa, rút được; không từ chối lẻ | Mở |
| Q-32 | Kiểm duyệt nội dung trước hay sau | B | 🟡 | Hậu kiểm | Mở |
| Q-33 | Hộ tự đăng ký hay Admin cấp | B | 🟡 | Admin cấp (như hiện có) | Mở |
| Q-34 | Ai quản lý phân loại | B | 🟢 | Seed cố định | Mở |
| Q-35 | Cắt gì khi thiếu thời gian | B | 🟡 | Danh sách cắt theo thứ tự | Mở |
| Q-13 | Lưu ảnh ở đâu | C | 🔴 | Đĩa cục bộ qua volume, sau interface | Mở |
| Q-16 | Giữ token DB hay đổi JWT | C | 🔴 | Giữ token DB | Mở |
| Q-17 | Cấu trúc gói backend | C | 🔴 | Giữ theo lớp | Mở |
| Q-19 | Lấy dữ liệu ở frontend | C | 🔴 | Hook tự viết + client chung | Mở |
| Q-20 | Sinh QR ở backend hay frontend | C | 🟡 | Backend trả PNG | Mở |
| Q-21 | Resize ảnh: thư viện, kích thước, giới hạn | C | 🟡 | Thumbnailator, 3 cỡ, JPEG | Mở |
| Q-22 | Công cụ kiểm thử | C | 🟡 | Python cho HTTP + JUnit cho logic | Mở |
| Q-23 | Phương án demo/triển khai | C | 🟡 | Laptop + Compose là chính | Mở |
| Q-18 | CSS thuần hay thư viện UI | D | 🔴 | CSS thuần + CSS Modules | Mở |
| Q-24 | Nguồn ảnh và dữ liệu demo | D | 🟡 | Ảnh giấy phép tự do, ghi nguồn | Mở |
| Q-36 | Ngôn ngữ giao diện | D | 🟢 | Chỉ tiếng Việt | Mở |
| Q-25 | Chia việc theo tính năng | E | 🔴 | Ba luồng A/B/C | Mở |
| Q-26 | Mốc thời gian | E | 🔴 | Đếm ngược từ ngày thi | Mở |
| Q-27 | Review và merge | E | 🟡 | PR + 1 review, squash | Mở |
| Q-28 | Cách dùng AI trong nhóm | E | 🟡 | Một phiên một FR, người giao đọc diff | Mở |

---

## A. Cần hỏi giảng viên — nhóm thống nhất cách hỏi

Nhóm không tự chốt được các câu này, nhưng nên thống nhất **phương án mình muốn** rồi hỏi dạng xác nhận ("Nhóm em định làm X, như vậy có được không ạ?") — dễ được trả lời hơn câu hỏi mở. Bản nháp tin nhắn ở cuối mục.

### Q-10 🔴 Lô có cần mốc sản xuất riêng không?

- **Bối cảnh:** Đề gốc §4.1: "mỗi lô sản phẩm có 1 mảng các mốc thời gian và hình ảnh chứng minh". Đặc tả §4.3: quy trình dùng chung theo loại sản phẩm, không lặp lại cho từng lô. Hai câu này quyết định có bảng `batch_milestones` hay không.
- **Phương án:** (a) Chỉ quy trình chung ở sản phẩm. (b) Chỉ mốc theo lô như đề gốc. (c) Hai tầng: công đoạn chung ở sản phẩm + mốc riêng của lô (ngày, ảnh), nhập tùy chọn.
- **Gợi ý:** (c). Thỏa cả hai văn bản; phần thêm chỉ là một bảng và một form. Landing page lô nhờ đó có ngày tháng thật — chính là thứ làm "truy xuất nguồn gốc" thuyết phục khi demo. Nếu giảng viên nói (a) là đủ thì bỏ bớt, không phải làm lại.
- **Nhóm chốt:** _chưa_

### Q-11 🔴 Có giữ trạng thái `In Production` và `Delivered` không?

- **Bối cảnh:** Đề gốc §4.2 liệt kê đủ 6 trạng thái. Đặc tả §4.5, §7: không quản lý sản xuất/giao hàng.
- **Phương án:** (a) Dừng ở `Accepted`. (b) Giữ đủ 6, hai trạng thái cuối do hộ tự bấm, chỉ mang tính thông tin. (c) Quản lý giao hàng đầy đủ (xác nhận hai phía, khiếu nại).
- **Gợi ý:** (b). Hai trạng thái thêm gần như không tốn công (hai lần chuyển trạng thái, không có dữ liệu mới) nhưng khớp từng chữ với đề gốc. (c) trái đặc tả.
- **Nhóm chốt:** _chưa_

### Q-04 🔴 Có thao tác chấp nhận báo giá trên website không?

- **Bối cảnh:** Đặc tả để ngỏ ("nếu có"). Đề gốc yêu cầu `Buyer Accepted` và "logic khóa bản ghi" — chỗ duy nhất trong hệ thống có tranh chấp đồng thời thật sự là bước chấp nhận.
- **Phương án:** (a) Báo giá chỉ để tham khảo, liên hệ ngoài nền tảng. (b) Buyer chấp nhận đúng một báo giá; các báo giá khác tự chuyển "không được chọn".
- **Gợi ý:** (b). Không có bước này thì không có gì để khóa, mất điểm kỹ thuật §4.2. Giao diện ghi rõ "chấp nhận chỉ ghi nhận lựa chọn, không thay thế hợp đồng".
- **Nhóm chốt:** _chưa_

### Q-06 🔴 QR dẫn đến đâu?

- **Phương án:** (a) Landing page của lô. (b) Trang quy trình của loại sản phẩm.
- **Gợi ý:** (a), trang lô nhúng luôn quy trình của sản phẩm. Đề gốc ghi "các sản phẩm cùng lô sẽ cùng QR code của lô" nên QR phải gắn với lô.
- **Nhóm chốt:** _chưa_

### Q-01 🟡 Khách chưa đăng nhập xem được gì?

- **Phương án:** (a) Mọi thứ công khai xem tự do; chỉ gửi yêu cầu báo giá mới cần đăng nhập. (b) Phải đăng nhập mới xem chi tiết.
- **Gợi ý:** (a). Người quét QR là khách vãng lai, bắt đăng nhập thì landing page vô nghĩa.
- **Nhóm chốt:** _chưa_

### Q-03 🟡 Yêu cầu mở hiển thị cho hộ nào?

- **Phương án:** (a) Mọi hộ đã duyệt. (b) Hộ có ít nhất một sản phẩm thuộc phân loại của yêu cầu. (c) Hệ thống chấm điểm phù hợp.
- **Gợi ý:** (b). Khớp chữ "các hộ gia đình phù hợp" của đề gốc, cài bằng một câu truy vấn. (c) quá sức với bài tập.
- **Cần bàn thêm:** hộ mới chưa có sản phẩm sẽ không thấy yêu cầu mở nào — chấp nhận được không?
- **Nhóm chốt:** _chưa_

### Q-09 🟡 Có bắt buộc triển khai lên Internet và dùng dữ liệu thật không?

- **Gợi ý:** Hỏi rõ. Tạm coi là không bắt buộc, dữ liệu minh họa. Liên quan Q-23.
- **Nhóm chốt:** _chưa_

### Q-05 🟢 Lô do nhiều hộ cùng cung ứng thể hiện thế nào?

- **Phương án:** (a) Một hộ chịu trách nhiệm, các hộ khác ghi bằng văn bản. (b) Lô liên kết nhiều hộ có vai trò.
- **Gợi ý:** (a). Đặc tả nói các hộ tự thỏa thuận; (b) kéo theo phân quyền nhiều chủ rất rắc rối.
- **Nhóm chốt:** _chưa_

### Q-07 🟢 Xác minh pháp nhân cần lưu gì?

- **Phương án:** (a) Chỉ trạng thái đã duyệt + ngày duyệt. (b) Lưu tệp giấy tờ/con dấu.
- **Gợi ý:** (a). Chứng nhận chất lượng do hộ tự đăng ảnh là đủ cho mục "chứng nhận" của đề gốc; lưu giấy tờ pháp lý kéo theo yêu cầu bảo mật không cần thiết.
- **Nhóm chốt:** _chưa_

### Bản nháp tin nhắn gửi giảng viên

> Thưa thầy/cô, nhóm em xin xác nhận vài điểm trước khi lập trình vCraft:
> 1. Nhật ký sản xuất: nhóm em định cho mỗi loại sản phẩm một quy trình dùng chung, và mỗi lô có thêm các mốc ngày + ảnh riêng. Như vậy có đáp ứng mục 4.1 không ạ?
> 2. Luồng báo giá: nhóm em giữ đủ 6 trạng thái, Buyer chấp nhận một báo giá trên web; "Đang sản xuất" và "Đã giao" do hộ tự cập nhật, không kèm hợp đồng/thanh toán. Như vậy có được không ạ?
> 3. Mã QR của lô dẫn đến trang giới thiệu lô (có kèm quy trình). Đúng ý đề chưa ạ?
> 4. Yêu cầu báo giá không chỉ định hộ sẽ hiện cho các hộ có sản phẩm cùng phân loại. Như vậy có được coi là "hộ phù hợp" không ạ?
> 5. Khách xem nội dung công khai không cần đăng nhập, chỉ đăng nhập khi gửi yêu cầu báo giá — được không ạ?
> 6. Buổi vấn đáp có yêu cầu web chạy trên Internet không, hay chạy trên máy nhóm là đủ ạ?

---

## B. Nghiệp vụ — nhóm tự chốt

### Q-02 🟡 Người ngoài xem lô riêng tư bằng cách nào?

- **Phương án:** (a) Liên kết riêng chứa mã bí mật, chủ lô tạo và thu hồi được. (b) Mời theo tài khoản Buyer. (c) Lô riêng tư chỉ chủ lô xem.
- **Gợi ý:** (a). Một cột `share_token`, không cần bảng phân quyền; demo trực quan (mở liên kết thì thấy, bỏ mã thì 404). (b) chặt chẽ hơn nhưng tốn công gấp nhiều lần.
- **Nhóm chốt:** _chưa_

### Q-12 🟡 Buyer có tài khoản bằng cách nào?

- **Phương án:** (a) Tự đăng ký tên đăng nhập + mật khẩu. (b) Đăng ký có xác minh email. (c) Admin cấp.
- **Gợi ý:** (a), kèm thông tin liên hệ (tên hiển thị, điện thoại/email) để hộ gọi lại. Xác minh email cần dịch vụ gửi thư — không đáng cho bài tập.
- **Nhóm chốt:** _chưa_

### Q-33 🟡 Hộ tự đăng ký chờ duyệt, hay chỉ Admin cấp tài khoản?

- **Bối cảnh:** Mã hiện tại: Admin tạo xưởng + tài khoản, rồi duyệt. Đề gốc: "kiểm duyệt và cấp tài khoản".
- **Phương án:** (a) Giữ nguyên: chỉ Admin cấp. (b) Thêm form hộ tự đăng ký → chờ Admin duyệt.
- **Gợi ý:** (a). Đã chạy, đã có test, đúng chữ "cấp tài khoản". (b) để P2 nếu dư thời gian.
- **Nhóm chốt:** _chưa_

### Q-14 🟡 Yêu cầu chỉ định gửi được cho một hay nhiều hộ?

- **Gợi ý:** Nhiều hộ. Bảng nối `quote_request_targets` xử lý cả hai trường hợp; chỉ cho một hộ thì luồng "so sánh báo giá" mất ý nghĩa.
- **Nhóm chốt:** _chưa_

### Q-15 🟡 Yêu cầu có hạn chót nhận báo giá không?

- **Phương án:** (a) Không có hạn; Buyer tự chấp nhận hoặc hủy. (b) Hạn tùy chọn, kiểm tra lúc hộ gửi báo giá. (c) Hạn bắt buộc + job nền tự đóng.
- **Gợi ý:** (b). Không cần job nền; quá hạn thì chỉ từ chối báo giá mới, Buyer vẫn chấp nhận được báo giá đã có.
- **Nhóm chốt:** _chưa_

### Q-31 🟡 Hộ có sửa/rút báo giá được không? Buyer có từ chối từng báo giá không?

- **Phương án:** (a) Gửi rồi là cố định. (b) Sửa và rút được cho đến khi yêu cầu được chấp nhận; không có "từ chối" riêng lẻ. (c) Thêm từ chối từng báo giá + thương lượng nhiều vòng.
- **Gợi ý:** (b). Sửa/rút tạo ra tình huống tranh chấp với thao tác chấp nhận — đúng thứ cần để trình diễn khóa bản ghi. (c) thành hệ thống đàm phán, vượt phạm vi.
- **Nhóm chốt:** _chưa_

### Q-29 🟡 "Xưởng đang mở cửa" xác định thế nào?

- **Bối cảnh:** Đề gốc: bản đồ "để biết xưởng nào đang mở cửa cho khách vào tham quan".
- **Phương án:** (a) Hộ tự bật/tắt công tắc "đang mở". (b) Tính từ giờ mở cửa theo thứ trong tuần. (c) Kết hợp: giờ theo thứ + công tắc "tạm đóng" + cờ "có nhận khách tham quan".
- **Gợi ý:** (c) nhưng làm theo bước: trước tiên (b) + cờ nhận khách, thêm công tắc tạm đóng sau. Tính `openNow` ở backend theo múi giờ Việt Nam để không lệch khi demo.
- **Nhóm chốt:** _chưa_

### Q-30 🟡 Hiển thị giá thế nào?

- **Phương án:** (a) Bắt buộc có giá. (b) Giá tham khảo tùy chọn; để trống thì hiện "Liên hệ báo giá". (c) Khoảng giá theo số lượng.
- **Gợi ý:** (b), ở cả sản phẩm và lô. Phù hợp định hướng mua sỉ (giá phụ thuộc số lượng); nút kêu gọi dẫn sang tạo yêu cầu báo giá.
- **Nhóm chốt:** _chưa_

### Q-32 🟡 Nội dung của hộ cần Admin duyệt trước khi hiển thị không?

- **Phương án:** (a) Duyệt trước từng sản phẩm/lô. (b) Hậu kiểm: hiển thị ngay, Admin ẩn khi vi phạm. 
- **Gợi ý:** (b). Chỉ tài khoản hộ cần duyệt trước (đã có). Duyệt trước từng nội dung làm demo chậm và thêm một hàng đợi không ai yêu cầu.
- **Nhóm chốt:** _chưa_

### Q-08 🟢 Thông báo cho người dùng qua kênh nào?

- **Gợi ý:** Chỉ số đếm "mới" trên menu trang quản lý của hộ và của Buyer. Không email, không realtime.
- **Nhóm chốt:** _chưa_

### Q-34 🟢 Ai quản lý danh sách phân loại?

- **Gợi ý:** Seed cố định cho gốm (cốc chén, bình lọ, đĩa bát, tượng trang trí, đồ thờ, quà tặng theo đơn…). Admin thêm/sửa là P2.
- **Nhóm chốt:** _chưa_

### Q-35 🟡 Thiếu thời gian thì cắt gì trước?

- **Gợi ý (cắt theo thứ tự):** báo cáo vi phạm (FR-ADMIN-04) → sự kiện có thời gian/địa điểm (FR-NEWS-03) → đổi mật khẩu (FR-AUTH-06) → ghi chú nhiều hộ (FR-BATCH-08) → nội dung nổi bật do Admin chọn (thay bằng "mới nhất") → liên kết riêng cho lô riêng tư (lô riêng tư chỉ chủ xem). **Không cắt:** bốn yêu cầu kỹ thuật §4.1–4.4 và giao diện các trang công khai.
- **Nhóm chốt:** _chưa_

---

## C. Kỹ thuật

### Q-16 🔴 Giữ cơ chế token lưu DB hay chuyển sang JWT?

- **Bối cảnh:** Mã đang dùng token ngẫu nhiên, DB lưu mã băm. Bản đánh giá v0 trong ghi chú nhóm gợi ý JWT.
- **Gợi ý:** Giữ nguyên. Đang chạy, có test, đăng xuất và khóa hộ có hiệu lực ngay. JWT không đem lại gì ở quy mô này mà phải viết lại. Chi tiết: ADR-0002.
- **Nhóm chốt:** _chưa_

### Q-17 🔴 Backend chia gói theo lớp hay theo module?

- **Phương án:** (a) Giữ theo lớp (`controller/`, `service/`…), đặt tên file theo module. (b) Chuyển sang theo module (`batch/`, `quote/`…).
- **Gợi ý:** (a). Sẽ có khoảng 10 controller — vẫn dễ tìm; đổi cấu trúc lúc này tốn công và gây xung đột khi ba người cùng làm. Nếu muốn (b) thì phải làm **ngay bây giờ**, trước khi chia việc.
- **Nhóm chốt:** _chưa_

### Q-13 🔴 Ảnh upload lưu ở đâu?

- **Phương án:** (a) Đĩa server qua Docker volume. (b) Dịch vụ ngoài (Cloudinary, S3…). (c) Lưu trong PostgreSQL.
- **Gợi ý:** (a), viết sau một interface `MediaStorage`. Demo không phụ thuộc mạng và tài khoản bên ngoài; và tự resize ở backend mới đúng yêu cầu §4.4 (Cloudinary resize hộ thì mất điểm kỹ năng). Nhớ sao lưu thư mục ảnh cùng dữ liệu seed.
- **Nhóm chốt:** _chưa_

### Q-19 🔴 Frontend lấy và lưu dữ liệu server thế nào?

- **Phương án:** (a) Mở rộng hook tự viết hiện có (`useCatalog`) thành `useApi`/`useMutation` + một client fetch chung; phiên đăng nhập bằng Context. (b) Thêm TanStack Query. (c) Redux/Zustand.
- **Gợi ý:** (a). Ít thứ phải học và phải giải thích khi vấn đáp. Chọn (b) nếu trong nhóm đã có người quen — nó giảm nhiều mã lặp cho tải lại sau khi sửa. (c) không cần.
- **Nhóm chốt:** _chưa_

### Q-20 🟡 Sinh mã QR ở backend hay frontend?

- **Phương án:** (a) Backend sinh PNG (ZXing) qua `GET /api/batches/{code}/qr.png`. (b) Frontend vẽ bằng thư viện JS.
- **Gợi ý:** (a). Hộ tải về in được, ảnh có URL ổn định, cùng chỗ với `APP_BASE_URL`. Đề cho phép cả hai.
- **Cần bàn thêm:** khi demo, điện thoại quét QR phải truy cập được máy chạy web → `APP_BASE_URL` là IP LAN hoặc tên miền (xem Q-23).
- **Nhóm chốt:** _chưa_

### Q-21 🟡 Resize ảnh: thư viện, kích thước, giới hạn?

- **Gợi ý:** Thumbnailator. Ba biến thể theo cạnh dài: 320 px (danh sách), 800 px (chi tiết), 1600 px (xem lớn); JPEG chất lượng khoảng 80 (WebP cần thêm plugin, để sau). Nhận JPEG/PNG/WebP, tối đa 8 MB mỗi ảnh và 10 ảnh mỗi lần. Xử lý đồng bộ trong request; bỏ metadata EXIF và xoay ảnh theo hướng chụp.
- **Cần bàn thêm:** có giữ ảnh gốc không? Gợi ý: không, chỉ giữ bản 1600 px để tiết kiệm đĩa.
- **Nhóm chốt:** _chưa_

### Q-22 🟡 Kiểm thử bằng công cụ gì?

- **Phương án:** (a) Tiếp tục script Python gọi HTTP (đang có `phase02.py`). (b) Chuyển hết sang JUnit + Spring test. (c) Cả hai: Python cho phân quyền/luồng đầu-cuối, JUnit cho logic thuần (bảng chuyển trạng thái, tính giờ mở cửa).
- **Gợi ý:** (c). Script Python đã có nền, viết nhanh, kiểm được cả ca hai request song song. Mỗi module một file `backend/tests/<module>.py`.
- **Nhóm chốt:** _chưa_

### Q-23 🟡 Demo và triển khai theo phương án nào?

- **Phương án:** (a) Laptop chạy Docker Compose; điện thoại quét QR qua cùng mạng (phát hotspot từ điện thoại). (b) Triển khai lên một máy chủ có tên miền. (c) Cả hai: (b) là chính, (a) dự phòng.
- **Gợi ý:** Chuẩn bị (a) trước cho chắc, làm (b) nếu Q-09 yêu cầu hoặc còn thời gian. Dù phương án nào cũng diễn tập trọn kịch bản trên đúng máy và mạng sẽ dùng, ít nhất một lần trước ngày thi.
- **Nhóm chốt:** _chưa_

---

## D. Giao diện và nội dung

### Q-18 🔴 CSS thuần hay dùng thư viện UI?

- **Phương án:** (a) CSS thuần + biến CSS + CSS Modules (Vite hỗ trợ sẵn). (b) Tailwind CSS. (c) Bộ component có sẵn (MUI, Ant Design…).
- **Gợi ý:** (a) cho khu công khai — giao diện là tiêu chí chấm và cần bản sắc riêng; bộ component có sẵn dễ ra giao diện "trang quản trị" na ná nhau. Nếu người làm frontend quen Tailwind thì (b) cũng ổn. Khu quản trị có thể dùng style đơn giản dùng chung.
- **Phải chốt trước khi chia việc**, vì đổi giữa chừng là viết lại toàn bộ style.
- **Nhóm chốt:** _chưa_

### Q-24 🟡 Ảnh và dữ liệu demo lấy từ đâu, ai chuẩn bị?

- **Bối cảnh:** Hiện chỉ có 3 ảnh SVG minh họa. Giao diện image-centric cần ảnh thật, chất lượng tốt.
- **Gợi ý:** Ưu tiên ảnh tự chụp (đi Bát Tràng một buổi nếu được) hoặc ảnh giấy phép tự do (Wikimedia Commons, Unsplash, Pexels), ghi nguồn trong một file danh sách. Mục tiêu seed: 6–8 hộ có tọa độ thật ở Bát Tràng, 60–100 sản phẩm thật + sinh thêm để đủ vài trăm cho bài toán hiệu năng, 4–5 lô có mốc sản xuất, 5–6 tin tức, vài yêu cầu ở các trạng thái khác nhau. Giao một người phụ trách.
- **Nhóm chốt:** _chưa_

### Q-36 🟢 Giao diện chỉ tiếng Việt?

- **Gợi ý:** Chỉ tiếng Việt. Đa ngôn ngữ không được yêu cầu.
- **Nhóm chốt:** _chưa_

---

## E. Cách làm việc

### Q-25 🔴 Chia việc thế nào?

- **Bối cảnh:** Ghi chú nhóm: Huy điều phối frontend, Thắng điều phối backend/DB, Dương fullstack/QA/triển khai; sau khi có nền thì chia theo tính năng.
- **Gợi ý:**
  - Nền tảng (M1): Huy — khung frontend, client API, đăng nhập, bố cục, design tokens. Thắng — validation, chuẩn hóa lỗi, phân trang, migration nền cho `workshops`/`products`. Dương — module ảnh (upload + resize), seed, Docker.
  - Song song (M2), mỗi người làm trọn API + giao diện: **A. Triển lãm** (hồ sơ hộ, sản phẩm, quy trình, danh mục, trang chủ) — Huy · **C. Báo giá** (state machine, khóa bản ghi) — Thắng · **B. Truy xuất + bản đồ** (lô, mốc, QR, Leaflet) — Dương.
  - Quản trị + tin tức (M3): ai xong trước nhận.
- **Cần bàn thêm:** mỗi người có muốn làm cả hai phía không; ai trình bày phần nào khi vấn đáp.
- **Nhóm chốt:** _chưa_

### Q-26 🔴 Các mốc thời gian?

- **Bối cảnh:** Chưa rõ ngày thi vấn đáp; kế hoạch 8 tuần trong ghi chú nhóm chưa gắn ngày.
- **Gợi ý:** Xác định ngày thi rồi đếm ngược: đóng băng tính năng trước thi 1 tuần (chỉ sửa lỗi, seed, tập demo); M2 xong trước đó 1 tuần; M1 gói trong 1 tuần kể từ khi chốt tài liệu. Điền ngày cụ thể vào `04-plan/roadmap.md`.
- **Nhóm chốt:** _chưa_

### Q-27 🟡 Quy trình review và merge?

- **Gợi ý:** Mỗi việc một nhánh + PR vào `master`; cần một người khác xem; squash merge; bật bảo vệ nhánh `master` trên GitHub. PR nhỏ (dưới vài trăm dòng) để review kịp. Ai thêm migration thì báo số trong nhóm chat để tránh trùng. Chi tiết: `03-engineering/git-workflow.md`.
- **Nhóm chốt:** _chưa_

### Q-28 🟡 Dùng AI trong nhóm theo quy tắc nào?

- **Gợi ý:** Cả nhóm dùng chung `AGENTS.md` và `docs/` làm ngữ cảnh. Mỗi phiên AI làm một FR hoặc một nhóm FR cùng module, trên nhánh riêng. Người giao việc đọc toàn bộ diff trước khi mở PR và phải giải thích được mã đó khi vấn đáp. AI không tự commit/push. Tài liệu lệch mã thì sửa ngay trong PR đó.
- **Nhóm chốt:** _chưa_
