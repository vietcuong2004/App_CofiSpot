# ☕ Ứng dụng Cofi Spot - "Cafe Quanh Ta"

**Cofi Spot** là ứng dụng Android hỗ trợ người dùng **khám phá quán cà phê**, **check-in tại địa điểm**, **đánh giá chất lượng**, **tích điểm đổi voucher**, và **trò chuyện với chatbot hỗ trợ**. Với bản đồ tương tác, hệ thống gợi ý thông minh và quản lý quán linh hoạt, ứng dụng hướng tới việc tạo ra trải nghiệm cà phê tiện lợi và thú vị cho mọi người.

---

# 🌟 Danh sách Tính Năng Chính

## 1. 🔐 Quản lý tài khoản

### 1.1 📝 Đăng ký tài khoản
**Các bước thực hiện:**
1. Tại màn hình chính, chọn **"Đăng ký"**.
2. Nhập thông tin:
   - Email
   - Mật khẩu & Nhập lại mật khẩu
3. Nhấn **"Đăng ký"**.
4. Hệ thống kiểm tra dữ liệu:
   - Nếu hợp lệ → Gửi email xác thực → Tạo tài khoản trên Firebase.
   - Nếu không → Hiển thị lỗi tương ứng (email đã tồn tại, mật khẩu yếu...).

---

### 1.2 🔑 Đăng nhập (Phân quyền)
**Các bước thực hiện:**
1. Tại màn hình chính, chọn **"Đăng nhập"**.
2. Nhập email và mật khẩu.
3. Nhấn nút **"Đăng nhập"**.
4. Hệ thống:
   - Kiểm tra tài khoản → nếu đúng thì đăng nhập.
   - Kiểm tra quyền:
     - 👤 `role = Customer` → Giao diện người dùng.
     - 🛠️ `role = Admin` → Giao diện quản trị.
5. Ghi nhận thời gian đăng nhập gần nhất.

---

### 1.3 🚪 Đăng xuất
**Các bước thực hiện:**
1. Từ trang chủ, mở menu tài khoản.
2. Chọn **"Đăng xuất"**.
3. Hệ thống xóa phiên làm việc và chuyển về màn hình đăng nhập.

---

### 1.4 ❓ Quên mật khẩu
**Các bước thực hiện:**
1. Tại màn hình đăng nhập, chọn **"Quên mật khẩu"**.
2. Nhập địa chỉ email đã đăng ký.
3. Nhấn nút **"Gửi email"**.
4. Hệ thống gửi liên kết đặt lại mật khẩu đến hộp thư người dùng.

---

### 1.5 👤 Quản lý hồ sơ cá nhân
**Các bước thực hiện:**
1. Từ giao diện chính, chọn biểu tượng tài khoản → mở **"Hồ sơ cá nhân"**.
2. Hệ thống hiển thị thông tin hiện tại: tên, email, số điện thoại, avatar.
3. Người dùng chỉnh sửa:
   - Tên, SĐT, Avatar (upload lên Imgur nếu thay đổi).
   - Đổi mật khẩu nếu muốn.
4. Nhấn **"Lưu"** để cập nhật.

---

## 2. 📍 Check-in Quán Cà Phê
**Các bước thực hiện:**
1. Mở trang chính → chọn tab **"Check-in"**.
2. Hệ thống kiểm tra vị trí hiện tại bằng GPS.
3. Nếu cách quán < 50m → cho phép check-in.
4. Hiển thị thông báo thành công và chuyển sang giao diện **đánh giá quán**.

---

## 3. ⭐ Đánh giá Quán Cà Phê
**Các bước thực hiện:**
1. Sau khi check-in → tự động chuyển sang trang đánh giá.
2. Người dùng:
   - Chọn số sao (1–5) ⭐
   - Viết bình luận (tối đa 1500 ký tự)
   - Tải ảnh/video (tuỳ chọn)
   - Chọn hoạt động tại quán (Boardgame, Workshop…)
3. Nhấn nút **"Gửi đánh giá"**.
4. Hệ thống kiểm tra, lưu vào Firestore, cộng +10 điểm thưởng và hiển thị thông báo thành công.

---

## 4. 🔍 Tìm kiếm Quán

