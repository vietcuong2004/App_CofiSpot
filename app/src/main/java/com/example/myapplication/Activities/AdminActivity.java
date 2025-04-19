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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.example.myapplication.Adapter.AdminCafeAdapter;
import com.example.myapplication.Model.CafeAdmin;
import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

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
import org.json.JSONException;
import org.json.JSONObject;

public class AdminActivity extends AppCompatActivity implements AdminCafeAdapter.OnCafeActionListener {

    private static final String TAG = "AdminActivity";
    private RecyclerView rvCafes;
    private AdminCafeAdapter cafeAdapter;
    private List<CafeAdmin> cafeList;
    private FirebaseFirestore db;
    private Button btnAddCafe;
    private ImageButton btnLogout;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> locationPickerLauncher;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();
    private HorizontalScrollView mediaContainer;
    private OkHttpClient client;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private View dialogView;
    private static final String IMGUR_CLIENT_ID = "44708ec159ebd14";
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/upload";
    private static final int STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        client = new OkHttpClient();

        requestStoragePermissions();

        rvCafes = findViewById(R.id.rv_cafes);
        cafeList = new ArrayList<>();
        cafeAdapter = new AdminCafeAdapter(this, cafeList, this);
        rvCafes.setLayoutManager(new LinearLayoutManager(this));
        rvCafes.setAdapter(cafeAdapter);

