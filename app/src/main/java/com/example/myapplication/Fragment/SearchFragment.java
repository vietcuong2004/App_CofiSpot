package com.example.myapplication.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Activities.ReviewActivity;
import com.example.myapplication.Adapter.SearchCafeAdapter;
import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private List<Cafe> cafeList;
    private float maxDistance = 20000; // Mặc định 20km
    private float minRating = 0; // Mặc định tất cả
    private String selectedActivity = "Tất cả"; // Mặc định tất cả hoạt động
    private String searchQuery = ""; // Chuỗi tìm kiếm
    private FirebaseFirestore db;
    private SearchCafeAdapter cafeAdapter;
    private boolean hasShownLocationNotReady = false;
    private Marker currentLocationMarker;
    private BitmapDescriptor cafeMarkerIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Khởi tạo FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Khởi tạo danh sách quán cà phê
        cafeList = new ArrayList<>();

        // Khởi tạo adapter cho InfoWindow
        cafeAdapter = new SearchCafeAdapter(requireContext(), null);

        // Resize icon marker
        cafeMarkerIcon = resizeMarkerIcon(R.drawable.ic_cafe_marker1, 75, 75);

        // Thiết lập ô tìm kiếm
        EditText etSearchCafe = view.findViewById(R.id.et_search_cafe);
        etSearchCafe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim().toLowerCase();
                showNearbyCafes();
            }
        });

        // Thiết lập bộ lọc
        Spinner spinnerDistance = view.findViewById(R.id.spinner_distance);
        Spinner spinnerRating = view.findViewById(R.id.spinner_rating);
        Spinner spinnerActivity = view.findViewById(R.id.spinner_activity);

        if (spinnerDistance == null || spinnerRating == null || spinnerActivity == null) {
            Log.e(TAG, "One or more spinners are null");
            Toast.makeText(requireContext(), "Lỗi giao diện: Không tìm thấy bộ lọc!", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Đặt giá trị mặc định cho Spinner khoảng cách là "Dưới 20km" (position 3)
        spinnerDistance.setSelection(3);

        // Kiểm tra Bundle từ HomeFragment
        Bundle args = getArguments();
        if (args != null) {
            maxDistance = args.getFloat("maxDistance", 20000); // Lấy maxDistance, mặc định 20km
            boolean fromExplore = args.getBoolean("fromExplore", false);
            if (fromExplore) {
                // Đã đặt spinnerDistance ở trên, không cần đặt lại
                spinnerDistance.setSelection(3); // "Dưới 20km" ở vị trí 3
            }
        }

        // Thiết lập bản đồ lớn
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_large);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }

                mMap.setInfoWindowAdapter(cafeAdapter);

                mMap.setOnMarkerClickListener(marker -> {
                    if (marker.equals(currentLocationMarker)) {
                        marker.setTitle("Vị trí hiện tại");
                        marker.showInfoWindow(); // Bấm vào icon Quán hiển thị thông tin quán, thông tin hiển thị trong SearchAdapter
                    } else {
                        Cafe cafe = (Cafe) marker.getTag();
                        if (cafe != null) {
                            cafeAdapter.setCafe(cafe);
                            marker.showInfoWindow();
                        }
                    }
                    return false;
                });

                mMap.setOnInfoWindowClickListener(marker -> {
                    if (!marker.equals(currentLocationMarker)) {
                        Cafe cafe = (Cafe) marker.getTag();
                        if (cafe != null) {
                            Intent intent = new Intent(requireContext(), ReviewActivity.class);
                            intent.putExtra("cafeId", cafe.getId());
                            intent.putExtra("sourceFragment", "SearchFragment");
                            startActivity(intent);
                        }
                    }
                });

                getCurrentLocation();
            });
        } else {
            Log.e(TAG, "SupportMapFragment is null");
            Toast.makeText(requireContext(), "Không thể khởi tạo bản đồ!", Toast.LENGTH_SHORT).show();
        }

        spinnerDistance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: maxDistance = 1000; break;  // Dưới 1 km
                    case 1: maxDistance = 5000; break;  // Dưới 5 km
                    case 2: maxDistance = 10000; break; // Dưới 10 km
                    case 3: maxDistance = 20000; break; // Dưới 20 km
                }
                showNearbyCafes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: minRating = 0; break;   // Tất cả
                    case 1: minRating = 3; break;   // 3 sao trở lên
                    case 2: minRating = 4; break;   // 4 sao trở lên
                    case 3: minRating = 5; break;   // 5 sao
                }
                showNearbyCafes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedActivity = parent.getItemAtPosition(position).toString();
                showNearbyCafes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Tải danh sách quán cà phê từ Firestore
        loadCafesFromFirestore();

        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLocation = location;
                cafeAdapter.setCurrentLocation(currentLocation);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (mMap != null) {
                    mMap.clear();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    currentLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí hiện tại"));
                    showNearbyCafes();
                } else {
                    Log.e(TAG, "GoogleMap is null in getCurrentLocation");
                }
            } else {
                Toast.makeText(requireContext(), "Không thể lấy vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get location: " + e.getMessage());
            Toast.makeText(requireContext(), "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            getCurrentLocation();
//            if (mMap != null) {
//                try {
//                    mMap.setMyLocationEnabled(true);
//                } catch (SecurityException e) {
//                    Log.e(TAG, "SecurityException: " + e.getMessage());
//                }
//            }
//        } else {
//            Toast.makeText(requireContext(), "Bạn cần cấp quyền để sử dụng tính năng này!", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void loadCafesFromFirestore() {
        db.collection("cafes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cafeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Cafe cafe = document.toObject(Cafe.class);
                            cafe.setId(document.getId());
                            cafeList.add(cafe);
                            showNearbyCafes();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cafe: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cafes: " + e.getMessage());
                    Toast.makeText(requireContext(), "Lỗi khi tải danh sách quán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showNearbyCafes() {
        if (currentLocation == null || mMap == null) {
            if (!hasShownLocationNotReady) {
                Log.e(TAG, "Current location or GoogleMap is null");
                Toast.makeText(requireContext(), "Vị trí hiện tại chưa sẵn sàng!", Toast.LENGTH_SHORT).show();
                hasShownLocationNotReady = true;
            }
            return;
        }

        hasShownLocationNotReady = false;

        mMap.clear();
        LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Vị trí hiện tại"));

        for (Cafe cafe : cafeList) {
            if (cafe == null || cafe.getLat() == 0 || cafe.getLng() == 0) {
                Log.e(TAG, "Invalid cafe data: " + (cafe != null ? cafe.getName() : "null"));
                continue;
            }

            if (!searchQuery.isEmpty()) {
                String cafeName = cafe.getName() != null ? cafe.getName().toLowerCase() : "";
                if (!cafeName.contains(searchQuery)) {
                    continue;
                }
            }

            Location cafeLocation = new Location("");
            cafeLocation.setLatitude(cafe.getLat());
            cafeLocation.setLongitude(cafe.getLng());

            float distance = currentLocation.distanceTo(cafeLocation);
            double rating = cafe.getRatingStar() != null ? cafe.getRatingStar() : 0.0;
            String activity = cafe.getActivity() != null ? cafe.getActivity() : "Không có";

            boolean activityMatch;
            if (selectedActivity.equals("Tất cả")) {
                activityMatch = true;
            } else if (selectedActivity.equals("Không có")) {
                activityMatch = cafe.getActivity() == null;
            } else if (selectedActivity.equals("Others")) {
                activityMatch = cafe.getActivity() != null &&
                        !cafe.getActivity().equals("Boardgame") &&
                        !cafe.getActivity().equals("Book") &&
                        !cafe.getActivity().equals("Workshop");
            } else {
                activityMatch = selectedActivity.equals(activity);
            }

            if (distance <= maxDistance && rating >= minRating && activityMatch) {
                LatLng cafeLatLng = new LatLng(cafe.getLat(), cafe.getLng());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(cafeLatLng)
                        .title(cafe.getName())
                        .icon(cafeMarkerIcon));
                if (marker != null) {
                    marker.setTag(cafe);
                }
            }
        }
    }

    private BitmapDescriptor resizeMarkerIcon(int resourceId, int width, int height) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        if (originalBitmap == null) {
            Log.e(TAG, "Failed to load marker icon from resource ID: " + resourceId);
            return BitmapDescriptorFactory.defaultMarker();
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }
}