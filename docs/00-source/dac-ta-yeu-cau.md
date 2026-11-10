<!-- NGUỒN GỐC - KHÔNG SỬA. Trích tự động từ original/dac-ta-yeu-cau.docx ngày 2026-11-10; bảng bị dàn phẳng thành từng dòng. -->

  
vCraft  
ĐẶC TẢ YÊU CẦU PHẦN MỀM  
Nền tảng triển lãm số làng nghề và kết nối mua sỉ  
Bài tập lớn môn INT3306 – Phát triển ứng dụng webFall 2026  
  
Thông tin tài liệu  
Thuộc tính  
Nội dung  
Tên hệ thống  
vCraft  
Mục đích tài liệu  
Tổng hợp và chuẩn hóa các yêu cầu nghiệp vụ, chức năng và định hướng giao diện trước khi thiết kế, lập trình.  
Định hướng  
Tập trung vào trình bày, trải nghiệm người dùng và kết nối mua sỉ; không xây dựng hệ thống quản lý toàn bộ giao dịch.  
Đối tượng sử dụng  
Hộ gia đình/nghệ nhân, Buyer/đối tác mua sỉ, Ban quản lý làng nghề.  
Trạng thái  
Bản đặc tả ban đầu; các nội dung chưa được xác nhận được đánh dấu là đề xuất hoặc cần thống nhất.  
1. Tổng quan hệ thống  
vCraft là nền tảng web phục vụ việc quảng bá làng nghề truyền thống thông qua không gian triển lãm số cho từng hộ gia đình/nghệ nhân, giới thiệu sản phẩm và lô hàng, trình bày quy trình sản xuất, đồng thời kết nối các bên có nhu cầu mua bán sỉ.  
Hệ thống đóng vai trò giới thiệu thông tin và hỗ trợ kết nối. Các bên tự trao đổi, thương lượng và quản lý việc ký kết, thanh toán, sản xuất, giao nhận cũng như xử lý thiếu hụt hoặc lỗi hàng hóa. vCraft không thay thế các bên để điều hành giao dịch.  
2. Mục tiêu và nguyên tắc thiết kế  
Ưu tiên giao diện trực quan, giàu hình ảnh, có bản sắc làng nghề và trình bày tốt trên các kích thước màn hình.  
Tập trung vào giao dịch mua sỉ với số lượng lớn; không định hướng thành cửa hàng bán lẻ trực tuyến.  
Tách thông tin trưng bày sản phẩm/quy trình sản xuất khỏi thông tin giao dịch của từng lô hàng.  
Cho phép lô hàng được tạo từ sản phẩm đã sản xuất trước hoặc được sản xuất theo yêu cầu đặt hàng.  
Bảo đảm thông tin riêng tư chỉ hiển thị cho người được phép xem.  
Mỗi hộ gia đình là một đơn vị pháp nhân độc lập, có con dấu và tự chịu trách nhiệm về giao dịch của mình.  
Ban quản lý hỗ trợ quảng bá, xúc tiến đầu tư và duy trì môi trường hoạt động công bằng, đúng quy định; không đại diện hộ gia đình ký hợp đồng.  
3. Vai trò người dùng  
3.1. Hộ gia đình/Nghệ nhân (Artisan)  
Quản lý hồ sơ hộ gia đình, xưởng sản xuất, lịch sử làm nghề, hình ảnh và chứng nhận.  
Quản lý danh mục sản phẩm và quy trình sản xuất dùng chung cho các lô hàng cùng loại sản phẩm.  
Đăng, cập nhật và quản lý các lô hàng; chọn lô hàng công khai hoặc riêng tư.  
Nhận yêu cầu mua sỉ, chủ động liên hệ với Buyer và gửi báo giá.  
Tự thương lượng điều kiện giao dịch, tổ chức sản xuất và phân chia công việc cho các hộ khác nếu cần.  
3.2. Buyer/Đối tác mua sỉ  
Khám phá làng nghề, hộ gia đình, sản phẩm và lô hàng.  
Tìm lô hàng theo nhu cầu, xem thông tin được công khai và liên hệ bên bán.  
Tạo yêu cầu mua sỉ theo số lượng, loại sản phẩm, yêu cầu kỹ thuật và thời hạn dự kiến.  
Có thể chỉ định một hộ gia đình hoặc không chỉ định hộ để các bên bán phù hợp chủ động liên hệ.  
Xem các báo giá được gửi riêng cho mình và tự thương lượng, quyết định có tiếp tục giao dịch hay không.  
3.3. Ban quản lý làng nghề (Admin)  
Xác minh hộ gia đình và thông tin liên quan để tăng độ tin cậy của nền tảng.  
Quản lý tin tức, thông báo cộng đồng, sự kiện, hội chợ và chương trình xúc tiến đầu tư.  
Quảng bá hình ảnh làng nghề, giới thiệu nghệ nhân và các sản phẩm/lô hàng nổi bật.  
Tiếp nhận phản ánh, xem xét nội dung vi phạm quy định nền tảng và thực hiện biện pháp phù hợp.  
Theo dõi số liệu tổng quan về hoạt động trên nền tảng để phục vụ quảng bá và phát triển làng nghề.  
Không đứng ra ký hợp đồng thay hộ gia đình; không nhận vai trò điều phối hoặc chia lô đơn hàng cho các hộ.  
4. Yêu cầu chức năng  
4.1. Trang chủ và triển lãm số  
Giới thiệu làng nghề, lịch sử, giá trị văn hóa và các hoạt động nổi bật.  
Trình bày hình ảnh nổi bật, hộ gia đình tiêu biểu, sản phẩm và lô hàng đáng chú ý.  
Cung cấp điều hướng rõ ràng đến danh mục hộ gia đình, sản phẩm, lô hàng, bản đồ và tin tức.  
Thiết kế theo hướng image-centric: hình ảnh chất lượng tốt, bố cục nhất quán, nội dung dễ khám phá.  
4.2. Hồ sơ hộ gia đình  
Mỗi hộ có một trang hồ sơ riêng gồm tên, thông tin giới thiệu, lịch sử làm nghề, hình ảnh xưởng và thông tin liên hệ.  
Có thể hiển thị chứng nhận và trạng thái xác minh của Ban quản lý.  
Hiển thị địa điểm xưởng trên bản đồ nếu hộ cung cấp tọa độ.  
Thông tin pháp nhân được quản lý theo từng hộ độc lập; việc công khai từng trường thông tin cần tuân theo quyền riêng tư và quy định áp dụng.  
4.3. Sản phẩm và quy trình sản xuất  
Hộ gia đình quản lý danh mục các loại sản phẩm do mình sản xuất.  
Mỗi loại sản phẩm có trang mô tả quy trình sản xuất dùng chung cho các lô hàng cùng loại.  
Quy trình có thể gồm nhiều công đoạn; mỗi công đoạn có tên, mô tả và hình ảnh minh họa.  
Không yêu cầu lặp lại toàn bộ quy trình sản xuất cho từng lô hàng nếu các lô cùng loại dùng chung quy trình.  
Landing page lô hàng liên kết đến quy trình tương ứng để người xem hiểu cách sản phẩm được tạo ra.  
Mã QR dẫn đến landing page lô hàng hoặc trang truy xuất phù hợp; cách áp dụng cụ thể cần được thống nhất khi thiết kế.  
4.4. Quản lý lô hàng  
Lô hàng là đơn vị thông tin chính phục vụ việc giới thiệu nguồn cung và kết nối mua sỉ.  
Một lô hàng có thể là hàng đã sản xuất trước hoặc hàng được sản xuất theo đơn đặt hàng.  
Thông tin cơ bản dự kiến gồm mã/tên lô, hộ chịu trách nhiệm, loại sản phẩm, mô tả, hình ảnh, số lượng, giá tham khảo hoặc giá chào, khả năng cung ứng và thời gian dự kiến.  
Mỗi lô hàng có landing page riêng, trình bày thông tin giao dịch của lô và liên kết đến nội dung trưng bày/quy trình sản xuất dùng chung.  
Hộ gia đình được cập nhật thông tin và trạng thái hiển thị của lô hàng.  
Lô hàng có thể ở chế độ công khai hoặc riêng tư. Lô riêng tư không được hiển thị trong danh sách công khai và chỉ người được cấp quyền mới có thể xem.  
Nếu một lô hàng liên quan đến nhiều hộ, các hộ tự thỏa thuận việc phối hợp và phân chia công việc; hệ thống không cung cấp chức năng Admin phân lô hoặc điều phối sản xuất.  
4.5. Yêu cầu mua sỉ và báo giá  
Buyer có thể tạo yêu cầu mua sỉ với thông tin như loại sản phẩm, số lượng, yêu cầu kỹ thuật, thời hạn dự kiến và nội dung bổ sung.  
Buyer có thể chỉ định hộ gia đình nhận yêu cầu hoặc để yêu cầu mở để bên bán phù hợp chủ động liên hệ.  
Hộ gia đình có thể phản hồi yêu cầu và gửi báo giá, bao gồm mức giá và thời gian dự kiến cung ứng.  
Thông tin báo giá của từng hộ được giữ riêng tư; Buyer xem được các báo giá gửi đến mình, còn các hộ không được mặc định xem báo giá của nhau.  
Buyer và hộ gia đình tự trao đổi, thương lượng và quyết định việc tiếp tục giao dịch. Việc chấp nhận báo giá trên nền tảng, nếu có, không được hiểu là hệ thống quản lý hoặc thay thế hợp đồng giữa các bên.  
Không xây dựng quy trình bắt buộc để quản lý hợp đồng, thanh toán, giao hàng hoặc xử lý khiếu nại thương mại.  
4.6. Bản đồ làng nghề  
Hiển thị các xưởng có tọa độ trên bản đồ tương tác.  
Khi chọn một ghim, người dùng xem thông tin tóm tắt của hộ gia đình và có thể mở trang hồ sơ.  
Có thể hiển thị thông tin tham quan như giờ mở cửa nếu hộ cung cấp.  
Công nghệ bản đồ có thể sử dụng thư viện phù hợp như Leaflet; lựa chọn cuối cùng được quyết định trong giai đoạn thiết kế.  
4.7. Chức năng Ban quản lý  
Quản lý thông tin xác minh hộ gia đình.  
Đăng, sửa và ẩn tin tức/thông báo cộng đồng.  
Quản lý thông tin sự kiện, hội chợ, triển lãm và hoạt động xúc tiến đầu tư.  
Chọn nội dung, hộ gia đình hoặc lô hàng để giới thiệu nổi bật theo tiêu chí minh bạch.  
Tiếp nhận báo cáo nội dung sai lệch hoặc có dấu hiệu vi phạm quy định nền tảng.  
Xem thống kê tổng quan như số hộ, số sản phẩm, số lô hàng và nhu cầu mua sỉ.  
Không quản lý đơn hàng, không phân lô đơn hàng và không đại diện hộ gia đình ký hợp đồng.  
5. Quy tắc riêng tư và quyền truy cập  
Thông tin trưng bày công khai có thể được khách truy cập xem mà không cần quyền truy cập đặc biệt.  
Thông tin lô hàng riêng tư chỉ được xem bởi chủ sở hữu và các tài khoản được cấp quyền.  
Yêu cầu mua sỉ và báo giá không được công khai mặc định.  
Buyer chỉ xem được các yêu cầu và báo giá thuộc phạm vi của mình.  
Hộ gia đình chỉ xem được yêu cầu/báo giá mà mình có quyền tham gia; không mặc định được xem báo giá của hộ khác.  
Admin thực hiện nhiệm vụ quản trị nền tảng, xác minh và kiểm duyệt; quyền truy cập dữ liệu riêng tư cần được giới hạn theo mục đích quản trị.  
6. Yêu cầu giao diện và kỹ thuật  
Giao diện ưu tiên hình ảnh, có bản sắc văn hóa làng nghề và phân cấp nội dung rõ ràng.  
Thiết kế responsive cho máy tính và thiết bị di động.  
Hỗ trợ tải lên nhiều hình ảnh; có cơ chế tối ưu hoặc tạo ảnh kích thước phù hợp để tránh làm chậm trang.  
Có tìm kiếm, lọc và sắp xếp danh sách sản phẩm/lô hàng ở mức phù hợp với phạm vi bài tập lớn.  
Có xác thực và phân quyền theo vai trò đối với các khu vực quản lý và thông tin riêng tư.  
Công nghệ frontend, backend, cơ sở dữ liệu và thư viện được lựa chọn tự do, miễn đáp ứng yêu cầu chức năng và có thể demo ổn định.  
Mức độ triển khai trực tuyến, dịch vụ lưu trữ ảnh và yêu cầu QR cụ thể cần được nhóm thống nhất.  
7. Phạm vi không triển khai  
Không xây dựng hệ thống Admin nhận đơn hàng rồi phân chia cho các hộ.  
Không để Chủ tịch/Ban quản lý đứng ra ký hợp đồng thay các hộ gia đình.  
Không xây dựng quy trình quản lý bắt buộc cho thanh toán, giao hàng, sản xuất hoặc xử lý thiếu/lỗi hàng hóa.  
Không bắt buộc triển khai chat thời gian thực, cổng thanh toán hay chữ ký số nếu chưa có yêu cầu bổ sung.  
Không xây dựng hệ thống điều phối nội bộ giữa nhiều hộ; việc phối hợp do các hộ tự tổ chức.  
8. Các nội dung cần xác nhận trước khi triển khai  
Mã  
Vấn đề  
Câu hỏi cần chốt  
Q-01  
Tài khoản Buyer  
Khách có được xem nội dung công khai mà không đăng nhập không? Những thao tác nào bắt buộc đăng nhập?  
Q-02  
Quyền xem lô riêng tư  
Chủ lô cấp quyền trực tiếp, gửi liên kết riêng hay dùng cơ chế mời tài khoản?  
Q-03  
Yêu cầu mở  
Yêu cầu mua không chỉ định hộ sẽ được hiển thị cho mọi hộ hay chỉ các hộ phù hợp theo danh mục/ngành nghề?  
Q-04  
Báo giá  
Báo giá chỉ được gửi như thông tin tham khảo hay cần thao tác chấp nhận/từ chối ngay trên website?  
Q-05  
Nhiều hộ cùng cung ứng  
Landing page thể hiện một hộ đầu mối hay danh sách nhiều hộ cùng tham gia? Ai là bên chịu trách nhiệm trước Buyer?  
Q-06  
QR  
QR dẫn đến landing page lô hàng hay trang quy trình sản xuất của loại sản phẩm?  
Q-07  
Xác minh pháp nhân  
Hệ thống chỉ hiển thị trạng thái xác minh hay cần lưu giấy tờ/con dấu dưới dạng tệp?  
Q-08  
Thông báo  
Cần thông báo trong website, qua email hay chỉ hiển thị yêu cầu mới trong trang quản lý?  
Q-09  
Phạm vi demo  
Có bắt buộc triển khai Internet và dùng dữ liệu thực tế hay có thể dùng dữ liệu minh họa?  
9. Tiêu chí nghiệm thu đề xuất  
Các tiêu chí dưới đây là đề xuất để nhóm tự kiểm tra sản phẩm; cần đối chiếu với yêu cầu chính thức của giảng viên.  
Người dùng có thể khám phá làng nghề, xem hồ sơ hộ gia đình, sản phẩm, quy trình và bản đồ.  
Hộ gia đình có thể tạo và cập nhật lô hàng; trang landing page hiển thị đúng thông tin của lô.  
Nội dung quy trình sản xuất được dùng chung cho các lô cùng loại thay vì lặp lại không cần thiết.  
Có thể phân biệt lô công khai và lô riêng tư; người không có quyền không xem được nội dung riêng tư.  
Buyer có thể tạo yêu cầu mua sỉ, chỉ định hoặc không chỉ định hộ, và xem báo giá thuộc phạm vi của mình.  
Hộ gia đình có thể phản hồi yêu cầu và gửi báo giá.  
Ban quản lý có thể xác minh hộ, đăng tin tức, quản lý sự kiện và xem thống kê; không có chức năng Admin phân lô đơn hàng.  
Giao diện hiển thị tốt, hình ảnh được tối ưu và các luồng demo chính hoạt động ổn định.  
10. Kết luận  
vCraft được định hướng là một nền tảng triển lãm số và kết nối mua sỉ cho làng nghề. Trọng tâm phát triển là trải nghiệm khám phá, hồ sơ hộ gia đình, trang sản phẩm/quy trình sản xuất, landing page lô hàng, bản đồ và cơ chế gửi yêu cầu mua/báo giá có phân quyền. Các hoạt động thương lượng, ký kết, sản xuất, giao hàng và phối hợp giữa nhiều hộ do các bên tự quản lý; Ban quản lý chỉ thực hiện vai trò quảng bá, xúc tiến, xác minh và giám sát hoạt động nền tảng.  
