package com.example.myapp.presentation.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.repositories.TaskRepository;
import com.example.myapp.data.repositories.CategoryRepository;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.UserStats;
import com.example.myapp.domain.utils.StatsCalculator;

import java.util.List;

public class UserStatsViewModel extends ViewModel {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public MutableLiveData<UserStats> stats     = new MutableLiveData<>();
    public MutableLiveData<Boolean>   isLoading = new MutableLiveData<>(false);

    public UserStatsViewModel(Context context) {
        taskRepository     = new TaskRepository(context);
        categoryRepository = new CategoryRepository(context);
    }

    public void loadStats(String userUid) {
        isLoading.setValue(true);

        List<Task> allTasks        = taskRepository.getTasksForCalendar(userUid);
        List<Category> categories  = categoryRepository.getCategoriesLocally(userUid);

        UserStats computed = StatsCalculator.calculate(allTasks, categories);
        stats.setValue(computed);
        isLoading.setValue(false);
    }
}