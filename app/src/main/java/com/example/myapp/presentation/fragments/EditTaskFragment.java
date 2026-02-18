package com.example.myapp.presentation.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.domain.models.Task;
import com.example.myapp.presentation.viewmodels.TaskViewModel;
import com.example.myapp.presentation.viewmodels.TaskViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class EditTaskFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";

    private TaskViewModel viewModel;
    private Task currentTask;
    private String userUid;

    private EditText etTitle, etDescription;
    private Spinner spinnerDifficulty, spinnerImportance;
    private Button btnScheduledTime, btnSave;
    private TextView tvScheduledTime;

    private long scheduledTime = 0;

    public static EditTaskFragment newInstance(String taskId) {
        EditTaskFragment f = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, taskId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TaskViewModelFactory factory = new TaskViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);

        initViews(view);
        setupSpinners();

        String taskId = getArguments() != null ? getArguments().getString(ARG_TASK_ID) : null;
        if (taskId != null) viewModel.loadTaskById(taskId);

        viewModel.selectedTask.observe(getViewLifecycleOwner(), task -> {
            if (task != null && currentTask == null) {
                currentTask = task;
                populateFields();
            }
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });

        btnScheduledTime.setOnClickListener(v -> pickDateTime(time -> {
            scheduledTime = time;
            tvScheduledTime.setText(android.text.format.DateFormat
                    .format("dd.MM.yyyy HH:mm", time));
        }));

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void initViews(View v) {
        etTitle           = v.findViewById(R.id.etEditTaskTitle);
        etDescription     = v.findViewById(R.id.etEditTaskDescription);
        spinnerDifficulty = v.findViewById(R.id.spinnerEditDifficulty);
        spinnerImportance = v.findViewById(R.id.spinnerEditImportance);
        btnScheduledTime  = v.findViewById(R.id.btnEditScheduledTime);
        tvScheduledTime   = v.findViewById(R.id.tvEditScheduledTime);
        btnSave           = v.findViewById(R.id.btnEditSaveTask);
    }

    private void setupSpinners() {
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Veoma lak (1 XP)", "Lak (3 XP)",
                        "Težak (7 XP)", "Ekstremno težak (20 XP)"});
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(diffAdapter);

        ArrayAdapter<String> impAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Normalan (1 XP)", "Važan (3 XP)",
                        "Ekstremno važan (10 XP)", "Specijalan (100 XP)"});
        impAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImportance.setAdapter(impAdapter);
    }

    private void populateFields() {
        etTitle.setText(currentTask.getTitle());
        etDescription.setText(currentTask.getDescription());
        scheduledTime = currentTask.getScheduledTime();
        tvScheduledTime.setText(android.text.format.DateFormat.format(
                "dd.MM.yyyy HH:mm", scheduledTime));
        spinnerDifficulty.setSelection(difficultyIndex(currentTask.getDifficulty()));
        spinnerImportance.setSelection(importanceIndex(currentTask.getImportance()));
    }

    private void saveChanges() {
        if (currentTask == null) return;
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etTitle.setError("Naziv je obavezan");
            return;
        }

        currentTask.setTitle(title);
        currentTask.setDescription(etDescription.getText().toString().trim());
        currentTask.setScheduledTime(scheduledTime);
        currentTask.setDifficulty(getDifficulty(spinnerDifficulty.getSelectedItemPosition()));
        currentTask.setImportance(getImportance(spinnerImportance.getSelectedItemPosition()));

        viewModel.updateTask(currentTask, userUid);
    }

    private void pickDateTime(java.util.function.Consumer<Long> onPicked) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, y, m, d) ->
                new TimePickerDialog(requireContext(), (tp, h, min) -> {
                    cal.set(y, m, d, h, min, 0);
                    onPicked.accept(cal.getTimeInMillis());
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show(),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private int difficultyIndex(String d) {
        switch (d) {
            case Task.DIFFICULTY_EASY:    return 1;
            case Task.DIFFICULTY_HARD:    return 2;
            case Task.DIFFICULTY_EXTREME: return 3;
            default:                      return 0;
        }
    }

    private int importanceIndex(String i) {
        switch (i) {
            case Task.IMPORTANCE_IMPORTANT: return 1;
            case Task.IMPORTANCE_EXTREME:   return 2;
            case Task.IMPORTANCE_SPECIAL:   return 3;
            default:                        return 0;
        }
    }

    private String getDifficulty(int pos) {
        switch (pos) {
            case 1:  return Task.DIFFICULTY_EASY;
            case 2:  return Task.DIFFICULTY_HARD;
            case 3:  return Task.DIFFICULTY_EXTREME;
            default: return Task.DIFFICULTY_VERY_EASY;
        }
    }

    private String getImportance(int pos) {
        switch (pos) {
            case 1:  return Task.IMPORTANCE_IMPORTANT;
            case 2:  return Task.IMPORTANCE_EXTREME;
            case 3:  return Task.IMPORTANCE_SPECIAL;
            default: return Task.IMPORTANCE_NORMAL;
        }
    }
}