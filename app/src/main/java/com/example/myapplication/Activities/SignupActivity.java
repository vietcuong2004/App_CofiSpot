package com.example.myapplication.Activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText edname, edphone, edemail, edpassword, edrppassword;
    private Button btnsignup;
    private TextView txtLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        edname = findViewById(R.id.edname);
        edphone = findViewById(R.id.edphone);
        edemail = findViewById(R.id.edemail);
        edpassword = findViewById(R.id.edpassword);
        edrppassword = findViewById(R.id.edrppassword);
        btnsignup = findViewById(R.id.btnsignup);
        txtLogin = findViewById(R.id.txtLogin);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnsignup.setOnClickListener(view -> {
            // Get input values
            String name = edname.getText().toString().trim();
            String phone = edphone.getText().toString().trim();
            String email = edemail.getText().toString().trim();
            String password = edpassword.getText().toString().trim();
            String rppassword = edrppassword.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || rppassword.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPhone(phone)) {
                Toast.makeText(SignupActivity.this, "Số điện thoại không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(rppassword)) {
                Toast.makeText(SignupActivity.this, "Mật khẩu không khớp nhau!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(SignupActivity.this, "Địa chỉ email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6 || !Character.isUpperCase(password.charAt(0))) {
                Toast.makeText(SignupActivity.this, "Mật khẩu phải có ít nhất 6 kí tự và viết hoa chữ cái đầu tiên!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user in Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success, save additional info to Firestore
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserToFirestore(user, name, email, phone);
                        } else {
                            // If sign up fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        txtLogin.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }

    private void saveUserToFirestore(FirebaseUser user, String name, String email, String phone) {
        // Create a new user document in Firestore with additional fields
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("role", "Customer"); // Default role
        userData.put("avatar", null);     // Default avatar URI (null)
        userData.put("points", 10);        // Default points (0)
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Data saved successfully
                    Toast.makeText(SignupActivity.this, "Đăng ký thành công! Bạn được tặng 10 điểm.", Toast.LENGTH_SHORT).show();

                    // Redirect to login with prefilled email
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error saving data
                    Toast.makeText(SignupActivity.this, "Lỗi khi lưu thông tin người dùng!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving user data", e);

                    // Delete the user from Authentication if Firestore save fails
                    user.delete().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Could not delete user after Firestore failure", task.getException());
                        }
                    });
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Simple validation: 10-15 digits
        return phone.matches("^[0-9]{10,15}$");
    }
}