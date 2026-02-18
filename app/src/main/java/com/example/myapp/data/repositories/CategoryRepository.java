package com.example.myapp.data.repositories;

import android.content.Context;
import android.util.Log;

import com.example.myapp.data.datasource.local.LocalDataSource;
import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.datasource.remote.RemoteDataSource;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;

import java.util.List;
import java.util.UUID;

public class CategoryRepository {

    private static final String TAG = "CategoryRepository";

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    public CategoryRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
    }

    // ─────────────────────────────────────────
    // KREIRANJE
    // ─────────────────────────────────────────

    public void createCategory(String userUid, String name, String color,
                               OnResult<Void> callback) {
        // Proveri da li je boja zauzeta
        if (localDataSource.isColorTaken(userUid, color)) {
            if (callback != null)
                callback.onFailure(new Exception("COLOR_TAKEN"));
            return;
        }

        Category category = new Category(
                UUID.randomUUID().toString(),
                userUid,
                name,
                color
        );

        boolean saved = localDataSource.saveCategory(category);
        if (!saved) {
            if (callback != null)
                callback.onFailure(new Exception("COLOR_TAKEN"));
            return;
        }

        remoteDataSource.saveCategory(category, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Category saved to cloud: " + category.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to save category to cloud", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // ČITANJE
    // ─────────────────────────────────────────

    public List<Category> getCategoriesLocally(String userUid) {
        return localDataSource.getCategoriesForUser(userUid);
    }

    public void syncFromCloud(String userUid, OnResult<List<Category>> callback) {
        remoteDataSource.getCategoriesForUser(userUid, new OnResult<List<Category>>() {
            @Override public void onSuccess(List<Category> categories) {
                localDataSource.clearCategoriesForUser(userUid);
                for (Category c : categories) localDataSource.saveCategory(c);
                if (callback != null) callback.onSuccess(categories);
            }
            @Override public void onFailure(Exception e) {
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // PROMENA BOJE
    // ─────────────────────────────────────────

    public void updateCategoryColor(Category category, String newColor,
                                    OnResult<Void> callback) {
        // Proveri da li je nova boja zauzeta od strane druge kategorije
        if (localDataSource.isColorTaken(category.getUserUid(), newColor)) {
            if (callback != null)
                callback.onFailure(new Exception("COLOR_TAKEN"));
            return;
        }

        category.setColor(newColor);
        localDataSource.updateCategory(category);

        remoteDataSource.updateCategory(category, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Category color updated: " + category.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update category color", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // BRISANJE
    // ─────────────────────────────────────────

    // Brisanje je dozvoljeno samo ako nema aktivnih zadataka u toj kategoriji.
    // Lista svih zadataka se prosleđuje jer CategoryRepository
    // ne zavisi od TaskRepository — izbegavamo kružnu zavisnost.
    public void deleteCategory(Category category, List<Task> allUserTasks,
                               OnResult<Void> callback) {
        boolean hasActiveTasks = false;
        for (Task t : allUserTasks) {
            if (category.getId().equals(t.getCategoryId())
                    && Task.STATUS_ACTIVE.equals(t.getStatus())) {
                hasActiveTasks = true;
                break;
            }
        }

        if (hasActiveTasks) {
            if (callback != null)
                callback.onFailure(new Exception("HAS_ACTIVE_TASKS"));
            return;
        }

        localDataSource.deleteCategory(category.getId());

        remoteDataSource.deleteCategory(category, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Category deleted: " + category.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to delete category from cloud", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }
}