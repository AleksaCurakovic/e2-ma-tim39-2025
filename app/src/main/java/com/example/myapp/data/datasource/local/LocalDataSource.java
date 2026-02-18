package com.example.myapp.data.datasource.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myapp.domain.models.User;

import java.util.ArrayList;
import java.util.Arrays;
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
        return user;
    }

}