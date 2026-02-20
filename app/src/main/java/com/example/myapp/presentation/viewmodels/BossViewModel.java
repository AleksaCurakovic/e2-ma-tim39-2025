package com.example.myapp.presentation.viewmodels;

import android.content.Context;
import android.util.Log;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.repositories.BossRepository;
import com.example.myapp.data.repositories.TaskRepository;
import com.example.myapp.data.repositories.UserRepository;
import com.example.myapp.domain.models.Boss;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.utils.BossCalculator;

public class BossViewModel extends ViewModel {

    private final BossRepository bossRepository;
    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    public MutableLiveData<Boss>    boss         = new MutableLiveData<>();
    public MutableLiveData<User>    user         = new MutableLiveData<>();
    public MutableLiveData<Float>   successRate  = new MutableLiveData<>();
    public MutableLiveData<BossCalculator.AttackResult> lastAttack = new MutableLiveData<>();
    public MutableLiveData<Boolean> battleOver   = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> hasMoreBosses = new MutableLiveData<>(false);
    public MutableLiveData<BossCalculator.BattleReward> reward     = new MutableLiveData<>();

    public MutableLiveData<String>  errorMessage = new MutableLiveData<>();

    public BossViewModel(Context context) {
        bossRepository = new BossRepository(context);
        userRepository = new UserRepository(context);
        taskRepository =  new TaskRepository(context);
    }

    // ─────────────────────────────────────────
    // INICIJALIZACIJA
    // ─────────────────────────────────────────

    public void initBattle(String userUid) {
        User localUser = userRepository.getUserLocally(userUid);
        if (localUser == null) return;
        user.setValue(localUser);

        Boss nextBoss = bossRepository.getNextBoss(userUid);
        if (nextBoss == null) {
            errorMessage.setValue("NO_BOSS_AVAILABLE");
            return;
        }

        boss.setValue(nextBoss);

        // Učitaj taskove i izračunaj success rate kroz BossCalculator
        List<Task> allTasks = taskRepository.getAllTasks(userUid);
        float rate = BossCalculator.calculateSuccessRate(
                allTasks, localUser.getLevelStartTimestamp(), localUser.getLevelEndTimestamp());
        successRate.setValue(rate);

        Log.d("BossViewModel", "Battle init. Boss level: " + nextBoss.getBossLevel()
                + " HP: " + nextBoss.getCurrentHp()
                + " Success rate: " + (int)(rate * 100) + "%");
    }

    // ─────────────────────────────────────────
    // NAPAD
    // ─────────────────────────────────────────

    public void attack(String userUid) {
        Boss currentBoss = boss.getValue();
        User currentUser = user.getValue();
        Float rate       = successRate.getValue();

        if (currentBoss == null || currentUser == null || rate == null) return;
        if (currentBoss.isBattleOver()) return;

        BossCalculator.AttackResult result =
                BossCalculator.simulateAttack(rate, currentUser.getPowerPoints());

        currentBoss.applyAttack(result.hit, currentUser.getPowerPoints());
        lastAttack.setValue(result);
        boss.setValue(currentBoss);

        Log.d("BossViewModel", "Attack: hit=" + result.hit
                + " roll=" + result.randomRoll
                + " bossHp=" + currentBoss.getCurrentHp()
                + " attacksLeft=" + currentBoss.getAttacksLeft());

        if (currentBoss.isBattleOver()) {
            battleOver.setValue(true);
            finalizeBattle(userUid, currentBoss);
        }
    }

    public void forfeitBattle(String userUid) {
        Boss currentBoss = boss.getValue();
        if (currentBoss == null || Boolean.TRUE.equals(battleOver.getValue())) return;

        bossRepository.forfeitBattle(currentBoss, new OnResult<Void>() {
            @Override public void onSuccess(Void r) {
                Log.d("BossViewModel", "Battle forfeited. Boss reset to full HP.");
            }
            @Override public void onFailure(Exception e) {
                Log.e("BossViewModel", "Failed to forfeit battle", e);
            }
        });
    }

    // ─────────────────────────────────────────
    // FINALIZACIJA
    // ─────────────────────────────────────────

    private void finalizeBattle(String userUid, Boss currentBoss) {
        User currentUser = user.getValue();
        if (currentUser == null) {
            errorMessage.postValue("BATTLE_FINALIZE_FAILED");
            return;
        }

        // applyBattleResult mutira currentUser (coins, pp) i odmah poziva callback
        bossRepository.applyBattleResult(currentBoss, currentUser,
                new OnResult<BossCalculator.BattleReward>() {
                    @Override public void onSuccess(BossCalculator.BattleReward result) {
                        // Sačuvaj mutirani user (BossRepository to više ne radi)
                        userRepository.updateUser(currentUser, new OnResult<Void>() {
                            @Override public void onSuccess(Void v) {
                                Log.d("BossViewModel", "User saved after battle.");
                            }
                            @Override public void onFailure(Exception e) {
                                Log.e("BossViewModel", "Failed to save user after battle", e);
                            }
                        });

                        reward.postValue(result);
                        user.postValue(currentUser);

                        if (currentBoss.isDefeated()) {
                            Boss next = bossRepository.getNextBoss(userUid);
                            hasMoreBosses.postValue(next != null);
                        }

                        Log.d("BossViewModel", "Battle finalized. Defeated: "
                                + currentBoss.isDefeated());
                    }
                    @Override public void onFailure(Exception e) {
                        Log.e("BossViewModel", "Failed to finalize battle", e);
                        errorMessage.postValue("BATTLE_FINALIZE_FAILED");
                    }
                });
    }
}