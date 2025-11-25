package dsa.upc.edu.listapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.LoginRequest;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoToRegister;

    private EETACBROSSystemService system;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("EETACBORSPreferences", MODE_PRIVATE);

        if (prefs.getBoolean("isLoggedIn", false)) {
            goToShop();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);


        btnLogin.setOnClickListener(v -> loginUser());
        btnGoToRegister.setOnClickListener(v -> goToRegister());
    }

    private void loginUser() {
        LoginRequest request = new LoginRequest(
                etUsername.getText().toString(),
                etPassword.getText().toString()
        );

        EETACBROSSystemService api = API.getGithub();

        api.loginUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    if (user != null) {
                        editor.putInt("userId", user.id);
                        editor.putString("username", user.username);
                    }
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                    goToShop();
                }
                else if (response.code() == 404){
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
                else if (response.code() == 400) {
                    Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void goToShop() {
        Intent intent = new Intent(LoginActivity.this, ShopActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
