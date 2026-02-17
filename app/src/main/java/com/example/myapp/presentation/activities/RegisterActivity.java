package com.example.myapp.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapp.domain.services.AuthManager;
import com.example.myapp.presentation.viewmodels.RegisterViewModel;
import com.example.myapp.presentation.viewmodels.RegisterViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.myapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private EditText emailInput;
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private List<ImageView> avatarViews;
    private ImageView selectedAvatarView;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicijalizuj ViewModel
        RegisterViewModelFactory factory = new RegisterViewModelFactory(getApplicationContext());
        viewModel = new ViewModelProvider(this, factory).get(RegisterViewModel.class);


        // Inicijalizuj Views
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);

        setupAvatarSelection();
        observeViewModel();
        restoreFormState();
    }

    // ─────────────────────────────────────────
    // OBSERVE VIEWMODEL
    // ─────────────────────────────────────────

    private void setupAvatarSelection() {
        avatarViews = new ArrayList<>();
        avatarViews.add(findViewById(R.id.avatar1));
        avatarViews.add(findViewById(R.id.avatar2));
        avatarViews.add(findViewById(R.id.avatar3));
        avatarViews.add(findViewById(R.id.avatar4));
        avatarViews.add(findViewById(R.id.avatar5));

        // Postavi click listener na svaki avatar
        for (ImageView avatarView : avatarViews) {
            avatarView.setOnClickListener(v -> selectAvatar((ImageView) v));
        }

        // Default - prvi avatar selektovan
        selectedAvatarView = avatarViews.get(0);
        selectedAvatarView.setBackgroundResource(R.drawable.avatar_selected);
    }

    private void selectAvatar(ImageView clicked) {
        // Ukloni highlight sa svih
        for (ImageView av : avatarViews) {
            av.setBackgroundResource(R.drawable.avatar_normal);
        }

        // Dodaj highlight na kliknuti
        clicked.setBackgroundResource(R.drawable.avatar_selected);
        selectedAvatarView = clicked;

        // Sačuvaj u ViewModel
        viewModel.selectedAvatar.setValue(getAvatarId(clicked));
    }

    private void observeViewModel() {

        // Prati loading stanje
        viewModel.isLoading.observe(this, isLoading -> {
            registerButton.setEnabled(!isLoading);
        });

        // Prati uspešnu registraciju
        viewModel.registerSuccess.observe(this, success -> {
            if (success) {
                Toast.makeText(this,
                        "Verifikacioni email poslat! Molimo verifikujte email pre prijavljivanja.",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        // Prati greške
        viewModel.errorMessage.observe(this, error -> {
            if (error == null || error.isEmpty()) return;
            switch (error) {
                case "EMAIL_EMPTY":
                    emailInput.setError("Email je obavezan");
                    emailInput.requestFocus();
                    break;
                case "EMAIL_INVALID":
                    emailInput.setError("Unesite validan email");
                    emailInput.requestFocus();
                    break;
                case "USERNAME_EMPTY":
                    usernameInput.setError("Korisničko ime je obavezno");
                    usernameInput.requestFocus();
                    break;
                case "PASSWORD_EMPTY":
                    passwordInput.setError("Šifra je obavezna");
                    passwordInput.requestFocus();
                    break;
                case "PASSWORDS_DONT_MATCH":
                    confirmPasswordInput.setError("Šifre se ne poklapaju");
                    confirmPasswordInput.requestFocus();
                    break;
                case "EMAIL_IN_USE":
                    emailInput.setError("Email je već u upotrebi");
                    emailInput.requestFocus();
                    break;
                case "NO_CONNECTION":
                    emailInput.setError("Doslo je do problema sa vezom");
                    emailInput.requestFocus();
                    break;
                default:
                    Toast.makeText(this, "Greška pri registraciji", Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    // ─────────────────────────────────────────
    // RESTORE FORM STATE - nakon rotacije
    // ─────────────────────────────────────────

    private void restoreFormState() {
        String email = viewModel.emailInput.getValue();
        String username = viewModel.usernameInput.getValue();
        String savedAvatarId = viewModel.selectedAvatar.getValue();

        if (email != null && !email.isEmpty()) {
            emailInput.setText(email);
        }
        if (username != null && !username.isEmpty()) {
            usernameInput.setText(username);
        }

        // Vrati selektovani avatar
        if (savedAvatarId != null) {
            for (ImageView av : avatarViews) {
                if (getAvatarId(av).equals(savedAvatarId)) {
                    selectAvatar(av);
                    break;
                }
            }
        }
    }

    private String getAvatarId(ImageView view) {
        int id = view.getId();
        if (id == R.id.avatar1) return "skeleton";
        if (id == R.id.avatar2) return "elf";
        if (id == R.id.avatar3) return "death";
        if (id == R.id.avatar4) return "horns";
        if (id == R.id.avatar5) return "wizard";
        return "skeleton";
    }

    // ─────────────────────────────────────────
    // EVENT HANDLERS
    // ─────────────────────────────────────────

    public void registerUser(View v) {
        viewModel.register(
                emailInput.getText().toString().trim(),
                usernameInput.getText().toString().trim(),
                passwordInput.getText().toString().trim(),
                confirmPasswordInput.getText().toString().trim(),
                getAvatarId(selectedAvatarView)
        );
    }


    public void goToLogin(View v) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}