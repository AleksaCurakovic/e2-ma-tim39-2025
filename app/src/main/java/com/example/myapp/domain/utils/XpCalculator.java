package com.example.myapp.domain.utils;

import com.example.myapp.domain.models.Task;

import java.util.Calendar;
import java.util.List;

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

    public static int calculateTaskXp(Task task, int userLevel, List<Task> allTasks) {
        int xp = 0;

        // Provjeri kvotu za težinu — ako je ok, dodaj XP za težinu
        if (!isWithinDifficultyQuota(task.getDifficulty(), allTasks)) {
            xp += difficultyXpForLevel(task.getDifficulty(), userLevel);
        }else {
            task.setViolatedQuota(true);
        }

        // Provjeri kvotu za bitnost — ako je ok, dodaj XP za bitnost
        if (!isWithinImportanceQuota(task.getImportance(), allTasks)) {
            xp += importanceXpForLevel(task.getImportance(), userLevel);
        }else {
            task.setViolatedQuota(true);
        }

        return xp;
    }

    // ─── Kvota za TEŽINU ───
    public static boolean isWithinDifficultyQuota(String difficulty, List<Task> allTasks) {
        long now = System.currentTimeMillis();
        int limit = getDifficultyDailyLimit(difficulty);

        if (limit == 0) {
            // Extreme → max 1 nedeljno
            return countDoneInWeekByDifficulty(allTasks, difficulty, now) < 1;
        }
        return countDoneInDayByDifficulty(allTasks, difficulty, now) < limit;
    }

    // ─── Kvota za BITNOST ───
    public static boolean isWithinImportanceQuota(String importance, List<Task> allTasks) {
        long now = System.currentTimeMillis();
        int limit = getImportanceDailyLimit(importance);

        if (limit == 0) {
            // Special → max 1 mesečno
            return countDoneInMonthByImportance(allTasks, importance, now) < 1;
        }
        return countDoneInDayByImportance(allTasks, importance, now) < limit;
    }

    private static int getDifficultyDailyLimit(String difficulty) {
        switch (difficulty) {
            case Task.DIFFICULTY_VERY_EASY: return 5;
            case Task.DIFFICULTY_EASY:      return 5;
            case Task.DIFFICULTY_HARD:      return 2;
            case Task.DIFFICULTY_EXTREME:   return 0;
            default:                        return Integer.MAX_VALUE;
        }
    }

    // ─── Limiti za bitnost ───
    private static int getImportanceDailyLimit(String importance) {
        switch (importance) {
            case Task.IMPORTANCE_NORMAL:    return 5;
            case Task.IMPORTANCE_IMPORTANT: return 5;
            case Task.IMPORTANCE_EXTREME:   return 2;
            case Task.IMPORTANCE_SPECIAL:   return 0;
            default:                        return Integer.MAX_VALUE;
        }
    }

    // ─── Broj done po težini u danu ───
    private static int countDoneInDayByDifficulty(List<Task> allDoneTasks,
                                                  String difficulty,
                                                  long referenceTime) {
        long[] bounds = getDayBounds(referenceTime);
        int count = 0;
        for (Task t : allDoneTasks) {
            if (!Task.STATUS_DONE.equals(t.getStatus())) continue;
            if (!difficulty.equals(t.getDifficulty())) continue;
            if (t.getCompletedAt() >= bounds[0]
                    && t.getCompletedAt() <= bounds[1]) count++;
        }
        return count;
    }

    // ─── Broj done po bitnosti u danu ───
    private static int countDoneInDayByImportance(List<Task> allDoneTasks,
                                                  String importance,
                                                  long referenceTime) {
        long[] bounds = getDayBounds(referenceTime);
        int count = 0;
        for (Task t : allDoneTasks) {
            if (!Task.STATUS_DONE.equals(t.getStatus())) continue;
            if (!importance.equals(t.getImportance())) continue;
            if (t.getCompletedAt() >= bounds[0]
                    && t.getCompletedAt() <= bounds[1]) count++;
        }
        return count;
    }

    // ─── Broj done po težini u nedelji ───
    private static int countDoneInWeekByDifficulty(List<Task> allDoneTasks,
                                                   String difficulty,
                                                   long referenceTime) {
        long[] bounds = getWeekBounds(referenceTime);
        int count = 0;
        for (Task t : allDoneTasks) {
            if (!Task.STATUS_DONE.equals(t.getStatus())) continue;
            if (!difficulty.equals(t.getDifficulty())) continue;
            if (t.getCompletedAt() >= bounds[0]
                    && t.getCompletedAt() <= bounds[1]) count++;
        }
        return count;
    }


    // ─── Početak i kraj dana ───
    private static long[] getDayBounds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        long end = cal.getTimeInMillis() - 1;
        return new long[]{start, end};
    }

    // ─── Početak i kraj nedelje ───
    private static long[] getWeekBounds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        long end = cal.getTimeInMillis() - 1;
        return new long[]{start, end};
    }

    private static int countDoneInMonthByImportance(List<Task> allDoneTasks,
                                                    String importance,
                                                    long referenceTime) {
        long[] bounds = getMonthBounds(referenceTime);
        int count = 0;
        for (Task t : allDoneTasks) {
            if (!Task.STATUS_DONE.equals(t.getStatus())) continue;
            if (!importance.equals(t.getImportance())) continue;
            if (t.getCompletedAt() >= bounds[0]
                    && t.getCompletedAt() <= bounds[1]) count++;
        }
        return count;
    }

    // ─── Početak i kraj meseca ───
    private static long[] getMonthBounds(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis() - 1;
        return new long[]{start, end};
    }
}