### 4.1 📌 Tìm theo tên
**Các bước thực hiện:**
1. Chọn mục **"Tìm kiếm"**.
2. Nhập tên quán → nhấn nút **"Tìm"**.
3. Hệ thống tìm trong cơ sở dữ liệu (không phân biệt hoa thường).
4. Hiển thị kết quả trên bản đồ bằng các marker 📍.
5. Nhấn vào marker để xem thông tin chi tiết: tên, địa chỉ, ảnh, đánh giá, hoạt động.

---

### 4.2 🧲 Lọc theo tiêu chí
**Các bước thực hiện:**
1. Tại màn hình bản đồ, chọn bộ lọc.
2. Chọn 1 hoặc nhiều tiêu chí:
   - Khoảng cách: 5km, 10km…
   - Số sao: từ 4 sao trở lên
   - Hoạt động: Làm việc, Boardgame…
3. Nhấn **"Lọc"** → hệ thống lọc và hiển thị kết quả trên bản đồ.

---

### 4.3 🗺️ Chỉ đường
**Các bước thực hiện:**
1. Chọn một quán cà phê từ danh sách/tìm kiếm.
2. Nhấn nút **"Chỉ đường"**.
3. Hệ thống mở Google Maps với định vị từ vị trí hiện tại đến quán đã chọn.

---

## 5. 🎯 Gợi ý Quán Nổi Bật
**Tự động thực hiện khi vào màn hình trang chủ:**
1. Hệ thống truy vấn các quán có sao trung bình cao nhất.
2. Lấy top 5 → hiển thị danh sách ngang (RecyclerView).
3. Mỗi mục gồm: ảnh, tên quán, sao đánh giá, nút “chỉ đường”.

---

## 6. 🔔 Quản lý Thông báo

### 6.1 📬 Nhận thông báo
- Gửi thông báo mỗi khi người dùng đăng nhập hoặc chuyển tab.

### 6.2 📖 Xem lại thông báo
- Chọn mục **"Thông báo"** trong menu.
- Hiển thị toàn bộ lịch sử theo thứ tự thời gian.

### 6.3 🧹 Xóa thông báo
- Trong màn hình thông báo, nhấn **"Xóa tất cả"** → hệ thống xóa khỏi Firestore.

---

## 7. 🎁 Quản lý Điểm Thưởng

### 7.1 ➕ Tự động tích điểm
| Hành động     | Điểm nhận được |
|---------------|----------------|
| Đăng nhập     | +1             |
| Check-in      | +5             |
| Đánh giá      | +10            |

- Điểm được cập nhật ngay và hiển thị thông báo (+ điểm).

### 7.2 🎫 Đổi điểm lấy voucher
**Các bước thực hiện:**
1. Vào mục **"Voucher"**.
2. Chọn một voucher muốn đổi → xem thông tin chi tiết.
3. Nhấn nút **"Đổi điểm"**.
4. Nếu đủ điểm → hệ thống trừ điểm và thêm voucher vào tài khoản.

---

## 8. 🏪 Quản lý Quán (Chỉ dành cho Admin)

### 8.1 ➕ Thêm quán
1. Nhập thông tin quán: tên, mô tả, hoạt động, hình ảnh.
2. Chọn nút thêm vị trí: Hiển thị bản đồ --> Admin Ghim vị trí trên Google Map → lưu `Geopoint` trong Firestore.
3. Nhấn **"Thêm quán"** để lưu lại.

### 8.2 🛠️ Sửa quán
- Chọn quán → nhấn **"Sửa"**, cập nhật nội dung và nhấn **"Lưu"**.

### 8.3 ❌ Xóa quán
- Chọn quán → nhấn **"Xóa"**, xác nhận lần nữa → quán sẽ bị xoá vĩnh viễn.

### 8.4 📋 Xem danh sách quán
- Hiển thị toàn bộ quán (RecycleView), mỗi item có nút Sửa / Xóa.

---

## 9. 🤖 Chatbot Hỗ Trợ

**Các bước sử dụng:**
1. Chọn **"Chatbot"** từ menu.
2. Chatbot gửi lời chào đầu tiên.
3. Người dùng nhập câu hỏi (ví dụ: *"Có voucher nào không?"*).
4. Chatbot phản hồi dựa trên từ khóa có sẵn.

---

📌 **Các kiến thức sử dụng**: Firebase, Firestore, Google Maps API, Imgur API, RecyclerView, Navigation Drawer.
