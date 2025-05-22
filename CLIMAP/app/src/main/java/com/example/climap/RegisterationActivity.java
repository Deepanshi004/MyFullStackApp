package com.example.climap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterationActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput, passwordConfirmInput;
    private Button registerBtn;
    private TextView goToLoginText;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration); // Your XML file name

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirmInput = findViewById(R.id.passwordConfirmInput);
        registerBtn = findViewById(R.id.registerBtn);
        goToLoginText = findViewById(R.id.goToLoginText);

        // Retrofit service instance
        apiService = ApiClient.getRetrofit("http://192.168.1.10:5000/").create(ApiService.class); // change to your backend IP

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = passwordConfirmInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest registerRequest = new RegisterRequest(name, email, password);
            apiService.register(registerRequest).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegisterationActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterationActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    Toast.makeText(RegisterationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Go to Login screen
        goToLoginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterationActivity.this, LoginActivity.class));
            finish();
        });
    }
}
