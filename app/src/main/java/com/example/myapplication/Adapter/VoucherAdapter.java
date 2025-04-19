package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Model.Voucher;
import com.example.myapplication.R;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private List<Voucher> voucherList;
    private OnVoucherClickListener listener;
    private boolean isStoreMode; // True: Cửa hàng, False: Kho voucher

    public VoucherAdapter(List<Voucher> voucherList, boolean isStoreMode, OnVoucherClickListener listener) {
        this.voucherList = voucherList;
        this.isStoreMode = isStoreMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.tvVoucherName.setText(voucher.getName());
        holder.tvVoucherDescription.setText(voucher.getDescription());

        if (isStoreMode) {
            holder.tvVoucherCost.setText(String.format("Chi phí: %d điểm", voucher.getPointsCost()));
            holder.btnAction.setText("Đổi");
            holder.btnAction.setOnClickListener(v -> listener.onVoucherRedeem(voucher));
        } else {
            holder.tvVoucherCost.setText(String.format("Số lượng: %d", voucher.getQuantity()));
            holder.btnAction.setVisibility(View.GONE); // Không cần nút "Đổi" trong kho
        }
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvVoucherName, tvVoucherDescription, tvVoucherCost;
        Button btnAction;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVoucherName = itemView.findViewById(R.id.tv_voucher_name);
            tvVoucherDescription = itemView.findViewById(R.id.tv_voucher_description);
            tvVoucherCost = itemView.findViewById(R.id.tv_voucher_cost);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }

    public interface OnVoucherClickListener {
        void onVoucherRedeem(Voucher voucher);
    }
}