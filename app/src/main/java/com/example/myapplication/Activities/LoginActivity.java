package com.example.myapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText edEmail, edPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private TextView txtSignup, txtForgerPass;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        edEmail = findViewById(R.id.edemailLg);
        edPassword = findViewById(R.id.edpasswordLg);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);
        txtForgerPass = findViewById(R.id.txtForgetPass);

        btnLogin.setOnClickListener(v -> loginUser());

        txtSignup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        txtForgerPass.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class)));
    }

    private void loginUser() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = mAuth.getCurrentUser().getUid();
                    DocumentReference userRef = db.collection("users").document(userId);

                    // Kiểm tra và reset nhiệm vụ trước khi xử lý đăng nhập
                    checkAndResetDailyTasks(userId, userRef);

                    // Lấy thông tin người dùng từ Firestore
                    userRef.get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Kiểm tra và cập nhật nhiệm vụ đăng nhập hàng ngày
                                    checkAndUpdateDailyLogin(userId, documentSnapshot);

                                    // Chuyển hướng dựa trên vai trò
                                    String role = documentSnapshot.getString("role");
                                    if (role != null && role.equals("Admin")) {
                                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    }
                                    finish();
                                } else {
                                    // Nếu tài liệu không tồn tại, khởi tạo dữ liệu cơ bản
                                    initializeUserData(userId);
                                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(LoginActivity.this, "Lỗi khi kiểm tra vai trò!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Sai Tài Khoản Hoặc Mật khẩu!", Toast.LENGTH_SHORT).show());
    }

    // Thêm phương thức kiểm tra và reset nhiệm vụ
    private void checkAndResetDailyTasks(String userId, DocumentReference userRef) {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Date lastUpdated = null;
                Map<String, Object> dailyTasksMap = (Map<String, Object>) documentSnapshot.get("daily_tasks");
                if (dailyTasksMap != null && dailyTasksMap.containsKey("daily_login")) {
                    Map<String, Object> loginTask = (Map<String, Object>) dailyTasksMap.get("daily_login");
                    if (loginTask != null && loginTask.get("last_updated") != null) {
                        lastUpdated = ((com.google.firebase.Timestamp) loginTask.get("last_updated")).toDate();
                    }
                }

                Calendar now = Calendar.getInstance();
                Calendar today6AM = Calendar.getInstance();
                today6AM.set(Calendar.HOUR_OF_DAY, 6);
                today6AM.set(Calendar.MINUTE, 0);
                today6AM.set(Calendar.SECOND, 0);
                today6AM.set(Calendar.MILLISECOND, 0);

                if (lastUpdated == null || lastUpdated.before(today6AM.getTime())) {
                    resetDailyTasks(userRef);
                }
            }
        });
    }

    // Thêm phương thức reset nhiệm vụ
    private void resetDailyTasks(DocumentReference userRef) {
        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("completed", false);
        taskData.put("points_earned", 0);
        taskData.put("last_updated", FieldValue.serverTimestamp());

        updates.put("daily_tasks.checkin_cafe", taskData);
        updates.put("daily_tasks.daily_login", taskData);
        updates.put("daily_tasks.write_review", taskData);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Có thể thông báo nếu cần
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi reset nhiệm vụ!", Toast.LENGTH_SHORT).show();
                });
    }

    // Kiểm tra và cập nhật nhiệm vụ đăng nhập hàng ngày
    private void checkAndUpdateDailyLogin(String userId, com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        DocumentReference userRef = db.collection("users").document(userId);

        // Lấy thời gian đăng nhập cuối cùng
        Date lastLogin = documentSnapshot.getDate("last_login");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Kiểm tra xem có phải lần đăng nhập đầu tiên trong ngày không
        if (lastLogin == null || !new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(lastLogin).equals(currentDate)) {
            // Cập nhật trạng thái nhiệm vụ daily_login
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("completed", true);
            taskData.put("points_earned", 1); // 1 điểm cho đăng nhập hàng ngày
            taskData.put("last_updated", FieldValue.serverTimestamp());

            Map<String, Object> updates = new HashMap<>();
            updates.put("daily_tasks.daily_login", taskData);
            updates.put("last_login", FieldValue.serverTimestamp()); // Cập nhật thời gian đăng nhập cuối cùng
            updates.put("points", FieldValue.increment(1)); // Tăng điểm tổng cộng

            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Nhiệm vụ đăng nhập hàng ngày hoàn thành! +1 điểm", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi cập nhật nhiệm vụ đăng nhập!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu đã đăng nhập trong ngày, không làm gì thêm
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    // Khởi tạo dữ liệu người dùng nếu tài liệu chưa tồn tại
    private void initializeUserData(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("points", 0);
        userData.put("last_login", FieldValue.serverTimestamp());

        Map<String, Object> dailyTasks = new HashMap<>();
        dailyTasks.put("checkin_cafe", createTaskMap(false, 0));
        dailyTasks.put("daily_login", createTaskMap(false, 0));
        dailyTasks.put("write_review", createTaskMap(false, 0));
        userData.put("daily_tasks", dailyTasks);

        Map<String, Object> vouchers = new HashMap<>(); // Khởi tạo kho voucher rỗng
        userData.put("vouchers", vouchers);

        userRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Khởi tạo dữ liệu người dùng thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi khởi tạo dữ liệu người dùng!", Toast.LENGTH_SHORT).show();
                });
    }

    // Tạo dữ liệu nhiệm vụ mặc định
    private Map<String, Object> createTaskMap(boolean completed, int points) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("completed", completed);
        taskData.put("points_earned", points);
        taskData.put("last_updated", null);
        return taskData;
    }
}