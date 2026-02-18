package com.example.myapp.presentation.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;
import com.example.myapp.presentation.adapters.TaskAdapter;
import com.example.myapp.presentation.viewmodels.TaskViewModel;
import com.example.myapp.presentation.viewmodels.TaskViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskListFragment extends Fragment implements TaskAdapter.TaskClickListener {

    private TaskViewModel viewModel;
    private TaskAdapter adapter;
    private String userUid;

    private RecyclerView rvTasks;
    private TabLayout tabTaskType;
    private FloatingActionButton fabAddTask;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TaskViewModelFactory factory = new TaskViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);

        rvTasks     = view.findViewById(R.id.rvTasks);
        tabTaskType = view.findViewById(R.id.tabTaskType);
        fabAddTask  = view.findViewById(R.id.fabAddTask);

        adapter = new TaskAdapter(new ArrayList<>(), this);
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);

        tabTaskType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { filterAndShow(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewModel.tasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) filterAndShow(tabTaskType.getSelectedTabPosition());
        });

        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Map<String, String> colorMap = new HashMap<>();
                for (Category c : categories) colorMap.put(c.getId(), c.getColor());
                adapter.setCategoryColors(colorMap);
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
                            "Ne možete označiti budući zadatak", Toast.LENGTH_SHORT).show();
                    break;
                case "TASK_EXPIRED":
                    Toast.makeText(requireContext(),
                            "Zadatak je istekao i označen kao neurađen", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        fabAddTask.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new CreateTaskFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewModel.loadTaskList(userUid);
        viewModel.loadCategories(userUid);
    }

    private void filterAndShow(int tabPos) {
        List<Task> all = viewModel.tasks.getValue();
        if (all == null) return;
        List<Task> filtered;
        if (tabPos == 0) {
            filtered = all.stream()
                    .filter(t -> Task.REPEAT_NONE.equals(t.getRepeatType()))
                    .collect(Collectors.toList());
        } else {
            filtered = all.stream()
                    .filter(t -> !Task.REPEAT_NONE.equals(t.getRepeatType()))
                    .collect(Collectors.toList());
        }
        adapter.updateTasks(filtered);
    }

    @Override public void onTaskClick(Task task) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, TaskDetailFragment.newInstance(task.getId()))
                .addToBackStack(null)
                .commit();
    }

    @Override public void onMarkDone(Task task)      { viewModel.markDone(task, userUid); }
    @Override public void onMarkCancelled(Task task) { viewModel.markCancelled(task, userUid); }
    @Override public void onMarkPaused(Task task)    { viewModel.markPaused(task, userUid); }
    @Override public void onMarkActive(Task task)    { viewModel.markActive(task, userUid); }
}