package com.example.myapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Adapter.DailyTaskAdapter;
import com.example.myapplication.Adapter.VoucherAdapter;
import com.example.myapplication.Model.DailyTask;
import com.example.myapplication.Model.Voucher;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RewardsFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvPoints;
    private RecyclerView rvMissions, rvStore, rvUserVouchers;
    private DailyTaskAdapter taskAdapter;
    private VoucherAdapter storeAdapter, userVoucherAdapter;
    private List<DailyTask> dailyTasks = new ArrayList<>();
    private List<Voucher> storeVouchers = new ArrayList<>();
    private List<Voucher> userVouchers = new ArrayList<>();
    private long userPoints = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        tvPoints = view.findViewById(R.id.tv_points);
        rvMissions = view.findViewById(R.id.rv_missions);
        rvStore = view.findViewById(R.id.rv_store);
        rvUserVouchers = view.findViewById(R.id.rv_user_vouchers);

        setupTaskRecyclerView();
        setupStoreRecyclerView();
        setupUserVoucherRecyclerView();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        // Kiểm tra và reset nhiệm vụ nếu cần
        checkAndResetDailyTasks(userId, userRef);

        // Lắng nghe thay đổi từ Firestore
        userRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                tvPoints.setText("Lỗi khi tải dữ liệu");
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                Long points = documentSnapshot.getLong("points");
                userPoints = points != null ? points : 0;
                tvPoints.setText("Tổng điểm thưởng: " + userPoints);

                Map<String, Object> dailyTasksMap = (Map<String, Object>) documentSnapshot.get("daily_tasks");
                if (dailyTasksMap != null) {
                    updateDailyTasksList(dailyTasksMap);
                }

                Map<String, Object> vouchersMap = (Map<String, Object>) documentSnapshot.get("vouchers");
                updateUserVouchersList(vouchersMap);
            } else {
                tvPoints.setText("Không có dữ liệu");
            }
        });

        return view;
    }

    private void setupTaskRecyclerView() {
        taskAdapter = new DailyTaskAdapter(dailyTasks);
        rvMissions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMissions.setAdapter(taskAdapter);
    }

    private void setupStoreRecyclerView() {
        // Tạo sẵn 3 voucher trong cửa hàng
        storeVouchers.add(new Voucher("voucher_50", "Giảm 50%", "Áp dụng cho tất cả các quán", 100));
        storeVouchers.add(new Voucher("voucher_20", "Giảm 20%", "Áp dụng cho quán Coffee House", 50));
        storeVouchers.add(new Voucher("voucher_10", "Giảm 10%", "Áp dụng cho tất cả các quán", 20));

        storeAdapter = new VoucherAdapter(storeVouchers, true, this::redeemVoucher);
        rvStore.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStore.setAdapter(storeAdapter);
    }

    private void setupUserVoucherRecyclerView() {
        userVoucherAdapter = new VoucherAdapter(userVouchers, false, null);
        rvUserVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUserVouchers.setAdapter(userVoucherAdapter);
    }

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
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi khi reset nhiệm vụ!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDailyTasksList(Map<String, Object> dailyTasksMap) {
        dailyTasks.clear();

        Map<String, Object> checkinTask = (Map<String, Object>) dailyTasksMap.get("checkin_cafe");
        if (checkinTask != null) {
            boolean completed = checkinTask.get("completed") != null && (boolean) checkinTask.get("completed");
            Object points = checkinTask.get("points_earned");
            int pointsValue = points instanceof String ? Integer.parseInt((String) points) : ((Number) points).intValue();
            dailyTasks.add(new DailyTask("checkin_cafe", "Check-in tại quán cafe", pointsValue, completed, "Hàng ngày"));
        }

        Map<String, Object> loginTask = (Map<String, Object>) dailyTasksMap.get("daily_login");
        if (loginTask != null) {
            boolean completed = loginTask.get("completed") != null && (boolean) loginTask.get("completed");
            Object points = loginTask.get("points_earned");
            int pointsValue = points instanceof String ? Integer.parseInt((String) points) : ((Number) points).intValue();
            dailyTasks.add(new DailyTask("daily_login", "Đăng nhập hàng ngày", pointsValue, completed, "Hàng ngày"));
        }

        Map<String, Object> reviewTask = (Map<String, Object>) dailyTasksMap.get("write_review");
        if (reviewTask != null) {
            boolean completed = reviewTask.get("completed") != null && (boolean) reviewTask.get("completed");
            Object points = reviewTask.get("points_earned");
            int pointsValue = points instanceof String ? Integer.parseInt((String) points) : ((Number) points).intValue();
            dailyTasks.add(new DailyTask("write_review", "Viết đánh giá", pointsValue, completed, "Hàng ngày"));
        }

        taskAdapter.notifyDataSetChanged();
    }

    private void updateUserVouchersList(Map<String, Object> vouchersMap) {
        userVouchers.clear();
        if (vouchersMap != null) {
            for (Voucher storeVoucher : storeVouchers) {
                String voucherId = storeVoucher.getId();
                if (vouchersMap.containsKey(voucherId)) {
                    Long quantity = (Long) vouchersMap.get(voucherId);
                    if (quantity != null && quantity > 0) {
                        userVouchers.add(new Voucher(
                                voucherId,
                                storeVoucher.getName(),
                                storeVoucher.getDescription(),
                                storeVoucher.getPointsCost(),
                                quantity.intValue()
                        ));
                    }
                }
            }
        }
        userVoucherAdapter.notifyDataSetChanged();
    }

    private void redeemVoucher(Voucher voucher) {
        if (userPoints < voucher.getPointsCost()) {
            Toast.makeText(getContext(), "Không đủ điểm để đổi voucher này!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("points", FieldValue.increment(-voucher.getPointsCost()));
        updates.put("vouchers." + voucher.getId(), FieldValue.increment(1));

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đổi " + voucher.getName() + " thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi đổi voucher!", Toast.LENGTH_SHORT).show();
                });
    }
}