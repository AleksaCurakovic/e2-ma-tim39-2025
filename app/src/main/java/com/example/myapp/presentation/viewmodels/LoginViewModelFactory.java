package com.example.myapp.presentation.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.domain.services.AuthManager;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final AuthManager authManager;

    public LoginViewModelFactory(Context context) {
        this.authManager = new AuthManager(context.getApplicationContext());
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(authManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}