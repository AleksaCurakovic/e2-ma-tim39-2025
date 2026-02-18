package com.example.myapp.data.repositories;

import android.content.Context;
import android.util.Log;

import com.example.myapp.data.datasource.local.LocalDataSource;
import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.datasource.remote.RemoteDataSource;
import com.example.myapp.domain.models.Task;
import com.example.myapp.domain.utils.XpCalculator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class TaskRepository {

    private static final String TAG = "TaskRepository";

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    public TaskRepository(Context context) {
        this.localDataSource  = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
    }

    // ─────────────────────────────────────────
    // KREIRANJE
    // ─────────────────────────────────────────

    public void createTask(Task task, int userLevel, OnResult<Void> callback) {
        int xp = XpCalculator.calculateTaskXp(
                task.getDifficulty(), task.getImportance(), userLevel);
        task.setXpValue(xp);
        task.setStatus(Task.STATUS_ACTIVE);
        task.setCreatedAt(System.currentTimeMillis());

        if (!task.isRepeating()) {
            task.setId(UUID.randomUUID().toString());
            localDataSource.saveTask(task);
            remoteDataSource.saveTask(task, new OnResult<Void>() {
                @Override public void onSuccess(Void result) {
                    Log.d(TAG, "Task saved to cloud: " + task.getId());
                    if (callback != null) callback.onSuccess(null);
                }
                @Override public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to save task to cloud", e);
                    if (callback != null) callback.onFailure(e);
                }
            });
        } else {
            createRecurringTasks(task, callback);
        }
    }

    private void createRecurringTasks(Task template, OnResult<Void> callback) {
        String groupId = UUID.randomUUID().toString();
        template.setRecurrenceGroupId(groupId);

        List<Task> instances = generateInstances(template);
        for (Task instance : instances) {
            localDataSource.saveTask(instance);
        }
        Log.d(TAG, "Created " + instances.size() + " recurring tasks locally");

        remoteDataSource.saveTasksBatch(instances, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Recurring tasks saved to cloud");
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to save recurring tasks to cloud", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    private List<Task> generateInstances(Task template) {
        List<Task> instances = new ArrayList<>();

        long current = template.getRepeatStartDate() > 0
                ? template.getRepeatStartDate()
                : System.currentTimeMillis();

        long end = template.getRepeatEndDate();
        if (end == 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            end = cal.getTimeInMillis();
        }

        long intervalMs = template.getRepeatInterval()
                * (Task.REPEAT_WEEKLY.equals(template.getRepeatType()) ? 7 : 1)
                * 24L * 60 * 60 * 1000;

        while (current <= end) {
            Task instance = new Task(
                    UUID.randomUUID().toString(),
                    template.getUserUid(),
                    template.getTitle(),
                    template.getDescription(),
                    template.getCategoryId(),
                    Task.STATUS_ACTIVE,
                    template.getDifficulty(),
                    template.getImportance(),
                    template.getXpValue(),
                    current,
                    template.getCreatedAt(),
                    template.getRepeatType(),
                    template.getRepeatInterval(),
                    template.getRepeatStartDate(),
                    template.getRepeatEndDate(),
                    template.getId(),
                    template.getRecurrenceGroupId()
            );
            instances.add(instance);
            current += intervalMs;
        }
        return instances;
    }

    // ─────────────────────────────────────────
    // ČITANJE
    // ─────────────────────────────────────────

    // Za kalendar — SVE (uključujući prošle, neurađene, otkazane)
    public List<Task> getAllTasks(String userUid) {
        return localDataSource.getAllTasksForUser(userUid);
    }

    // Za listu — samo aktivni i pauzirani (trenutni i budući)
    // Specifikacija: "prikazuju se samo trenutni i budući zadaci"
    // Pauzirani su vidljivi jer korisnik njima još uvek može upravljati
    // Neurađeni i otkazani su vidljivi SAMO u kalendaru
    public List<Task> getTasksForList(String userUid) {
        return localDataSource.getActiveTasksForUser(userUid);
    }

    public List<Task> getTasksForDay(String userUid, long dayStart, long dayEnd) {
        return localDataSource.getTasksForDay(userUid, dayStart, dayEnd);
    }

    public Task getTask(String taskId) {
        return localDataSource.getTask(taskId);
    }

    // ─────────────────────────────────────────
    // IZMENA
    // Spec: ne mogu se menjati završeni ili odrađeni zadaci
    // Za ponavljajući — samo buduća ponavljanja
    // ─────────────────────────────────────────

    public void updateTask(Task task, OnResult<Void> callback) {
        if (!task.isEditable()) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_EDITABLE"));
            return;
        }

        if (task.isRepeating() && task.getRecurrenceGroupId() != null) {
            // Spec: samo buduća ponavljanja se menjaju
            updateFutureRecurringTasks(task, callback);
        } else {
            localDataSource.updateTask(task);
            remoteDataSource.updateTask(task, new OnResult<Void>() {
                @Override public void onSuccess(Void result) {
                    Log.d(TAG, "Task updated: " + task.getId());
                    if (callback != null) callback.onSuccess(null);
                }
                @Override public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to update task", e);
                    if (callback != null) callback.onFailure(e);
                }
            });
        }
    }

    private void updateFutureRecurringTasks(Task task, OnResult<Void> callback) {
        // Učitaj sve buduće iz grupe lokalno i ažuriraj ih
        List<Task> allTasks = localDataSource.getAllTasksForUser(task.getUserUid());
        long now = System.currentTimeMillis();

        for (Task t : allTasks) {
            if (task.getRecurrenceGroupId().equals(t.getRecurrenceGroupId())
                    && t.getScheduledTime() >= now
                    && t.isEditable()) {
                // Prenesi izmene — samo polja koja se mogu menjati po spec
                t.setTitle(task.getTitle());
                t.setDescription(task.getDescription());
                t.setDifficulty(task.getDifficulty());
                t.setImportance(task.getImportance());
                t.setXpValue(task.getXpValue());
                // Napomena: scheduledTime se ne propagira na ostale
                // jer svaki ima svoje vreme u seriji
                localDataSource.updateTask(t);
            }
        }

        // Ažuriraj i na cloudu
        remoteDataSource.updateFutureRecurringTasks(
                task.getUserUid(),
                task.getRecurrenceGroupId(),
                task, now, new OnResult<Void>() {
                    @Override public void onSuccess(Void result) {
                        Log.d(TAG, "Future recurring tasks updated");
                        if (callback != null) callback.onSuccess(null);
                    }
                    @Override public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to update future recurring tasks", e);
                        if (callback != null) callback.onFailure(e);
                    }
                });
    }

    // ─────────────────────────────────────────
    // BRISANJE
    // Spec: nije moguće obrisati završene zadatke
    // Brisanje ponavljajućeg = brisanje svih budućih
    // Prethodno završeni ostaju vidljivi u kalendaru
    // ─────────────────────────────────────────

    public void deleteTask(Task task, OnResult<Void> callback) {
        if (!task.isDeletable()) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_DELETABLE"));
            return;
        }

        if (task.isRepeating() && task.getRecurrenceGroupId() != null) {
            localDataSource.deleteFutureRecurringTasks(
                    task.getRecurrenceGroupId(), System.currentTimeMillis());

            remoteDataSource.deleteFutureRecurringTasks(
                    task.getUserUid(), task.getRecurrenceGroupId(),
                    System.currentTimeMillis(), new OnResult<Void>() {
                        @Override public void onSuccess(Void result) {
                            Log.d(TAG, "Future recurring tasks deleted");
                            if (callback != null) callback.onSuccess(null);
                        }
                        @Override public void onFailure(Exception e) {
                            Log.e(TAG, "Failed to delete recurring tasks", e);
                            if (callback != null) callback.onFailure(e);
                        }
                    });
        } else {
            localDataSource.deleteTask(task.getId());
            remoteDataSource.deleteTask(task.getId(), new OnResult<Void>() {
                @Override public void onSuccess(Void result) {
                    Log.d(TAG, "Task deleted: " + task.getId());
                    if (callback != null) callback.onSuccess(null);
                }
                @Override public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to delete task", e);
                    if (callback != null) callback.onFailure(e);
                }
            });
        }
    }

    // ─────────────────────────────────────────
    // OZNAČAVANJE STATUSA
    // Spec: samo aktivan zadatak može biti označen
    // Zadatak se može označiti do 3 dana unazad
    // Ne može se označiti budući zadatak
    // ─────────────────────────────────────────

    // Vraća XP koji treba dodati (0 ako nije validno)
    public int markDone(Task task, OnResult<Void> callback) {
        if (!task.canBeMarked()) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_ACTIVE"));
            return 0;
        }

        long now = System.currentTimeMillis();
        long threeDaysMs = 3L * 24 * 60 * 60 * 1000;

        // Zadatak mora biti u prošlosti
        if (task.getScheduledTime() > now) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_IN_FUTURE"));
            return 0;
        }

        // Ne sme biti stariji od 3 dana — automatski postaje neurađen
        if (now - task.getScheduledTime() > threeDaysMs) {
            markUndone(task, new OnResult<Void>() {
                @Override public void onSuccess(Void result) {
                    Log.d(TAG, "Task auto-marked undone (expired): " + task.getId());
                }
                @Override public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to auto-mark undone", e);
                }
            });
            if (callback != null)
                callback.onFailure(new Exception("TASK_EXPIRED"));
            return 0;
        }

        task.setStatus(Task.STATUS_DONE);
        localDataSource.updateTaskStatus(task.getId(), Task.STATUS_DONE);
        remoteDataSource.updateTaskStatus(task.getId(), Task.STATUS_DONE, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Task marked done: " + task.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to mark task done", e);
                if (callback != null) callback.onFailure(e);
            }
        });

        return task.getXpValue();
    }

    // Spec: otkazano = rešavanje onemogućeno ne korisnikovom krivicom
    // Otkazan zadatak ne može više biti izmenjen, obrisan ili označavan
    public void markCancelled(Task task, OnResult<Void> callback) {
        if (!task.canBeMarked()) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_ACTIVE"));
            return;
        }
        task.setStatus(Task.STATUS_CANCELLED);
        localDataSource.updateTaskStatus(task.getId(), Task.STATUS_CANCELLED);
        remoteDataSource.updateTaskStatus(task.getId(), Task.STATUS_CANCELLED, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Task cancelled: " + task.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to cancel task", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // Spec: pauziranje važi SAMO za ponavljajuće zadatke
    public void markPaused(Task task, OnResult<Void> callback) {
        if (!task.canBeMarked()) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_ACTIVE"));
            return;
        }
        if (!task.isRepeating()) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_REPEATING"));
            return;
        }
        task.setStatus(Task.STATUS_PAUSED);
        localDataSource.updateTaskStatus(task.getId(), Task.STATUS_PAUSED);
        remoteDataSource.updateTaskStatus(task.getId(), Task.STATUS_PAUSED, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Task paused: " + task.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to pause task", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // Spec: pauziran zadatak može se u svakom trenutku vratiti u aktivan
    public void markActive(Task task, OnResult<Void> callback) {
        if (!Task.STATUS_PAUSED.equals(task.getStatus())) {
            if (callback != null)
                callback.onFailure(new Exception("TASK_NOT_PAUSED"));
            return;
        }
        task.setStatus(Task.STATUS_ACTIVE);
        localDataSource.updateTaskStatus(task.getId(), Task.STATUS_ACTIVE);
        remoteDataSource.updateTaskStatus(task.getId(), Task.STATUS_ACTIVE, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Task activated: " + task.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to activate task", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // Automatski poziv — ne zahteva korisnički input
    public void markUndone(Task task, OnResult<Void> callback) {
        task.setStatus(Task.STATUS_UNDONE);
        localDataSource.updateTaskStatus(task.getId(), Task.STATUS_UNDONE);
        remoteDataSource.updateTaskStatus(task.getId(), Task.STATUS_UNDONE, new OnResult<Void>() {
            @Override public void onSuccess(Void result) {
                Log.d(TAG, "Task marked undone: " + task.getId());
                if (callback != null) callback.onSuccess(null);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to mark task undone", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ─────────────────────────────────────────
    // SYNC
    // ─────────────────────────────────────────

    public void syncFromCloud(String userUid, OnResult<List<Task>> callback) {
        remoteDataSource.getTasksForUser(userUid, new OnResult<List<Task>>() {
            @Override public void onSuccess(List<Task> tasks) {
                localDataSource.clearTasksForUser(userUid);
                for (Task t : tasks) localDataSource.saveTask(t);
                Log.d(TAG, "Synced " + tasks.size() + " tasks from cloud");
                if (callback != null) callback.onSuccess(tasks);
            }
            @Override public void onFailure(Exception e) {
                Log.e(TAG, "Failed to sync tasks from cloud", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }
}