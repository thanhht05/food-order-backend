# KẾ HOẠCH TRIỂN KHAI CÁC TÍNH NĂNG NÂNG CAO VÀ PRODUCTION

Tài liệu này tổng hợp danh sách các công việc cụ thể cần thực hiện để phát triển 5 tính năng quan trọng và đưa hệ thống Food Order lên môi trường Production. 
Tài liệu tập trung vào các đầu việc nghiệp vụ, các bước cấu hình và thứ tự triển khai mà không đi sâu vào chi tiết mã nguồn.

---

## 1. ĐĂNG NHẬP MẠNG XÃ HỘI (OAUTH2 LOGIN - GOOGLE / FACEBOOK)

### Mục tiêu
Cho phép người dùng đăng nhập hoặc đăng ký tài khoản nhanh chóng bằng tài khoản Google hoặc Facebook, sau đó hệ thống tự động sinh JWT (Access Token & Refresh Token) tương tự như luồng đăng nhập truyền thống.

### Các công việc cần thực hiện:

#### Bước 1: Chuẩn bị thông tin ứng dụng trên nền tảng bên thứ ba
- Đăng ký tài khoản nhà phát triển trên Google Cloud Console.
- Tạo một dự án mới, tạo thông tin xác thực OAuth Client ID (loại Web Application).
- Cấu hình địa chỉ chuyển hướng xác thực hợp lệ (Authorized Redirect URI) trỏ về backend.
- Lấy thông tin Định danh ứng dụng (Client ID) và Khóa bí mật (Client Secret).
- Thực hiện các bước tương tự trên cổng nhà phát triển Meta for Developers cho Facebook Login.

#### Bước 2: Cập nhật thư viện và cấu hình ứng dụng
- Khai báo thêm thư viện hỗ trợ Spring Security OAuth2 Client trong tập tin quản lý thư viện của dự án.
- Khai báo các thông tin Client ID, Client Secret, Scope truy cập (email, profile) và đường dẫn chuyển hướng trong tập tin cấu hình của dự án.
- Bổ sung các biến môi trường tương ứng vào tập tin mẫu môi trường để đảm bảo tính bảo mật.

#### Bước 3: Cập nhật cơ sở dữ liệu
- Bổ sung vào bảng thông tin người dùng các trường:
  - Nhà cung cấp xác thực (ghi nhận người dùng đăng nhập qua mật khẩu nội bộ, Google hay Facebook).
  - Mã định danh người dùng từ nhà cung cấp (ID duy nhất mà Google hoặc Facebook trả về).
- Cho phép trường mật khẩu được để trống đối với người dùng đăng nhập hoàn toàn qua mạng xã hội.

#### Bước 4: Xây dựng dịch vụ xử lý thông tin người dùng OAuth2
- Tạo dịch vụ tiếp nhận thông tin người dùng trả về sau khi đăng nhập thành công ở cổng mạng xã hội.
- Kiểm tra email trả về từ mạng xã hội trong hệ thống:
  - Nếu email chưa từng tồn tại: Tự động khởi tạo tài khoản mới với vai trò khách hàng mặc định, kích hoạt sẵn trạng thái hoạt động và lưu thông tin nhà cung cấp.
  - Nếu email đã tồn tại: Cập nhật mã định danh nhà cung cấp và đánh dấu liên kết tài khoản.

#### Bước 5: Xây dựng bộ điều hướng kết quả đăng nhập (Success Handler)
- Sau khi xác thực thành công, hệ thống sinh cặp mã xác thực JWT (Access Token và Refresh Token).
- Chuyển hướng trình duyệt của người dùng về trang giao diện phía Frontend kèm theo Token trên tham số đường dẫn (hoặc thiết lập qua Cookie bảo mật).

#### Bước 6: Cập nhật chuỗi bộ lọc bảo mật (Security Filter Chain)
- Kích hoạt tính năng OAuth2 Login trong cấu hình bảo mật Spring Security.
- Cấu hình trang đăng nhập tùy chỉnh hoặc sử dụng điểm cuối mặc định.
- Gắn bộ điều hướng thành công đã xây dựng vào chuỗi bảo mật.

