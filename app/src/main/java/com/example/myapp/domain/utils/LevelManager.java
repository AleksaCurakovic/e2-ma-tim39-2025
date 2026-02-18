package com.example.myapp.domain.utils;

import static java.lang.Math.min;

public class LevelManager {

    // ─── Titule ───
    // Nivo 0 = Početnik, Nivo 1 = Ratnik, Nivo 2+ = Veštac
    private static final String[] TITLES = {
            "Početnik",  // nivo 1
            "Ratnik",    // nivo 2
            "Veštac",    // nivo 3
    };

    // ─────────────────────────────────────────
    // XP POTREBAN ZA PRELAZAK NIVOA
    // Nivo 1: 200
    // Svaki sledeći: prethodni * 2 + prethodni / 2
    // Zaokružiti na prvu narednu stoticu
    // ─────────────────────────────────────────

    public static int xpRequiredForLevel(int level) {
        if (level == 1) return 200;

        double xp = 200;
        for (int i = 2; i <= level; i++) {
            xp = xp * 2 + xp / 2;
        }
        return (int) xp;
    }

    // ─────────────────────────────────────────
    // PP NAGRADA ZA PREĐENI NIVO
    // Nivo 1: 40 PP
    // Svaki sledeći: prethodni + 3/4 * prethodni, zaokružiti
    // ─────────────────────────────────────────

    public static int ppRewardForLevel(int level) {
        if (level == 1) return 40;

        double pp = 40;
        for (int i = 2; i <= level; i++) {
            pp = pp + (3.0 / 4.0) * pp;
        }
        return (int) Math.round(pp);
    }

    // ─────────────────────────────────────────
    // XP ZA ZADATAK — delegira na XpCalculator
    // ─────────────────────────────────────────

    public static int calculateTaskXp(String difficulty, String importance, int userLevel) {
        return XpCalculator.calculateTaskXp(difficulty, importance, userLevel);
    }

    // ─────────────────────────────────────────
    // TITULA ZA NIVO
    // ─────────────────────────────────────────

    public static String titleForLevel(int level) {
        return TITLES[min(level - 1, level)];
    }

    // ─────────────────────────────────────────
    // PROVERA LEVEL UP
    // ─────────────────────────────────────────

    public static int checkLevelUp(int currentXp, int currentLevel) {
        int xpNeeded = xpRequiredForLevel(currentLevel + 1);
        if (currentXp >= xpNeeded) {
            return currentLevel + 1;
        }
        return -1;
    }

    // ─────────────────────────────────────────
    // PROGRESS DATA
    // ─────────────────────────────────────────

    public static int xpToNextLevel(int currentXp, int currentLevel) {
        int xpNeeded = xpRequiredForLevel(currentLevel + 1);
        return Math.max(0, xpNeeded - currentXp);
    }

    public static float progressToNextLevel(int currentXp, int currentLevel) {
        int xpForCurrent = xpRequiredForLevel(currentLevel);
        int xpForNext    = xpRequiredForLevel(currentLevel + 1);
        if (xpForNext <= xpForCurrent) return 1f;
        float progress = (float)(currentXp - xpForCurrent)
                / (float)(xpForNext - xpForCurrent);
        return Math.max(0f, min(1f, progress));
    }
}