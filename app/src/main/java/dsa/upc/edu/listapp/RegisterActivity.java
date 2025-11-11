package dsa.upc.edu.listapp;

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

    private EditText etUsername, etNom, etCognom1, etCognom2, etEmail, etPassword, etDataNaixement;
    private Button btnRegister;

    private EETACBROSSystemService system;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etNom = findViewById(R.id.etNom);
        etCognom1 = findViewById(R.id.etCognom1);
        etCognom2 = findViewById(R.id.etCognom2);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDataNaixement = findViewById(R.id.etDataNaixement);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
//        RegisterRequest request = new RegisterRequest(
//                etUsername.getText().toString(),
//                etNom.getText().toString(),
//                etCognom1.getText().toString(),
//                etCognom2.getText().toString(),
//                etEmail.getText().toString(),
//                etPassword.getText().toString(),
//                etDataNaixement.getText().toString()
//        );

        RegisterRequest request = new RegisterRequest("username","manel","colominas","Ruiz","manelcolominas@gmail.com","111111111","11/02/2003");

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
}