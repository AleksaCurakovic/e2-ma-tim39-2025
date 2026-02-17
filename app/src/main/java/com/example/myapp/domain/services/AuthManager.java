package com.example.myapp.domain.services;

import android.content.Context;
import android.util.Log;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.repositories.UserRepository;
import com.example.myapp.domain.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class AuthManager {

    private static final String TAG = "AuthManager";

    private final FirebaseAuth auth;
    private final UserRepository userRepository;

    public AuthManager(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.userRepository = new UserRepository(context);
    }

    // ─────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────

    public void register(String email, String password, String username, String avatar, OnResult<Void> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        Log.d(TAG, "Firebase user created: " + firebaseUser.getUid());

                        sendVerificationEmail(firebaseUser);

                        User user = new User(
                                firebaseUser.getUid(),
                                email,
                                username,
                                avatar,
                                1,
                                "Novak",
                                10,
                                0,
                                100,
                                new ArrayList<>()
                        );

                        auth.signOut();

                        userRepository.saveUser(user, new OnResult<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.d(TAG, "User saved successfully");
                                if (callback != null) callback.onSuccess(null);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to save user", e);
                                // Auth korisnik je kreiran ali čuvanje nije uspelo
                                // Svejedno vrati success jer Firebase Auth je OK
                                if (callback != null) callback.onSuccess(null);
                            }
                        });

                    } else {
                        Log.e(TAG, "Failed to create Firebase user", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    // ─────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────

    public void login(String email, String password, OnResult<User> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (!firebaseUser.isEmailVerified()) {
                            Log.w(TAG, "Email not verified: " + firebaseUser.getEmail());
                            auth.signOut();
                            if (callback != null) callback.onFailure(new Exception("EMAIL_NOT_VERIFIED"));
                            return;
                        }

                        // Email verifikovan - povuci podatke sa clouda
                        userRepository.fetchFromCloud(firebaseUser.getUid(), new OnResult<User>() {
                            @Override
                            public void onSuccess(User user) {
                                Log.d(TAG, "Login successful: " + user.getUid());
                                if (callback != null) callback.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to fetch user after login", e);
                                if (callback != null) callback.onFailure(e);
                            }
                        });

                    } else {
                        Log.e(TAG, "Login failed", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    // ─────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────

    public void logout() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";
        userRepository.clearLocalData();
        auth.signOut();
        Log.d(TAG, "User logged out: " + uid);
    }

    // ─────────────────────────────────────────
    // AUTH STATUS
    // ─────────────────────────────────────────

    // Vraca FirebaseUser ako postoji validan auth token
    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    // Proverava da li je korisnik ulogovan i verifikovan
    public boolean isLoggedIn() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    // ─────────────────────────────────────────
    // VERIFICATION EMAIL
    // ─────────────────────────────────────────

    public void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Verification email sent to: " + user.getEmail());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send verification email", e);
                });
    }

    public void resendVerificationEmail(OnResult<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure(new Exception("No user logged in"));
            return;
        }

        user.sendEmailVerification()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Verification email resent to: " + user.getEmail());
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to resend verification email", e);
                    if (callback != null) callback.onFailure(e);
                });
    }
}