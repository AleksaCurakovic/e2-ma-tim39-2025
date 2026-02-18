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


    //TABELA: CATEGORIES
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_USER_UID = "user_uid";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_COLOR = "color";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ─────────────────────────────────────────
    // KREIRANJE TABELA
    // ─────────────────────────────────────────
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
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
                        COLUMN_EQUIPPED_ITEMS + " TEXT DEFAULT ''" +
                        ")";
        db.execSQL(sql);
        Log.d(TAG, "Table created: " + TABLE_USERS);
    }

}