package com.example.myapplication.Adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Model.Cafe;
import com.example.myapplication.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

public class SearchCafeAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;
    private Cafe cafe;
    private Location currentLocation;
    private LayoutInflater inflater;

    public SearchCafeAdapter(Context context, Location currentLocation) {
        this.context = context;
        this.currentLocation = currentLocation;
        this.inflater = LayoutInflater.from(context);
    }

    public void setCafe(Cafe cafe) {
        this.cafe = cafe;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Nếu marker là vị trí hiện tại, không hiển thị thông tin quán cà phê
        if ("Vị trí hiện tại".equals(marker.getTitle())) {
            return null; // Google Maps sẽ hiển thị tiêu đề mặc định "Vị trí hiện tại"
        }

        if (cafe == null) return null;

        View view = inflater.inflate(R.layout.item_cafe, null);

        ImageView ivCafeImage = view.findViewById(R.id.ivCafeImage);
        TextView tvCafeName = view.findViewById(R.id.tvCafeName);
        TextView tvAddress = view.findViewById(R.id.tvAddress);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvRating = view.findViewById(R.id.tvRating);
        TextView tvActivity = view.findViewById(R.id.tvActivity);

        // Hiển thị thông tin quán
        tvCafeName.setText(cafe.getName() != null ? cafe.getName() : "Tên quán");
        tvAddress.setText("Địa chỉ: " + (cafe.getLocationText() != null ? cafe.getLocationText() : "Không có địa chỉ"));
        tvDescription.setText("Mô tả: " + (cafe.getDescription() != null ? cafe.getDescription() : "Không có mô tả"));
        tvRating.setText("Đánh giá: " + (cafe.getRatingStar() != null ? String.format("%.1f", cafe.getRatingStar()) : "0.0") + "/5");
        tvActivity.setText("Hoạt động: " + (cafe.getActivity() != null ? cafe.getActivity() : "Không có"));

        // Hiển thị hình ảnh
        if (cafe.getImage1() != null && !cafe.getImage1().isEmpty()) {
            Picasso.get().load(cafe.getImage1()).into(ivCafeImage);
        } else {
            ivCafeImage.setImageResource(R.drawable.ic_placeholder);
        }

        return view;
    }
}