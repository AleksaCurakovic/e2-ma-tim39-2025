package com.example.myapp.data.datasource.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapp.domain.models.Boss;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.models.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LocalDataSource {

    private static final String TAG = "LocalDataSource";
    private final DatabaseHelper dbHelper;

    public LocalDataSource(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // ─────────────────────────────────────────
    // USER OPERACIJE
    // ─────────────────────────────────────────

    public void saveUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userToContentValues(user);

        long result = db.replace(DatabaseHelper.TABLE_USERS, null, values);

        if (result == -1) {
            Log.e(TAG, "Failed to save user: " + user.getUid());
        } else {
            Log.d(TAG, "User saved locally: " + user.getUid());
        }

        db.close();
    }

    public User getUser(String uid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_UID + "=?",
                new String[]{uid},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        db.close();
        return user;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userToContentValues(user);

        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_UID + "=?",
                new String[]{user.getUid()}
        );

        if (rows == 0) {
            Log.e(TAG, "Failed to update user: " + user.getUid());
        } else {
            Log.d(TAG, "User updated locally: " + user.getUid());
        }

        db.close();
    }

    public void deleteUser(String uid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                DatabaseHelper.TABLE_USERS,
                DatabaseHelper.COLUMN_UID + "=?",
                new String[]{uid}
        );
        Log.d(TAG, "Deleted " + rows + " user(s) with uid: " + uid);
        db.close();
    }

    public void clearAllUsers() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_USERS, null, null);
        Log.d(TAG, "All local users cleared");
        db.close();
    }

    private ContentValues userToContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_UID, user.getUid());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COLUMN_LEVEL, user.getLevel());
        values.put(DatabaseHelper.COLUMN_TITLE, user.getTitle());
        values.put(DatabaseHelper.COLUMN_POWER_POINTS, user.getPowerPoints());
        values.put(DatabaseHelper.COLUMN_XP, user.getXp());
        values.put(DatabaseHelper.COLUMN_COINS, user.getCoins());
        values.put(DatabaseHelper.COLUMN_LEVEL_START_TS, user.getLevelStartTimestamp());
        values.put(DatabaseHelper.COLUMN_LEVEL_END_TS, user.getLevelEndTimestamp());
        values.put(DatabaseHelper.COLUMN_BOSS_DEFEATED,
                user.isBossDefeated() ? 1 : 0);

        String badgesString = "";
        if (user.getBadges() != null && !user.getBadges().isEmpty()) {
            badgesString = String.join(",", user.getBadges());
        }
        values.put(DatabaseHelper.COLUMN_BADGES, badgesString);

        String equippedString = "";
        if (user.getEquippedItemIds() != null && !user.getEquippedItemIds().isEmpty()) {
            equippedString = String.join(",", user.getEquippedItemIds());
        }
        values.put(DatabaseHelper.COLUMN_EQUIPPED_ITEMS, equippedString);

        return values;
    }



    private User cursorToUser(Cursor cursor) {
        String badgesString = cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BADGES));
        List<String> badges = new ArrayList<>();
        if (badgesString != null && !badgesString.isEmpty()) {
            badges = new ArrayList<>(Arrays.asList(badgesString.split(",")));
        }

        String equippedString = cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EQUIPPED_ITEMS));
        List<String> equipped = new ArrayList<>();
        if (equippedString != null && !equippedString.isEmpty()) {
            equipped = new ArrayList<>(Arrays.asList(equippedString.split(",")));
        }

        User user = new User(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LEVEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POWER_POINTS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_XP)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COINS)),
                badges
        );
        user.setEquippedItemIds(equipped);
        user.setBossDefeated(cursor.getInt(cursor.getColumnIndexOrThrow(
                DatabaseHelper.COLUMN_BOSS_DEFEATED)) == 1);
        user.setLevelStartTimestamp(cursor.getLong(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LEVEL_START_TS)));
        user.setLevelEndTimestamp(cursor.getLong(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LEVEL_END_TS)));
        return user;
    }

    // ─────────────────────────────────────────
    // CATEGORY OPERACIJE
    // ─────────────────────────────────────────

    public boolean saveCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, categoryToContentValues(category));
        db.close();
        if (result == -1) {
            Log.e(TAG, "Failed to save category (color may already exist): " + category.getName());
            return false;
        }
        Log.d(TAG, "Category saved locally: " + category.getName());
        return true;
    }

    public List<Category> getCategoriesForUser(String userUid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Category> categories = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CATEGORIES, null,
                DatabaseHelper.COLUMN_CATEGORY_USER_UID + "=?", new String[]{userUid},
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) categories.add(cursorToCategory(cursor));
            cursor.close();
        }
        db.close();
        return categories;
    }

    public boolean isColorTaken(String userUid, String color) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COLUMN_CATEGORY_ID},
                DatabaseHelper.COLUMN_CATEGORY_USER_UID + "=? AND " +
                        DatabaseHelper.COLUMN_CATEGORY_COLOR + "=?",
                new String[]{userUid, color},
                null, null, null);
        boolean taken = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return taken;
    }

    public void updateCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.update(
                DatabaseHelper.TABLE_CATEGORIES,
                categoryToContentValues(category),
                DatabaseHelper.COLUMN_CATEGORY_ID + "=?",
                new String[]{category.getId()});
        if (rows == 0) Log.e(TAG, "Failed to update category: " + category.getId());
        else Log.d(TAG, "Category updated locally: " + category.getName());
        db.close();
    }

    public void deleteCategory(String categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COLUMN_CATEGORY_ID + "=?", new String[]{categoryId});
        Log.d(TAG, "Category deleted: " + categoryId);
        db.close();
    }

    public void clearCategoriesForUser(String userUid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COLUMN_CATEGORY_USER_UID + "=?", new String[]{userUid});
        Log.d(TAG, "Categories cleared for user: " + userUid);
        db.close();
    }

    private ContentValues categoryToContentValues(Category category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CATEGORY_ID,       category.getId());
        values.put(DatabaseHelper.COLUMN_CATEGORY_USER_UID, category.getUserUid());
        values.put(DatabaseHelper.COLUMN_CATEGORY_NAME,     category.getName());
        values.put(DatabaseHelper.COLUMN_CATEGORY_COLOR,    category.getColor());
        return values;
    }

    private Category cursorToCategory(Cursor cursor) {
        return new Category(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_USER_UID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_COLOR))
        );
    }

    // ─────────────────────────────────────────
    // TASK OPERACIJE
    // ─────────────────────────────────────────

    public void saveTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.replace(DatabaseHelper.TABLE_TASKS, null, taskToContentValues(task));
        db.close();
    }

    public Task getTask(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Task task = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS, null,
                DatabaseHelper.COLUMN_TASK_ID + "=?", new String[]{taskId},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            task = cursorToTask(cursor);
            cursor.close();
        }
        db.close();
        return task;
    }

    // Svi zadaci — za kalendar (uključujući prošle)
    public List<Task> getAllTasksForUser(String userUid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS, null,
                DatabaseHelper.COLUMN_TASK_USER_UID + "=?", new String[]{userUid},
                null, null, DatabaseHelper.COLUMN_TASK_SCHEDULED_TIME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) tasks.add(cursorToTask(cursor));
            cursor.close();
        }
        db.close();
        return tasks;
    }

    // Samo aktivni i pauzirani — za listu
    public List<Task> getUpComingTasksForUser(String userUid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        Cursor cursor = db.query(DatabaseHelper.TABLE_TASKS, null,
                DatabaseHelper.COLUMN_TASK_USER_UID + "=? AND" +
                        DatabaseHelper.COLUMN_TASK_SCHEDULED_TIME + " >= ?",
                new String[]{userUid, String.valueOf(startOfDay)},
                null, null, DatabaseHelper.COLUMN_TASK_SCHEDULED_TIME + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) tasks.add(cursorToTask(cursor));
            cursor.close();
        }
        db.close();
        return tasks;
    }


    public void updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(DatabaseHelper.TABLE_TASKS, taskToContentValues(task),
                DatabaseHelper.COLUMN_TASK_ID + "=?", new String[]{task.getId()});
        db.close();
    }

    public void updateTaskStatus(String taskId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TASK_STATUS, status);
        db.update(DatabaseHelper.TABLE_TASKS, values,
                DatabaseHelper.COLUMN_TASK_ID + "=?", new String[]{taskId});
        db.close();
    }

    public void deleteTask(String taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TASKS,
                DatabaseHelper.COLUMN_TASK_ID + "=?", new String[]{taskId});
        db.close();
    }

    // Briše buduće aktivne/pauzirane zadatke iz grupe
    public void deleteFutureRecurringTasks(String recurrenceGroupId, long fromTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TASKS,
                DatabaseHelper.COLUMN_TASK_RECURRENCE_GROUP + "=? AND " +
                        DatabaseHelper.COLUMN_TASK_SCHEDULED_TIME + " >= ? AND (" +
                        DatabaseHelper.COLUMN_TASK_STATUS + "=? OR " +
                        DatabaseHelper.COLUMN_TASK_STATUS + "=?)",
                new String[]{recurrenceGroupId, String.valueOf(fromTime),
                        Task.STATUS_ACTIVE, Task.STATUS_PAUSED});
        db.close();
    }

    public void clearTasksForUser(String userUid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TASKS,
                DatabaseHelper.COLUMN_TASK_USER_UID + "=?", new String[]{userUid});
        db.close();
    }

