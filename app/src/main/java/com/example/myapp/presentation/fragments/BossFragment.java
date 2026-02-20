package com.example.myapp.presentation.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.R;
import com.example.myapp.domain.models.Boss;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.utils.BossCalculator;
import com.example.myapp.presentation.activities.MainActivity;
import com.example.myapp.presentation.viewmodels.BossViewModel;
import com.example.myapp.presentation.viewmodels.BossViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

public class BossFragment extends Fragment implements SensorEventListener {

    private BossViewModel viewModel;
    private String userUid;

    // Sensor
    private SensorManager sensorManager;
    private Sensor        accelerometer;
    private long          lastShakeTime   = 0;
    private static final float SHAKE_THRESHOLD   = 2.5f;
    private static final int   SHAKE_COOLDOWN_MS = 1000;

    // ─── Views — borba ───
    private ScrollView   layoutBattle;
    private ImageView    imgBoss;
    private ProgressBar  progressBossHp, progressUserPp;
    private TextView     tvBossHp, tvUserPp, tvAttacksLeft,
            tvSuccessRate, tvAttackResult;
    private Button       btnAttack;

    // ─── Views — rezultat ───
    private LinearLayout layoutResult;
    private ImageView    imgChest;
    private TextView     tvResultTitle, tvResultCoins, tvShakeHint;
    private Button       btnClose;

    private boolean chestOpened = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_boss, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        BossViewModelFactory factory = new BossViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(BossViewModel.class);

        sensorManager = (SensorManager) requireContext()
                .getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        initViews(view);
        setupObservers();
        viewModel.initBattle(userUid);

        btnAttack.setOnClickListener(v -> viewModel.attack(userUid));
        btnClose.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    // ─────────────────────────────────────────
    // INIT VIEWS
    // ─────────────────────────────────────────

    private void initViews(View v) {
        layoutBattle   = v.findViewById(R.id.layoutBattle);
        imgBoss        = v.findViewById(R.id.imgBoss);
        progressBossHp = v.findViewById(R.id.progressBossHp);
        progressUserPp = v.findViewById(R.id.progressUserPp);
        tvBossHp       = v.findViewById(R.id.tvBossHp);
        tvUserPp       = v.findViewById(R.id.tvUserPp);
        tvAttacksLeft  = v.findViewById(R.id.tvAttacksLeft);
        tvSuccessRate  = v.findViewById(R.id.tvSuccessRate);
        tvAttackResult = v.findViewById(R.id.tvAttackResult);
        btnAttack      = v.findViewById(R.id.btnAttack);
        layoutResult   = v.findViewById(R.id.layoutResult);
        imgChest       = v.findViewById(R.id.imgChest);
        tvResultTitle  = v.findViewById(R.id.tvResultTitle);
        tvResultCoins  = v.findViewById(R.id.tvResultCoins);
        tvShakeHint    = v.findViewById(R.id.tvShakeHint);
        btnClose       = v.findViewById(R.id.btnClose);
    }

    // ─────────────────────────────────────────
    // OBSERVERS
    // ─────────────────────────────────────────

