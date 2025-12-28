package dsa.upc.edu.listapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SplashScreenActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        prefs = getSharedPreferences("EETACBROSPreferences", MODE_PRIVATE);
        userId = getUserIdSafely();

        if (userId != -1) {
            Toast.makeText(SplashScreenActivity.this, "Already logged in", Toast.LENGTH_SHORT).show();
        }

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (userId != -1) {
                goToProfile();
            } else {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }, 3000);
    }

    private int getUserIdSafely() {
        int userId = -1;
        try {
            userId = prefs.getInt("userId", -1);
        } catch (ClassCastException e) {
            String legacyUserId = prefs.getString("userId", null);
            if (legacyUserId != null) {
                try {
                    userId = Integer.parseInt(legacyUserId);
                    // Update to new format
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("userId", userId);
                    editor.apply();
                } catch (NumberFormatException nfe) {
                    // Ignored
                }
            }
        }
        return userId;
    }

    private void goToProfile() {
        Intent intent = new Intent(SplashScreenActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}
