package com.example.myapp.presentation.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.UserStats;
import com.example.myapp.presentation.viewmodels.UserStatsViewModelFactory;
import com.example.myapp.presentation.viewmodels.UserStatsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserStatsFragment extends Fragment {

    private UserStatsViewModel viewModel;
    private String userUid;

    // Views
    private TextView tvActiveDays, tvLongestStreak;
    private TextView tvTotalCreated, tvTotalDone, tvTotalUndone, tvTotalCancelled;
    private TextView tvSpecialStarted, tvSpecialCompleted;
    private PieChart pieTaskStatus;
    private BarChart barCategory;
    private LineChart lineXp, lineDifficulty;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        UserStatsViewModelFactory factory = new UserStatsViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(UserStatsViewModel.class);

        initViews(view);
        setupObservers();
        viewModel.loadStats(userUid);
    }

    private void initViews(View v) {
        tvActiveDays       = v.findViewById(R.id.tvActiveDays);
        tvLongestStreak    = v.findViewById(R.id.tvLongestStreak);
        tvTotalCreated     = v.findViewById(R.id.tvTotalCreated);
        tvTotalDone        = v.findViewById(R.id.tvTotalDone);
        tvTotalUndone      = v.findViewById(R.id.tvTotalUndone);
        tvTotalCancelled   = v.findViewById(R.id.tvTotalCancelled);
        tvSpecialStarted   = v.findViewById(R.id.tvSpecialStarted);
        tvSpecialCompleted = v.findViewById(R.id.tvSpecialCompleted);
        pieTaskStatus      = v.findViewById(R.id.pieTaskStatus);
        barCategory        = v.findViewById(R.id.barCategory);
        lineXp             = v.findViewById(R.id.lineXp);
        lineDifficulty     = v.findViewById(R.id.lineDifficulty);
    }

    private void setupObservers() {
        viewModel.stats.observe(getViewLifecycleOwner(), this::bindStats);
    }

    private void bindStats(UserStats stats) {
        if (stats == null) return;

        // ─── Streak ───
        tvActiveDays.setText("Aktivnih dana: " + stats.getActiveDaysStreak());
        tvLongestStreak.setText("Najduži niz: " + stats.getLongestStreak() + " dana");

        // ─── Ukupno ───
        tvTotalCreated.setText("Kreirani: "   + stats.getTotalCreated());
        tvTotalDone.setText("Urađeni: "        + stats.getTotalDone());
        tvTotalUndone.setText("Neurađeni: "    + stats.getTotalUndone());
        tvTotalCancelled.setText("Otkazani: "  + stats.getTotalCancelled());

        // ─── Specijalne misije ───
        tvSpecialStarted.setText("Započete misije: "   + stats.getSpecialMissionsStarted());
        tvSpecialCompleted.setText("Završene misije: " + stats.getSpecialMissionsCompleted());

        // ─── Grafikoni ───
        setupPieChart(stats);
        setupBarChart(stats);
        setupLineXpChart(stats);
        setupLineDifficultyChart(stats);
    }

    // ─── Donut grafikon — status zadataka ───
    private void setupPieChart(UserStats stats) {
        List<PieEntry> entries = new ArrayList<>();
        if (stats.getTotalDone()      > 0) entries.add(new PieEntry(stats.getTotalDone(),      "Urađeni"));
        if (stats.getTotalUndone()    > 0) entries.add(new PieEntry(stats.getTotalUndone(),    "Neurađeni"));
        if (stats.getTotalCancelled() > 0) entries.add(new PieEntry(stats.getTotalCancelled(), "Otkazani"));

        if (entries.isEmpty()) {
            pieTaskStatus.setNoDataText("Nema podataka");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#9E9E9E"),
                Color.parseColor("#FF5722")
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieTaskStatus.setData(data);
        pieTaskStatus.setHoleRadius(45f);       // donut efekat
        pieTaskStatus.setTransparentCircleRadius(50f);
        pieTaskStatus.getDescription().setEnabled(false);
        pieTaskStatus.setEntryLabelColor(Color.BLACK);
        pieTaskStatus.animateY(800);
        pieTaskStatus.invalidate();
    }

    // ─── Bar grafikon — zadaci po kategoriji ───
    private void setupBarChart(UserStats stats) {
        if (stats.getDonePerCategory() == null || stats.getDonePerCategory().isEmpty()) {
            barCategory.setNoDataText("Nema podataka");
            return;
        }

        List<BarEntry> entries  = new ArrayList<>();
        List<String> labels     = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : stats.getDonePerCategory().entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            String name = stats.getCategoryNames() != null
                    ? stats.getCategoryNames().getOrDefault(entry.getKey(), entry.getKey())
                    : entry.getKey();
            labels.add(name);
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Zadaci po kategoriji");
        dataSet.setColors(
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#F44336")
        );
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barCategory.setData(data);
        barCategory.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barCategory.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barCategory.getXAxis().setGranularity(1f);
        barCategory.getAxisRight().setEnabled(false);
        barCategory.getDescription().setEnabled(false);
        barCategory.animateY(800);
        barCategory.invalidate();
    }

    // ─── Line grafikon — XP poslednjih 7 dana ───
    private void setupLineXpChart(UserStats stats) {
        if (stats.getXpLast7Days() == null) return;

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        String[] days = {"Pon", "Uto", "Sre", "Čet", "Pet", "Sub", "Ned"};

        for (int i = 0; i < stats.getXpLast7Days().size(); i++) {
            entries.add(new Entry(i, stats.getXpLast7Days().get(i)));
            labels.add(days[i % 7]);
        }

        LineDataSet dataSet = new LineDataSet(entries, "XP poslednjih 7 dana");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#2196F3"));
        dataSet.setFillAlpha(50);

        LineData data = new LineData(dataSet);
        lineXp.setData(data);
        lineXp.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineXp.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineXp.getXAxis().setGranularity(1f);
        lineXp.getAxisRight().setEnabled(false);
        lineXp.getDescription().setEnabled(false);
        lineXp.animateX(800);
        lineXp.invalidate();
    }

    // ─── Line grafikon — prosečna težina ───
    private void setupLineDifficultyChart(UserStats stats) {
        if (stats.getAvgDifficultyLast7Days() == null) return;

        List<Entry> entries = new ArrayList<>();
        String[] days = {"Pon", "Uto", "Sre", "Čet", "Pet", "Sub", "Ned"};
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < stats.getAvgDifficultyLast7Days().size(); i++) {
            entries.add(new Entry(i, stats.getAvgDifficultyLast7Days().get(i)));
            labels.add(days[i % 7]);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Prosečna težina");
        dataSet.setColor(Color.parseColor("#FF9800"));
        dataSet.setCircleColor(Color.parseColor("#FF9800"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#FF9800"));
        dataSet.setFillAlpha(50);

        LineData data = new LineData(dataSet);
        lineDifficulty.setData(data);
        lineDifficulty.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineDifficulty.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineDifficulty.getXAxis().setGranularity(1f);
        lineDifficulty.getAxisRight().setEnabled(false);
        lineDifficulty.getDescription().setEnabled(false);
        lineDifficulty.animateX(800);
        lineDifficulty.invalidate();
    }
}