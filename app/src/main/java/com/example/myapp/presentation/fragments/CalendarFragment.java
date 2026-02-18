package com.example.myapp.presentation.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;
import com.example.myapp.presentation.activities.MainActivity;
import com.example.myapp.presentation.adapters.TaskAdapter;
import com.example.myapp.presentation.viewmodels.TaskViewModel;
import com.example.myapp.presentation.viewmodels.TaskViewModelFactory;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment implements TaskAdapter.TaskClickListener {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private String userUid;

    private MaterialCalendarView calendarView;
    private RecyclerView rvDayTasks;
    private TextView tvSelectedDate, tvNoTasks;

    private Map<String, String> categoryColors = new HashMap<>();
    // dan -> lista taskova tog dana
    private Map<String, List<Task>> tasksByDay = new HashMap<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TaskViewModelFactory factory = new TaskViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);

        initViews(view);
        setupRecycler();
        setupCalendar();
        setupObservers();

        viewModel.loadAllTasks(userUid);
        viewModel.loadCategories(userUid);
    }

    private void initViews(View v) {
        calendarView    = v.findViewById(R.id.calendarView);
        rvDayTasks      = v.findViewById(R.id.rvDayTasks);
        tvSelectedDate  = v.findViewById(R.id.tvSelectedDate);
        tvNoTasks       = v.findViewById(R.id.tvNoTasks);
    }

    private void setupRecycler() {
        adapter = new TaskAdapter(new ArrayList<>(), this);
        rvDayTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDayTasks.setAdapter(adapter);
    }

    private void setupCalendar() {
        // Postavi danas kao selektovani dan
        CalendarDay today = CalendarDay.today();
        calendarView.setSelectedDate(today);
        showTasksForDay(today);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            showTasksForDay(date);
        });
    }

    private void setupObservers() {
        viewModel.tasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks == null) return;
            buildTasksByDay(tasks);
            refreshCalendarDecorators(tasks);
            // Prikaži taskove za trenutno selektovani dan
            showTasksForDay(calendarView.getSelectedDate());
        });

        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories == null) return;
            categoryColors.clear();
            for (Category c : categories) {
                categoryColors.put(c.getId(), c.getColor());
            }
            adapter.setCategoryColors(categoryColors);
            // Refresh decoratora sa bojama
            List<Task> allTasks = viewModel.tasks.getValue();
            if (allTasks != null) refreshCalendarDecorators(allTasks);
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                // Reload nakon akcije
                viewModel.loadAllTasks(userUid);
            }
        });

        viewModel.xpEarned.observe(getViewLifecycleOwner(), xp -> {
            if (xp != null && xp > 0)
                Toast.makeText(requireContext(), "+" + xp + " XP!", Toast.LENGTH_SHORT).show();
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) return;
            switch (error) {
                case "TASK_IN_FUTURE":
                    Toast.makeText(requireContext(),
                            "Ne možete označiti budući zadatak",
                            Toast.LENGTH_SHORT).show();
                    break;
                case "TASK_EXPIRED":
                    Toast.makeText(requireContext(),
                            "Zadatak je istekao i označen kao neurađen",
                            Toast.LENGTH_SHORT).show();
                    viewModel.loadAllTasks(userUid);
                    break;
            }
        });
    }

    // ─── Grupiši taskove po danu ───
    private void buildTasksByDay(List<Task> tasks) {
        tasksByDay.clear();
        for (Task task : tasks) {
            String dayKey = getDayKey(task.getScheduledTime());
            if (!tasksByDay.containsKey(dayKey)) {
                tasksByDay.put(dayKey, new ArrayList<>());
            }
            tasksByDay.get(dayKey).add(task);
        }
    }

    // ─── Prikaži taskove za odabrani dan ───
    private void showTasksForDay(CalendarDay day) {
        if (day == null) return;

        String dateStr = day.getYear() + "-" + day.getMonth() + "-" + day.getDay();
        tvSelectedDate.setText(day.getDay() + "." + day.getMonth()
                + "." + day.getYear() + ".");

        List<Task> dayTasks = tasksByDay.get(dateStr);
        if (dayTasks == null || dayTasks.isEmpty()) {
            tvNoTasks.setVisibility(View.VISIBLE);
            rvDayTasks.setVisibility(View.GONE);
            adapter.updateTasks(new ArrayList<>());
        } else {
            tvNoTasks.setVisibility(View.GONE);
            rvDayTasks.setVisibility(View.VISIBLE);
            adapter.updateTasks(dayTasks);
        }
    }

    // ─── Dekoratori — tačke za dane koji imaju taskove ───
    private void refreshCalendarDecorators(List<Task> tasks) {
        calendarView.removeDecorators();

        // Grupiši dane po boji kategorije
        Map<String, Set<CalendarDay>> colorToDays = new HashMap<>();

        for (Task task : tasks) {
            String color = "#BBBBBB"; // default ako nema kategorije
            if (task.getCategoryId() != null && categoryColors.containsKey(task.getCategoryId())) {
                color = categoryColors.get(task.getCategoryId());
            }

            CalendarDay day = taskToCalendarDay(task.getScheduledTime());
            if (!colorToDays.containsKey(color)) {
                colorToDays.put(color, new HashSet<>());
            }
            colorToDays.get(color).add(day);
        }

        // Dodaj dekorator za svaku boju
        for (Map.Entry<String, Set<CalendarDay>> entry : colorToDays.entrySet()) {
            try {
                int colorInt = Color.parseColor(entry.getKey());
                calendarView.addDecorator(
                        new TaskDotDecorator(colorInt, entry.getValue()));
            } catch (Exception ignored) {}
        }
    }

    private CalendarDay taskToCalendarDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return CalendarDay.from(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private String getDayKey(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(Calendar.YEAR) + "-"
                + (cal.get(Calendar.MONTH) + 1) + "-"
                + cal.get(Calendar.DAY_OF_MONTH);
    }

    // ─── TaskAdapter.TaskClickListener ───

    @Override
    public void onTaskClick(Task task) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        TaskDetailFragment.newInstance(task.getId()))
                .addToBackStack(null)
                .commit();
    }

    @Override public void onMarkDone(Task task)      { viewModel.markDone(task, userUid); }
    @Override public void onMarkCancelled(Task task) { viewModel.markCancelled(task, userUid); }
    @Override public void onMarkPaused(Task task)    { viewModel.markPaused(task, userUid); }
    @Override public void onMarkActive(Task task)    { viewModel.markActive(task, userUid); }

    // ─── Dekorator klasa ───
    private static class TaskDotDecorator implements DayViewDecorator {

        private final int color;
        private final Set<CalendarDay> days;

        TaskDotDecorator(int color, Set<CalendarDay> days) {
            this.color = color;
            this.days  = days;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return days.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(8, color));
        }
    }
}