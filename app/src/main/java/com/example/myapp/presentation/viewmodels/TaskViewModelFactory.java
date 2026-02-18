package com.example.myapp.presentation.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TaskViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public TaskViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(TaskViewModel.class)) {
            return (T) new TaskViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel: " + modelClass);
    }
}