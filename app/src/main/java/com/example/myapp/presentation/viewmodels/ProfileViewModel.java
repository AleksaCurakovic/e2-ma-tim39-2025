package com.example.myapp.presentation.viewmodels;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapp.data.repositories.UserRepository;
import com.example.myapp.domain.models.User;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;

    public MutableLiveData<User> user = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);


    public ProfileViewModel(Context context) {
        userRepository = new UserRepository(context);
    }

    // ─── Učitaj korisnika lokalno (brzo) ───
    public void loadUser(String uid) {
        User localUser = userRepository.getUserLocally(uid);
        if (localUser != null) {
            user.setValue(localUser);
        }
        // Sinhronizuj sa cloudom u pozadini
        isLoading.setValue(true);
        userRepository.fetchFromCloud(uid, new com.example.myapp.data.datasource.remote.OnResult<User>() {
            @Override public void onSuccess(User result) {
                isLoading.postValue(false);
                user.postValue(result);
            }
            @Override public void onFailure(Exception e) {
                isLoading.postValue(false);
                // Lokalni podaci su već prikazani — ne prikazujemo grešku
            }
        });
    }

}