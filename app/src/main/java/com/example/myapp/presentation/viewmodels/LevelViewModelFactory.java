package com.example.myapp.presentation.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class LevelViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public LevelViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LevelViewModel.class)) {
            return (T) new LevelViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass);
    }
}