package com.example.myapp.domain.models;

import com.example.myapp.domain.utils.BossCalculator;

import java.util.UUID;

public class Boss {

    public static final int MAX_ATTACKS = 5;

    private String id;
    private String userUid;
    private int    bossLevel;
    private int    maxHp;
    private int    currentHp;
    private int    attacksLeft;

    public Boss() {}

    public Boss(String userUid, int bossLevel) {
        this.id          = UUID.randomUUID().toString();
        this.userUid     = userUid;
        this.bossLevel   = bossLevel;
        this.maxHp       = BossCalculator.hpForBoss(bossLevel);
        this.currentHp   = this.maxHp;
        this.attacksLeft = MAX_ATTACKS;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public int getBossLevel() { return bossLevel; }
    public void setBossLevel(int bossLevel) { this.bossLevel = bossLevel; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getAttacksLeft() { return attacksLeft; }
    public void setAttacksLeft(int attacksLeft) { this.attacksLeft = attacksLeft; }

    public boolean isBattleOver() {
        return isDefeated() || attacksLeft <= 0;
    }

    public boolean isDefeated() {
        return currentHp <= 0;
    }

    public boolean isHalfHpDealt() {
        return currentHp <= maxHp / 2;
    }

    public float getHpPercent() {
        if (maxHp == 0) return 0f;
        return (float) currentHp / (float) maxHp;
    }

    public void applyAttack(boolean hit, int pp) {
        attacksLeft--;
        if (hit) {
            currentHp = Math.max(0, currentHp - pp);
        }
    }
}