# Giao diện và trải nghiệm

> Trạng thái: **Đề xuất**. Chưa có wireframe; nhóm (người điều phối frontend) duyệt trước khi làm hàng loạt. Giao diện là tiêu chí chấm trực tiếp (đề gốc §4.4), không phải phần trang trí.

## 1. Định hướng

- **Image-centric**: ảnh là nội dung chính; chữ hỗ trợ. Thẻ sản phẩm/hộ/lô lấy ảnh làm trọng tâm, tỉ lệ khung cố định.
- **Bản sắc gốm Bát Tràng**: bảng màu lấy từ men lam, đất nung, men ngà; kiểu chữ có chân cho tiêu đề, không chân cho nội dung. Giá trị cụ thể khai báo một lần bằng biến CSS trong `:root` (đã có trong `frontend/src/styles.css`) — component không viết màu/cỡ chữ trực tiếp.
- **Kể chuyện**: hồ sơ hộ, trang sản phẩm và landing page lô trình bày như một câu chuyện có trình tự (con người → quy trình → sản phẩm), không phải bảng thông số.
- Tránh giao diện kiểu sàn thương mại điện tử: không giỏ hàng, không nút "Mua ngay"; lời kêu gọi hành động là **"Gửi yêu cầu báo giá"** và **"Xem xưởng trên bản đồ"**.

## 2. Bản đồ màn hình và tuyến đường

Đường dẫn tiếng Việt không dấu (đã có `/san-pham`).

| Đường dẫn | Màn hình | Quyền | FR |
|---|---|---|---|
| `/` | Trang chủ triển lãm | Công khai | FR-HOME |
| `/ho-gia-dinh` · `/ho-gia-dinh/:slug` | Danh sách hộ · Hồ sơ hộ | Công khai | FR-WS |
| `/san-pham` · `/san-pham/:slug` | Danh mục (lọc hộ/phân loại) · Chi tiết + quy trình | Công khai | FR-PROD |
| `/lo-hang` | Danh sách lô công khai | Công khai | FR-BATCH-07 |
| `/lo/:publicCode` | **Landing page lô** (đích của QR) | Công khai / liên kết riêng | FR-BATCH-02..06 |
| `/ban-do` | Bản đồ làng nghề | Công khai | FR-MAP |
| `/tin-tuc` · `/tin-tuc/:slug` | Tin tức | Công khai | FR-NEWS |
| `/dang-nhap` · `/dang-ky` | Đăng nhập · Đăng ký Buyer | Công khai | FR-AUTH |
| `/mua-si/yeu-cau` · `/mua-si/yeu-cau/moi` · `/mua-si/yeu-cau/:id` | Yêu cầu của tôi · Tạo · Chi tiết + so sánh báo giá | BUYER | FR-QUOTE |
| `/xuong` | Bảng điều khiển của hộ | ARTISAN | — |
| `/xuong/ho-so` | Sửa hồ sơ, tọa độ, giờ mở cửa, chứng nhận | ARTISAN | FR-WS |
| `/xuong/san-pham[/:id]` | Quản lý sản phẩm + công đoạn | ARTISAN | FR-PROD |
| `/xuong/lo-hang[/:id]` | Quản lý lô, mốc sản xuất, QR, công khai/riêng tư | ARTISAN | FR-BATCH |
| `/xuong/yeu-cau[/:id]` | Yêu cầu nhận được, gửi báo giá, cập nhật tiến độ | ARTISAN | FR-QUOTE |
| `/quan-tri` | Thống kê | ADMIN | FR-ADMIN-02 |
| `/quan-tri/ho-gia-dinh` | Cấp tài khoản, duyệt hộ | ADMIN | FR-AUTH-03, FR-ADMIN-01 |
| `/quan-tri/tin-tuc` | Quản lý tin | ADMIN | FR-NEWS-01 |
| `/quan-tri/noi-bat` · `/quan-tri/bao-cao` | Nổi bật · Báo cáo vi phạm | ADMIN | FR-ADMIN-03, 04 |

Ba khu vực có bố cục riêng: **công khai** (header + footer giàu hình ảnh), **khu quản lý của hộ/buyer** và **khu quản trị** (thanh bên, gọn, thiên về thao tác).

## 3. Màn hình trọng điểm

