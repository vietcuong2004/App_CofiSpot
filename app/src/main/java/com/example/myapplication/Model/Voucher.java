package com.example.myapplication.Model;

public class Voucher {
    private String id;
    private String name;
    private String description;
    private int pointsCost; // Số điểm cần để đổi
    private int quantity; // Số lượng trong kho của người dùng (chỉ áp dụng cho kho voucher)

    public Voucher(String id, String name, String description, int pointsCost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsCost = pointsCost;
        this.quantity = 0; // Mặc định không có trong kho
    }

    public Voucher(String id, String name, String description, int pointsCost, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsCost = pointsCost;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPointsCost() { return pointsCost; }
    public int getQuantity() { return quantity; }
}