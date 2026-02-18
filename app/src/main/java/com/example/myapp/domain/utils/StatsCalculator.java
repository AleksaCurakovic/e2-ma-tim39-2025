package com.example.myapp.domain.utils;

import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.UserStats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsCalculator {

    /**
     * Izračunava sve statistike na osnovu liste zadataka i kategorija.
     */
    public static UserStats calculate(List<Task> allTasks, List<Category> categories) {
        UserStats stats = new UserStats();

        // ─── Ukupni zadaci po statusu ───
        int created = allTasks.size();
        int done = 0, undone = 0, cancelled = 0;
        for (Task t : allTasks) {
            switch (t.getStatus()) {
                case Task.STATUS_DONE:      done++;      break;
                case Task.STATUS_UNDONE:    undone++;    break;
                case Task.STATUS_CANCELLED: cancelled++; break;
            }
        }
        stats.setTotalCreated(created);
        stats.setTotalDone(done);
        stats.setTotalUndone(undone);
        stats.setTotalCancelled(cancelled);

        // ─── Zadaci po kategoriji ───
        Map<String, Integer> donePerCategory = new HashMap<>();
        Map<String, String> categoryNames = new HashMap<>();
        for (Category c : categories) {
            donePerCategory.put(c.getId(), 0);
            categoryNames.put(c.getId(), c.getName());
        }
        for (Task t : allTasks) {
            if (Task.STATUS_DONE.equals(t.getStatus()) && t.getCategoryId() != null) {
                donePerCategory.merge(t.getCategoryId(), 1, Integer::sum);
            }
        }
        stats.setDonePerCategory(donePerCategory);
        stats.setCategoryNames(categoryNames);

        // ─── Streak (niz dana) ───
        int[] streaks = calculateStreaks(allTasks);
        stats.setActiveDaysStreak(streaks[0]);
        stats.setLongestStreak(streaks[1]);

        // ─── XP poslednjih 7 dana ───
        stats.setXpLast7Days(calculateXpLast7Days(allTasks));

        // ─── Prosečna težina poslednjih 7 dana ───
        stats.setAvgDifficultyLast7Days(calculateAvgDifficultyLast7Days(allTasks));

        // Specijalne misije — placeholder, dodati kada se implementuje celina 7
        stats.setSpecialMissionsStarted(0);
        stats.setSpecialMissionsCompleted(0);

        return stats;
    }

    // ─── Streak kalkulacija ───
    // Niz se ne prekida ako nema zadataka u danu, već samo neurešavanjem
    private static int[] calculateStreaks(List<Task> tasks) {
        // Skupi sve dane koji imaju bar jedan zadatak koji NIJE undone
        // (done, cancelled, paused — ne prekidaju streak)
        // Undone prekida streak

        Map<String, Boolean> dayStatus = new HashMap<>();
        // key = "yyyy-MM-dd", value = false ako ima undone, true ako je ok

        for (Task t : tasks) {
            String day = getDayKey(t.getScheduledTime());
            if (Task.STATUS_UNDONE.equals(t.getStatus())) {
                dayStatus.put(day, false); // undone prekida streak
            } else {
                if (!dayStatus.containsKey(day)) {
                    dayStatus.put(day, true);
                }
                // Ako je već false (undone), ostaje false
            }
        }

        // Sortiraj dane
        List<String> days = new ArrayList<>(dayStatus.keySet());
        Collections.sort(days);

        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 0;

        String today = getDayKey(System.currentTimeMillis());

        for (int i = 0; i < days.size(); i++) {
            String day = days.get(i);
            boolean ok = dayStatus.get(day);

            if (ok) {
                tempStreak++;
                if (tempStreak > longestStreak) longestStreak = tempStreak;
            } else {
                tempStreak = 0;
            }

            // Trenutni streak — od danas unazad
            if (day.equals(today)) {
                currentStreak = tempStreak;
            }
        }

        // Ako je zadnji dan u listi danas ili juče — currentStreak je aktivan
        if (!days.isEmpty()) {
            String lastDay = days.get(days.size() - 1);
            if (!lastDay.equals(today) && !lastDay.equals(getYesterdayKey())) {
                currentStreak = 0;
            }
        }

        return new int[]{currentStreak, longestStreak};
    }

    // ─── XP poslednjih 7 dana ───
    private static List<Float> calculateXpLast7Days(List<Task> tasks) {
        List<Float> xpPerDay = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dayKey = getDayKey(cal.getTimeInMillis());

            float xp = 0;
            for (Task t : tasks) {
                if (Task.STATUS_DONE.equals(t.getStatus())
                        && getDayKey(t.getScheduledTime()).equals(dayKey)) {
                    xp += t.getXpValue();
                }
            }
            xpPerDay.add(xp);
        }
        return xpPerDay;
    }

    // ─── Prosečna težina po danu (poslednjih 7) ───
    private static List<Float> calculateAvgDifficultyLast7Days(List<Task> tasks) {
        List<Float> avgPerDay = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dayKey = getDayKey(cal.getTimeInMillis());

            float total = 0;
            int count = 0;
            for (Task t : tasks) {
                if (Task.STATUS_DONE.equals(t.getStatus())
                        && getDayKey(t.getScheduledTime()).equals(dayKey)) {
                    total += difficultyToFloat(t.getDifficulty());
                    count++;
                }
            }
            avgPerDay.add(count > 0 ? total / count : 0f);
        }
        return avgPerDay;
    }

    private static float difficultyToFloat(String difficulty) {
        switch (difficulty) {
            case Task.DIFFICULTY_VERY_EASY: return 1f;
            case Task.DIFFICULTY_EASY:      return 2f;
            case Task.DIFFICULTY_HARD:      return 3f;
            case Task.DIFFICULTY_EXTREME:   return 4f;
            default: return 1f;
        }
    }

    private static String getDayKey(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c.get(Calendar.YEAR) + "-"
                + c.get(Calendar.MONTH) + "-"
                + c.get(Calendar.DAY_OF_MONTH);
    }

    private static String getYesterdayKey() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        return getDayKey(c.getTimeInMillis());
    }
}