1. **Landing page lô** — mở từ điện thoại sau khi quét QR, thiết kế mobile-first: ảnh lô → tên, hộ sản xuất (kèm huy hiệu xác minh) → dòng thời gian mốc sản xuất của lô (ngày + ảnh) → quy trình của sản phẩm → câu chuyện hộ → nút xem xưởng trên bản đồ / gửi yêu cầu báo giá.
2. **Bản đồ** — bản đồ chiếm phần lớn màn hình; ghim hai kiểu (đang mở / đóng — ý nghĩa chờ Q-29); popup có ảnh, tên, giờ mở cửa, liên kết hồ sơ; bộ lọc "Đang mở cửa".
3. **Danh mục sản phẩm** — lưới ảnh, bộ lọc hộ + phân loại, tìm kiếm, phân trang; chỉ tải thumbnail.
4. **So sánh báo giá (Buyer)** — bảng/thẻ các báo giá: hộ, giá, thời gian; nút chấp nhận có hộp xác nhận; thanh tiến trình 6 trạng thái.
5. **Trình sửa quy trình và mốc sản xuất (Artisan)** — thêm/xóa/sắp xếp công đoạn, kéo thả nhiều ảnh, xem trước.

## 4. Quy tắc cho mọi màn hình

- Ba trạng thái bắt buộc: đang tải (khung xương), rỗng (có hướng dẫn hành động tiếp theo), lỗi (thông báo + nút thử lại).
- Ảnh: luôn có `width`/`height` hoặc `aspect-ratio`, `loading="lazy"`, `alt` có nghĩa; danh sách dùng thumbnail, chi tiết dùng cỡ vừa/lớn; có ảnh thay thế khi lỗi (đã có `ProductImage`).
- Form: nhãn rõ ràng, lỗi hiển thị cạnh trường bằng tiếng Việt, vô hiệu hóa nút khi đang gửi, hành động không hoàn tác được phải xác nhận.
- Responsive: thử ở 390, 768, 1280 px. Vùng bấm tối thiểu 44 px trên di động.
- Trợ năng cơ bản: HTML ngữ nghĩa, điều hướng được bằng bàn phím, tương phản đủ, `lang="vi"`.
- Mọi chữ hiển thị bằng tiếng Việt; dùng đúng thuật ngữ ở `01-requirements/glossary.md`.
- Định dạng: tiền `1.250.000 ₫`, ngày `dd/MM/yyyy`. Giá bị hộ ẩn hiển thị "Liên hệ báo giá".

## 5. Tham khảo và kiểu chữ

### 5.1. Các trang đã khảo sát (2026-11-13)

Font, cỡ chữ và màu được đo trực tiếp trên trang bằng trình duyệt; phần bố cục là nhận xét từ ảnh chụp màn hình đầu trang.

