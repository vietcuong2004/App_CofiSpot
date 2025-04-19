package com.example.myapplication.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapter.ReviewAdapter;
import com.example.myapplication.Model.Review;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReviewActivity extends AppCompatActivity {

    private TextView tvCafeName, tvRating, tvAddress, tvDescription, tvActivity;
    private ImageView ivCafeImage;
    private Button btnReview;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cafeId, userId;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private final List<Uri> selectedImageUris = new ArrayList<>();
    private final List<String> uploadedImageUrls = new ArrayList<>();
    private OkHttpClient client; //Biến khởi tạo để up ảnh lên imgur
    private static final String IMGUR_CLIENT_ID = "44708ec159ebd14";
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/upload";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private HorizontalScrollView mediaContainer;
    private String sourceFragment;

    private static final int ITEM_HEIGHT_DP = 266;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        db = FirebaseFirestore.getInstance(); //Kết nối đến Firebase
        mAuth = FirebaseAuth.getInstance();
        client = new OkHttpClient(); //Khởi tạo client HTTP để upload hình ảnh lên Imgur.

        requestStoragePermissions();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.back_icon).setOnClickListener(v -> onBackPressed());

        tvCafeName = findViewById(R.id.tv_cafe_name);
        tvRating = findViewById(R.id.tv_rating);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvActivity = findViewById(R.id.tv_activity);
        ivCafeImage = findViewById(R.id.iv_cafe_image);
        btnReview = findViewById(R.id.btn_review);
        rvReviews = findViewById(R.id.rv_reviews);

        reviewAdapter = new ReviewAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        rvReviews.setLayoutManager(layoutManager);
        rvReviews.setAdapter(reviewAdapter);

        cafeId = getIntent().getStringExtra("cafeId");
        sourceFragment = getIntent().getStringExtra("sourceFragment");

        Log.d("ReviewActivity", "Cafe ID: " + cafeId);

        loadCafeInfo();
        loadReviews();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        if (selectedImageUris.size() < 3) {
                            selectedImageUris.add(uri);
                            Toast.makeText(this, "Đã chọn " + selectedImageUris.size() + "/3 hình ảnh", Toast.LENGTH_SHORT).show();
                            updateMediaContainer();
                        } else {
                            Toast.makeText(this, "Đã đạt tối đa 3 hình ảnh!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnReview.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cafeId == null) {
                Toast.makeText(this, "Không tìm thấy ID quán!", Toast.LENGTH_SHORT).show();
                return;
            }
            showReviewDialog();
        });
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES
                }, PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền truy cập bị từ chối, không thể chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadCafeInfo() {
        if (cafeId != null) {
            db.collection("cafes").document(cafeId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) { //DocumentSnapshot là kiểu dữ liệu trả về từ firebase
                            String cafeName = documentSnapshot.getString("name");
                            Double ratingStar = documentSnapshot.getDouble("ratingStar");
                            String locationText = documentSnapshot.getString("locationText");
                            String description = documentSnapshot.getString("description");
                            String image1 = documentSnapshot.getString("image1");
                            String activity = documentSnapshot.getString("activity");

                            tvCafeName.setText(cafeName != null ? cafeName : "Tên quán");
                            tvRating.setText("Đánh giá: " + (ratingStar != null ? String.format("%.1f", ratingStar) : "0.0") + "/5");
                            tvAddress.setText("Địa chỉ: " + (locationText != null ? locationText : "Không có địa chỉ"));
                            tvDescription.setText("Mô tả: " + (description != null ? description : "Không có mô tả"));
                            tvActivity.setText("Hoạt động: " + (activity != null ? activity : "Không có hoạt động"));

                            if (image1 != null && !image1.isEmpty()) {
                                Picasso.get().load(image1).into(ivCafeImage);
                            } else {
                                ivCafeImage.setImageResource(R.drawable.ic_placeholder);
                            }
                        } else {
                            Toast.makeText(this, "Không tìm thấy thông tin quán!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi tải thông tin quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Không tìm thấy ID quán!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReviews() {
        if (cafeId == null) {
            Toast.makeText(this, "Không tìm thấy ID quán để tải đánh giá!", Toast.LENGTH_SHORT).show();
            Log.e("ReviewActivity", "cafeId is null");
            return;
        }

        Log.d("ReviewActivity", "Loading reviews for cafeId: " + cafeId);
        db.collection("reviews")
                .whereEqualTo("cafeId", cafeId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("ReviewActivity", "Number of reviews retrieved: " + queryDocumentSnapshots.size());
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("ReviewActivity", "No reviews found for cafeId: " + cafeId);
                        reviewAdapter.setReviewList(new ArrayList<>());
                        Toast.makeText(this, "Không có bình luận nào cho quán này!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Review> reviews = queryDocumentSnapshots.toObjects(Review.class);
                    Log.d("ReviewActivity", "Successfully converted to Review objects: " + reviews.size());
                    for (Review review : reviews) {
                        Log.d("ReviewActivity", "Review: " + review.getComment() + ", Username: " + review.getUsername());
                    }
                    reviewAdapter.setReviewList(reviews);
                    reviewAdapter.notifyDataSetChanged();

                    setRecyclerViewHeight(reviews.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("ReviewActivity", "Error loading reviews: " + e.getMessage(), e);
                    Toast.makeText(this, "Lỗi khi tải đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //Hàm dùng để tính toán kích thước hiển thị cuar Recycle View, xem hết các bình luận
    private void setRecyclerViewHeight(int itemCount) {
        float density = getResources().getDisplayMetrics().density;
        int itemHeightPx = (int) (ITEM_HEIGHT_DP * density);
        int totalHeightPx = itemCount * itemHeightPx;

        ViewGroup.LayoutParams params = rvReviews.getLayoutParams();
        params.height = totalHeightPx;
        rvReviews.setLayoutParams(params);

        Log.d("ReviewActivity", "Set RecyclerView height: " + totalHeightPx + "px for " + itemCount + " items");
    }


    //Upload ảnh đánh giá
    private void updateMediaContainer() {
        if (mediaContainer == null) return;

        mediaContainer.removeAllViews();

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                450
        ));
        linearLayout.setPadding(8, 8, 8, 8);

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);
            View mediaView = LayoutInflater.from(this).inflate(R.layout.item_media, linearLayout, false);

            ImageView ivMedia = mediaView.findViewById(R.id.ivMedia);
            ImageButton btnRemove = mediaView.findViewById(R.id.btnRemove);
            TextView tvMediaCount = mediaView.findViewById(R.id.tvMediaCount);

            ivMedia.setImageURI(uri);
            tvMediaCount.setText((i + 1) + "/3");

            btnRemove.setOnClickListener(v -> {
                selectedImageUris.remove(uri);
                updateMediaContainer();
                Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
            });

            linearLayout.addView(mediaView);
        }

        mediaContainer.addView(linearLayout);
    }

    //Hiện dialog đánh giá
    private void showReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
        Spinner spinnerActivity = dialogView.findViewById(R.id.spinnerActivity);
        EditText editTextOtherActivity = dialogView.findViewById(R.id.editTextOtherActivity);
        View btnAddImages = dialogView.findViewById(R.id.btnAddImages);
        Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);
        mediaContainer = dialogView.findViewById(R.id.mediaContainer);

        updateMediaContainer();

        boolean hasStoragePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED :
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.activity_review_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedActivity = parent.getItemAtPosition(position).toString();
                if (selectedActivity.equalsIgnoreCase("others")) {
                    editTextOtherActivity.setVisibility(View.VISIBLE);
                } else {
                    editTextOtherActivity.setVisibility(View.GONE);
                    editTextOtherActivity.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextOtherActivity.setVisibility(View.GONE);
                editTextOtherActivity.setText("");
            }
        });

        btnAddImages.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để thêm hình ảnh!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hasStoragePermission) {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập để chọn ảnh!", Toast.LENGTH_SHORT).show();
                requestStoragePermissions();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        AlertDialog dialog = builder.create();
        btnSubmitReview.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để gửi đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }

            float rating = ratingBar.getRating();
            String comment = editTextComment.getText().toString().trim();
            String activity = spinnerActivity.getSelectedItem().toString();
            String otherActivityDescription = editTextOtherActivity.getText().toString().trim();

            if (activity.equals("Chọn hoạt động")) {
                activity = null;
            }
            if (activity != null && activity.equalsIgnoreCase("others") && otherActivityDescription.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mô tả hoạt động!", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadMediaAndSubmitReview(rating, comment, activity, otherActivityDescription, dialog);
        });

        dialog.show();
    }

    private void uploadMediaAndSubmitReview(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        uploadedImageUrls.clear();

        if (!selectedImageUris.isEmpty()) {
            uploadImages(rating, comment, activity, otherActivityDescription, dialog);
        } else {
            submitReview(rating, comment, activity, otherActivityDescription, dialog);
        }
    }


    //Upload Hình ảnh
    private void uploadImages(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        int totalImages = selectedImageUris.size();
        final int[] uploadedCount = {0};

        for (Uri imageUri : selectedImageUris) {
            try {
                byte[] imageBytes = readBytesFromUri(imageUri);
                if (imageBytes == null) {
                    Toast.makeText(this, "Không thể đọc dữ liệu hình ảnh!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/*"), imageBytes))
                        .addFormDataPart("type", "file")
                        .build();

                Request request = new Request.Builder()
                        .url(IMGUR_UPLOAD_URL)
                        .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(ReviewActivity.this, "Lỗi khi upload hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            try {
                                JSONObject json = new JSONObject(responseBody);
                                String imageUrl = json.getJSONObject("data").getString("link");
                                uploadedImageUrls.add(imageUrl);
                                uploadedCount[0]++;

                                if (uploadedCount[0] == totalImages) {
                                    submitReview(rating, comment, activity, otherActivityDescription, dialog);
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ReviewActivity.this, "Lỗi khi phân tích phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(ReviewActivity.this, "Lỗi khi upload hình ảnh: " + response.message(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                        }
                        response.close();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ReviewActivity.this, "Lỗi khi xử lý hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
        }
    }

    private void submitReview(float rating, String comment, String activity, String otherActivityDescription, AlertDialog dialog) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            String username = documentSnapshot.getString("name");
            if (username == null || username.isEmpty()) {
                username = "Ẩn danh";
            }
            Log.d("ReviewActivity", "Username retrieved: " + username);

            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("userId", userId);
            reviewData.put("cafeId", cafeId);
            reviewData.put("rating", rating);
            reviewData.put("comment", comment.isEmpty() ? null : comment);
            reviewData.put("activity", activity);
            reviewData.put("otherActivityDescription", activity != null && activity.equalsIgnoreCase("others") ? otherActivityDescription : null);
            reviewData.put("images", uploadedImageUrls);
            reviewData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
            reviewData.put("username", username);

            // Thêm log để kiểm tra giá trị trước khi lưu
            Log.d("ReviewActivity", "Saving review - activity: " + activity + ", otherActivityDescription: " + otherActivityDescription);

            db.collection("reviews").add(reviewData)
                    .addOnSuccessListener(documentReference -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                            updateCafeRating(rating);
                            updateUserPoints();
                            updateDailyTaskStatus(userId, "write_review", 10);
                            dialog.dismiss();
                            selectedImageUris.clear();
                            uploadedImageUrls.clear();
                            loadCafeInfo();
                            loadReviews();
                        });
                    })
                    .addOnFailureListener(e -> runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi khi gửi đánh giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }));
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
    }

    private void updateDailyTaskStatus(String userId, String taskId, int points) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("completed", true);
        taskData.put("points_earned", points);
        taskData.put("last_updated", com.google.firebase.firestore.FieldValue.serverTimestamp());

        Map<String, Object> updates = new HashMap<>();
        updates.put("daily_tasks." + taskId, taskData);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật nhiệm vụ!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCafeRating(float rating) {
        DocumentReference cafeRef = db.collection("cafes").document(cafeId);
        cafeRef.get().addOnSuccessListener(documentSnapshot -> {
            Long ratingCount = documentSnapshot.getLong("ratingCount");
            Double currentRating = documentSnapshot.getDouble("ratingStar");

            long newRatingCount = (ratingCount != null ? ratingCount : 0) + 1;
            double totalRating = (currentRating != null ? currentRating * (newRatingCount - 1) : 0) + rating;
            double newAverageRating = totalRating / newRatingCount;

            Map<String, Object> updates = new HashMap<>();
            updates.put("ratingCount", newRatingCount);
            updates.put("ratingStar", newAverageRating);

            cafeRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        tvRating.setText("Đánh giá: " + String.format("%.1f", newAverageRating) + "/5");
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật đánh giá quán!", Toast.LENGTH_SHORT).show());
        });
    }

    // Cập nhật điểm cho user: đánh giá 1 lần được 10 điểm
    private void updateUserPoints() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Long currentPoints = documentSnapshot.getLong("points");
            int newPoints = (currentPoints != null ? currentPoints.intValue() : 0) + 10;

            userRef.update("points", newPoints)
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi cập nhật điểm!", Toast.LENGTH_SHORT).show());
        });
    }

    //lấy nội dung tệp từ Uri và chuyển thành định dạng mà các thư viện như OkHttp có thể sử dụng để upload lên server.
    private byte[] readBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //Bấm nút Back quay lại màn hình trước đó:
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sourceFragment != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("fragmentToLoad", sourceFragment);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }
}