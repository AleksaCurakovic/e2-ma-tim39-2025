package com.example.myapp.domain.models;

public class Task {

    // ─── Status konstante ───
    public static final String STATUS_ACTIVE    = "active";
    public static final String STATUS_DONE      = "done";
    public static final String STATUS_UNDONE    = "undone";
    public static final String STATUS_PAUSED    = "paused";
    public static final String STATUS_CANCELLED = "cancelled";

    // ─── Težina ───
    public static final String DIFFICULTY_VERY_EASY = "very_easy";  // 1 XP
    public static final String DIFFICULTY_EASY      = "easy";       // 3 XP
    public static final String DIFFICULTY_HARD      = "hard";       // 7 XP
    public static final String DIFFICULTY_EXTREME   = "extreme";    // 20 XP

    // ─── Bitnost ───
    public static final String IMPORTANCE_NORMAL    = "normal";     // 1 XP
    public static final String IMPORTANCE_IMPORTANT = "important";  // 3 XP
    public static final String IMPORTANCE_EXTREME   = "extreme_imp";// 10 XP
    public static final String IMPORTANCE_SPECIAL   = "special";    // 100 XP

    // ─── Ponavljanje ───
    public static final String REPEAT_NONE   = "none";
    public static final String REPEAT_DAILY  = "daily";
    public static final String REPEAT_WEEKLY = "weekly";

    private String id;
    private String userUid;
    private String title;
    private String description;
    private String categoryId;
    private String status;
    private String difficulty;
    private String importance;
    private int xpValue;
    private long scheduledTime;
    private long createdAt;
    private String repeatType;
    private int repeatInterval;
    private long repeatStartDate;
    private long repeatEndDate;
    private String parentTaskId;
    private String recurrenceGroupId;
    private long completedAt;
    private boolean violatedQuota;

    public Task() {}

    public Task(String id, String userUid, String title, String description,
                String categoryId, String status, String difficulty, String importance,
                int xpValue, long scheduledTime, long createdAt,
                String repeatType, int repeatInterval,
                long repeatStartDate, long repeatEndDate,
                String parentTaskId, String recurrenceGroupId, long completedAt, boolean violatedQuota) {
        this.id = id;
        this.userUid = userUid;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.status = status;
        this.difficulty = difficulty;
        this.importance = importance;
        this.xpValue = xpValue;
        this.scheduledTime = scheduledTime;
        this.createdAt = createdAt;
        this.repeatType = repeatType;
        this.repeatInterval = repeatInterval;
        this.repeatStartDate = repeatStartDate;
        this.repeatEndDate = repeatEndDate;
        this.parentTaskId = parentTaskId;
        this.recurrenceGroupId = recurrenceGroupId;
        this.completedAt = completedAt;
        this.violatedQuota = violatedQuota;
    }

    // ─── Getters & Setters ───

    public boolean isViolatedQuota() { return violatedQuota; }
    public void setViolatedQuota(boolean violatedQuota) {
        this.violatedQuota = violatedQuota;
    }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }

    public int getXpValue() { return xpValue; }
    public void setXpValue(int xpValue) { this.xpValue = xpValue; }

    public long getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(long scheduledTime) { this.scheduledTime = scheduledTime; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getRepeatType() { return repeatType; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }

    public int getRepeatInterval() { return repeatInterval; }
    public void setRepeatInterval(int repeatInterval) { this.repeatInterval = repeatInterval; }

    public long getRepeatStartDate() { return repeatStartDate; }
    public void setRepeatStartDate(long repeatStartDate) { this.repeatStartDate = repeatStartDate; }

    public long getRepeatEndDate() { return repeatEndDate; }
    public void setRepeatEndDate(long repeatEndDate) { this.repeatEndDate = repeatEndDate; }

    public String getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(String parentTaskId) { this.parentTaskId = parentTaskId; }

    public String getRecurrenceGroupId() { return recurrenceGroupId; }
    public void setRecurrenceGroupId(String recurrenceGroupId) { this.recurrenceGroupId = recurrenceGroupId; }

    // ─── Helper metode ───

    public boolean isRepeating() {
        return repeatType != null && !repeatType.equals(REPEAT_NONE);
    }

    public boolean isEditable() {
        return status.equals(STATUS_ACTIVE) || status.equals(STATUS_PAUSED);
    }

    public boolean isDeletable() {
        return status.equals(STATUS_ACTIVE) || status.equals(STATUS_PAUSED);
    }

    public boolean canBeMarked() {
        return status.equals(STATUS_ACTIVE);
    }
}