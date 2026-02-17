package com.example.myapp.presentation.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapp.R;
import com.example.myapp.presentation.viewmodels.LoginViewModel;
import com.example.myapp.presentation.viewmodels.LoginViewModelFactory;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicijalizuj ViewModel sa Factory
        LoginViewModelFactory factory = new LoginViewModelFactory(getApplicationContext());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Inicijalizuj Views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        observeViewModel();
        restoreFormState();
    }

    // ─────────────────────────────────────────
    // OBSERVE VIEWMODEL
    // ─────────────────────────────────────────

    private void observeViewModel() {

        viewModel.isLoading.observe(this, isLoading -> {
            loginButton.setEnabled(!isLoading);
        });

        viewModel.loginSuccess.observe(this, success -> {
            if (success) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });


        viewModel.errorMessage.observe(this, error -> {
            if (error == null || error.isEmpty()) return;
            switch (error) {
                // ── Lokalna validacija ──
                case "EMAIL_EMPTY":
                    emailInput.setError("Email je obavezan");
                    emailInput.requestFocus();
                    break;
                case "EMAIL_INVALID":
                    emailInput.setError("Unesite validan email");
                    emailInput.requestFocus();
                    break;
                case "PASSWORD_EMPTY":
                    passwordInput.setError("Šifra je obavezna");
                    passwordInput.requestFocus();
                    break;
                // ── Firebase greške ──
                case "INVALID_CREDENTIALS":
                    passwordInput.setError("Pogrešan email ili šifra");
                    passwordInput.requestFocus();
                    break;
                case "INVALID_USER":
                    emailInput.setError("Korisnik ne postoji");
                    emailInput.requestFocus();
                    break;
                case "NO_CONNECTION":
                    emailInput.setError("Doslo je do problema sa vezom");
                    emailInput.requestFocus();
                    break;
                default:
                    Toast.makeText(this, "Greška pri prijavi", Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    // ─────────────────────────────────────────
    // RESTORE FORM STATE
    // ─────────────────────────────────────────

    private void restoreFormState() {
        String email = viewModel.emailInput.getValue();
        if (email != null && !email.isEmpty()) {
            emailInput.setText(email);
        }
    }


    // ─────────────────────────────────────────
    // EVENT HANDLERS
    // ─────────────────────────────────────────

    public void loginUser(View v) {
        viewModel.login(
                emailInput.getText().toString().trim(),
                passwordInput.getText().toString().trim()
        );
    }

    public void goToRegister(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}