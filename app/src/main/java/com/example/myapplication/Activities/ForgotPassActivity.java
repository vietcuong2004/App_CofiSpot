package com.example.myapplication.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        Button btnsenemail = findViewById(R.id.btnsenemail);
        EditText edmail = findViewById(R.id.edmail);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        LottieAnimationView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnsenemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edmail.getText().toString();
                if (email.isEmpty()){
                    Toast.makeText(ForgotPassActivity.this, "Email không được bỏ trống!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassActivity.this, "Chúng tôi đã gửi mail đến hộp thư của bạn để đổi mật khẩu!" + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPassActivity.this, "Không thể gửi mail. Hãy kiểm tra lại địa chỉ email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }
}
