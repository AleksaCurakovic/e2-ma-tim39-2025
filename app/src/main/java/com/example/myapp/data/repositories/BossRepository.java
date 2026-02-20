package com.example.myapp.data.repositories;

import android.content.Context;
import android.util.Log;

import com.example.myapp.data.datasource.local.LocalDataSource;
import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.datasource.remote.RemoteDataSource;
import com.example.myapp.domain.models.Boss;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.utils.BossCalculator;
import com.example.myapp.domain.utils.LevelManager;

import java.util.List;

public class BossRepository {

    private static final String TAG = "BossRepository";

    private final LocalDataSource  localDataSource;
    private final RemoteDataSource remoteDataSource;

    public BossRepository(Context context) {
        this.localDataSource  = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
    }

    // ─────────────────────────────────────────
    // KREIRANJE BOSA PRI LEVEL UP
    // Poziva se iz UserRepository.checkAndApplyLevelUp
    // ─────────────────────────────────────────

    // getNextBoss — uzima prvog iz liste (najmanji level)
    public Boss getNextBoss(String userUid) {
        List<Boss> bosses = localDataSource.getAllBosses(userUid);
        if (bosses.isEmpty()) return null;
        return bosses.get(0);
    }

    // Kreiranje bosa pri level up
    public void createBossForLevel(String userUid, int level, OnResult<Void> callback) {
        Boss boss = new Boss(userUid, level);
        localDataSource.saveBoss(boss);
        remoteDataSource.saveBoss(boss, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Boss created for level: " + level);
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to save boss to remote", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    public void applyBattleResult(Boss boss, User user,
                                  OnResult<BossCalculator.BattleReward> callback) {

        BossCalculator.BattleReward reward = BossCalculator.calculateReward(
                boss.getBossLevel(),
                boss.isDefeated(),
                boss.getMaxHp(),
                boss.getCurrentHp());

        if (reward.coins > 0) {
            user.setCoins(user.getCoins() + reward.coins);
        }

        if (boss.isDefeated()) {
            // PP nagrada samo pri pobedi
            int pp = LevelManager.ppRewardForLevel(boss.getBossLevel());
            user.setPowerPoints(user.getPowerPoints() + pp);

            localDataSource.deleteBoss(boss.getId());
            remoteDataSource.deleteBoss(boss.getId(), new OnResult<Void>() {
                @Override public void onSuccess(Void r) {
                    Log.d(TAG, "Boss deleted after defeat. Level: " + boss.getBossLevel());
                }
                @Override public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to delete boss from remote", e);
                }
            });
        } else {
            // Resetuj bosa — čeka sledeći nivo sa punim HP
            boss.setCurrentHp(boss.getMaxHp());
            boss.setAttacksLeft(Boss.MAX_ATTACKS);

            localDataSource.saveBoss(boss);
            remoteDataSource.updateBoss(boss, new OnResult<Void>() {
                @Override public void onSuccess(Void r) {
                    Log.d(TAG, "Boss reset, waiting for next level. Level: "
                            + boss.getBossLevel());
                }
                @Override public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to reset boss on remote", e);
                }
            });
        }

        if (callback != null) callback.onSuccess(reward);
    }

    public void forfeitBattle(Boss boss, OnResult<Void> callback) {
        boss.setCurrentHp(boss.getMaxHp());
        boss.setAttacksLeft(Boss.MAX_ATTACKS);

        localDataSource.saveBoss(boss);
        remoteDataSource.updateBoss(boss, new OnResult<Void>() {
            @Override public void onSuccess(Void r) {
                Log.d(TAG, "Battle forfeited, boss reset. Level: " + boss.getBossLevel());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to reset boss after forfeit", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }


    public void syncFromCloud(String userUid, OnResult<Void> callback) {
        remoteDataSource.getBossesForUser(userUid, new OnResult<List<Boss>>() {
            @Override
            public void onSuccess(List<Boss> bosses) {
                for (Boss boss : bosses) {
                    localDataSource.saveBoss(boss);
                }
                Log.d(TAG, "Bosses synced: " + bosses.size());
                if (callback != null) callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to sync bosses", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }
}