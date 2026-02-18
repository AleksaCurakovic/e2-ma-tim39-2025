package com.example.myapp.presentation.viewmodels;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.datasource.remote.OnResult;
import com.example.myapp.data.repositories.UserRepository;
import com.example.myapp.domain.models.User;
import com.example.myapp.domain.utils.LevelManager;

public class LevelViewModel extends ViewModel {

    private final UserRepository userRepository;

    public MutableLiveData<User>    user          = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading     = new MutableLiveData<>(false);
    public MutableLiveData<Integer> xpRequired    = new MutableLiveData<>();
    public MutableLiveData<Integer> xpToNext      = new MutableLiveData<>();
    public MutableLiveData<Float>   levelProgress = new MutableLiveData<>();

    public MutableLiveData<UserRepository.LevelUpResult> levelUpEvent = new MutableLiveData<>();

    public LevelViewModel(Context context) {
        userRepository = new UserRepository(context);
    }

    // ─────────────────────────────────────────
    // UČITAVANJE
    // ─────────────────────────────────────────

    public void loadUser(String userUid) {
        isLoading.setValue(true);
        User localUser = userRepository.getUserLocally(userUid);
        if (localUser != null) {
            user.setValue(localUser);
            updateProgressData(localUser);
        }
        isLoading.setValue(false);
    }

    // ─────────────────────────────────────────
    // LEVEL UP CHECK
    // ─────────────────────────────────────────

    public void checkLevelUp(String userUid) {
        userRepository.checkAndApplyLevelUp(userUid,
                new OnResult<UserRepository.LevelUpResult>() {
                    @Override public void onSuccess(UserRepository.LevelUpResult result) {
                        if (result.leveledUp) {
                            levelUpEvent.postValue(result);
                            loadUser(userUid);
                        }
                    }
                    @Override public void onFailure(Exception e) {
                        Log.e("LevelViewModel", "Level up check failed", e);
                    }
                });
    }

    // ─────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────

    private void updateProgressData(User u) {
        int required   = LevelManager.xpRequiredForLevel(u.getLevel() + 1);
        int toNext     = LevelManager.xpToNextLevel(u.getXp(), u.getLevel());
        float progress = LevelManager.progressToNextLevel(u.getXp(), u.getLevel());

        xpRequired.setValue(required);
        xpToNext.setValue(toNext);
        levelProgress.setValue(progress);
    }
}