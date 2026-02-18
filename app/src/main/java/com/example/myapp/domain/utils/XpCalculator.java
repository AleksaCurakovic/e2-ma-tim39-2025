package com.example.myapp.domain.utils;

import com.example.myapp.domain.models.Task;

public class XpCalculator {

    // ─── Bazne XP vrednosti ───

    public static int baseDifficultyXp(String difficulty) {
        switch (difficulty) {
            case Task.DIFFICULTY_VERY_EASY: return 1;
            case Task.DIFFICULTY_EASY:      return 3;
            case Task.DIFFICULTY_HARD:      return 7;
            case Task.DIFFICULTY_EXTREME:   return 20;
            default: return 1;
        }
    }

    public static int baseImportanceXp(String importance) {
        switch (importance) {
            case Task.IMPORTANCE_NORMAL:    return 1;
            case Task.IMPORTANCE_IMPORTANT: return 3;
            case Task.IMPORTANCE_EXTREME:   return 10;
            case Task.IMPORTANCE_SPECIAL:   return 100;
            default: return 1;
        }
    }

    // ─── XP uvećan po nivou ───
    // Formula: prethodni + prethodni/2 (zaokruži)

    public static int difficultyXpForLevel(String difficulty, int level) {
        double xp = baseDifficultyXp(difficulty);
        for (int i = 0; i < level; i++) {
            xp = Math.round(xp + xp / 2.0);
        }
        return (int) xp;
    }

    public static int importanceXpForLevel(String importance, int level) {
        double xp = baseImportanceXp(importance);
        for (int i = 0; i < level; i++) {
            xp = Math.round(xp + xp / 2.0);
        }
        return (int) xp;
    }

    public static int calculateTaskXp(String difficulty, String importance, int userLevel) {
        return difficultyXpForLevel(difficulty, userLevel)
                + importanceXpForLevel(importance, userLevel);
    }
}