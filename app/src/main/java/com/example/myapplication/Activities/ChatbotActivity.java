package com.example.myapplication.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapter.MessageAdapter;
import com.example.myapplication.Model.Chatbot;
import com.example.myapplication.Model.Message;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private TextInputEditText etMessage;
    private MessageAdapter messageAdapter;
    private Chatbot chatbot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo các thành phần
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        messageAdapter = new MessageAdapter();
        chatbot = new Chatbot();

        // Thiết lập RecyclerView
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(messageAdapter);

        // Xử lý nút Gửi
        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());

        // Tin nhắn chào mừng từ chatbot
        messageAdapter.addMessage(new Message("Xin chào! Tôi là chatbot hỗ trợ. Bạn cần giúp gì?", Message.TYPE_BOT));
    }

    private void sendMessage() {
        String userMessage = etMessage.getText().toString().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // Thêm tin nhắn của người dùng
        messageAdapter.addMessage(new Message(userMessage, Message.TYPE_USER));

        // Thêm tin nhắn "Chatbot đang trả lời..."
        messageAdapter.addMessage(new Message("", Message.TYPE_TYPING));

        // Cuộn RecyclerView xuống tin nhắn mới nhất
        rvChat.scrollToPosition(messageAdapter.getItemCount() - 1);

        // Tạo độ trễ 3 giây trước khi hiển thị câu trả lời
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Xóa tin nhắn "Chatbot đang trả lời..."
            messageAdapter.removeTypingMessage();

            // Lấy câu trả lời từ chatbot
            String botResponse = chatbot.getResponse(userMessage);
            messageAdapter.addMessage(new Message(botResponse, Message.TYPE_BOT));

            // Cuộn RecyclerView xuống tin nhắn mới nhất
            rvChat.scrollToPosition(messageAdapter.getItemCount() - 1);
        }, 2000); // Độ trễ 2 giây

        // Xóa nội dung trong EditText
        etMessage.setText("");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}