package com.example.myapp.domain.utils;

import com.example.myapp.domain.models.Task;
import java.util.List;

public class BossCalculator {

    // ─────────────────────────────────────────
    // HP BOSA
    // Bos 1: 200 HP
    // Svaki sledeći: prethodni * 2 + prethodni / 2
    // Zaokružiti na prvu narednu stoticu
    // ─────────────────────────────────────────

    public static int hpForBoss(int bossLevel) {
        if (bossLevel <= 1) return 200;
        double hp = 200;
        for (int i = 2; i <= bossLevel; i++) {
            hp = hp * 2 + hp / 2;
            hp = Math.ceil(hp / 100.0) * 100;
        }
        return (int) hp;
    }

    // ─────────────────────────────────────────
    // NOVČIĆI
    // Bos 1: 200
    // Svaki sledeći: prethodni * 1.2, zaokruži
    // ─────────────────────────────────────────

    public static int coinsForBoss(int bossLevel) {
        if (bossLevel <= 1) return 200;
        double coins = 200;
        for (int i = 2; i <= bossLevel; i++) {
            coins = coins * 1.2;
        }
        return (int) Math.round(coins);
    }

    // ─────────────────────────────────────────
    // USPEŠNOST U ETAPI
    // done / (done + undone)
    // Isključuje: paused, cancelled
    // Zadaci koji prevazilaze kvotu se ne računaju
    // ─────────────────────────────────────────

    public static float calculateSuccessRate(List<Task> allTasks,
                                             long levelStartTimestamp,
                                             long levelEndTimestamp) {

        int done   = 0;
        int undone = 0;

        for (Task task : allTasks) {
            if (Task.STATUS_DONE.equals(task.getStatus())) {
                if (task.getCompletedAt() < levelStartTimestamp) continue;
                if (task.getCompletedAt() > levelEndTimestamp) continue;
                if (!task.isViolatedQuota()) done++;
            } else if (Task.STATUS_UNDONE.equals(task.getStatus())) {
                if (task.getCreatedAt() < levelStartTimestamp) continue;
                if (task.getCreatedAt() > levelEndTimestamp) continue;
                undone++;
            }
        }

        int total = done + undone;
        if (total == 0) return 1f;
        return (float) done / (float) total;
    }

    // ─────────────────────────────────────────
    // SIMULACIJA NAPADA
    // Random 0-100, ako < successRate*100 → pogodak
    // ─────────────────────────────────────────

    public static AttackResult simulateAttack(float successRate, int pp) {
        int random = (int)(Math.random() * 100);
        boolean hit = random < (int)(successRate * 100);
        return new AttackResult(hit, hit ? pp : 0, random);
    }

    // ─────────────────────────────────────────
    // NAGRADE
    // ─────────────────────────────────────────

    public static BattleReward calculateReward(int bossLevel,
                                               boolean defeated,
                                               int maxHp,
                                               int remainingHp) {
        if (defeated) {
            return new BattleReward(coinsForBoss(bossLevel), true);
        }

        int damageDealt = maxHp - remainingHp;
        if (damageDealt >= maxHp / 2) {
            return new BattleReward(coinsForBoss(bossLevel) / 2, false);
        }

        return new BattleReward(0, false);
    }

    // ─────────────────────────────────────────
    // RESULT MODELI
    // ─────────────────────────────────────────

    public static class AttackResult {
        public final boolean hit;
        public final int damageDealt;
        public final int randomRoll;

        public AttackResult(boolean hit, int damageDealt, int randomRoll) {
            this.hit         = hit;
            this.damageDealt = damageDealt;
            this.randomRoll  = randomRoll;
        }
    }

    public static class BattleReward {
        public final int     coins;
        public final boolean bossDefeated;

        public BattleReward(int coins, boolean bossDefeated) {
            this.coins        = coins;
            this.bossDefeated = bossDefeated;
        }
    }
}