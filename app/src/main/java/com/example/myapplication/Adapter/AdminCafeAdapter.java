package com.example.myapplication.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.CafeAdmin;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminCafeAdapter extends RecyclerView.Adapter<AdminCafeAdapter.CafeViewHolder> {

    private Context context;
    private List<CafeAdmin> cafeList;
    private OnCafeActionListener listener;

    public AdminCafeAdapter(Context context, List<CafeAdmin> cafeList, OnCafeActionListener listener) {
        this.context = context;
        this.cafeList = cafeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CafeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cafe_admin, parent, false);
        return new CafeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CafeViewHolder holder, int position) {
        CafeAdmin cafe = cafeList.get(position);

        // Ánh xạ dữ liệu
        holder.tvCafeName.setText(cafe.getName() != null ? cafe.getName() : "Tên Quán");
        holder.tvAddress.setText(cafe.getLocationText() != null ? "Địa chỉ: " + cafe.getLocationText() : "Địa chỉ: Không có địa chỉ");
        holder.tvDescription.setText(cafe.getDescription() != null ? "Mô tả: " + cafe.getDescription() : "Mô tả: Không có mô tả");
        holder.tvActivity.setText(cafe.getActivity() != null ? "Hoạt động: " + cafe.getActivity() : "Hoạt động: Không có");

        // Tải hình ảnh
        if (cafe.getImage1() != null && !cafe.getImage1().isEmpty()) {
            Picasso.get()
                    .load(cafe.getImage1())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.ivCafeImage);
        } else {
            holder.ivCafeImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Gán sự kiện với kiểm tra null
        if (holder.btnEdit != null) {
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(cafe);
                }
            });
        } else {
            Log.e("AdminCafeAdapter", "btnEdit is null at position " + position);
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(cafe);
                }
            });
        } else {
            Log.e("AdminCafeAdapter", "btnDelete is null at position " + position);
        }
    }

    @Override
    public int getItemCount() {
        return cafeList.size();
    }

    static class CafeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCafeImage;
        TextView tvCafeName, tvAddress, tvDescription, tvActivity;
        Button btnEdit, btnDelete;

        CafeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCafeImage = itemView.findViewById(R.id.ivCafeImage);
            tvCafeName = itemView.findViewById(R.id.tvCafeName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvActivity = itemView.findViewById(R.id.tvActivity);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface OnCafeActionListener {
        void onEditClick(CafeAdmin cafe);
        void onDeleteClick(CafeAdmin cafe);
    }
}