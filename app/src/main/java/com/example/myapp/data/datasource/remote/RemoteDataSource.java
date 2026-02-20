package com.example.myapp.data.datasource.remote;

import android.util.Log;

import com.example.myapp.domain.models.Boss;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.models.Category;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteDataSource {

    private static final String TAG = "RemoteDataSource";

    // Firestore kolekcije
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_CATEGORIES = "categories";

    private static final String COLLECTION_TASKS = "tasks";


    // Firestore polja - User
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_AVATAR = "avatar";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_POWER_POINTS = "powerPoints";
    private static final String FIELD_XP = "xp";
    private static final String FIELD_COINS = "coins";
    private static final String FIELD_BADGES = "badges";
    private static final String FIELD_EQUIPPED_ITEMS = "equipped_items";
    private static final String FIELD_LEVEL_START_TS = "level_start_timestamp";
    private static final String FIELD_LEVEL_END_TS = "level_end_timestamp";

    private static final String FIELD_BOSS_DEFEATED = "boss_defeated";

    // ─────────────────────────────────────────
    // POLJA — CATEGORY
    // ─────────────────────────────────────────
    private static final String FIELD_CATEGORY_NAME = "name";
    private static final String FIELD_CATEGORY_COLOR = "color";
    private static final String FIELD_CATEGORY_USER_UID = "userUid";

    // ─────────────────────────────────────────
    // POLJA — ZADATAK
    // ─────────────────────────────────────────
    private static final String FIELD_TASK_USER_UID = "user_uid";
    private static final String FIELD_TASK_TITLE = "title";
    private static final String FIELD_TASK_DESCRIPTION = "description";
    private static final String FIELD_TASK_CATEGORY_ID = "category_id";
    private static final String FIELD_TASK_STATUS = "status";
    private static final String FIELD_TASK_DIFFICULTY = "difficulty";
    private static final String FIELD_TASK_IMPORTANCE = "importance";
    private static final String FIELD_TASK_XP_VALUE = "xp_value";
    private static final String FIELD_TASK_SCHEDULED_TIME = "scheduled_time";
    private static final String FIELD_TASK_CREATED_AT = "created_at";
    private static final String FIELD_TASK_REPEAT_TYPE  = "repeat_type";
    private static final String FIELD_TASK_REPEAT_INTERVAL = "repeat_interval";
    private static final String FIELD_TASK_REPEAT_START = "repeat_start";
    private static final String FIELD_TASK_REPEAT_END = "repeat_end";
    private static final String FIELD_TASK_PARENT_ID  = "parent_task_id";
    private static final String FIELD_TASK_RECURRENCE_GROUP = "recurrence_group_id";

    private static final String FIELD_TASK_COMPLETED_AT = "completed_at";

    private static final String FIELD_TASK_VIOLATED_QUOTA = "counted_for_xp";


    private static final String COLLECTION_BOSSES = "bosses";
    private static final String FIELD_BOSS_USER_UID    = "user_uid";
    private static final String FIELD_BOSS_LEVEL       = "boss_level";
    private static final String FIELD_BOSS_MAX_HP      = "max_hp";
    private static final String FIELD_BOSS_CURRENT_HP  = "current_hp";
    private static final String FIELD_BOSS_ATTACKS_LEFT = "attacks_left";


    private final FirebaseFirestore firestore;

    public RemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // ─────────────────────────────────────────
    // USER OPERACIJE
    // ─────────────────────────────────────────

    public void saveUser(User user, OnResult<Void> callback) {
        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .set(userToMap(user))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User saved to Firestore: " + user.getUid());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save user to Firestore: " + user.getUid(), e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void getUser(String uid, OnResult<User> callback) {
        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        user.setUid(documentSnapshot.getId());
                        Log.d(TAG, "User fetched from Firestore: " + uid);
                        if (callback != null) callback.onSuccess(user);
                    } else {
                        Log.e(TAG, "User not found in Firestore: " + uid);
                        if (callback != null) callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user from Firestore: " + uid, e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void updateUser(User user, OnResult<Void> callback) {
        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .update(userToMap(user))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User updated in Firestore: " + user.getUid());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user in Firestore: " + user.getUid(), e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void deleteUser(String uid, OnResult<Void> callback) {
        firestore.collection(COLLECTION_USERS)
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User deleted from Firestore: " + uid);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete user from Firestore: " + uid, e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_EMAIL, user.getEmail());
        map.put(FIELD_USERNAME, user.getUsername());
        map.put(FIELD_AVATAR, user.getAvatar());
        map.put(FIELD_LEVEL, user.getLevel());
        map.put(FIELD_TITLE, user.getTitle());
        map.put(FIELD_POWER_POINTS, user.getPowerPoints());
        map.put(FIELD_XP, user.getXp());
        map.put(FIELD_COINS, user.getCoins());
        map.put(FIELD_BADGES, user.getBadges() != null ? user.getBadges() : new ArrayList<>());
        map.put(FIELD_EQUIPPED_ITEMS, user.getEquippedItemIds() != null
                ? user.getEquippedItemIds() : new ArrayList<>());
        map.put(FIELD_LEVEL_START_TS, user.getLevelStartTimestamp());
        map.put(FIELD_LEVEL_END_TS, user.getLevelEndTimestamp());
        map.put(FIELD_BOSS_DEFEATED, user.isBossDefeated());
        return map;
    }

    // ─────────────────────────────────────────
    // CATEGORY OPERACIJE
    // ─────────────────────────────────────────

    public void saveCategory(Category category, OnResult<Void> callback) {
        firestore.collection(COLLECTION_CATEGORIES)
                .document(category.getId())
                .set(categoryToMap(category))
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Category saved to Firestore: " + category.getId());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save category to Firestore", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void getCategoriesForUser(String userUid, OnResult<List<Category>> callback) {
        firestore.collection(COLLECTION_CATEGORIES)
                .whereEqualTo(FIELD_CATEGORY_USER_UID, userUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Category> categories = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        Category category = new Category(
                                doc.getId(),
                                doc.getString(FIELD_CATEGORY_USER_UID),
                                doc.getString(FIELD_CATEGORY_NAME),
                                doc.getString(FIELD_CATEGORY_COLOR)
                        );
                        categories.add(category);
                    }
                    Log.d(TAG, "Fetched " + categories.size() + " categories for: " + userUid);
                    if (callback != null) callback.onSuccess(categories);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch categories", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void updateCategory(Category category, OnResult<Void> callback) {
        firestore.collection(COLLECTION_CATEGORIES)
                .document(category.getId())
                .update(categoryToMap(category))
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Category updated in Firestore: " + category.getId());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update category in Firestore", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void deleteCategory(Category category, OnResult<Void> callback) {
        firestore.collection(COLLECTION_CATEGORIES)
                .document(category.getId())
                .delete()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Category deleted from Firestore: " + category.getId());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete category from Firestore", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    private Map<String, Object> categoryToMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_CATEGORY_USER_UID, category.getUserUid());
        map.put(FIELD_CATEGORY_NAME,     category.getName());
        map.put(FIELD_CATEGORY_COLOR,    category.getColor());
        return map;
    }

    // ─────────────────────────────────────────
    // ZADATAK OPERACIJE
    // ─────────────────────────────────────────
    public void saveTask(Task task, OnResult<Void> callback) {
        firestore.collection(COLLECTION_TASKS)
                .document(task.getId())
                .set(taskToMap(task))
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Task saved to Firestore: " + task.getId());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save task", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void saveTasksBatch(List<Task> tasks, OnResult<Void> callback) {
        WriteBatch batch = firestore.batch();
        for (Task task : tasks) {
            batch.set(firestore.collection(COLLECTION_TASKS)
                    .document(task.getId()), taskToMap(task));
        }
        batch.commit()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Batch saved " + tasks.size() + " tasks");
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Batch save failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void getTasksForUser(String userUid, OnResult<List<Task>> callback) {
        firestore.collection(COLLECTION_TASKS)
                .whereEqualTo("userUid", userUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Task> tasks = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Task t = documentToTask(doc);
                        if (t != null) tasks.add(t);
                    }
                    Log.d(TAG, "Fetched " + tasks.size() + " tasks for: " + userUid);
                    if (callback != null) callback.onSuccess(tasks);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch tasks", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void updateTask(Task task, OnResult<Void> callback) {
        firestore.collection(COLLECTION_TASKS)
                .document(task.getId())
                .update(taskToMap(task))
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Task updated: " + task.getId());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update task", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void updateTaskStatus(String taskId, String status, OnResult<Void> callback) {
        firestore.collection(COLLECTION_TASKS)
                .document(taskId)
                .update("status", status)
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void deleteTask(String taskId, OnResult<Void> callback) {
        firestore.collection(COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Task deleted: " + taskId);
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete task", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void deleteFutureRecurringTasks(String userUid, String groupId,
                                           long fromTime, OnResult<Void> callback) {
        firestore.collection(COLLECTION_TASKS)
                .whereEqualTo(FIELD_TASK_USER_UID, userUid)
                .whereEqualTo(FIELD_TASK_RECURRENCE_GROUP, groupId)
                .whereGreaterThanOrEqualTo(FIELD_TASK_SCHEDULED_TIME, fromTime)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString(FIELD_TASK_STATUS);
                        if (Task.STATUS_ACTIVE.equals(status)
                                || Task.STATUS_PAUSED.equals(status)) {
                            batch.delete(doc.getReference());
                        }
                    }
                    batch.commit()
                            .addOnSuccessListener(v -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void updateFutureRecurringTasks(String userUid,
                                           String groupId,
                                           Task changes,
                                           boolean shiftScheduledTime,
                                           OnResult<Void> callback) {

        firestore.collection(COLLECTION_TASKS)
                .whereEqualTo(FIELD_TASK_USER_UID, userUid)
                .whereEqualTo(FIELD_TASK_RECURRENCE_GROUP, groupId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = firestore.batch();
                    long newScheduledTime = System.currentTimeMillis() + changes.getRepeatInterval();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString(FIELD_TASK_STATUS);

                        // Samo editable taskove menjamo
                        if (Task.STATUS_ACTIVE.equals(status) || Task.STATUS_PAUSED.equals(status)) {
                            batch.update(doc.getReference(), FIELD_TASK_TITLE, changes.getTitle());
                            batch.update(doc.getReference(), FIELD_TASK_DESCRIPTION, changes.getDescription());
                            batch.update(doc.getReference(), FIELD_TASK_DIFFICULTY, changes.getDifficulty());
                            batch.update(doc.getReference(), FIELD_TASK_IMPORTANCE, changes.getImportance());
                            batch.update(doc.getReference(), FIELD_TASK_XP_VALUE, changes.getXpValue());
                            batch.update(doc.getReference(), FIELD_TASK_STATUS, changes.getStatus());

                            if (shiftScheduledTime) {
                                batch.update(doc.getReference(), FIELD_TASK_SCHEDULED_TIME, newScheduledTime);
                            }
                        }
                    }

                    batch.commit()
                            .addOnSuccessListener(v -> {
                                if (callback != null) callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

// ─── Task helpers ───

    private Map<String, Object> taskToMap(Task task) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_TASK_USER_UID, task.getUserUid());
        map.put(FIELD_TASK_TITLE, task.getTitle());
        map.put(FIELD_TASK_DESCRIPTION, task.getDescription());
        map.put(FIELD_TASK_CATEGORY_ID, task.getCategoryId());
        map.put(FIELD_TASK_STATUS, task.getStatus());
        map.put(FIELD_TASK_DIFFICULTY, task.getDifficulty());
        map.put(FIELD_TASK_IMPORTANCE, task.getImportance());
        map.put(FIELD_TASK_XP_VALUE, task.getXpValue());
        map.put(FIELD_TASK_SCHEDULED_TIME, task.getScheduledTime());
        map.put(FIELD_TASK_CREATED_AT, task.getCreatedAt());
        map.put(FIELD_TASK_REPEAT_TYPE, task.getRepeatType());
        map.put(FIELD_TASK_REPEAT_INTERVAL, task.getRepeatInterval());
        map.put(FIELD_TASK_REPEAT_START, task.getRepeatStartDate());
        map.put(FIELD_TASK_REPEAT_END, task.getRepeatEndDate());
        map.put(FIELD_TASK_PARENT_ID, task.getParentTaskId());
        map.put(FIELD_TASK_RECURRENCE_GROUP, task.getRecurrenceGroupId());
        map.put(FIELD_TASK_VIOLATED_QUOTA, task.isViolatedQuota());
        map.put(FIELD_TASK_COMPLETED_AT, task.getCompletedAt());
        return map;
    }


    private Task documentToTask(DocumentSnapshot doc) {
        try {
            return new Task(
                    doc.getId(),
                    doc.getString(FIELD_TASK_USER_UID),
                    doc.getString(FIELD_TASK_TITLE),
                    doc.getString(FIELD_TASK_DESCRIPTION),
                    doc.getString(FIELD_TASK_CATEGORY_ID),
                    doc.getString(FIELD_TASK_STATUS),
                    doc.getString(FIELD_TASK_DIFFICULTY),
                    doc.getString(FIELD_TASK_IMPORTANCE),
                    doc.getLong(FIELD_TASK_XP_VALUE) != null
                            ? doc.getLong(FIELD_TASK_XP_VALUE).intValue() : 0,
                    doc.getLong(FIELD_TASK_SCHEDULED_TIME) != null
                            ? doc.getLong(FIELD_TASK_SCHEDULED_TIME) : 0,
                    doc.getLong(FIELD_TASK_CREATED_AT) != null
                            ? doc.getLong(FIELD_TASK_CREATED_AT) : 0,
                    doc.getString(FIELD_TASK_REPEAT_TYPE),
                    doc.getLong(FIELD_TASK_REPEAT_INTERVAL) != null
                            ? doc.getLong(FIELD_TASK_REPEAT_INTERVAL).intValue() : 1,
                    doc.getLong(FIELD_TASK_REPEAT_START) != null
                            ? doc.getLong(FIELD_TASK_REPEAT_START) : 0,
                    doc.getLong(FIELD_TASK_REPEAT_END) != null
                            ? doc.getLong(FIELD_TASK_REPEAT_END) : 0,
                    doc.getString(FIELD_TASK_PARENT_ID),
                    doc.getString(FIELD_TASK_RECURRENCE_GROUP),
                    doc.getLong(FIELD_TASK_COMPLETED_AT) != null ?
                            doc.getLong(FIELD_TASK_COMPLETED_AT) : 0L,
                    doc.getBoolean(FIELD_TASK_VIOLATED_QUOTA) != null
                            ? doc.getBoolean(FIELD_TASK_VIOLATED_QUOTA) : false
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse task document: " + doc.getId(), e);
            return null;
        }
    }

    public void saveBoss(Boss boss, OnResult<Void> callback) {
        firestore.collection(COLLECTION_BOSSES)
                .document(boss.getId())
                .set(bossToMap(boss))
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void updateBoss(Boss boss, OnResult<Void> callback) {
        firestore.collection(COLLECTION_BOSSES)
                .document(boss.getId())
                .set(bossToMap(boss))
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void deleteBoss(String bossId, OnResult<Void> callback) {
        firestore.collection(COLLECTION_BOSSES)
                .document(bossId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    public void getBossesForUser(String userUid, OnResult<List<Boss>> callback) {
        firestore.collection(COLLECTION_BOSSES)
                .whereEqualTo(FIELD_BOSS_USER_UID, userUid)
                .orderBy(FIELD_BOSS_LEVEL, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Boss> bosses = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        bosses.add(documentToBoss(doc));
                    }
                    if (callback != null) callback.onSuccess(bosses);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    private Map<String, Object> bossToMap(Boss boss) {
        Map<String, Object> map = new HashMap<>();
        map.put(FIELD_BOSS_USER_UID,     boss.getUserUid());
        map.put(FIELD_BOSS_LEVEL,   boss.getBossLevel());
        map.put(FIELD_BOSS_MAX_HP,       boss.getMaxHp());
        map.put(FIELD_BOSS_CURRENT_HP,   boss.getCurrentHp());
        map.put(FIELD_BOSS_ATTACKS_LEFT, boss.getAttacksLeft());
        return map;
    }

    private Boss documentToBoss(DocumentSnapshot doc) {
        Boss boss = new Boss();
        boss.setId(doc.getId());
        boss.setUserUid(doc.getString(FIELD_BOSS_USER_UID));
        boss.setBossLevel(doc.getLong(FIELD_BOSS_LEVEL) != null
                ? doc.getLong(FIELD_BOSS_LEVEL).intValue() : 0);
        boss.setMaxHp(doc.getLong(FIELD_BOSS_MAX_HP) != null
                ? doc.getLong(FIELD_BOSS_MAX_HP).intValue() : 0);
        boss.setCurrentHp(doc.getLong(FIELD_BOSS_CURRENT_HP) != null
                ? doc.getLong(FIELD_BOSS_CURRENT_HP).intValue() : 0);
        boss.setAttacksLeft(doc.getLong(FIELD_BOSS_ATTACKS_LEFT) != null
                ? doc.getLong(FIELD_BOSS_ATTACKS_LEFT).intValue() : 0);
        return boss;
    }

}