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
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;
import com.example.myapp.presentation.viewmodels.TaskViewModel;
import com.example.myapp.presentation.viewmodels.TaskViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CreateTaskFragment extends Fragment {

    private TaskViewModel viewModel;
    private String userUid;

    private EditText etTitle, etDescription, etRepeatInterval;
    private Spinner spinnerDifficulty, spinnerImportance, spinnerCategory;
    private RadioGroup rgRepeat, rgRepeatUnit;
    private LinearLayout layoutRepeatOptions;
    private Button btnScheduledTime, btnRepeatStart, btnRepeatEnd, btnSave;
    private TextView tvScheduledTime, tvRepeatStart, tvRepeatEnd;

    private long scheduledTime = 0;
    private long repeatStart   = 0;
    private long repeatEnd     = 0;

    private List<Category> categoryList = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TaskViewModelFactory factory = new TaskViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);

        initViews(view);
        setupSpinners();
        setupRepeatToggle();
        setupTimePickers();
        setupObservers();

        viewModel.loadCategories(userUid);

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void initViews(View v) {
        etTitle           = v.findViewById(R.id.etTaskTitle);
        etDescription     = v.findViewById(R.id.etTaskDescription);
        etRepeatInterval  = v.findViewById(R.id.etRepeatInterval);
        spinnerDifficulty = v.findViewById(R.id.spinnerDifficulty);
        spinnerImportance = v.findViewById(R.id.spinnerImportance);
        spinnerCategory   = v.findViewById(R.id.spinnerCategory);
        rgRepeat          = v.findViewById(R.id.rgRepeat);
        rgRepeatUnit      = v.findViewById(R.id.rgRepeatUnit);
        layoutRepeatOptions = v.findViewById(R.id.layoutRepeatOptions);
        btnScheduledTime  = v.findViewById(R.id.btnScheduledTime);
        btnRepeatStart    = v.findViewById(R.id.btnRepeatStart);
        btnRepeatEnd      = v.findViewById(R.id.btnRepeatEnd);
        tvScheduledTime   = v.findViewById(R.id.tvScheduledTime);
        tvRepeatStart     = v.findViewById(R.id.tvRepeatStart);
        tvRepeatEnd       = v.findViewById(R.id.tvRepeatEnd);
        btnSave           = v.findViewById(R.id.btnSaveTask);
    }

    private void setupSpinners() {
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Veoma lak", "Lak",
                        "Težak", "Ekstremno težak"});
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(diffAdapter);

        ArrayAdapter<String> impAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Normalan", "Važan",
                        "Ekstremno važan", "Specijalan"});
        impAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImportance.setAdapter(impAdapter);
    }

    private void setupRepeatToggle() {
        layoutRepeatOptions.setVisibility(View.GONE);
        rgRepeat.setOnCheckedChangeListener((group, checkedId) -> {
            layoutRepeatOptions.setVisibility(
                    checkedId == R.id.rbOneTime ? View.GONE : View.VISIBLE);
        });
    }

    private void setupTimePickers() {
        btnScheduledTime.setOnClickListener(v -> pickDateTime(time -> {
            scheduledTime = time;
            tvScheduledTime.setText(android.text.format.DateFormat
                    .format("dd.MM.yyyy HH:mm", time));
        }));
        btnRepeatStart.setOnClickListener(v -> pickDateTime(time -> {
            repeatStart = time;
            tvRepeatStart.setText(android.text.format.DateFormat
                    .format("dd.MM.yyyy HH:mm", time));
        }));
        btnRepeatEnd.setOnClickListener(v -> pickDateTime(time -> {
            repeatEnd = time;
            tvRepeatEnd.setText(android.text.format.DateFormat
                    .format("dd.MM.yyyy HH:mm", time));
        }));
    }

    private void pickDateTime(java.util.function.Consumer<Long> onPicked) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (datePicker, y, m, d) ->
                new TimePickerDialog(requireContext(), (timePicker, h, min) -> {
                    cal.set(y, m, d, h, min, 0);
                    onPicked.accept(cal.getTimeInMillis());
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show(),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupObservers() {
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            if (categories == null) return;
            categoryList = categories;
            List<String> names = new ArrayList<>();
            names.add("Bez kategorije");
            for (Category c : categories) names.add(c.getName());
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, names);
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(catAdapter);
        });

        viewModel.successMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) return;
            switch (error) {
                case "TITLE_EMPTY":
                    etTitle.setError("Naziv je obavezan");
                    etTitle.requestFocus();
                    break;
                case "TIME_EMPTY":
                    Toast.makeText(requireContext(),
                            "Odaberite vreme izvršenja", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(requireContext(),
                            "Greška pri kreiranju", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        boolean isRepeating = rgRepeat.getCheckedRadioButtonId() != R.id.rbOneTime;

        String repeatType = Task.REPEAT_NONE;
        int repeatInterval = 1;

        if (isRepeating) {
            repeatType = (rgRepeatUnit.getCheckedRadioButtonId() == R.id.rbWeekly)
                    ? Task.REPEAT_WEEKLY : Task.REPEAT_DAILY;
            try {
                repeatInterval = Integer.parseInt(etRepeatInterval.getText().toString());
                if (repeatInterval < 1) repeatInterval = 1;
            } catch (NumberFormatException e) {
                repeatInterval = 1;
            }
            if (repeatStart == 0) repeatStart = System.currentTimeMillis();
        }

        // Kategorija
        String categoryId = null;
        int catPos = spinnerCategory.getSelectedItemPosition();
        if (catPos > 0 && !categoryList.isEmpty() && catPos - 1 < categoryList.size()) {
            categoryId = categoryList.get(catPos - 1).getId();
        }

        Task task = new Task(
                UUID.randomUUID().toString(),
                userUid,
                title,
                etDescription.getText().toString().trim(),
                categoryId,
                Task.STATUS_ACTIVE,
                getDifficulty(spinnerDifficulty.getSelectedItemPosition()),
                getImportance(spinnerImportance.getSelectedItemPosition()),
                0, // XP se računa u repozitorijumu
                scheduledTime,
                System.currentTimeMillis(),
                repeatType,
                repeatInterval,
                repeatStart,
                repeatEnd,
                null,
                null,
                0L,
                false
        );

        viewModel.createTask(task, userUid);
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