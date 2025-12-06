package dsa.upc.edu.listapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;


public class SplashScreenActivity extends AppCompatActivity {


    private SharedPreferences prefs;
    //private boolean isLoggedIn;
    private Integer userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefs = getSharedPreferences("EETACBROSPreferences", MODE_PRIVATE);
        userId = prefs.getInt("userId",-1);
        if (userId != -1) {
            Toast.makeText(SplashScreenActivity.this, "Already logged in", Toast.LENGTH_SHORT).show();
        }

        // Seria conveniende guardar la contraseña y el nombre de usuario o solamenet el id
        // del usuario en las SharedPreferences para que asi durante el SplashScreen se loguee
        // automaticamente. Si no se ha podido loguearse con exito entonces le mandamos a la actividad
        // de login.

        final Handler handler = new Handler();
        handler.postDelayed(() ->
                {
                    Intent intent = null;
                    if (userId != -1) {
                        goToShop();
                    }
                    else {
                        intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                },
                3000);
    }

    private void goToShop() {
        Intent intent = new Intent(SplashScreenActivity.this, ShopActivity.class);
        startActivity(intent);
    }

}