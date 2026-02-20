package com.example.myapp.presentation.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BossViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public BossViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BossViewModel.class)) {
            return (T) new BossViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass);
    }
}