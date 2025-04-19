package com.example.myapplication.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.Model.DailyTask;
import com.example.myapplication.R;
import java.util.List;

public class DailyTaskAdapter extends RecyclerView.Adapter<DailyTaskAdapter.TaskViewHolder> {

    private List<DailyTask> taskList;

    public DailyTaskAdapter(List<DailyTask> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        DailyTask task = taskList.get(position);
        holder.tvTaskName.setText(task.getName());
        holder.tvTaskPoints.setText(String.format("+%d điểm", task.getPoints()));

        if (task.isCompleted()) {
            holder.ivTaskStatus.setImageResource(R.drawable.ic_task_completed);
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.ivTaskStatus.setImageResource(R.drawable.ic_task_pending);
            holder.itemView.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTaskStatus;
        TextView tvTaskName, tvTaskPoints;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTaskStatus = itemView.findViewById(R.id.iv_task_status);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            tvTaskPoints = itemView.findViewById(R.id.tv_task_points);
        }
    }
}