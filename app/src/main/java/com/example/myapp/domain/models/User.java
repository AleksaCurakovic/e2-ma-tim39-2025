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
    private List<String> badges;

    // Prazan konstruktor za Firestore
    public User() {
        this.badges = new ArrayList<>();
    }

    // Pun konstruktor
    public User(String uid, String email, String username, String avatar,
                int level, String title, int powerPoints, int xp, int coins,
                List<String> badges) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.level = level;
        this.title = title;
        this.powerPoints = powerPoints;
        this.xp = xp;
        this.coins = coins;
        this.badges = badges != null ? badges : new ArrayList<>();
    }

    // Getters
    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getAvatar() { return avatar; }
    public int getLevel() { return level; }
    public String getTitle() { return title; }
    public int getPowerPoints() { return powerPoints; }
    public int getXp() { return xp; }
    public int getCoins() { return coins; }
    public List<String> getBadges() { return badges; }
    @Exclude
    public int getBadgeCount() { return badges != null ? badges.size() : 0; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setLevel(int level) { this.level = level; }
    public void setTitle(String title) { this.title = title; }
    public void setPowerPoints(int powerPoints) { this.powerPoints = powerPoints; }
    public void setXp(int xp) { this.xp = xp; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setBadges(List<String> badges) { this.badges = badges; }
}