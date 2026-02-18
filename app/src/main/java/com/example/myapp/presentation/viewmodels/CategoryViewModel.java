package com.example.myapp.presentation.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.repositories.CategoryRepository;
import com.example.myapp.data.repositories.TaskRepository;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<String> successMessage = new MutableLiveData<>();

    public CategoryViewModel(Context context) {
        categoryRepository = new CategoryRepository(context);
        taskRepository = new TaskRepository(context);
    }

    // ─────────────────────────────────────────
    // UČITAVANJE
    // ─────────────────────────────────────────

    public void loadCategories(String userUid) {
        List<Category> local = categoryRepository.getCategoriesLocally(userUid);
        categories.setValue(local);
    }

    // ─────────────────────────────────────────
    // KREIRANJE
    // ─────────────────────────────────────────

    public void createCategory(String userUid, String name, String color) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("NAME_EMPTY");
            return;
        }
        if (color == null || color.isEmpty()) {
            errorMessage.setValue("COLOR_EMPTY");
            return;
        }

        isLoading.setValue(true);
        categoryRepository.createCategory(userUid, name.trim(), color, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                isLoading.postValue(false);
                successMessage.postValue("Kategorija kreirana!");
                loadCategories(userUid);
            }
            @Override public void onFailure(Exception e) {
                isLoading.postValue(false);
                if ("COLOR_TAKEN".equals(e.getMessage())) {
                    errorMessage.postValue("COLOR_TAKEN");
                } else {
                    errorMessage.postValue("CREATE_FAILED");
                }
            }
        });
    }

    // ─────────────────────────────────────────
    // PROMENA BOJE
    // ─────────────────────────────────────────

    public void updateColor(String userUid, Category category, String newColor) {
        if (newColor == null || newColor.isEmpty()) {
            errorMessage.setValue("COLOR_EMPTY");
            return;
        }

        isLoading.setValue(true);
        categoryRepository.updateCategoryColor(category, newColor, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                isLoading.postValue(false);
                successMessage.postValue("Boja promenjena!");
                loadCategories(userUid);
            }
            @Override public void onFailure(Exception e) {
                isLoading.postValue(false);
                if ("COLOR_TAKEN".equals(e.getMessage())) {
                    errorMessage.postValue("COLOR_TAKEN");
                } else {
                    errorMessage.postValue("UPDATE_FAILED");
                }
            }
        });
    }

    // ─────────────────────────────────────────
    // BRISANJE
    // ─────────────────────────────────────────

    public void deleteCategory(String userUid, Category category) {
        isLoading.setValue(true);
        List<Task> allTasks = taskRepository.getAllTasks(userUid);

        categoryRepository.deleteCategory(category, allTasks, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                isLoading.postValue(false);
                successMessage.postValue("Kategorija obrisana!");
                loadCategories(userUid);
            }
            @Override public void onFailure(Exception e) {
                isLoading.postValue(false);
                if ("HAS_ACTIVE_TASKS".equals(e.getMessage())) {
                    errorMessage.postValue("HAS_ACTIVE_TASKS");
                } else {
                    errorMessage.postValue("DELETE_FAILED");
                }
            }
        });
    }
}