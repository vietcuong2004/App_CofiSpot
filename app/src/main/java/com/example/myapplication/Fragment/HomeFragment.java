package com.example.myapplication.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Activities.ReviewActivity;
import com.example.myapplication.Adapter.HomeCafeAdapter;
import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeCafeAdapter cafeAdapter;
    private List<Cafe> cafeList;
    private FirebaseFirestore db;
    private TextView tvGreeting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Thiết lập lời chào
        tvGreeting = view.findViewById(R.id.tv_greeting);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvGreeting.setText("Xin chào, " + (name != null ? name : "Khách") + " ☕");
                    }
                });

        // Thiết lập nút "Bắt đầu khám phá"
        Button btnExplore = view.findViewById(R.id.btn_explore);
        btnExplore.setOnClickListener(v -> {
            // Chuyển sang SearchFragment
            SearchFragment searchFragment = new SearchFragment();
            Bundle bundle = new Bundle();
            bundle.putFloat("maxDistance", 5000); // 5km
            bundle.putBoolean("fromExplore", true); // Đánh dấu là từ nút "Khám phá"
            searchFragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null)
                    .commit();

            // Cập nhật BottomNavigationView mà không kích hoạt listener
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                ((HomeActivity) requireActivity()).setManualNavigation(true); // Đặt cờ trước khi thay đổi
                bottomNavigationView.setSelectedItemId(R.id.nav_search);
            }
        });

        // Thiết lập RecyclerView cuộn ngang
        recyclerView = view.findViewById(R.id.recycler_view_hot_cafes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Giới hạn hiển thị tối đa 3 quán
        recyclerView.setItemViewCacheSize(3);
        recyclerView.setHasFixedSize(true);

        // Thêm hiệu ứng snap (dừng đúng item)
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        cafeList = new ArrayList<>();
        cafeAdapter = new HomeCafeAdapter(requireContext(), cafeList, cafe -> {
            // Xử lý khi nhấn vào quán cafe
            Intent intent = new Intent(requireContext(), ReviewActivity.class);
            intent.putExtra("cafeId", cafe.getId());
            intent.putExtra("sourceFragment", "HomeFragment"); // Thêm sourceFragment
            startActivity(intent);
        });
        recyclerView.setAdapter(cafeAdapter);

        // Tải danh sách quán cà phê từ Firestore
        loadCafesFromFirestore();

        return view;
    }

    private void loadCafesFromFirestore() {
        db.collection("cafes")
                .limit(5) // Giới hạn chỉ lấy 5 quán đầu tiên
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cafeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Cafe cafe = document.toObject(Cafe.class);
                        cafe.setId(document.getId());
                        cafeList.add(cafe);
                    }
                    cafeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Lỗi khi tải danh sách quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}