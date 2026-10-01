# DANH SÁCH & HƯỚNG DẪN TỐI ƯU CÁC VẤN ĐỀ N+1 QUERY TRONG DỰ ÁN

Tài liệu này tổng hợp toàn bộ các điểm nghẽn hiệu năng liên quan đến **N+1 Query** và thao tác cơ sở dữ liệu chưa tối ưu trong dự án **Food Order Backend (Spring Boot)**. Bạn có thể dựa vào tài liệu này để tự kiểm tra, tái cấu trúc và tối ưu mã nguồn.

---

## 📑 MỤC LỤC

1. [Nguyên nhân gốc rễ trong dự án](#1-nguyên-nhân-gốc-rễ-trong-dự-án)
2. [Chi tiết từng vị trí bị lỗi N+1 Query](#2-chi-tiết-từng-vị-trí-bị-lỗi-n1-query)
   - [Vấn đề 1: Xem giỏ hàng (`CartService`) - [Cực kỳ nghiêm trọng]](#vấn-đề-1-api-giỏ-hàng-cartservice---mức-độ-cực-kỳ-nghiêm-trọng)
   - [Vấn đề 2: Tìm kiếm & phân trang sản phẩm (`ProductService`) - [Nghiêm trọng]](#vấn-đề-2-api-tìm-kiếm--phân-trang-sản-phẩm-productservice---mức-độ-cao)
   - [Vấn đề 3: Admin xem danh sách đơn hàng (`OrderService`) - [Nghiêm trọng]](#vấn-đề-3-api-admin-xem-danh-sách-toàn-bộ-đơn-hàng-orderservice---mức-độ-cao)
   - [Vấn đề 4: Chi tiết đơn hàng (`OrderService`) - [Trung bình]](#vấn-đề-4-api-chi-tiết-đơn-hàng-orderservice---mức-độ-trung-bình)
   - [Vấn đề 5: Quản lý danh sách người dùng (`UserService`) - [Trung bình]](#vấn-đề-5-api-danh-sách-người-dùng-userservice---mức-độ-trung-bình)
   - [Vấn đề 6: Tìm kiếm món ăn bằng AI (`ProductService`) - [Trung bình]](#vấn-đề-6-api-tìm-kiếm-ai-cho-chatbot-productservice---mức-độ-trung-bình)
   - [Vấn đề 7: Anti-Pattern Database trong vòng lặp (`OrderService.placeOrder`) - [Trung bình]](#vấn-đề-7-anti-pattern-thao-tác-db-trong-vòng-lặp-orderserviceplaceorder---mức-độ-trung-bình)
3. [Các phương pháp khắc phục chuẩn chỉnh](#3-các-phương-pháp-khắc-phục-chuẩn-chỉnh)
4. [Checklist theo dõi tiến độ](#4-checklist-theo-dõi-tiến-độ)

---

## 1. NGUYÊN NHÂN GỐC RỄ TRONG DỰ ÁN

1. **Thiếu cấu hình `fetch = FetchType.LAZY` trên `@ManyToOne` và `@OneToOne`**:
   - Theo đặc tả JPA, mặc định của `@ManyToOne` và `@OneToOne` là **`EAGER`**.
   - Khi bạn truy vấn một danh sách đối tượng (ví dụ: `productRepository.findAll()` hoặc qua Specification), Hibernate tự động sinh thêm câu lệnh `SELECT` cho từng thực thể con liên kết.
2. **Truy cập Collection LAZY bên trong vòng lặp DTO Mapping**:
   - Đối với `@OneToMany` (ví dụ: `Product -> List<ProductImage> lstImg`), mặc định là **`LAZY`**.
   - Khi Service duyệt qua từng phần tử và gọi getter (`p.getLstImg()`, `cart.getCartDetails()`), Hibernate buộc phải gửi một truy vấn `SELECT` mới đến DB cho từng bản ghi.
3. **Chưa áp dụng Batch Fetching**:
   - Dự án chưa bật `default_batch_fetch_size` trong `application.properties`, khiến Hibernate không thể tự động gộp các câu `SELECT ... WHERE id = ?` thành `WHERE id IN (?, ?, ...)`.

---

## 2. CHI TIẾT TỪNG VỊ TRÍ BỊ LỖI N+1 QUERY

---

### VẤN ĐỀ 1: API Giỏ hàng (`CartService`) - Mức độ: [CỰC KỲ NGHIÊM TRỌNG]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/cart/service/CartService.java`
  - Các hàm bị ảnh hưởng:
    - `getCartDetailsByUser()` (khoảng dòng 242 - 291)
    - `mapToResponse(Cart cart)` (khoảng dòng 293 - 338)
- **Đoạn code gây lỗi**:

  ```java
  List<CartDetail> cartDetails = cart.getCartDetails(); // Trigger query lấy CartDetail 1 query

  for (CartDetail cd : cartDetails) {
      Product product = cd.getProduct();           // 🔴 N câu query SELECT lấy Product
      product.getCategory().getName();             // 🔴 N câu query SELECT lấy Category của từng Product
      product.getLstImg().get(0).getImgName();     // 🔴 N câu query SELECT lấy Images của từng Product
  }
  ```

- **Số lượng query phát sinh**:
  Nếu người dùng có **10 món** trong giỏ:
  - 1 query lấy Cart
  - 1 query lấy CartDetail list
  - 10 queries lấy Product
  - 10 queries lấy Category
  - 10 queries lấy ProductImage
    $$\Rightarrow \mathbf{32 \text{ queries}} \text{ cho 1 lần xem giỏ hàng!}$$
- **Gợi ý cách fix**:
  - Viết câu truy vấn `JOIN FETCH` trong `CartDetailRepository`:
    ```java
    @Query("SELECT cd FROM CartDetail cd " +
           "JOIN FETCH cd.product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.lstImg " +
           "WHERE cd.cart = :cart")
    List<CartDetail> findByCartWithProductDetails(@Param("cart") Cart cart);
    ```
  - Trong `CartService.getCartDetailsByUser()`, dùng hàm này thay vì `cart.getCartDetails()`.

---

### VẤN ĐỀ 2: API Tìm kiếm & Phân trang Sản phẩm (`ProductService`) - Mức độ: [CAO]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/product/service/ProductService.java`
  - Hàm bị ảnh hưởng: `search(...)` (khoảng dòng 264 - 310)
  - Thực thể: `src/main/java/com/thanh/foodOrder/feature/product/domain/Product.java` (dòng 54 - 72)
- **Đoạn code gây lỗi**:

  ```java
  Page<Product> pages = productRepository.findAll(spec, pageable); // 1 query lấy 1 trang N products

  for (Product p : pages.getContent()) {
      p.getCategory().getName(); // 🔴 N query SELECT Category (do @ManyToOne Category mặc định EAGER)

      List<ProductImage> images = p.getLstImg(); // 🔴 N query SELECT ProductImage (do collection LAZY)
      for (ProductImage i : images) {
          dtoImg.setName(i.getImgName());
      }
  }
  ```

- **Số lượng query phát sinh**:
  Với 1 trang có **20 sản phẩm**:
  - 1 query SELECT products (kèm COUNT query cho phân trang)
  - 20 queries lấy Category
  - 20 queries lấy ProductImage
    $$\Rightarrow \mathbf{41 \text{ queries}} \text{ cho mỗi lần lướt trang Menu/Trang chủ!}$$
- **Gợi ý cách fix**:
  - Sửa `Product.java`:
    ```java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catrgory_id")
    private Category category;
    ```
  - Trong `ProductRepository`, sử dụng `@EntityGraph`:
    ```java
    @EntityGraph(attributePaths = {"category", "lstImg"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
    ```
  - Bật `default_batch_fetch_size` để gom nhóm các truy vấn liên quan đến collection `lstImg`.

---

### VẤN ĐỀ 3: API Admin xem danh sách toàn bộ đơn hàng (`OrderService`) - Mức độ: [CAO]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/order/service/OrderService.java`
  - Hàm bị ảnh hưởng: `getAllOrder(...)` (khoảng dòng 71 - 82)
  - DTO: `src/main/java/com/thanh/foodOrder/feature/order/dto/AdminOrderResponseDTO.java` (dòng 26 - 41)
  - Thực thể: `src/main/java/com/thanh/foodOrder/feature/order/domain/Order.java` (dòng 78 - 86)
- **Đoạn code gây lỗi**:

  ```java
  List<Order> lstOrders = this.orderRepository.findAll(spec); // 1 query SELECT orders

  for (Order od : lstOrders) {
      AdminOrderResponseDTO item = AdminOrderResponseDTO.from(od);
      // Bên trong from(od):
      // order.getUser().getFullName(); // 🔴 N query SELECT User
      // order.getUser().getEmail();
      // Nếu order có voucher: Hibernate tiếp tục 🔴 N query SELECT Voucher
  }
  ```

- **Số lượng query phát sinh**:
  Nếu hệ thống có **100 đơn hàng**:
  - 1 query lấy danh sách Order
  - 100 queries lấy User
  - 100 queries lấy Voucher
    $$\Rightarrow \mathbf{101 - 201 \text{ queries}} \text{ mỗi lần Admin vào xem danh sách đơn hàng!}$$
- **Gợi ý cách fix**:
  - Thêm phương thức có `JOIN FETCH` trong `OrderRepository`:
    ```java
    @Query("SELECT o FROM Order o " +
           "JOIN FETCH o.user " +
           "LEFT JOIN FETCH o.voucher " +
           "ORDER BY o.orderDate DESC")
    List<Order> findAllWithUserAndVoucher();
    ```
  - Hoặc nếu dùng Specification:
    ```java
    @EntityGraph(attributePaths = {"user", "voucher"})
    List<Order> findAll(Specification<Order> spec);
    ```

---

### VẤN ĐỀ 4: API Chi tiết đơn hàng (`OrderService`) - Mức độ: [TRUNG BÌNH]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/order/service/OrderService.java`
  - Hàm bị ảnh hưởng: `getOrderDetail(Long id)` & `mapToOrderResponseDTO(...)` (khoảng dòng 163 - 202)
- **Đoạn code gây lỗi**:

  ```java
  List<OrderDetail> orderDetails = this.orderDetailRepository.findByOrderId(id);

  for (OrderDetail od : orderDetails) {
      od.getProduct().getId();
      od.getProduct().getName(); // 🔴 Mỗi dòng món ăn trong đơn bắn thêm 1 query SELECT Product
  }
  ```

- **Gợi ý cách fix**:
  - Sửa `OrderDetailRepository.java`:
    ```java
    @Query("SELECT od FROM OrderDetail od JOIN FETCH od.product WHERE od.order.id = :orderId")
    List<OrderDetail> findByOrderIdWithProduct(@Param("orderId") Long orderId);
    ```

---

### VẤN ĐỀ 5: API Danh sách người dùng (`UserService`) - Mức độ: [TRUNG BÌNH]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/user/service/UserService.java`
  - Hàm bị ảnh hưởng: `getAllUsers(...)` (khoảng dòng 145 - 175)
  - Thực thể: `src/main/java/com/thanh/foodOrder/feature/user/domain/User.java` (dòng 56 - 58)
- **Đoạn code gây lỗi**:

  ```java
  users = this.userRepository.findAll(pageable); // 1 query lấy N users

  List<ResponseUserDTO> userDTOs = users.getContent().stream()
      .map(user -> this.convertUserToResUserDTO(user)) // 🔴 user.getRole().getName() gây N query SELECT Role
      .collect(Collectors.toList());
  ```

- **Gợi ý cách fix**:
  - Đổi `@ManyToOne` trong `User.java` sang `fetch = FetchType.LAZY`.
  - Trong `UserRepository.java`, thêm `@EntityGraph(attributePaths = {"role"})` cho các hàm tìm kiếm/phân trang:

    ```java
    @EntityGraph(attributePaths = {"role"})
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"role"})
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    ```

---

### VẤN ĐỀ 6: API Tìm kiếm AI cho Chatbot (`ProductService`) - Mức độ: [TRUNG BÌNH]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/product/service/ProductService.java` (dòng 338 - 365)
  - `src/main/java/com/thanh/foodOrder/feature/product/repository/ProductRepository.java` (dòng 34 - 48)
- **Đoạn code gây lỗi**:

  ```java
  // Trong ProductRepository:
  @Query("SELECT p FROM Product p WHERE ...") // ❌ Không join fetch ảnh
  List<Product> searchForAi(...);

  // Trong ProductService:
  product.getLstImg().get(0).getImgName(); // 🔴 N query SELECT ProductImage
  ```

- **Gợi ý cách fix**:
  - Bổ sung `LEFT JOIN FETCH p.lstImg` vào câu truy vấn `@Query` của `searchForAi` trong `ProductRepository`:
    ```java
    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN FETCH p.lstImg
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ...)
    """)
    ```

---

### VẤN ĐỀ 7: Anti-Pattern Thao tác DB trong vòng lặp (`OrderService.placeOrder`) - Mức độ: [TRUNG BÌNH]

- **Tệp mã nguồn**:
  - `src/main/java/com/thanh/foodOrder/feature/order/service/OrderService.java` (dòng 340 - 342 và dòng 376 - 394)
- **Đoạn code gây lỗi**:

  ```java
  // Trong createOrderDetal:
  for (CartDetail cd : cartDetails) {
      OrderDetail od = ...;
      orderDetailRepository.save(od); // ❌ LỖI: Gọi save() từng entity trong vòng for (N lệnh INSERT riêng rẽ)
      orderDetailsToSave.add(od);
  }

  // Trong placeOrder:
  List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(orderDetailsToSave); // ❌ LỖI: Lại gọi saveAll() thêm 1 lần nữa!
  ```

- **Gợi ý cách fix**:
  - Xóa bỏ dòng `orderDetailRepository.save(od);` trong vòng for của hàm `createOrderDetal`.
  - Chỉ gom các đối tượng vào `List<OrderDetail>` và thực hiện gọi `orderDetailRepository.saveAll(...)` một lần duy nhất.

---

## 3. CÁC PHƯƠNG PHÁP KHẮC PHỤC CHUẨN CHỈNH

### Phương pháp 1: Bật Batch Fetching trong `application.properties` (Nên làm ngay)

Thêm cấu hình sau vào `src/main/resources/application.properties`:

```properties
# Tự động gộp N truy vấn LAZY thành 1 truy vấn WHERE id IN (?, ?, ...)
spring.jpa.properties.hibernate.default_batch_fetch_size=25
```

> **Tác dụng**: Giảm ngay lập tức số lượng câu query từ hàng trăm xuống chỉ còn 2 - 3 câu query dạng `IN (...)` mà không làm thay đổi logic mã nguồn.

### Phương pháp 2: Chuyển `@ManyToOne` và `@OneToOne` sang `FetchType.LAZY`

Rà soát toàn bộ các domain entity:

- `Product.java`: `@ManyToOne(fetch = FetchType.LAZY) private Category category;`
- `Order.java`: `@ManyToOne(fetch = FetchType.LAZY) private User user;`
- `Order.java`: `@ManyToOne(fetch = FetchType.LAZY) private Voucher voucher;`
- `OrderDetail.java`: `@ManyToOne(fetch = FetchType.LAZY) private Product product;`
- `CartDetail.java`: `@ManyToOne(fetch = FetchType.LAZY) private Product product;`
- `User.java`: `@ManyToOne(fetch = FetchType.LAZY) private Role role;`

### Phương pháp 3: Dùng `JOIN FETCH` hoặc `@EntityGraph` cho các API trả về danh sách

- Khi cần lấy dữ liệu quan hệ để hiển thị, hãy chủ động `JOIN FETCH` (trong JPQL) hoặc khai báo `@EntityGraph` trong Repository thay vì để getter tự kích hoạt câu truy vấn ngầm.

---

## 4. CHECKLIST THEO DÕI TIẾN ĐỘ

Bạn có thể đánh dấu `[x]` vào các mục dưới đây khi đã tối ưu xong:

- [ ] **Bước 1**: Thêm `spring.jpa.properties.hibernate.default_batch_fetch_size=25` vào `application.properties`.
- [ ] **Bước 2**: Chuyển các quan hệ `@ManyToOne` sang `FetchType.LAZY` trong các entity (`Product`, `Order`, `OrderDetail`, `CartDetail`, `User`).
- [ ] **Bước 3**: Tối ưu `CartService.getCartDetailsByUser` bằng `JOIN FETCH` trong `CartDetailRepository`.
- [ ] **Bước 4**: Tối ưu `ProductService.search` bằng `@EntityGraph` trong `ProductRepository`.
- [ ] **Bước 5**: Tối ưu `OrderService.getAllOrder` bằng `JOIN FETCH` User và Voucher trong `OrderRepository`.
- [ ] **Bước 6**: Tối ưu `OrderService.getOrderDetail` bằng `JOIN FETCH` Product trong `OrderDetailRepository`.
- [ ] **Bước 7**: Tối ưu `UserService.getAllUsers` bằng `@EntityGraph` cho Role trong `UserRepository`.
- [ ] **Bước 8**: Tối ưu `ProductRepository.searchForAi` bằng `LEFT JOIN FETCH p.lstImg`.
- [ ] **Bước 9**: Xóa bỏ lệnh `save()` thừa thãi trong vòng lặp của `OrderService.createOrderDetal`.
- [ ] **Bước 10**: Chạy lại toàn bộ test suite (`./gradlew test`) để xác minh không bị lỗi hồi quy.
