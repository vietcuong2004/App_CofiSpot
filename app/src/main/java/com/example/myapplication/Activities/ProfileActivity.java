package com.example.myapplication.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText edName, edPhone, edEmail, edPassword;
    private ImageView ivAvatar;
    private Button btnSave, btnChangePassword;
    private TextView toolbarTitle;
    private Uri avatarUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    // Thay YOUR_CLIENT_ID bằng Client ID của bạn từ Imgur
    private static final String IMGUR_CLIENT_ID = "44708ec159ebd14";
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Hồ Sơ Cá Nhân ☕");

        // Ánh xạ các view
        ivAvatar = findViewById(R.id.iv_avatar);
        edName = findViewById(R.id.ed_name);
        edPhone = findViewById(R.id.ed_phone);
        edEmail = findViewById(R.id.ed_email);
        edPassword = findViewById(R.id.ed_password);
        btnSave = findViewById(R.id.btn_save);
        btnChangePassword = findViewById(R.id.btn_change_password);

        // Khởi tạo launcher để chọn ảnh
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                avatarUri = result.getData().getData();
                // Hiển thị ảnh tạm thời trước khi lưu
                Glide.with(this)
                        .load(avatarUri)
                        .apply(RequestOptions.circleCropTransform()) // Cắt ảnh thành hình tròn
                        .placeholder(R.drawable.ic_account)
                        .error(R.drawable.ic_account)
                        .into(ivAvatar);
            }
        });

        // Load thông tin người dùng
        loadUserProfile();

        // Sự kiện chọn ảnh avatar
        ivAvatar.setOnClickListener(v -> pickImageFromGallery());

        // Sự kiện lưu thông tin
        btnSave.setOnClickListener(v -> saveProfileChanges());

        // Sự kiện thay đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            edEmail.setText(user.getEmail());
            edEmail.setEnabled(false);

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phone");
                            String avatar = documentSnapshot.getString("avatar");

                            edName.setText(name != null ? name : "");
                            edPhone.setText(phone != null ? phone : "");
                            if (avatar != null && !avatar.isEmpty()) {
                                Log.d(TAG, "Avatar URL: " + avatar);
                                Glide.with(this)
                                        .load(avatar)
                                        .apply(RequestOptions.circleCropTransform()) // Cắt ảnh thành hình tròn
                                        .placeholder(R.drawable.ic_account)
                                        .error(R.drawable.ic_account)
                                        .into(ivAvatar);
                            } else {
                                Log.d(TAG, "Không có avatar trong Firestore");
                                Glide.with(this)
                                        .load(R.drawable.ic_account)
                                        .apply(RequestOptions.circleCropTransform()) // Cắt ảnh mặc định thành hình tròn
                                        .into(ivAvatar);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải thông tin!", Toast.LENGTH_SHORT).show());
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveProfileChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String name = edName.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật thông tin
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);

        if (avatarUri != null) {
            uploadAvatarToImgur(user.getUid(), userData);
        } else {
            updateFirestore(user.getUid(), userData);
        }
    }

    private void uploadAvatarToImgur(String userId, Map<String, Object> userData) {
        if (avatarUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Chuyển ảnh thành byte array
            InputStream inputStream = getContentResolver().openInputStream(avatarUri);
            if (inputStream == null) {
                Toast.makeText(this, "Không thể đọc ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Tạo request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "avatar.jpg",
                            RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                    .build();

            // Tạo request
            Request request = new Request.Builder()
                    .url(IMGUR_UPLOAD_URL)
                    .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();

            // Gửi request
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Lỗi khi tải ảnh lên Imgur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    Log.e(TAG, "Lỗi upload Imgur: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String imageUrl = json.getJSONObject("data").getString("link");
                            userData.put("avatar", imageUrl);
                            updateFirestore(userId, userData);
                            runOnUiThread(() -> {
                                Toast.makeText(ProfileActivity.this, "Upload ảnh thành công!", Toast.LENGTH_SHORT).show();
                                // Hiển thị ảnh vừa upload dưới dạng hình tròn
                                Glide.with(ProfileActivity.this)
                                        .load(imageUrl)
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.ic_account)
                                        .error(R.drawable.ic_account)
                                        .into(ivAvatar);
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Lỗi khi phân tích phản hồi từ Imgur!", Toast.LENGTH_SHORT).show());
                            Log.e(TAG, "Lỗi phân tích JSON: " + e.getMessage());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Upload ảnh thất bại: " + response.message(), Toast.LENGTH_SHORT).show());
                        Log.e(TAG, "Phản hồi không thành công: " + response.message());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xử lý ảnh!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi xử lý ảnh: " + e.getMessage());
        }
    }

    private void updateFirestore(String userId, Map<String, Object> userData) {
        db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật hồ sơ!", Toast.LENGTH_SHORT).show());
    }

    private void changePassword() {
        String newPassword = edPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6 || !Character.isUpperCase(newPassword.charAt(0))) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự và bắt đầu bằng chữ cái in hoa!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9]{10,15}$");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}