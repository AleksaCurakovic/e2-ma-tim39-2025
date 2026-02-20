package com.example.myapp.domain.models;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String uid;
    private String email;
    private String username;
    private String avatar;
    private int level;
    private String title;
    private int powerPoints;
    private int xp;
    private int coins;
    private boolean bossDefeated;
    private long levelStartTimestamp;

    private long levelEndTimestamp;
    private List<String> badges;
    private List<String> equippedItemIds;

    public User() {}

    public User(String uid, String email, String username, String avatar,
                int level, String title, int powerPoints, int xp,
                int coins, List<String> badges) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.level = level;
        this.title = title;
        this.powerPoints = powerPoints;
        this.xp = xp;
        this.coins = coins;
        this.badges = badges;
    }

    // ─── Getters & Setters ───
    public long getLevelEndTimestamp() { return levelEndTimestamp; }
    public void setLevelEndTimestamp(long levelEndTimestamp) {
        this.levelEndTimestamp = levelEndTimestamp;
    }
    public boolean isBossDefeated() { return bossDefeated; }
    public void setBossDefeated(boolean bossDefeated) { this.bossDefeated = bossDefeated; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getLevelStartTimestamp() {
        return levelStartTimestamp;
    }

    public void setLevelStartTimestamp(long levelStartTimestamp) {
        this.levelStartTimestamp = levelStartTimestamp;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPowerPoints() { return powerPoints; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }

    public List<String> getEquippedItemIds() { return equippedItemIds; }
    public void setEquippedItemIds(List<String> equippedItemIds) { this.equippedItemIds = equippedItemIds; }
}