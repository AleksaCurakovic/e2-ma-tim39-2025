package com.example.myapp.data.repositories;

import android.content.Context;
import android.util.Log;

import com.example.myapp.data.datasource.local.LocalDataSource;
import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.datasource.remote.RemoteDataSource;
import com.example.myapp.domain.models.User;

public class UserRepository {

    private static final String TAG = "UserRepository";

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    public UserRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
    }

    // ─────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────

    public void saveUser(User user, OnResult<Void> callback) {
        // Sačuvaj lokalno - sinhrono, odmah
        localDataSource.saveUser(user);
        Log.d(TAG, "User saved locally: " + user.getUid());

        // Sačuvaj na cloud - asinhrono
        remoteDataSource.saveUser(user, new OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "User saved to cloud: " + user.getUid());
                if (callback != null) callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to save user to cloud: " + user.getUid(), e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────

    // Brzo čitanje - samo lokalno, bez interneta
    public User getUserLocally(String uid) {
        User user = localDataSource.getUser(uid);
        if (user != null) {
            Log.d(TAG, "User fetched locally: " + uid);
        } else {
            Log.w(TAG, "User not found locally: " + uid);
        }
        return user;
    }

    // Povlači sa clouda i sinhronizuje lokalno
    public void fetchFromCloud(String uid, OnResult<User> callback) {
        remoteDataSource.getUser(uid, new OnResult<User>() {
            @Override
            public void onSuccess(User user) {
                // Sinhronizuj lokalno
                localDataSource.saveUser(user);
                Log.d(TAG, "User fetched from cloud and synced locally: " + uid);
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to fetch user from cloud: " + uid, e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────

    public void updateUser(User user, OnResult<Void> callback) {
        // Ažuriraj lokalno odmah
        localDataSource.updateUser(user);
        Log.d(TAG, "User updated locally: " + user.getUid());

        // Ažuriraj na cloudu
        remoteDataSource.updateUser(user, new OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "User updated on cloud: " + user.getUid());
                if (callback != null) callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update user on cloud: " + user.getUid(), e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────

    public void deleteUser(String uid, OnResult<Void> callback) {
        // Obriši lokalno odmah
        localDataSource.deleteUser(uid);
        Log.d(TAG, "User deleted locally: " + uid);

        // Obriši na cloudu
        remoteDataSource.deleteUser(uid, new OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "User deleted from cloud: " + uid);
                if (callback != null) callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to delete user from cloud: " + uid, e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // CLEAR LOCAL (za logout)
    // ─────────────────────────────────────────

    public void clearLocalData() {
        localDataSource.clearAllUsers();
        Log.d(TAG, "Local data cleared");
    }
}