| Trang | Loại | Kiểu chữ đo được | Điều đáng học | Điều nên tránh |
|---|---|---|---|---|
| [vietnam.travel — Bát Tràng](https://vietnam.travel/vi/things-to-do/full-day-trip-bat-trang-pottery-village) | Cổng du lịch quốc gia | Tiêu đề Noto Serif Display 800, 64 px; nội dung Noto Serif 18 px màu xám ấm `rgb(98,91,83)`; điều hướng Noto Sans Display | Tiêu đề serif rất lớn đặt ngay dưới ảnh rộng; nội dung serif 18 px dễ đọc; nền trắng, ảnh là thứ duy nhất có màu | Thanh điều hướng nhiều tầng, quá nhiều mục |
| [Nhân Dân — chuyên đề làng nghề](https://special.nhandan.vn/lang-nghe-trong-phat-trien-kinh-te-tu-nhan-Tiem-nang-va-co-hoi-phat-trien/index.html) | Bài dài (longform) | PT Serif + Lato + font riêng của báo | Ảnh tràn toàn màn hình mở đầu, chữ đi sau; nhịp ảnh lớn – đoạn chữ ngắn – ảnh lớn. Hợp cho trang hồ sơ hộ và trang lô | — |
| [Tạp chí Công Thương — eMagazine Bát Tràng](https://tapchicongthuong.vn/magazine/emagazine--lang-gom-bat-trang-cau-chuyen-thanh-cong-tu-chuong-trinh-ocop-294506.htm) | eMagazine | Oswald (chữ hẹp, in hoa) + Great Vibes (chữ viết tay); nội dung sans 20 px | Ảnh mở đầu là **bàn tay trên bàn xoay** — hình ảnh đặc trưng nhất của nghề, hơn mọi ảnh sản phẩm | Font viết tay kiểu thiệp cưới cho chữ "Làng gốm": cách gợi "truyền thống" đã quá quen |
| [Hanoia](https://hanoia.com) | Thương hiệu sơn mài cao cấp | Gotham cho mọi thứ, tiêu đề in hoa 36 px | Tối giản, nhiều khoảng trắng, ảnh sản phẩm chụp kỹ làm việc chính | In hoa toàn bộ với tiếng Việt làm dấu chồng lên nhau, khó đọc |
| [Tạp chí Làng nghề Việt Nam](https://langngheviet.com.vn) | Cổng tin tức | Noto Sans + UTM Avo, nội dung 14 px | Cách phân loại tin làng nghề (OCOP, nghệ nhân, thị trường) — tham khảo cho module tin tức | Mật độ dày kiểu báo điện tử, chữ nhỏ, không hợp triển lãm |
| [CHUS](https://chus.vn) | Sàn quà tặng thủ công | Không đo được (CSS chưa tải lúc khảo sát) | Cách nhóm sản phẩm theo thương hiệu thủ công | Là sàn bán lẻ có giỏ hàng — đúng thứ vCraft không làm |

Nhận xét chung:

- Hầu hết trang Việt Nam dùng họ **Noto** hoặc font hình học kiểu Gotham/Avo, lý do thực dụng: đủ dấu tiếng Việt. Kết quả là các trang trông giống nhau.
- Hai cách gợi "truyền thống" phổ biến là font viết tay và hoa văn trang trí. Trang trông sang nhất (vietnam.travel, Hanoia) lại không dùng cả hai, mà để ảnh và khoảng trắng làm việc.
- Trang nào mạnh cũng mở đầu bằng **một ảnh lớn về con người hoặc bàn tay làm nghề**, không phải lưới sản phẩm.
- Không trang nào có trang truy xuất theo lô hay bản đồ xưởng — đây là chỗ vCraft khác biệt, nên đầu tư thiết kế vào trang lô và bản đồ.

### 5.2. Kiểu chữ đã chọn

| Vai trò | Font | Biến CSS | Dùng cho |
|---|---|---|---|
| Tiêu đề | **Alegreya** (400–800, có nghiêng) | `--font-display` | Tên thương hiệu, `h1`–`h2`, tên hộ, tên sản phẩm, trích dẫn lời nghệ nhân (nghiêng) |
| Nội dung và giao diện | **Be Vietnam Pro** (400, 500, 600, 700) | `--font-body` | Đoạn văn, điều hướng, nút, form, bảng, toàn bộ khu quản lý |

Lý do:

- **Alegreya** là serif có nét bút rõ: nét đậm nhạt thay đổi như đường bút lông vẽ men lam trên gốm Bát Tràng. Nó gợi thủ công bằng chính hình chữ, không cần đến font viết tay. Chữ hơi hẹp nên tiêu đề tiếng Việt dài vẫn gọn; dải độ đậm rộng đủ cho mọi cấp tiêu đề.
- **Be Vietnam Pro** được thiết kế riêng cho tiếng Việt: dấu thanh và dấu mũ chồng nhau (ế, ỗ, ữ) được đặt đúng vị trí, không bị dính vào dòng trên ở cỡ nhỏ. Đây là điểm yếu của nhiều font Latin thông dụng.
- Hai font khác nhau rõ rệt (serif nét bút / sans hình học) nên phân cấp tiêu đề – nội dung nhìn là thấy, và không trùng với cặp Noto mà các trang khảo sát đang dùng.
- Cả hai đều miễn phí (giấy phép OFL) và đã kiểm tra có bộ ký tự `vietnamese` trên Google Fonts.

Quy tắc dùng:

- Chiều cao dòng tiêu đề **1.2** (`--leading-display`), không thấp hơn: tiếng Việt có dấu chồng, 1.1–1.15 sẽ làm dấu dòng dưới chạm chữ dòng trên. Nội dung 1.6.
- **Không in hoa toàn bộ** tiêu đề hay nhãn tiếng Việt (dấu trên chữ hoa rất khó đọc). Nhấn mạnh bằng độ đậm hoặc cỡ chữ.
- Không thêm font thứ ba, không dùng font viết tay.
- Số liệu trong bảng (giá, số lượng) dùng Be Vietnam Pro với `font-variant-numeric: tabular-nums`.
- Font đang tải từ Google Fonts qua `<link>` trong `frontend/index.html`. Trước khi triển khai chính thức nên cân nhắc tự lưu file font trong dự án để trang không phụ thuộc dịch vụ ngoài.

## 6. Việc còn thiếu

- [ ] Wireframe cho 5 màn hình trọng điểm.
- [x] Kiểu chữ: Alegreya + Be Vietnam Pro (mục 5.2).
- [ ] Chốt bảng màu và bộ ảnh thật (tự chụp hoặc có giấy phép sử dụng — ghi nguồn). Ảnh nên có ít nhất một ảnh bàn tay nghệ nhân làm nghề cho đầu trang chủ.
- [x] Style: CSS thuần + biến CSS + CSS Modules, không dùng thư viện UI (Q-18 đã chốt, ADR-0003).