// ─── Task helpers ───

    private ContentValues taskToContentValues(Task task) {
        ContentValues v = new ContentValues();
        v.put(DatabaseHelper.COLUMN_TASK_ID,               task.getId());
        v.put(DatabaseHelper.COLUMN_TASK_USER_UID,         task.getUserUid());
        v.put(DatabaseHelper.COLUMN_TASK_TITLE,            task.getTitle());
        v.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION,      task.getDescription());
        v.put(DatabaseHelper.COLUMN_TASK_CATEGORY_ID,      task.getCategoryId());
        v.put(DatabaseHelper.COLUMN_TASK_STATUS,           task.getStatus());
        v.put(DatabaseHelper.COLUMN_TASK_DIFFICULTY,       task.getDifficulty());
        v.put(DatabaseHelper.COLUMN_TASK_IMPORTANCE,       task.getImportance());
        v.put(DatabaseHelper.COLUMN_TASK_XP_VALUE,         task.getXpValue());
        v.put(DatabaseHelper.COLUMN_TASK_SCHEDULED_TIME,   task.getScheduledTime());
        v.put(DatabaseHelper.COLUMN_TASK_CREATED_AT,       task.getCreatedAt());
        v.put(DatabaseHelper.COLUMN_TASK_REPEAT_TYPE,      task.getRepeatType());
        v.put(DatabaseHelper.COLUMN_TASK_REPEAT_INTERVAL,  task.getRepeatInterval());
        v.put(DatabaseHelper.COLUMN_TASK_REPEAT_START,     task.getRepeatStartDate());
        v.put(DatabaseHelper.COLUMN_TASK_REPEAT_END,       task.getRepeatEndDate());
        v.put(DatabaseHelper.COLUMN_TASK_PARENT_ID,        task.getParentTaskId());
        v.put(DatabaseHelper.COLUMN_TASK_RECURRENCE_GROUP, task.getRecurrenceGroupId());
        v.put(DatabaseHelper.COLUMN_TASK_COMPLETED_AT, task.getCompletedAt());
        v.put(DatabaseHelper.COLUMN_TASK_VIOLATED_QUOTA,
                task.isViolatedQuota() ? 1 : 0);
        return v;
    }

    private Task cursorToTask(Cursor c) {
        return new Task(
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_USER_UID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_TITLE)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DESCRIPTION)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_CATEGORY_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_STATUS)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DIFFICULTY)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_IMPORTANCE)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_XP_VALUE)),
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_SCHEDULED_TIME)),
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_CREATED_AT)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_REPEAT_TYPE)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_REPEAT_INTERVAL)),
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_REPEAT_START)),
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_REPEAT_END)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_PARENT_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_RECURRENCE_GROUP)),
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_COMPLETED_AT)),
                c.getInt(c.getColumnIndexOrThrow(
                        DatabaseHelper.COLUMN_TASK_VIOLATED_QUOTA)) == 1
        );
    }

    // ─────────────────────────────────────────
    // BOSS METODE
    // ─────────────────────────────────────────

    public void saveBoss(Boss boss) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insertWithOnConflict(DatabaseHelper.TABLE_BOSSES, null,
                bossToContentValues(boss), SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteBoss(String bossId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_BOSSES,
                DatabaseHelper.COLUMN_BOSS_ID + "=?",
                new String[]{bossId});
        db.close();
    }

    // Vraća sve neporažene bosove poređane po levelu
    public List<Boss> getAllBosses(String userUid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Boss> bosses = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOSSES, null,
                DatabaseHelper.COLUMN_BOSS_USER_UID + "=?",
                new String[]{userUid},
                null, null,
                DatabaseHelper.COLUMN_BOSS_LEVEL + " ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) bosses.add(cursorToBoss(cursor));
            cursor.close();
        }
        db.close();
        return bosses;
    }

