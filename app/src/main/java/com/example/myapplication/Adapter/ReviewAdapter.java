package com.example.myapplication.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Model.Review;
import com.example.myapplication.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList = new ArrayList<>();

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvUsername.setText("Người dùng: " + (review.getUsername() != null ? review.getUsername() : "Ẩn danh"));
        holder.tvRating.setText(review.getRating() + "/5");
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "Không có bình luận");

        // Sửa logic hiển thị hoạt động
        String activity = review.getActivity();
        String otherActivity = review.getOtherActivityDescription();
        Log.d("ReviewAdapter", "Activity: " + activity + ", OtherActivityDescription: " + otherActivity); // Thêm log

        if (activity != null && activity.equalsIgnoreCase("others") && otherActivity != null && !otherActivity.isEmpty()) {
            holder.tvActivity.setText(otherActivity);
        } else {
            holder.tvActivity.setText(activity != null ? activity : "Không có hoạt động");
        }

//        // Hiển thị mô tả khác nếu có
//        if (otherActivity != null && !otherActivity.isEmpty()) {
//            holder.tvOtherActivity.setVisibility(View.VISIBLE);
//        } else {
//            holder.tvOtherActivity.setVisibility(View.GONE);
//        }

        // Hiển thị thời gian
        if (review.getTimestamp() != null) {
            Date date = review.getTimestamp().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText("Thời gian: " + sdf.format(date));
        } else {
            holder.tvTimestamp.setText("Thời gian: Không có");
        }

        // Hiển thị hình ảnh
        ImageAdapter imageAdapter = new ImageAdapter(review.getImages());
        holder.rvImages.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.rvImages.setAdapter(imageAdapter);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void setReviewList(List<Review> reviews) {
        this.reviewList = reviews;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRating, tvComment, tvActivity, tvOtherActivity, tvTimestamp;
        RecyclerView rvImages;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvComment = itemView.findViewById(R.id.tv_comment);
            tvActivity = itemView.findViewById(R.id.tv_activity);
            //tvOtherActivity = itemView.findViewById(R.id.tv_other_activity);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            rvImages = itemView.findViewById(R.id.rv_images);
        }
    }
}