---

## 2. QUÊN MẬT KHẨU QUA EMAIL (FORGOT / RESET PASSWORD VỚI OTP)

### Mục tiêu
Cung cấp luồng khôi phục tài khoản tự động, an toàn thông qua mã xác thực một lần (OTP) gửi về hộp thư điện tử của người dùng.

### Các công việc cần thực hiện:

#### Bước 1: Thiết kế nơi lưu trữ và quản lý mã OTP
- Xác định giải pháp lưu trữ mã OTP tạm thời (sử dụng một bảng riêng trong cơ sở dữ liệu hoặc sử dụng bộ nhớ đệm Redis).
- Dữ liệu lưu trữ cần bao gồm:
  - Địa chỉ email yêu cầu.
  - Mã số xác thực OTP (dãy 6 chữ số ngẫu nhiên).
  - Thời gian hết hạn (thông thường từ 3 đến 5 phút).
  - Số lần đã nhập sai (để ngăn chặn tấn công dò mã).
  - Trạng thái đã được xác thực hay chưa.

#### Bước 2: Xây dựng chức năng gửi mã OTP (Forgot Password)
- Tạo API tiếp nhận yêu cầu quên mật khẩu với đầu vào là email của người dùng.
- Kiểm tra sự tồn tại của email trong hệ thống (nếu email không tồn tại, trả về thông báo chung để tránh làm lộ danh sách người dùng).
- Tạo thuật toán sinh ngẫu nhiên dãy số 6 chữ số.
- Lưu mã OTP cùng thời gian hết hạn vào hệ thống lưu trữ.
- Sử dụng dịch vụ gửi thư điện tử sẵn có để gửi giao diện email chuyên nghiệp chứa mã OTP đến người dùng.
- Áp dụng quy định giới hạn thời gian giữa các lần gửi lại mã (ví dụ: phải chờ ít nhất 60 giây mới được yêu cầu gửi lại mã mới).

#### Bước 3: Xây dựng chức năng xác thực mã OTP (Verify OTP)
- Tạo API tiếp nhận email và mã số OTP do người dùng nhập vào.
- Kiểm tra các điều kiện hợp lệ:
  - Mã còn trong thời hạn sử dụng.
  - Số lần thử sai chưa vượt quá giới hạn tối đa cho phép (ví dụ tối đa 5 lần).
  - Mã số nhập vào khớp chính xác với mã đã lưu.
- Nếu xác thực thành công: Đánh dấu trạng thái mã đã được xác thực hoặc sinh ra một mã xác nhận tạm thời (Reset Token) ngắn hạn để cho phép bước tiếp theo.

#### Bước 4: Xây dựng chức năng đặt lại mật khẩu mới (Reset Password)
- Tạo API tiếp nhận mật khẩu mới, xác nhận mật khẩu mới và mã xác nhận từ bước trước.
- Kiểm tra độ phức tạp của mật khẩu mới theo chuẩn bảo mật của hệ thống.
- Tiến hành mã hóa mật khẩu mới trước khi lưu vào cơ sở dữ liệu.
- Xóa bỏ hoặc hủy hiệu lực của mã OTP đã dùng để tránh việc tái sử dụng.
- Thu hồi toàn bộ Refresh Token hiện tại của tài khoản đó để buộc các thiết bị khác phải đăng nhập lại với mật khẩu mới.

---

## 3. BỘ NHỚ ĐỆM (CACHING VỚI REDIS)

### Mục tiêu
Tăng tốc độ phản hồi của các yêu cầu đọc dữ liệu thường xuyên, giảm tải truy vấn trực tiếp vào cơ sở dữ liệu MySQL, nâng cao khả năng chịu tải của toàn hệ thống.

### Các công việc cần thực hiện:

