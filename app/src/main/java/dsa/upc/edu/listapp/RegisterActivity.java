package dsa.upc.edu.listapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.RegisterRequest;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etName, etEmail, etPassword, etRepeatPassword;
    private Button btnRegister, btnGoToLogIn;

    private EETACBROSSystemService system;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogIn = findViewById(R.id.btnGoToLogIn);

        btnRegister.setOnClickListener(v -> registerUser());
        btnGoToLogIn.setOnClickListener(v -> goToLogin());
    }

    private void registerUser() {
        if (!checkPassword(etPassword.getText().toString(), etRepeatPassword.getText().toString())) return;
        RegisterRequest request = new RegisterRequest(
                etUsername.getText().toString(),
                etName.getText().toString(),
                etEmail.getText().toString(),
                etPassword.getText().toString()
        );

        EETACBROSSystemService api = API.getGithub();

        api.registerUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Usuari registrat correctament!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Error en el registre", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error de connexió: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkPassword(String password, String repeatPassword) {
        if (!password.equals(repeatPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must have at minimum 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        } else if(!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*")) {
            Toast.makeText(RegisterActivity.this, "Password must have at minimum a lower case letter, an upper case letter and a number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean checkEmail (String email) {
        
    }

    private void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}