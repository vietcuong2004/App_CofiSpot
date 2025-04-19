package com.example.myapplication.Model;

import java.util.HashMap;
import java.util.Map;

public class Chatbot {
    private Map<String, String> responses;

    public Chatbot() {
        // Khởi tạo dữ liệu nạp sẵn
        responses = new HashMap<>();
        responses.put("xin chào", "Xin chào! Tôi là chatbot hỗ trợ. Bạn cần giúp gì?");
        responses.put("giờ mở cửa", "Các quán cà phê thường mở cửa từ 7:00 sáng đến 10:00 tối. Bạn muốn biết quán cụ thể nào?");
        responses.put("checkin", "Để check-in, bạn cần đến quán cà phê và nhấn nút Check-in. Đảm bảo bạn ở trong bán kính 50m của quán nhé!");
        responses.put("điểm thưởng", "Bạn nhận được điểm thưởng khi check-in, đánh giá, hoặc bình luận. Điểm có thể đổi voucher tại mục Rewards.");
        responses.put("quán gần đây", "Bạn có thể xem danh sách quán gần đây trong tab Home hoặc Search. Hãy bật GPS để tôi giúp bạn tìm nhé!");

        // Thêm 10 cặp key-value mới liên quan đến dự án cà phê
        responses.put("đăng xuất", "Để đăng xuất, bạn nhấn vào biểu tượng đăng xuất trên thanh toolbar ở màn hình chính. Bạn có chắc muốn đăng xuất không?");
        responses.put("thông báo", "Thông báo sẽ hiển thị khi có ưu đãi mới hoặc bạn nhận được điểm thưởng. Bạn có thể xem lịch sử thông báo bằng cách nhấn vào biểu tượng chuông trên toolbar!");
        responses.put("voucher", "Bạn có thể đổi voucher trong tab Rewards. Kiểm tra số điểm của bạn và chọn voucher phù hợp nhé!");
        responses.put("bình luận", "Để bình luận về quán, bạn vào trang chi tiết của quán và nhập nhận xét. Đừng quên đánh giá để nhận thêm điểm thưởng!");
        responses.put("tìm quán", "Bạn có thể tìm quán cà phê trong tab Search. Nhập tên quán hoặc khu vực, tôi sẽ giúp bạn tìm quán phù hợp!");
        responses.put("ưu đãi", "Ưu đãi mới sẽ được thông báo qua mục Thông báo. Bạn cũng có thể kiểm tra trong tab Rewards để xem các voucher đang có!");
        responses.put("gps không hoạt động", "Nếu GPS không hoạt động, hãy kiểm tra xem bạn đã bật định vị chưa. Vào Cài đặt > Vị trí > Bật định vị, sau đó thử lại nhé!");
        responses.put("đăng nhập", "Để đăng nhập, bạn nhập email và mật khẩu trong màn hình đăng nhập. Nếu quên mật khẩu, nhấn vào 'Quên mật khẩu' để đặt lại!");
        responses.put("tạo tài khoản", "Để tạo tài khoản, bạn vào màn hình đăng ký, nhập email, mật khẩu và thông tin cá nhân. Sau đó xác nhận qua email để hoàn tất!");
        responses.put("liên hệ hỗ trợ", "Bạn có thể liên hệ hỗ trợ qua email support@cofispot.com hoặc gọi hotline 0123-456-789. Chúng tôi luôn sẵn sàng giúp bạn!");

        // Thêm 10 cặp key-value mới liên quan đến dự án cà phê
        responses.put("xem hồ sơ", "Bạn có thể xem hồ sơ của mình bằng cách nhấn vào biểu tượng tài khoản trên toolbar. Tại đó, bạn có thể chỉnh sửa thông tin cá nhân!");
        responses.put("checkin không được", "Nếu không check-in được, hãy kiểm tra GPS và đảm bảo bạn ở trong bán kính 50m của quán. Nếu vẫn không được, liên hệ hỗ trợ nhé!");
        responses.put("điểm hết hạn", "Điểm thưởng có thể hết hạn sau 6 tháng nếu không sử dụng. Hãy kiểm tra trong tab Rewards để đổi điểm trước khi hết hạn!");
        responses.put("quán yêu thích", "Bạn có thể thêm quán vào danh sách yêu thích bằng cách nhấn vào biểu tượng trái tim trong trang chi tiết của quán!");
        responses.put("xóa bình luận", "Để xóa bình luận, bạn vào trang chi tiết của quán, tìm bình luận của mình và nhấn vào biểu tượng thùng rác. Lưu ý, bạn chỉ có thể xóa bình luận của mình!");
        responses.put("báo cáo quán", "Nếu bạn muốn báo cáo một quán, hãy vào trang chi tiết của quán và chọn 'Báo cáo'. Điền lý do và gửi, chúng tôi sẽ xem xét ngay!");
        responses.put("thanh toán", "Hiện tại, ứng dụng chưa hỗ trợ thanh toán trực tuyến. Bạn có thể thanh toán tại quán và sử dụng voucher để được giảm giá!");
        responses.put("wifi quán", "Để biết thông tin wifi của quán, bạn vào trang chi tiết của quán. Một số quán có hiển thị thông tin wifi trong phần mô tả!");
        responses.put("đổi mật khẩu", "Để đổi mật khẩu, bạn vào phần hồ sơ, chọn 'Đổi mật khẩu', nhập mật khẩu cũ và mật khẩu mới, sau đó xác nhận!");
        responses.put("khuyến mãi hôm nay", "Để xem khuyến mãi hôm nay, bạn kiểm tra trong tab Rewards hoặc mục Thông báo. Các ưu đãi mới sẽ được cập nhật thường xuyên!");
    }

    public String getResponse(String message) {
        // Chuyển câu hỏi về chữ thường để so sánh
        message = message.toLowerCase().trim();

        // Tìm câu trả lời phù hợp
        for (String key : responses.keySet()) {
            if (message.contains(key)) {
                return responses.get(key);
            }
        }

        // Nếu không tìm thấy câu trả lời phù hợp
        return "Xin lỗi, tôi chưa hiểu câu hỏi của bạn. Bạn có thể hỏi lại không?";
    }
}