#### Bước 1: Cài đặt và cấu hình kết nối Redis
- Cài đặt dịch vụ Redis trên môi trường phát triển cục bộ và trên máy chủ.
- Khai báo thư viện Spring Data Redis và thư viện kết nối đệm vào tập tin quản lý dự án.
- Cấu hình thông tin địa chỉ máy chủ Redis, cổng kết nối và mật khẩu trong tập tin cấu hình ứng dụng.
- Bổ sung cấu hình cơ chế tuần tự hóa dữ liệu (Serializer) dạng JSON để dữ liệu lưu trong Redis dễ đọc và tương thích với các đối tượng trong ứng dụng.

#### Bước 2: Kích hoạt cơ chế Cache trong Spring Boot
- Bật tính năng quản lý bộ nhớ đệm toàn cục của Spring.
- Xây dựng lớp quản lý cấu hình bộ nhớ đệm (Cache Manager).
- Thiết lập thời gian sống (TTL) mặc định cho dữ liệu trong đệm (ví dụ: dữ liệu món ăn lưu 1 giờ, dữ liệu danh mục lưu 24 giờ, các dữ liệu thống kê ngắn hạn lưu 10 phút).
- Thiết lập cơ chế không lưu đệm đối với các giá trị rỗng (Null Value) nếu cần thiết.

#### Bước 3: Xác định và áp dụng lưu đệm cho các tác vụ đọc dữ liệu tải cao
- **Danh mục món ăn**: Lưu đệm danh sách tất cả các danh mục món ăn vì dữ liệu này ít khi thay đổi nhưng được gọi ở hầu hết các trang.
- **Chi tiết món ăn**: Lưu đệm thông tin chi tiết từng món ăn theo mã định danh.
- **Trang chủ / Món ăn nổi bật**: Lưu đệm kết quả danh sách món ăn bán chạy, món ăn mới.
- **Bảng giá và thông tin Voucher công khai**: Lưu đệm danh sách mã giảm giá đang hoạt động.

#### Bước 4: Xây dựng cơ chế làm mới và xóa bộ nhớ đệm (Cache Eviction)
- Khi quản trị viên thêm mới, cập nhật hoặc xóa một món ăn: Tự động xóa bản ghi đệm của món ăn đó và làm mới bộ nhớ đệm danh sách món ăn liên quan.
- Khi cập nhật hoặc xóa danh mục: Xóa toàn bộ bộ nhớ đệm danh mục để dữ liệu mới nhất được nạp vào ở lần gọi kế tiếp.
- Khi quản trị viên thay đổi trạng thái hoặc thông tin voucher: Xóa bộ nhớ đệm voucher tương ứng.

---

## 4. GIỚI HẠN TẦN SUẤT GỌI API (RATE LIMITING)

### Mục tiêu
Bảo vệ hệ thống trước các cuộc tấn công từ chối dịch vụ (DDoS), chống vét cạn dữ liệu (Scraping), ngăn chặn hành vi dò mật khẩu (Brute Force) và kiểm soát chi phí sử dụng tài nguyên bên ngoài (đặc biệt là dịch vụ Chatbot AI).

### Các công việc cần thực hiện:

#### Bước 1: Chọn giải pháp và bổ sung thư viện
- Sử dụng thuật toán Token Bucket (thùng thẻ bài) thông qua thư viện Bucket4j.
- Kết hợp thư viện với Redis để có thể áp dụng giới hạn tần suất đồng bộ trên toàn bộ hệ thống ngay cả khi mở rộng chạy nhiều máy chủ backend song song (Distributed Rate Limiting).

#### Bước 2: Xác định các chính sách giới hạn tần suất theo từng nhóm chức năng
- **Nhóm bảo mật cao (Đăng nhập, Quên mật khẩu, Gửi OTP)**:
  - Áp dụng hạn mức nghiêm ngặt (ví dụ: tối đa 5 yêu cầu trong vòng 1 phút cho mỗi địa chỉ IP).
  - Mục đích: Ngăn chặn dò mật khẩu và spam gửi thư rác.
