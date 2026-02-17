package com.example.myapp.presentation.viewmodels;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.domain.services.AuthManager;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterViewModel extends ViewModel {

    private static final String TAG = "RegisterViewModel";

    private final AuthManager authManager;

    // LiveData za UI stanja
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>(false);

    // LiveData za čuvanje stanja forme kroz rotaciju
    public final MutableLiveData<String> emailInput = new MutableLiveData<>("");
    public final MutableLiveData<String> usernameInput = new MutableLiveData<>("");
    public final MutableLiveData<String> selectedAvatar = new MutableLiveData<>("🧑");

    public RegisterViewModel(AuthManager authManager) {
        this.authManager = authManager;
    }

    public void register(String email, String username, String password,
                         String confirmPassword, String avatar) {

        // Validacija
        if (email.isEmpty()) {
            errorMessage.setValue("EMAIL_EMPTY");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("EMAIL_INVALID");
            return;
        }

        if (username.isEmpty()) {
            errorMessage.setValue("USERNAME_EMPTY");
            return;
        }

        if (password.isEmpty()) {
            errorMessage.setValue("PASSWORD_EMPTY");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("PASSWORDS_DONT_MATCH");
            return;
        }

        // Sačuvaj stanje forme
        emailInput.setValue(email);
        usernameInput.setValue(username);
        selectedAvatar.setValue(avatar);

        // Pokreni registraciju
        isLoading.setValue(true);

        authManager.register(email, password, username, avatar, new OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                registerSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                if (e instanceof FirebaseAuthUserCollisionException) {
                    errorMessage.setValue("EMAIL_IN_USE");
                }else if (e instanceof FirebaseNetworkException)
                    errorMessage.setValue("NO_CONNECTION");
                else {
                    errorMessage.setValue("UNKNOWN_ERROR");
                }
            }
        });
    }
}