package com.example.myapplication.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Activities.ReviewActivity;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class CheckinFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo ActivityResultLauncher để yêu cầu quyền
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    // Thay thế getOrDefault bằng cách kiểm tra thủ công để tương thích API 23
                    boolean fineLocationGranted = result.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) ?
                            result.get(Manifest.permission.ACCESS_FINE_LOCATION) : false;
                    boolean coarseLocationGranted = result.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION) ?
                            result.get(Manifest.permission.ACCESS_COARSE_LOCATION) : false;

                    if (fineLocationGranted) {
                        getCurrentLocation();
                    } else if (coarseLocationGranted) {
                        getCurrentLocation();
                    } else {
                        Toast.makeText(requireContext(), "Bạn cần cấp quyền để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkin, container, false);

        // Khởi tạo Firestore và Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Xử lý nút Check-in
        Button btnCheckin = view.findViewById(R.id.btn_checkin);
        btnCheckin.setOnClickListener(v -> checkin());

        // Lấy vị trí hiện tại
        getCurrentLocation();

        return view;
    }

    private void getCurrentLocation() {
        // Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLocation = location;
            }
        });
    }

    private void checkin() {
        if (currentLocation == null) {
            Toast.makeText(requireContext(), "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để check-in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        GeoPoint userGeoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

        // Khai báo các biến ngoài lambda
        final boolean[] canCheckin = {false};
        final String[] cafeId = {""};
        final String[] cafeName = {""};
        final double[] ratingStar = {0.0};
        final String[] address = {""};
        final String[] description = {""};
        final String[] image1 = {""};
        final String[] activity = {""};

        // Truy vấn các quán cà phê từ Firestore
        db.collection("cafes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    GeoPoint cafeLocation = document.getGeoPoint("location");
                    String name = document.getString("name");

                    if (cafeLocation != null && name != null) {
                        Location loc = new Location("");
                        loc.setLatitude(cafeLocation.getLatitude());
                        loc.setLongitude(cafeLocation.getLongitude());

                        float distance = currentLocation.distanceTo(loc);
                        if (distance <= 5000) { // KHOẢNG CÁCH CHECKIN
                            canCheckin[0] = true;
                            cafeId[0] = document.getId();
                            cafeName[0] = name;
                            ratingStar[0] = document.getDouble("ratingStar") != null ? document.getDouble("ratingStar") : 0.0;
                            address[0] = document.getString("address") != null ? document.getString("address") : "";
                            description[0] = document.getString("description");
                            image1[0] = document.getString("image1");
                            activity[0] = document.getString("activity");
                            break;
                        }
                    }
                }

                if (canCheckin[0]) {
                    // Lưu check-in vào Firestore
                    Map<String, Object> checkinData = new HashMap<>();
                    checkinData.put("userId", userId);
                    checkinData.put("cafeId", cafeId[0]);
                    checkinData.put("cafeName", cafeName[0]);
                    checkinData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    checkinData.put("location", userGeoPoint);

                    db.collection("checkins").add(checkinData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(requireContext(), "Check-in thành công tại " + cafeName[0], Toast.LENGTH_SHORT).show();
                                updateUserPoints(userId); // Thêm 1 điểm

                                // Cập nhật trạng thái nhiệm vụ check-in
                                updateDailyTaskStatus(userId, "checkin_cafe", 5); // điểm cho check-in

                                // Chuyển hướng sang ReviewActivity
                                Intent intent = new Intent(requireContext(), ReviewActivity.class);
                                intent.putExtra("cafeId", cafeId[0]);
                                intent.putExtra("cafeName", cafeName[0]);
                                intent.putExtra("ratingStar", ratingStar[0]);
                                intent.putExtra("address", address[0]);
                                intent.putExtra("description", description[0]);
                                intent.putExtra("image1", image1[0]);
                                intent.putExtra("activity", activity[0]);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi check-in!", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(requireContext(), "Không có quán cà phê nào trong vòng 50m!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Lỗi khi tải danh sách quán!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thêm phương thức cập nhật trạng thái nhiệm vụ
    private void updateDailyTaskStatus(String userId, String taskId, int points) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("completed", true);
        taskData.put("points_earned", points);
        taskData.put("last_updated", com.google.firebase.firestore.FieldValue.serverTimestamp());

        Map<String, Object> updates = new HashMap<>();
        updates.put("daily_tasks." + taskId, taskData);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Có thể thông báo nếu cần
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi khi cập nhật nhiệm vụ!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserPoints(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Long currentPoints = documentSnapshot.getLong("points");
            int newPoints = (currentPoints != null ? currentPoints.intValue() : 0) + 5;

            userRef.update("points", newPoints)
                    .addOnSuccessListener(aVoid -> {
                        // Có thể thêm thông báo nếu muốn
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi cập nhật điểm!", Toast.LENGTH_SHORT).show());
        });
    }
}