- **Nhóm dịch vụ tiêu tốn chi phí (Chatbot AI Groq)**:
  - Áp dụng hạn mức riêng cho từng người dùng đã đăng nhập (ví dụ: tối đa 10 tin nhắn trong vòng 1 phút, tối đa 100 tin nhắn mỗi ngày).
  - Mục đích: Tránh cạn kiệt hạn ngạch API và tránh phát sinh chi phí ngoài dự kiến.
- **Nhóm duyệt dữ liệu thông thường (Xem danh sách món ăn, tìm kiếm)**:
  - Áp dụng hạn mức thông thoáng (ví dụ: tối đa 60 đến 100 yêu cầu trong vòng 1 phút cho mỗi địa chỉ IP).
- **Nhóm đặt hàng và thanh toán**:
  - Áp dụng hạn mức vừa phải kết hợp khóa xử lý giao dịch để tránh việc người dùng bấm gửi đơn nhiều lần liên tiếp.

#### Bước 3: Xây dựng bộ lọc kiểm soát tần suất (Rate Limiting Filter / Interceptor)
- Tạo một bộ lọc chặn trước khi yêu cầu đi vào Controller.
- Trích xuất thông tin định danh: Ưu tiên dùng mã định danh người dùng nếu đã đăng nhập; nếu chưa đăng nhập thì trích xuất địa chỉ IP thực tế của khách hàng (lưu ý xử lý tiêu đề chuyển tiếp từ Proxy).
- Kiểm tra số lượng thẻ bài còn lại trong thùng tương ứng với điểm cuối API đang truy cập:
  - Nếu còn lượt: Cho phép yêu cầu đi tiếp vào hệ thống và bổ sung các thông tin số lượt còn lại vào tiêu đề phản hồi HTTP.
  - Nếu hết lượt: Chặn ngay yêu cầu, trả về mã phản hồi lỗi chuẩn (HTTP 429 - Quá nhiều yêu cầu) kèm thông báo thời gian cần chờ trước khi thử lại.

---

## 5. TRIỂN KHAI HỆ THỐNG LÊN MÔI TRƯỜNG PRODUCTION

### Mục tiêu
Đưa ứng dụng chạy ổn định, an toàn, có bảo mật SSL, tự động hóa quy trình cập nhật mã nguồn trên máy chủ thực tế (Cloud VPS / Dedicated Server).

### Các công việc cần thực hiện:

#### Bước 1: Chuẩn bị hạ tầng máy chủ và tên miền
- Thuê một máy chủ ảo (VPS) chạy hệ điều hành Linux (ưu tiên Ubuntu LTS) với cấu hình tối thiểu khuyến nghị từ 2 Nhân CPU và 4GB RAM trở lên.
- Đăng ký tên miền chính thức và cấu hình các bản ghi DNS trỏ về địa chỉ IP công khai của máy chủ.
- Cài đặt các công cụ cơ bản trên máy chủ: Docker, Docker Compose, Git và tường lửa hệ điều hành.
- Cấu hình tường lửa máy chủ: Chỉ mở các cổng mạng thiết yếu (cổng SSH quản trị, cổng web tiêu chuẩn HTTP và HTTPS), khóa toàn bộ các cổng nội bộ như MySQL và Redis để tránh lộ lọt dữ liệu ra bên ngoài.

#### Bước 2: Đóng gói ứng dụng với Docker
- Tạo tập tin hướng dẫn đóng gói ứng dụng (Dockerfile) với kỹ thuật nhiều giai đoạn (Multi-stage build):
  - Giai đoạn biên dịch: Sử dụng hình ảnh chứa bộ công cụ Gradle để biên dịch mã nguồn thành tập tin nén thực thi (.jar).
  - Giai đoạn chạy: Sử dụng hình ảnh máy ảo Java tối giản (JRE Alpine) để giảm dung lượng hình ảnh và tăng tốc độ khởi động.