        btnAddCafe = findViewById(R.id.btn_add_cafe);
        btnAddCafe.setOnClickListener(v -> showAddEditCafeDialog(null));

        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && selectedImageUris.size() < 3) {
                        selectedImageUris.add(uri);
                        Toast.makeText(this, "Đã chọn " + selectedImageUris.size() + "/3 hình ảnh", Toast.LENGTH_SHORT).show();
                        updateMediaContainer();
                    } else if (uri != null) {
                        Toast.makeText(this, "Đã đạt tối đa 3 hình ảnh!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedLat = data.getDoubleExtra("latitude", 0.0);
                        selectedLng = data.getDoubleExtra("longitude", 0.0);
                        String address = data.getStringExtra("address");
                        EditText etCafeAddress = dialogView != null ? dialogView.findViewById(R.id.et_cafe_address) : null;
                        if (etCafeAddress != null) {
                            etCafeAddress.setText(address);
                        }
                    }
                }
        );

        loadCafes();
    }

    private void loadCafes() {
        db.collection("cafes")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading cafes: ", error);
                        Toast.makeText(this, "Lỗi khi tải danh sách quán!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cafeList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : value) {
                            try {
                                CafeAdmin cafe = document.toObject(CafeAdmin.class);
                                if (cafe != null) {
                                    cafe.setId(document.getId());
                                    cafeList.add(cafe);
                                    Log.d(TAG, "Loaded cafe: " + cafe.getName() + ", ID: " + cafe.getId());
                                } else {
                                    Log.w(TAG, "Failed to deserialize document: " + document.getId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error deserializing document " + document.getId() + ": " + e.getMessage());
                            }
                        }
                        cafeAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Total cafes loaded: " + cafeList.size());
                    } else {
                        Log.d(TAG, "No cafes found in collection 'cafes'");
                        Toast.makeText(this, "Không có quán cà phê nào trong danh sách!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddEditCafeDialog(CafeAdmin cafe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_cafe, null);
        builder.setView(dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        EditText etCafeName = dialogView.findViewById(R.id.et_cafe_name);
        EditText etCafeAddress = dialogView.findViewById(R.id.et_cafe_address);
        ImageButton btnSelectLocation = dialogView.findViewById(R.id.btn_select_location);
        Spinner spinnerActivity = dialogView.findViewById(R.id.spinner_activity);
        EditText etOtherActivity = dialogView.findViewById(R.id.et_other_activity);
        LinearLayout layoutOtherActivity = dialogView.findViewById(R.id.layout_other_activity);
        EditText etCafeDescription = dialogView.findViewById(R.id.et_cafe_description);
        Button btnAddImages = dialogView.findViewById(R.id.btn_add_images);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        mediaContainer = dialogView.findViewById(R.id.media_container);

        selectedImageUris.clear();
        uploadedImageUrls.clear();
        selectedLat = cafe != null ? cafe.getLat() : 0.0;
        selectedLng = cafe != null ? cafe.getLng() : 0.0;

        if (cafe != null) {
            tvDialogTitle.setText("Sửa Quán Cà Phê");
            etCafeName.setText(cafe.getName());
            etCafeAddress.setText(cafe.getLocationText());
            etCafeDescription.setText(cafe.getDescription());
            if (cafe.getImage1() != null && !cafe.getImage1().isEmpty()) uploadedImageUrls.add(cafe.getImage1());
            if (cafe.getImage2() != null && !cafe.getImage2().isEmpty()) uploadedImageUrls.add(cafe.getImage2());
            if (cafe.getImage3() != null && !cafe.getImage3().isEmpty()) uploadedImageUrls.add(cafe.getImage3());
        } else {
            tvDialogTitle.setText("Thêm Quán Cà Phê");
        }

        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, SelectLocationActivity.class);
            locationPickerLauncher.launch(intent);
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.activity_review_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        if (cafe != null && cafe.getActivity() != null) {
            String activity = cafe.getActivity();
            if (!activity.equals("Boardgame") && !activity.equals("Book") && !activity.equals("Workshop")) {
                spinnerActivity.setSelection(adapter.getPosition("others"));
                etOtherActivity.setText(activity);
                layoutOtherActivity.setVisibility(View.VISIBLE);
            } else {
                spinnerActivity.setSelection(adapter.getPosition(activity));
            }
        }

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutOtherActivity.setVisibility(parent.getItemAtPosition(position).toString().equals("others") ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutOtherActivity.setVisibility(View.GONE);
            }
        });

        btnAddImages.setOnClickListener(v -> {
            if (!hasStoragePermission()) {
                requestStoragePermissions();
                return;
            }
            imagePickerLauncher.launch("image/*");
        });

        updateMediaContainer();

        AlertDialog dialog = builder.create();

        btnConfirm.setOnClickListener(v -> {
            // Vô hiệu hóa nút ngay khi nhấn
            btnConfirm.setEnabled(false);
            btnConfirm.setAlpha(0.5f); // Làm mờ nút để phản hồi trực quan

            String name = etCafeName.getText().toString().trim();
            String locationText = etCafeAddress.getText().toString().trim();
            String description = etCafeDescription.getText().toString().trim();
            String activity = spinnerActivity.getSelectedItem().toString();
            String otherActivity = etOtherActivity.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên quán!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true); // Kích hoạt lại nếu validation thất bại
                btnConfirm.setAlpha(1.0f);
                return;
            }
            if (locationText.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                return;
            }
            if (activity.equals("Chọn hoạt động")) {
                activity = null;
            }
            if (activity != null && activity.equals("others")) {
                if (otherActivity.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập mô tả hoạt động!", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setAlpha(1.0f);
                    return;
                }
                if (otherActivity.length() > 50) {
                    Toast.makeText(this, "Mô tả hoạt động không được vượt quá 50 ký tự!", Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setAlpha(1.0f);
                    return;
                }
            }
            if (selectedImageUris.isEmpty() && uploadedImageUrls.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 hình ảnh!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                return;
            }
            if (selectedLat == 0.0 && selectedLng == 0.0 && cafe == null) {
                Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true);
                btnConfirm.setAlpha(1.0f);
                return;
            }

            uploadMediaAndSaveCafe(cafe, name, locationText, description, activity, otherActivity, dialog, btnConfirm);
        });

        dialog.show();
    }

    private void uploadMediaAndSaveCafe(CafeAdmin cafe, String name, String locationText, String description,
                                        String activity, String otherActivity, AlertDialog dialog, Button btnConfirm) {
        if (selectedImageUris.isEmpty() && !uploadedImageUrls.isEmpty()) {
            saveCafe(cafe, name, locationText, description, activity, otherActivity, dialog, btnConfirm);
            return;
        }

        int totalImages = selectedImageUris.size();
        final int[] uploadedCount = {0};
        uploadedImageUrls.clear();

        for (Uri imageUri : selectedImageUris) {
            try {
                byte[] imageBytes = readBytesFromUri(imageUri);
                if (imageBytes == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không thể đọc dữ liệu hình ảnh!", Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                        btnConfirm.setAlpha(1.0f);
                        dialog.dismiss();
                    });
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
                            if (!isFinishing()) {
                                Toast.makeText(AdminActivity.this, "Lỗi khi upload hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                                btnConfirm.setAlpha(1.0f);
                                dialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                JSONObject json = new JSONObject(responseBody);
                                if (!json.has("data") || !json.getJSONObject("data").has("link")) {
                                    throw new JSONException("Phản hồi không chứa trường 'data' hoặc 'link'");
                                }
                                String imageUrl = json.getJSONObject("data").getString("link");
                                uploadedImageUrls.add(imageUrl);
                                uploadedCount[0]++;

                                if (uploadedCount[0] == totalImages) {
                                    runOnUiThread(() -> saveCafe(cafe, name, locationText, description, activity, otherActivity, dialog, btnConfirm));
                                }
                            } catch (JSONException e) {
                                runOnUiThread(() -> {
                                    if (!isFinishing()) {
                                        Toast.makeText(AdminActivity.this, "Lỗi phân tích JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                                        btnConfirm.setAlpha(1.0f);
                                        dialog.dismiss();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                if (!isFinishing()) {
                                    Toast.makeText(AdminActivity.this, "Lỗi khi upload hình ảnh!", Toast.LENGTH_SHORT).show();
                                    btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                                    btnConfirm.setAlpha(1.0f);
                                    dialog.dismiss();
                                }
                            });
                        }
                        response.close();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        Toast.makeText(AdminActivity.this, "Lỗi xử lý hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                        btnConfirm.setAlpha(1.0f);
                        dialog.dismiss();
                    }
                });
            }
        }
    }

    private void saveCafe(CafeAdmin cafe, String name, String locationText, String description,
                          String activity, String otherActivity, AlertDialog dialog, Button btnConfirm) {
        Map<String, Object> cafeData = new HashMap<>();
        cafeData.put("name", name);
        cafeData.put("locationText", locationText);
        cafeData.put("location", new GeoPoint(selectedLat, selectedLng));
        cafeData.put("description", description);
        cafeData.put("activity", activity != null && activity.equals("others") ? otherActivity : activity);
        cafeData.put("image1", uploadedImageUrls.size() > 0 ? uploadedImageUrls.get(0) : "");
        cafeData.put("image2", uploadedImageUrls.size() > 1 ? uploadedImageUrls.get(1) : "");
        cafeData.put("image3", uploadedImageUrls.size() > 2 ? uploadedImageUrls.get(2) : "");
        cafeData.put("lat", selectedLat);
        cafeData.put("lng", selectedLng);

        if (cafe == null) {
            // Thêm quán mới vào 'cafes'
            db.collection("cafes")
                    .add(cafeData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Added cafe with ID: " + documentReference.getId());
                        Toast.makeText(this, "Thêm quán thành công!", Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true); // Kích hoạt lại sau khi thành công
                        btnConfirm.setAlpha(1.0f);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding cafe: ", e);
                        Toast.makeText(this, "Lỗi khi thêm quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                        btnConfirm.setAlpha(1.0f);
                        dialog.dismiss();
                    });
        } else {
            // Sửa quán trong 'cafes'
            String docId = cafe.getId();
            db.collection("cafes")
                    .document(docId)
                    .set(cafeData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Updated cafe with ID: " + docId);
                        Toast.makeText(this, "Sửa quán thành công!", Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true); // Kích hoạt lại sau khi thành công
                        btnConfirm.setAlpha(1.0f);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating cafe: ", e);
                        Toast.makeText(this, "Lỗi khi sửa quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true); // Kích hoạt lại nếu thất bại
                        btnConfirm.setAlpha(1.0f);
                        dialog.dismiss();
                    });
        }
    }

    private void updateMediaContainer() {
        if (mediaContainer == null) return;

        mediaContainer.removeAllViews();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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

    private void requestStoragePermissions() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
        }
    }

    private boolean hasStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền truy cập bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] readBytesFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error reading URI: ", e);
            return null;
        }
    }

    @Override
    public void onEditClick(CafeAdmin cafe) {
        showAddEditCafeDialog(cafe);
    }

    @Override
    public void onDeleteClick(CafeAdmin cafe) {
        new AlertDialog.Builder(this)
                .setTitle("Xác Nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa quán " + cafe.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String docId = cafe.getId();
                    db.collection("cafes")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Deleted cafe with ID: " + docId);
                                Toast.makeText(this, "Xóa quán thành công!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting cafe: ", e);
                                Toast.makeText(this, "Lỗi khi xóa quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}