// ─── Mapiranje ───

    private ContentValues bossToContentValues(Boss boss) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOSS_ID,           boss.getId());
        values.put(DatabaseHelper.COLUMN_BOSS_USER_UID,     boss.getUserUid());
        values.put(DatabaseHelper.COLUMN_BOSS_LEVEL,        boss.getBossLevel());
        values.put(DatabaseHelper.COLUMN_BOSS_MAX_HP,       boss.getMaxHp());
        values.put(DatabaseHelper.COLUMN_BOSS_CURRENT_HP,   boss.getCurrentHp());
        values.put(DatabaseHelper.COLUMN_BOSS_ATTACKS_LEFT, boss.getAttacksLeft());
        return values;
    }

    private Boss cursorToBoss(Cursor cursor) {
        Boss boss = new Boss();
        boss.setId(cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOSS_ID)));
        boss.setUserUid(cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOSS_USER_UID)));
        boss.setBossLevel(cursor.getInt(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOSS_LEVEL)));
        boss.setMaxHp(cursor.getInt(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOSS_MAX_HP)));
        boss.setCurrentHp(cursor.getInt(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOSS_CURRENT_HP)));
        boss.setAttacksLeft(cursor.getInt(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOSS_ATTACKS_LEFT)));
        return boss;
    }

}