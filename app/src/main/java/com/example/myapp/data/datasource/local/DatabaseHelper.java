package com.example.myapp.data.datasource.local;

import com.example.myapp.domain.models.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 1;

    // ─────────────────────────────────────────
    // TABELA: USERS
    // ─────────────────────────────────────────
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POWER_POINTS = "power_points";
    public static final String COLUMN_XP = "xp";
    public static final String COLUMN_COINS = "coins";
    public static final String COLUMN_BADGES = "badges";
    public static final String COLUMN_EQUIPPED_ITEMS = "equipped_items";
    public static final String COLUMN_LEVEL_START_TS = "level_start_timestamp";

    public static final String COLUMN_LEVEL_END_TS = "level_end_timestamp";
    public static final String COLUMN_BOSS_DEFEATED = "boss_defeated";


    //TABELA: CATEGORIES
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_USER_UID = "user_uid";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_COLOR = "color";

    // ─── TASKS kolone ───
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_USER_UID = "user_uid";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_DESCRIPTION = "description";
    public static final String COLUMN_TASK_CATEGORY_ID = "category_id";
    public static final String COLUMN_TASK_STATUS = "status";
    public static final String COLUMN_TASK_DIFFICULTY = "difficulty";
    public static final String COLUMN_TASK_IMPORTANCE = "importance";
    public static final String COLUMN_TASK_XP_VALUE = "xp_value";
    public static final String COLUMN_TASK_SCHEDULED_TIME = "scheduled_time";
    public static final String COLUMN_TASK_CREATED_AT = "created_at";
    public static final String COLUMN_TASK_REPEAT_TYPE  = "repeat_type";
    public static final String COLUMN_TASK_REPEAT_INTERVAL = "repeat_interval";
    public static final String COLUMN_TASK_REPEAT_START = "repeat_start";
    public static final String COLUMN_TASK_REPEAT_END = "repeat_end";
    public static final String COLUMN_TASK_PARENT_ID  = "parent_task_id";
    public static final String COLUMN_TASK_RECURRENCE_GROUP = "recurrence_group_id";
    public static final String COLUMN_TASK_COMPLETED_AT = "completed_at";

    public static final String COLUMN_TASK_VIOLATED_QUOTA = "counted_for_xp";


    // ─── Boss tabela ───
    public static final String TABLE_BOSSES            = "bosses";
    public static final String COLUMN_BOSS_ID          = "id";
    public static final String COLUMN_BOSS_USER_UID    = "user_uid";
    public static final String COLUMN_BOSS_LEVEL       = "boss_level";
    public static final String COLUMN_BOSS_MAX_HP      = "max_hp";
    public static final String COLUMN_BOSS_CURRENT_HP  = "current_hp";
    public static final String COLUMN_BOSS_ATTACKS_LEFT = "attacks_left";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ─────────────────────────────────────────
    // KREIRANJE TABELA
    // ─────────────────────────────────────────
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createCategoryTable(db);
        createTaskTable(db);
        createBossTable(db);
        Log.d(TAG, "Database created - version: " + DATABASE_VERSION);
    }

    // ─────────────────────────────────────────
    // MIGRACIJE
    // ─────────────────────────────────────────
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    }

    // ─────────────────────────────────────────
    // CREATE TABLE METODE
    // ─────────────────────────────────────────
    private void createUsersTable(SQLiteDatabase db) {
        String sql =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COLUMN_UID           + " TEXT PRIMARY KEY, " +
                        COLUMN_EMAIL         + " TEXT, " +
                        COLUMN_USERNAME      + " TEXT, " +
                        COLUMN_AVATAR        + " TEXT, " +
                        COLUMN_LEVEL         + " INTEGER DEFAULT 0, " +
                        COLUMN_TITLE         + " TEXT, " +
                        COLUMN_POWER_POINTS  + " INTEGER DEFAULT 0, " +
                        COLUMN_XP            + " INTEGER DEFAULT 0, " +
                        COLUMN_COINS         + " INTEGER DEFAULT 0, " +
                        COLUMN_BADGES        + " TEXT DEFAULT '', " +
                        COLUMN_EQUIPPED_ITEMS + " TEXT DEFAULT ''," +
                        COLUMN_LEVEL_START_TS + " INTEGER DEFAULT 0, " +
                        COLUMN_LEVEL_END_TS + " INTEGER DEFAULT 0, " +
                        COLUMN_BOSS_DEFEATED + "INTEGER DEFAULT 0" + ")";
        db.execSQL(sql);
        Log.d(TAG, "Table created: " + TABLE_USERS);
    }

    private void createCategoryTable(SQLiteDatabase db){
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " ("
                + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CATEGORY_USER_UID + " TEXT NOT NULL, "
                + COLUMN_CATEGORY_NAME + " TEXT NOT NULL, "
                + COLUMN_CATEGORY_COLOR + " TEXT"
                + ");";

        db.execSQL(sql);
        Log.d(TAG, "Table created: " + TABLE_CATEGORIES);
    }

    private void createTaskTable(SQLiteDatabase db){
        String sql =
                "CREATE TABLE " + TABLE_TASKS + " (" +
                        COLUMN_TASK_ID               + " TEXT PRIMARY KEY, " +
                        COLUMN_TASK_USER_UID         + " TEXT NOT NULL, " +
                        COLUMN_TASK_TITLE            + " TEXT NOT NULL, " +
                        COLUMN_TASK_DESCRIPTION      + " TEXT, " +
                        COLUMN_TASK_CATEGORY_ID      + " TEXT, " +
                        COLUMN_TASK_STATUS           + " TEXT NOT NULL DEFAULT 'active', " +
                        COLUMN_TASK_DIFFICULTY       + " TEXT NOT NULL, " +
                        COLUMN_TASK_IMPORTANCE       + " TEXT NOT NULL, " +
                        COLUMN_TASK_XP_VALUE         + " INTEGER DEFAULT 0, " +
                        COLUMN_TASK_SCHEDULED_TIME   + " INTEGER NOT NULL, " +
                        COLUMN_TASK_CREATED_AT       + " INTEGER NOT NULL, " +
                        COLUMN_TASK_REPEAT_TYPE      + " TEXT DEFAULT 'none', " +
                        COLUMN_TASK_REPEAT_INTERVAL  + " INTEGER DEFAULT 1, " +
                        COLUMN_TASK_REPEAT_START     + " INTEGER DEFAULT 0, " +
                        COLUMN_TASK_REPEAT_END       + " INTEGER DEFAULT 0, " +
                        COLUMN_TASK_PARENT_ID        + " TEXT, " +
                        COLUMN_TASK_RECURRENCE_GROUP + " TEXT," +
                        COLUMN_TASK_VIOLATED_QUOTA + " INTEGER DEFAULT 0," +
                        COLUMN_TASK_COMPLETED_AT + " INTEGER DEFAULT 0" +
                        ")";
        db.execSQL(sql);
        Log.d(TAG, "Table created: " + TABLE_TASKS);
    }

    private void createBossTable(SQLiteDatabase db){
        String sql =
                "CREATE TABLE " + TABLE_BOSSES + " (" +
                        COLUMN_BOSS_ID           + " TEXT PRIMARY KEY, " +
                        COLUMN_BOSS_USER_UID     + " TEXT NOT NULL, " +
                        COLUMN_BOSS_LEVEL        + " INTEGER NOT NULL, " +
                        COLUMN_BOSS_MAX_HP       + " INTEGER NOT NULL, " +
                        COLUMN_BOSS_CURRENT_HP   + " INTEGER NOT NULL, " +
                        COLUMN_BOSS_ATTACKS_LEFT + " INTEGER NOT NULL " +
                        ")";
        db.execSQL(sql);
        Log.d(TAG, "Table created: " + TABLE_BOSSES);
    }

}