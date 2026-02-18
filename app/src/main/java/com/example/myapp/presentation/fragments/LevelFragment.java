package com.example.myapp.presentation.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.data.repositories.UserRepository;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.utils.LevelManager;
import com.example.myapp.presentation.viewmodels.LevelViewModel;
import com.example.myapp.presentation.viewmodels.LevelViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

public class LevelFragment extends Fragment {

    private LevelViewModel viewModel;
    private String userUid;

    private TextView tvTitle, tvLevel, tvXp, tvXpRequired,
            tvXpToNext, tvPowerPoints;
    private ProgressBar progressLevel;
    private LinearLayout layoutLevelUpBanner;
    private TextView tvLevelUpMessage;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LevelViewModelFactory factory = new LevelViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(LevelViewModel.class);

        initViews(view);
        setupObservers();
        viewModel.loadUser(userUid);
    }

    private void initViews(View v) {
        tvTitle             = v.findViewById(R.id.tvLevelTitle);
        tvLevel             = v.findViewById(R.id.tvLevelCurrent);
        tvXp                = v.findViewById(R.id.tvLevelXp);
        tvXpRequired        = v.findViewById(R.id.tvLevelXpRequired);
        tvXpToNext          = v.findViewById(R.id.tvLevelXpToNext);
        tvPowerPoints       = v.findViewById(R.id.tvLevelPowerPoints);
        progressLevel       = v.findViewById(R.id.progressLevel);
        layoutLevelUpBanner = v.findViewById(R.id.layoutLevelUpBanner);
        tvLevelUpMessage    = v.findViewById(R.id.tvLevelUpMessage);
    }

    private void setupObservers() {
        viewModel.user.observe(getViewLifecycleOwner(), this::bindUser);

        viewModel.levelProgress.observe(getViewLifecycleOwner(), progress -> {
            if (progress != null)
                progressLevel.setProgress((int)(progress * 100));
        });

        viewModel.xpRequired.observe(getViewLifecycleOwner(), required -> {
            if (required != null)
                tvXpRequired.setText("XP za nivo: " + required);
        });

        viewModel.xpToNext.observe(getViewLifecycleOwner(), toNext -> {
            if (toNext != null)
                tvXpToNext.setText("Nedostaje: " + toNext + " XP");
        });

        viewModel.levelUpEvent.observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.leveledUp)
                showLevelUpBanner(result);
        });
    }

    private void bindUser(User user) {
        if (user == null) return;
        tvTitle.setText(user.getTitle() != null
                ? user.getTitle() : LevelManager.titleForLevel(user.getLevel()));
        tvLevel.setText("Nivo " + user.getLevel());
        tvXp.setText("XP: " + user.getXp());
        tvPowerPoints.setText("PP: " + user.getPowerPoints());
    }

    private void showLevelUpBanner(UserRepository.LevelUpResult result) {
        layoutLevelUpBanner.setVisibility(View.VISIBLE);
        tvLevelUpMessage.setText("Prešli ste nivo " + result.newLevel + "!");
        layoutLevelUpBanner.postDelayed(() ->
                layoutLevelUpBanner.setVisibility(View.GONE), 3000);
    }
}