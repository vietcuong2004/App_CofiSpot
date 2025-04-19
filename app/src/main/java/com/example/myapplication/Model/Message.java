package com.example.myapplication.Model;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_TYPING = 2; // Thêm loại tin nhắn mới cho hiệu ứng "đang trả lời"

    private String content;
    private int type;

    public Message(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}