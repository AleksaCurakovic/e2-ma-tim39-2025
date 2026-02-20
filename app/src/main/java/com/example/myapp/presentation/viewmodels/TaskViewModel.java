package com.example.myapp.presentation.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.repositories.BossRepository;
import com.example.myapp.data.repositories.CategoryRepository;
import com.example.myapp.data.repositories.TaskRepository;
import com.example.myapp.data.repositories.UserRepository;
import com.example.myapp.domain.models.Category;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.models.User;

import java.util.List;

public class TaskViewModel extends ViewModel {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final BossRepository bossRepository;

    public MutableLiveData<List<Task>>     tasks          = new MutableLiveData<>();
    public MutableLiveData<List<Category>> categories     = new MutableLiveData<>();
    public MutableLiveData<Task>           selectedTask   = new MutableLiveData<>();
    public MutableLiveData<Boolean>        isLoading      = new MutableLiveData<>(false);
    public MutableLiveData<String>         errorMessage   = new MutableLiveData<>();
    public MutableLiveData<String>         successMessage = new MutableLiveData<>();
    public MutableLiveData<Integer>        xpEarned       = new MutableLiveData<>();

    public MutableLiveData<UserRepository.LevelUpResult> levelUpOccurred = new MutableLiveData<>();

    public TaskViewModel(Context context) {
        taskRepository     = new TaskRepository(context);
        userRepository     = new UserRepository(context);
        categoryRepository = new CategoryRepository(context);
        bossRepository = new BossRepository(context);
    }

    // ─────────────────────────────────────────
    // UČITAVANJE
    // ─────────────────────────────────────────

    // Za kalendar — svi zadaci uključujući prošle
    public void loadAllTasks(String userUid) {
        tasks.setValue(taskRepository.getAllTasks(userUid));
    }

    // Za listu
    public void loadTaskList(String userUid) {
        tasks.setValue(taskRepository.getTasksForList(userUid));
    }

    public void loadCategories(String userUid) {
        categories.setValue(categoryRepository.getCategoriesLocally(userUid));
    }

    public void loadTaskById(String taskId) {
        selectedTask.setValue(taskRepository.getTask(taskId));
    }

    // ─────────────────────────────────────────
    // KREIRANJE
    // ─────────────────────────────────────────

    public void createTask(Task task, String userUid) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            errorMessage.setValue("TITLE_EMPTY");
            return;
        }
        if (task.getScheduledTime() == 0) {
            errorMessage.setValue("TIME_EMPTY");
            return;
        }

        isLoading.setValue(true);
        User user = userRepository.getUserLocally(userUid);
        int level = user != null ? user.getLevel() : 0;

        taskRepository.createTask(task, level, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                isLoading.postValue(false);
                successMessage.postValue("Zadatak kreiran!");
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("CREATE_FAILED");
            }
        });
    }

    // ─────────────────────────────────────────
    // IZMENA
    // ─────────────────────────────────────────

    public void updateTask(Task task, String userUid) {
        isLoading.setValue(true);
        taskRepository.updateTask(task, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                isLoading.postValue(false);
                successMessage.postValue("Zadatak ažuriran!");
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("TASK_NOT_EDITABLE");
            }
        });
    }

    // ─────────────────────────────────────────
    // BRISANJE
    // ─────────────────────────────────────────

    public void deleteTask(Task task, String userUid) {
        taskRepository.deleteTask(task, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                successMessage.postValue("Zadatak obrisan!");
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                errorMessage.postValue("TASK_NOT_DELETABLE");
            }
        });
    }

    // ─────────────────────────────────────────
    // OZNAČAVANJE
    // ─────────────────────────────────────────

    public void markDone(Task task, String userUid) {
        int xp = taskRepository.markDone(task, userUid, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                if ("TASK_IN_FUTURE".equals(e.getMessage())) {
                    errorMessage.postValue("TASK_IN_FUTURE");
                } else if ("TASK_EXPIRED".equals(e.getMessage())) {
                    errorMessage.postValue("TASK_EXPIRED");
                }
            }
        });

        if (xp > 0) {
            xpEarned.setValue(xp);
            addXpToUser(userUid, xp);
        }
    }

    public void markCancelled(Task task, String userUid) {
        taskRepository.markCancelled(task, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                String msg = e.getMessage();
                if ("TASK_NOT_ACTIVE".equals(msg)) {
                    errorMessage.postValue("TASK_NOT_ACTIVE");
                } else {
                    errorMessage.postValue("ACTION_FAILED");
                }
            }
        });
    }

    public void markPaused(Task task, String userUid) {
        taskRepository.markPaused(task, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                String msg = e.getMessage();
                if ("TASK_NOT_REPEATING".equals(msg)) {
                    errorMessage.postValue("TASK_NOT_REPEATING");
                } else if ("TASK_NOT_ACTIVE".equals(msg)) {
                    errorMessage.postValue("TASK_NOT_ACTIVE");
                } else {
                    errorMessage.postValue("ACTION_FAILED");
                }
            }
        });
    }

    public void markActive(Task task, String userUid) {
        taskRepository.markActive(task, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                loadAllTasks(userUid);
            }
            @Override public void onFailure(Exception e) {
                String msg = e.getMessage();
                if ("TASK_NOT_PAUSED".equals(msg)) {
                    errorMessage.postValue("TASK_NOT_PAUSED");
                } else {
                    errorMessage.postValue("ACTION_FAILED");
                }
            }
        });
    }

    // ─────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────

    private void addXpToUser(String userUid, int xp) {
        User user = userRepository.getUserLocally(userUid);
        if (user != null) {
            user.setXp(user.getXp() + xp);
            userRepository.updateUser(user, new OnResult<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d("TaskViewModel", "XP updated: +" + xp);
                    userRepository.checkAndApplyLevelUp(userUid,
                            new OnResult<UserRepository.LevelUpResult>() {
                                @Override
                                public void onSuccess(UserRepository.LevelUpResult result) {
                                    if (result.leveledUp) {
                                        bossRepository.createBossForLevel(
                                                userUid,
                                                result.newLevel,
                                                new OnResult<Void>() {
                                                    @Override
                                                    public void onSuccess(Void r) {
                                                        Log.d("TaskViewModel",
                                                                "Boss created for level: "
                                                                        + result.newLevel);
                                                    }
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        Log.e("TaskViewModel",
                                                                "Failed to create boss", e);
                                                    }
                                                });
                                        levelUpOccurred.postValue(result);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("TaskViewModel", "Level up check failed", e);
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("TaskViewModel", "Failed to update XP", e);
                }
            });
        }
    }
}