package com.example.myapp.data.datasource.remote;

import android.util.Log;

import com.example.myapp.domain.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RemoteDataSource {

    private static final String TAG = "RemoteDataSource";

    // Firestore kolekcije
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_CATEGORIES = "categories";
    // private static final String COLLECTION_BADGES = "badges";
    // private static final String COLLECTION_EQUIPMENT = "equipment";

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

    // ─────────────────────────────────────────
    // POLJA — CATEGORY
    // ─────────────────────────────────────────
    private static final String FIELD_CATEGORY_NAME = "name";
    private static final String FIELD_CATEGORY_COLOR = "color";
    private static final String FIELD_CATEGORY_USER_UID = "userUid";


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
        map.put("equippedItemIds", user.getEquippedItemIds() != null
                ? user.getEquippedItemIds() : new ArrayList<>());
        return map;
    }


}