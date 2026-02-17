package com.example.myapp.presentation.viewmodels;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.services.AuthManager;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";

    private final AuthManager authManager;

    // LiveData za UI stanja
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);
    // LiveData za čuvanje stanja forme kroz rotaciju
    public final MutableLiveData<String> emailInput = new MutableLiveData<>("");

    public LoginViewModel(AuthManager authManager) {
        this.authManager = authManager;
    }

    public void login(String email, String password) {

        // ── Lokalna validacija ──
        if (email.isEmpty()) {
            errorMessage.setValue("EMAIL_EMPTY");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("EMAIL_INVALID");
            return;
        }
        if (password.isEmpty()) {
            errorMessage.setValue("PASSWORD_EMPTY");
            return;
        }

        // Sačuvaj email za rotaciju
        emailInput.setValue(email);

        isLoading.setValue(true);

        authManager.login(email, password, new OnResult<User>() {
            @Override
            public void onSuccess(User user) {
                isLoading.setValue(false);
                loginSuccess.setValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.setValue(false);
                // AuthManager uvek šalje konzistentne poruke
                if (e.getMessage().equals("EMAIL_NOT_VERIFIED")) {
                    errorMessage.setValue("EMAIL_NOT_VERIFED");
                }else if(e instanceof FirebaseAuthInvalidUserException)
                    errorMessage.setValue("INVALID_USER");
                else if(e instanceof FirebaseAuthInvalidCredentialsException)
                    errorMessage.setValue("INVALID_CREDENTIALS");
                else if (e instanceof FirebaseNetworkException)
                    errorMessage.setValue("NO_CONNECTION");
                else {
                    errorMessage.setValue(e.getMessage());
                }
            }
        });
    }

    public void resendVerificationEmail() {
        authManager.resendVerificationEmail(new OnResult<Void>() {
            @Override
            public void onSuccess(Void result) {
                errorMessage.setValue("VERIFICATION_EMAIL_SENT");
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
}