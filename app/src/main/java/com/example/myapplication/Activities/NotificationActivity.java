package com.example.myapplication.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapter.NotificationAdapter;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private Button btnClearNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
        ((android.widget.TextView) findViewById(R.id.toolbar_title)).setText("Thông Báo");

        // Khởi tạo RecyclerView
        rvNotifications = findViewById(R.id.rv_notifications);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);

        // Khởi tạo nút Clear
        btnClearNotifications = findViewById(R.id.btn_clear_notifications);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Lấy danh sách thông báo từ Firestore
        loadNotifications();

        // Xử lý sự kiện nhấn nút Clear
        btnClearNotifications.setOnClickListener(v -> clearAllNotifications());
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("notifications")
                .document(userId)
                .collection("user_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    notificationList.clear();
                    if (value != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : value) {
                            Notification notification = document.toObject(Notification.class);
                            notificationList.add(notification);
                        }
                    }
                    notificationAdapter.notifyDataSetChanged();
                });
    }

    private void clearAllNotifications() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("notifications")
                .document(userId)
                .collection("user_notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Xóa từng document trong subcollection
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Toast.makeText(this, "Đã xóa tất cả thông báo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa thông báo!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

