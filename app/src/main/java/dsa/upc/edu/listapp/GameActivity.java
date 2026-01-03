package dsa.upc.edu.listapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayerGameActivity;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.LoginRequest;
import dsa.upc.edu.listapp.github.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameActivity extends UnityPlayerGameActivity {

    private EETACBROSSystemService api;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = API.getGithub();
        prefs = getSharedPreferences("EETACBROSPreferences", MODE_PRIVATE);
    }

    // ---------------------------------------------------------
    // COMUNICACIÓ 1: Unity -> Android
    // ---------------------------------------------------------
    public void RebrePuntuacio(String puntuacio) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("UnityBridge", "Puntuació rebuda al UI Thread: " + puntuacio);

                try {
                    int puntuacioInt = Integer.parseInt(puntuacio);
                    int monedes = puntuacioInt / 1000;  // Conversió Puntuació --> Monedes

                    String username = prefs.getString("username", null);
                    String password = prefs.getString("password", null);

                    if (username != null && password != null) {
                        LoginRequest loginRequest = new LoginRequest(username, password);

                        api.loginUser(loginRequest).enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    User currentUser = response.body();
                                    currentUser.coins += monedes;
                                    currentUser.score += puntuacioInt;

                                    Log.d("UnityBridge", "Dades enviades al servidor: " +
                                            "User: " + currentUser.username +
                                            ", Pwd: " + currentUser.password +
                                            ", Coins: " + currentUser.coins +
                                            ", Score: " + currentUser.score);

                                    api.updateUserScore(currentUser).enqueue(new Callback<User>() {
                                        @Override
                                        public void onResponse(Call<User> call, Response<User> response) {
                                            if (response.isSuccessful()) {
                                                Toast.makeText(GameActivity.this, "Partida guardada!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(GameActivity.this, "Error guardant puntuació", Toast.LENGTH_SHORT).show();
                                            }
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(Call<User> call, Throwable t) {
                                            Toast.makeText(GameActivity.this, "Error de xarxa", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                } else {
                                    Toast.makeText(GameActivity.this, "Error login al finalitzar", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                Toast.makeText(GameActivity.this, "Error de xarxa al login", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                    else {
                        Toast.makeText(GameActivity.this, "Usuari no trobat", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } catch (Exception e) {
                    Toast.makeText(GameActivity.this, "Error processant dades: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}