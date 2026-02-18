package com.example.myapp.presentation.adapters;

import android.graphics.Color;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.domain.models.Task;

import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskClickListener {
        void onTaskClick(Task task);
        void onMarkDone(Task task);
        void onMarkCancelled(Task task);
        void onMarkPaused(Task task);
        void onMarkActive(Task task);
    }

    private List<Task> tasks;
    private final TaskClickListener listener;
    // mapa categoryId -> hex boja za indikator
    private Map<String, String> categoryColors;

    public TaskAdapter(List<Task> tasks, TaskClickListener listener) {
        this.tasks    = tasks;
        this.listener = listener;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public void setCategoryColors(Map<String, String> colors) {
        this.categoryColors = colors;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position), listener, categoryColors);
    }

    @Override public int getItemCount() { return tasks.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        View viewCategoryColor;
        TextView tvTaskTitle, tvTaskStatus, tvTaskXp, tvTaskTime;
        ImageButton btnTaskDone, btnTaskCancel, btnTaskPause, btnTaskActivate;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryColor = itemView.findViewById(R.id.viewTaskCategoryColor);
            tvTaskTitle       = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskStatus      = itemView.findViewById(R.id.tvTaskStatus);
            tvTaskXp          = itemView.findViewById(R.id.tvTaskXp);
            tvTaskTime        = itemView.findViewById(R.id.tvTaskTime);
            btnTaskDone       = itemView.findViewById(R.id.btnTaskDone);
            btnTaskCancel     = itemView.findViewById(R.id.btnTaskCancel);
            btnTaskPause      = itemView.findViewById(R.id.btnTaskPause);
            btnTaskActivate   = itemView.findViewById(R.id.btnTaskActivate);
        }

        void bind(Task task, TaskClickListener listener, Map<String, String> categoryColors) {
            tvTaskTitle.setText(task.getTitle());
            tvTaskStatus.setText(statusLabel(task.getStatus()));
            tvTaskXp.setText(task.getXpValue() + " XP");
            tvTaskTime.setText(android.text.format.DateFormat.format(
                    "dd.MM.yyyy HH:mm", task.getScheduledTime()));

            // Boja kategorije
            if (categoryColors != null && task.getCategoryId() != null
                    && categoryColors.containsKey(task.getCategoryId())) {
                try {
                    viewCategoryColor.setBackgroundColor(
                            Color.parseColor(categoryColors.get(task.getCategoryId())));
                } catch (Exception e) {
                    viewCategoryColor.setBackgroundColor(Color.GRAY);
                }
            } else {
                viewCategoryColor.setBackgroundColor(Color.LTGRAY);
            }

            // Status boja
            tvTaskStatus.setTextColor(statusColor(task.getStatus()));

            // Dugmad vidljivost
            boolean canMark    = task.canBeMarked();
            boolean isPaused   = Task.STATUS_PAUSED.equals(task.getStatus());
            boolean isRepeating = task.isRepeating();

            btnTaskDone.setVisibility(canMark ? View.VISIBLE : View.GONE);
            btnTaskCancel.setVisibility(canMark ? View.VISIBLE : View.GONE);
            btnTaskPause.setVisibility(canMark && isRepeating ? View.VISIBLE : View.GONE);
            btnTaskActivate.setVisibility(isPaused ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            btnTaskDone.setOnClickListener(v -> listener.onMarkDone(task));
            btnTaskCancel.setOnClickListener(v -> listener.onMarkCancelled(task));
            btnTaskPause.setOnClickListener(v -> listener.onMarkPaused(task));
            btnTaskActivate.setOnClickListener(v -> listener.onMarkActive(task));
        }

        private String statusLabel(String status) {
            switch (status) {
                case Task.STATUS_DONE:      return "Urađeno";
                case Task.STATUS_UNDONE:    return "Neurađeno";
                case Task.STATUS_CANCELLED: return "Otkazano";
                case Task.STATUS_PAUSED:    return "Pauzirano";
                default:                    return "Aktivno";
            }
        }

        private int statusColor(String status) {
            switch (status) {
                case Task.STATUS_DONE:      return Color.parseColor("#4CAF50");
                case Task.STATUS_CANCELLED: return Color.parseColor("#FF5722");
                case Task.STATUS_PAUSED:    return Color.parseColor("#FFC107");
                case Task.STATUS_UNDONE:    return Color.parseColor("#9E9E9E");
                default:                    return Color.parseColor("#2196F3");
            }
        }
    }
}