- Cấu hình chạy ứng dụng bằng người dùng không có quyền quản trị tối cao (Non-root user) bên trong container để gia tăng tính bảo mật.
- Cấu hình các thông số tối ưu bộ nhớ máy ảo Java (cấp phát mức RAM khởi điểm và mức RAM tối đa phù hợp với tài nguyên máy chủ).

#### Bước 3: Xây dựng kịch bản điều phối dịch vụ (Docker Compose)
- Viết kịch bản liên kết và khởi chạy đồng thời các dịch vụ cần thiết:
  - Dịch vụ ứng dụng backend Spring Boot.
  - Dịch vụ cơ sở dữ liệu MySQL (gắn ổ đĩa lưu trữ ngoài để đảm bảo dữ liệu không bị mất khi khởi động lại container).
  - Dịch vụ bộ nhớ đệm Redis.
- Tạo một mạng ảo nội bộ để các container tự do liên lạc với nhau bằng tên dịch vụ mà không cần phơi bày cổng ra internet.
- Thiết lập cơ chế kiểm tra trạng thái sống (Healthcheck) và thứ tự phụ thuộc khởi động (Backend chỉ được khởi động sau khi MySQL và Redis đã sẵn sàng nhận kết nối).

#### Bước 4: Thiết lập máy chủ chuyển tiếp Nginx và chứng chỉ bảo mật SSL
- Cài đặt Nginx làm máy chủ tiếp nhận cổng đầu vào (Reverse Proxy).
- Cấu hình chuyển hướng toàn bộ lưu lượng từ tên miền vào cổng nội bộ của ứng dụng backend.
- Cấu hình hỗ trợ nâng cấp giao thức cho kết nối thời gian thực WebSocket (đặc biệt quan trọng cho tính năng Chatbot và thông báo đơn hàng).
- Kích hoạt giao thức bảo mật HTTPS bằng chứng chỉ bảo mật miễn phí Let's Encrypt thông qua công cụ Certbot.
- Cấu hình tự động gia hạn chứng chỉ SSL định kỳ.
- Bật cơ chế tự động chuyển hướng toàn bộ người dùng từ kết nối không bảo mật (HTTP) sang kết nối an toàn (HTTPS).

#### Bước 5: Cấu hình an toàn cho cơ sở dữ liệu trên Production
- Đổi cơ chế quản lý lược đồ dữ liệu của Hibernate từ chế độ tự động cập nhật (Update) sang chế độ an toàn (Validate hoặc None) để tránh việc ứng dụng tự ý làm thay đổi cấu trúc bảng thực tế.
- Sử dụng công cụ di chuyển dữ liệu (như Flyway hoặc Liquibase) để quản lý lịch sử thay đổi cấu trúc bảng một cách có kiểm soát.
- Thiết lập quy trình tự động sao lưu định kỳ cơ sở dữ liệu MySQL mỗi ngày và đẩy bản sao lưu lên dịch vụ lưu trữ đám mây tách biệt.

#### Bước 6: Thiết lập đường ống tự động hóa triển khai (CI/CD Pipeline)
- Tạo kịch bản tự động hóa trên kho lưu trữ mã nguồn (ví dụ: GitHub Actions).
- Quy trình kiểm thử tự động: Mỗi khi có mã nguồn mới đẩy lên hoặc tạo yêu cầu gộp nhánh (Pull Request), hệ thống tự động chạy toàn bộ các bài kiểm thử tự động (Unit Test, Integration Test).
- Quy trình triển khai tự động:
  - Khi gộp mã nguồn vào nhánh chính (Main/Master), hệ thống tự động đăng nhập vào máy chủ qua kết nối SSH bảo mật.
  - Kéo mã nguồn mới nhất về máy chủ.
  - Tải lại các biến môi trường cấu hình mật.
  - Tiến hành đóng gói lại hình ảnh Docker và khởi động lại dịch vụ backend mà không làm gián đoạn các dịch vụ cơ sở dữ liệu.