    private void setupObservers() {
        viewModel.boss.observe(getViewLifecycleOwner(), this::updateBossUi);
        viewModel.user.observe(getViewLifecycleOwner(), this::updateUserUi);

        viewModel.successRate.observe(getViewLifecycleOwner(), rate -> {
            if (rate != null)
                tvSuccessRate.setText("Šansa za pogodak: " + (int)(rate * 100) + "%");
        });

        viewModel.lastAttack.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.hit) showHit(result.damageDealt);
            else            showMiss();
        });

        viewModel.battleOver.observe(getViewLifecycleOwner(), over -> {
            if (Boolean.TRUE.equals(over)) btnAttack.setEnabled(false);
        });

        // reward observer:
        //   null         → borba nije završena, ignorišemo
        //   bossDefeated → prikaži kovčeg (shake za otvaranje)
        //   !bossDefeated, coins > 0 → prikaži rezultat sa delimičnom nagradom
        //   !bossDefeated, coins = 0 → prikaži poraz bez nagrade
        viewModel.reward.observe(getViewLifecycleOwner(), reward -> {
            if (reward == null) return;
            if (reward.bossDefeated) {
                showVictory(reward);
            } else {
                showNotDefeated(reward);
            }
        });

        // Nakon pobede — ako ima još bosova, posle 3s počni novu borbu automatski
        viewModel.hasMoreBosses.observe(getViewLifecycleOwner(), hasMore -> {
            if (hasMore == null) return;

            if (hasMore) {
                // Ima još bosova — počni novu borbu za 3s
                handler.postDelayed(() -> {
                    if (!isAdded()) return;
                    layoutResult.setVisibility(View.GONE);
                    layoutBattle.setVisibility(View.VISIBLE);
                    btnAttack.setEnabled(true);
                    viewModel.hasMoreBosses.setValue(false);
                    viewModel.initBattle(userUid);
                }, 3000);
            } else {
                // Nema više bosova — sakrij tab, ali samo ako je borba stvarno završena
                // (false se emituje i pri inicijalizaciji pa proveravamo)
                if (Boolean.TRUE.equals(viewModel.battleOver.getValue())
                        && viewModel.reward.getValue() != null
                        && viewModel.reward.getValue().bossDefeated) {
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).updateBossTab(false);
                    }
                }
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error == null || error.isEmpty()) return;
            if ("NO_BOSS_AVAILABLE".equals(error)) {
                Toast.makeText(requireContext(),
                        "Nema dostupnog bosa. Prelazi nivo prvo!",
                        Toast.LENGTH_LONG).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(),
                        "Greška: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────
    // UI UPDATE — borba
    // ─────────────────────────────────────────

    private void updateBossUi(Boss boss) {
        if (boss == null) return;
        progressBossHp.setProgress((int)(boss.getHpPercent() * 100));
        tvBossHp.setText("HP: " + boss.getCurrentHp() + " / " + boss.getMaxHp());
        tvAttacksLeft.setText(boss.getAttacksLeft() + " / " + Boss.MAX_ATTACKS + " napada");
    }

    private void updateUserUi(User user) {
        if (user == null) return;
        tvUserPp.setText("PP: " + user.getPowerPoints());
        progressUserPp.setMax(Math.max(user.getPowerPoints(), 1));
        progressUserPp.setProgress(user.getPowerPoints());
    }

    // ─────────────────────────────────────────
    // ANIMACIJE NAPADA
    // ─────────────────────────────────────────

    private void showHit(int damage) {
        imgBoss.setImageResource(R.drawable.scp173_hit);
        imgBoss.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shake));

        tvAttackResult.setText("POGODAK! -" + damage + " HP");
        tvAttackResult.setTextColor(
                getResources().getColor(android.R.color.holo_green_dark, null));
        tvAttackResult.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            if (!isAdded()) return;
            imgBoss.clearAnimation();
            imgBoss.setImageResource(R.drawable.scp173);
        }, 600);

        handler.postDelayed(() -> {
            if (!isAdded()) return;
            tvAttackResult.setVisibility(View.INVISIBLE);
        }, 1500);
    }

    private void showMiss() {
        tvAttackResult.setText("PROMAŠAJ!");
        tvAttackResult.setTextColor(
                getResources().getColor(android.R.color.holo_red_dark, null));
        tvAttackResult.setVisibility(View.VISIBLE);
        layoutBattle.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.miss));

        handler.postDelayed(() -> {
            if (!isAdded()) return;
            tvAttackResult.setVisibility(View.INVISIBLE);
        }, 1500);
    }

    // ─────────────────────────────────────────
    // REZULTAT — POBEDA
    // Kovčeg se prikazuje, korisnik treba da protrese telefon da ga otvori.
    // ─────────────────────────────────────────

    private void showVictory(BossCalculator.BattleReward reward) {
        layoutBattle.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
        chestOpened = false;

        tvResultTitle.setText("BOS PORAŽEN! 🎉");
        tvResultTitle.setTextColor(
                getResources().getColor(android.R.color.holo_green_dark, null));

        imgChest.setVisibility(View.VISIBLE);
        imgChest.setImageResource(R.drawable.closed_chest);
        imgChest.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.chest_shake));

        tvResultCoins.setVisibility(View.GONE);
        tvShakeHint.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────
    // REZULTAT — BOS NIJE PORAŽEN
    // Spec: bos čeka sledeći nivo.
    // Nema kovčega. Coins se prikazuju odmah ako ih ima (≥50% HP slučaj).
    // ─────────────────────────────────────────

    private void showNotDefeated(BossCalculator.BattleReward reward) {
        layoutBattle.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);

        imgChest.setVisibility(View.GONE);
        tvShakeHint.setVisibility(View.GONE);

        tvResultTitle.setText("Bos nije poražen — čeka te posle sledećeg nivoa.");
        tvResultTitle.setTextColor(
                getResources().getColor(android.R.color.holo_orange_dark, null));

        if (reward.coins > 0) {
            tvResultCoins.setText("+" + reward.coins + " novčića 🪙 (nanesena šteta ≥50% HP)");
        } else {
            tvResultCoins.setText("Nema nagrade.");
        }
        tvResultCoins.setVisibility(View.VISIBLE);
    }

    // ─────────────────────────────────────────
    // OTVARANJE KOVČEGA (shake pri pobedi)
    // ─────────────────────────────────────────

    private void openChest() {
        if (chestOpened) return;
        BossCalculator.BattleReward currentReward = viewModel.reward.getValue();
        if (currentReward == null) return;

        chestOpened = true;
        imgChest.clearAnimation();
        imgChest.setImageResource(R.drawable.open_chest);
        imgChest.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.chest_open));

        tvShakeHint.setVisibility(View.GONE);
        tvResultCoins.setText("+" + currentReward.coins + " novčića 🪙");
        tvResultCoins.setVisibility(View.VISIBLE);
        tvResultCoins.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }

    // ─────────────────────────────────────────
    // SHAKE SENSOR
    // Tokom borbe: shake = napad
    // Nakon pobede (reward != null && bossDefeated): shake = otvori kovčeg
    // ─────────────────────────────────────────

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        long now = System.currentTimeMillis();
        if (now - lastShakeTime < SHAKE_COOLDOWN_MS) return;

        float gX     = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY     = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ     = event.values[2] / SensorManager.GRAVITY_EARTH;
        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD) {
            lastShakeTime = now;

            BossCalculator.BattleReward currentReward = viewModel.reward.getValue();
            if (currentReward != null && currentReward.bossDefeated) {
                openChest();
            } else {
                Boss currentBoss = viewModel.boss.getValue();
                if (currentBoss != null && !currentBoss.isBattleOver()) {
                    viewModel.attack(userUid);
                }
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacksAndMessages(null);
    }

    // Korisnik napustio fragment usred borbe = forfeit.
    // Bos se resetuje na max HP, nema nagrade, čeka sledeći nivo.
    // Ako je borba završena (battleOver = true) preskačemo — već sređeno.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.forfeitBattle(userUid);
    }
}