package com.example.myapp.presentation.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.domain.models.Task;
import com.example.myapp.presentation.activities.MainActivity;
import com.example.myapp.presentation.viewmodels.TaskViewModel;
import com.example.myapp.presentation.viewmodels.TaskViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

public class TaskDetailFragment extends Fragment {

    private static final String ARG_TASK_ID = "task_id";

    private TaskViewModel viewModel;
    private Task currentTask;
    private String userUid;

    private TextView tvTitle, tvDescription, tvStatus, tvDifficulty,
            tvImportance, tvXp, tvScheduledTime, tvRepeatInfo;
    private Button btnDone, btnCancel, btnPause, btnActivate, btnEdit, btnDelete;

    public static TaskDetailFragment newInstance(String taskId) {
        TaskDetailFragment f = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, taskId);
        f.setArguments(args);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TaskViewModelFactory factory = new TaskViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(TaskViewModel.class);

        initViews(view);

        String taskId = getArguments() != null ? getArguments().getString(ARG_TASK_ID) : null;
        if (taskId != null) {
            viewModel.loadTaskById(taskId);
        }

        viewModel.selectedTask.observe(getViewLifecycleOwner(), task -> {
            if (task != null) {
                currentTask = task;
                bindTask();
            }
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
                case "TASK_IN_FUTURE":
                    Toast.makeText(requireContext(),
                            "Ne možete označiti budući zadatak", Toast.LENGTH_SHORT).show();
                    break;
                case "TASK_EXPIRED":
                    Toast.makeText(requireContext(),
                            "Zadatak je istekao", Toast.LENGTH_SHORT).show();
                    break;
                case "TASK_NOT_DELETABLE":
                    Toast.makeText(requireContext(),
                            "Završeni zadaci se ne mogu brisati", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void initViews(View v) {
        tvTitle         = v.findViewById(R.id.tvDetailTitle);
        tvDescription   = v.findViewById(R.id.tvDetailDescription);
        tvStatus        = v.findViewById(R.id.tvDetailStatus);
        tvDifficulty    = v.findViewById(R.id.tvDetailDifficulty);
        tvImportance    = v.findViewById(R.id.tvDetailImportance);
        tvXp            = v.findViewById(R.id.tvDetailXp);
        tvScheduledTime = v.findViewById(R.id.tvDetailScheduledTime);
        tvRepeatInfo    = v.findViewById(R.id.tvDetailRepeatInfo);
        btnDone         = v.findViewById(R.id.btnDetailDone);
        btnCancel       = v.findViewById(R.id.btnDetailCancel);
        btnPause        = v.findViewById(R.id.btnDetailPause);
        btnActivate     = v.findViewById(R.id.btnDetailActivate);
        btnEdit         = v.findViewById(R.id.btnDetailEdit);
        btnDelete       = v.findViewById(R.id.btnDetailDelete);
    }

    private void bindTask() {
        tvTitle.setText(currentTask.getTitle());
        tvDescription.setText(currentTask.getDescription() != null
                ? currentTask.getDescription() : "Nema opisa");
        tvStatus.setText("Status: " + statusLabel(currentTask.getStatus()));
        tvDifficulty.setText("Težina: " + difficultyLabel(currentTask.getDifficulty()));
        tvImportance.setText("Bitnost: " + importanceLabel(currentTask.getImportance()));
        tvXp.setText("XP vrednost: " + currentTask.getXpValue());
        tvScheduledTime.setText("Vreme: " + android.text.format.DateFormat.format(
                "dd.MM.yyyy HH:mm", currentTask.getScheduledTime()));

        if (currentTask.isRepeating()) {
            tvRepeatInfo.setVisibility(View.VISIBLE);
            tvRepeatInfo.setText("Ponavljanje: svakih " + currentTask.getRepeatInterval()
                    + " " + (Task.REPEAT_WEEKLY.equals(currentTask.getRepeatType())
                    ? "nedelja" : "dana"));
        } else {
            tvRepeatInfo.setVisibility(View.GONE);
        }

        boolean canMark   = currentTask.canBeMarked();
        boolean isPaused  = Task.STATUS_PAUSED.equals(currentTask.getStatus());

        btnDone.setVisibility(canMark ? View.VISIBLE : View.GONE);
        btnCancel.setVisibility(canMark ? View.VISIBLE : View.GONE);
        btnPause.setVisibility(canMark && currentTask.isRepeating() ? View.VISIBLE : View.GONE);
        btnActivate.setVisibility(isPaused ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(currentTask.isEditable() ? View.VISIBLE : View.GONE);
        btnDelete.setVisibility(currentTask.isDeletable() ? View.VISIBLE : View.GONE);

        btnDone.setOnClickListener(v -> viewModel.markDone(currentTask, userUid));
        btnCancel.setOnClickListener(v -> viewModel.markCancelled(currentTask, userUid));
        btnPause.setOnClickListener(v -> viewModel.markPaused(currentTask, userUid));
        btnActivate.setOnClickListener(v -> viewModel.markActive(currentTask, userUid));
        btnEdit.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer,
                            EditTaskFragment.newInstance(currentTask.getId()))
                    .addToBackStack(null)
                    .commit();
        });
        btnDelete.setOnClickListener(v -> viewModel.deleteTask(currentTask, userUid));
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

    private String difficultyLabel(String d) {
        switch (d) {
            case Task.DIFFICULTY_VERY_EASY: return "Veoma lak";
            case Task.DIFFICULTY_EASY:      return "Lak";
            case Task.DIFFICULTY_HARD:      return "Težak";
            case Task.DIFFICULTY_EXTREME:   return "Ekstremno težak";
            default: return d;
        }
    }

    private String importanceLabel(String i) {
        switch (i) {
            case Task.IMPORTANCE_NORMAL:    return "Normalan";
            case Task.IMPORTANCE_IMPORTANT: return "Važan";
            case Task.IMPORTANCE_EXTREME:   return "Ekstremno važan";
            case Task.IMPORTANCE_SPECIAL:   return "Specijalan";
            default: return i;
        }
    }
}