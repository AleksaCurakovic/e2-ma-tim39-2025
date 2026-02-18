package com.example.myapp.presentation.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.domain.models.User;
import com.example.myapp.presentation.adapters.BadgeAdapter;
import com.example.myapp.presentation.viewmodels.ProfileViewModel;
import com.example.myapp.presentation.viewmodels.ProfileViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;




import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private String userUid;

    // Views — javni profil
    private ImageView imgAvatar;
    private TextView tvUsername, tvLevel, tvTitle;
    private RecyclerView rvBadges;
    private ImageView imgQrCode;

    // Views — privatno (samo za vlasnika)
    private TextView tvXp, tvCoins, tvPowerPoints;
    private LinearLayout layoutPrivateInfo;


    private BadgeAdapter badgeAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ProfileViewModelFactory factory = new ProfileViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);

        initViews(view);
        setupBadgeRecycler();
        setupObservers();

        viewModel.loadUser(userUid);

    }

    private void initViews(View v) {
        imgAvatar             = v.findViewById(R.id.imgProfileAvatar);
        tvUsername            = v.findViewById(R.id.tvProfileUsername);
        tvLevel               = v.findViewById(R.id.tvProfileLevel);
        tvTitle               = v.findViewById(R.id.tvProfileTitle);
        rvBadges              = v.findViewById(R.id.rvProfileBadges);
        imgQrCode             = v.findViewById(R.id.imgProfileQrCode);
        tvXp                  = v.findViewById(R.id.tvProfileXp);
        tvCoins               = v.findViewById(R.id.tvProfileCoins);
        tvPowerPoints         = v.findViewById(R.id.tvProfilePowerPoints);
        layoutPrivateInfo     = v.findViewById(R.id.layoutPrivateInfo);
    }

    private void setupBadgeRecycler() {
        badgeAdapter = new BadgeAdapter(new ArrayList<>());
        rvBadges.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rvBadges.setAdapter(badgeAdapter);
    }


    private void setupObservers() {
        viewModel.user.observe(getViewLifecycleOwner(), this::bindUser);
    }

    private void bindUser(User user) {
        if (user == null) return;

        // Avatar
        int avatarRes = getAvatarResource(user.getAvatar());
        imgAvatar.setImageResource(avatarRes);

        // Javne informacije
        tvUsername.setText(user.getUsername());
        tvLevel.setText("Nivo " + user.getLevel());
        tvTitle.setText(user.getTitle() != null ? user.getTitle() : "Početnik");

        // Bedževi
        if (user.getBadges() != null) {
            badgeAdapter.updateBadges(user.getBadges());
        }

        // QR kod — enkoduje UID korisnika
        generateQrCode(user.getUid());

        // Privatne informacije (samo vlasnik vidi)
        layoutPrivateInfo.setVisibility(View.VISIBLE);
        tvXp.setText("XP: " + user.getXp());
        tvCoins.setText("Novčići: " + user.getCoins());
        tvPowerPoints.setText("PP: " + user.getPowerPoints());
    }

    private void generateQrCode(String content) {
        try {
            QRGEncoder qrgEncoder = new QRGEncoder(content, null, QRGContents.Type.TEXT, 300);
            imgQrCode.setImageBitmap(qrgEncoder.getBitmap());
        } catch (Exception e) {
            imgQrCode.setVisibility(View.GONE);
        }
    }

    private int getAvatarResource(String avatarId) {
        if (avatarId == null) return R.drawable.horns;
        switch (avatarId) {
            case "elf":     return R.drawable.elf;
            case "death":   return R.drawable.death;
            case "horns":   return R.drawable.horns;
            case "wizard":  return R.drawable.wizard;
            default:        return R.drawable.skeleton;
        }
    }

}