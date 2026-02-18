package com.example.myapp.domain.models;

import java.util.List;
import java.util.Map;

public class UserStats {

    private int activeDaysStreak;        // trenutni niz uzastopnih dana
    private int longestStreak;           // najduži niz ikad

    // Ukupni zadaci po statusu
    private int totalCreated;
    private int totalDone;
    private int totalUndone;
    private int totalCancelled;

    // Zadaci završeni po kategoriji: categoryId -> broj
    private Map<String, Integer> donePerCategory;
    // Naziv kategorije: categoryId -> name (za prikaz na grafu)
    private Map<String, String> categoryNames;

    // XP po danu za poslednjih 7 dana: index 0 = najstariji
    private List<Float> xpLast7Days;

    // Prosečna težina završenih zadataka po danu (za line graf)
    private List<Float> avgDifficultyLast7Days;

    // Specijalne misije
    private int specialMissionsStarted;
    private int specialMissionsCompleted;

    public UserStats() {}

    // ─── Getters & Setters ───

    public int getActiveDaysStreak() { return activeDaysStreak; }
    public void setActiveDaysStreak(int activeDaysStreak) { this.activeDaysStreak = activeDaysStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public int getTotalCreated() { return totalCreated; }
    public void setTotalCreated(int totalCreated) { this.totalCreated = totalCreated; }

    public int getTotalDone() { return totalDone; }
    public void setTotalDone(int totalDone) { this.totalDone = totalDone; }

    public int getTotalUndone() { return totalUndone; }
    public void setTotalUndone(int totalUndone) { this.totalUndone = totalUndone; }

    public int getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(int totalCancelled) { this.totalCancelled = totalCancelled; }

    public Map<String, Integer> getDonePerCategory() { return donePerCategory; }
    public void setDonePerCategory(Map<String, Integer> donePerCategory) { this.donePerCategory = donePerCategory; }

    public Map<String, String> getCategoryNames() { return categoryNames; }
    public void setCategoryNames(Map<String, String> categoryNames) { this.categoryNames = categoryNames; }

    public List<Float> getXpLast7Days() { return xpLast7Days; }
    public void setXpLast7Days(List<Float> xpLast7Days) { this.xpLast7Days = xpLast7Days; }

    public List<Float> getAvgDifficultyLast7Days() { return avgDifficultyLast7Days; }
    public void setAvgDifficultyLast7Days(List<Float> avgDifficultyLast7Days) { this.avgDifficultyLast7Days = avgDifficultyLast7Days; }

    public int getSpecialMissionsStarted() { return specialMissionsStarted; }
    public void setSpecialMissionsStarted(int specialMissionsStarted) { this.specialMissionsStarted = specialMissionsStarted; }

    public int getSpecialMissionsCompleted() { return specialMissionsCompleted; }
    public void setSpecialMissionsCompleted(int specialMissionsCompleted) { this.specialMissionsCompleted = specialMissionsCompleted; }
}