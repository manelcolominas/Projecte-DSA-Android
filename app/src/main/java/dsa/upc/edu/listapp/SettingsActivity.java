package dsa.upc.edu.listapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private EETACBROSSystemService api;
    private SharedPreferences prefs;

    private EditText usernameEditText, emailEditText, nameEditText, currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button profileBtn, clearBtn, saveBtn, deleteAccountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("EETACBROSPreferences", MODE_PRIVATE);
        api = API.getGithub();

        // EditTexts
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        nameEditText = findViewById(R.id.name);
        currentPasswordEditText = findViewById(R.id.currentPassword);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);

        // Buttons
        profileBtn = findViewById(R.id.profileBtn);
        clearBtn = findViewById(R.id.clearBtn);
        saveBtn = findViewById(R.id.saveBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

        profileBtn.setOnClickListener(v -> goToProfile());

        clearBtn.setOnClickListener(v -> clearForm());

        saveBtn.setOnClickListener(v -> saveChanges());

        deleteAccountBtn.setOnClickListener(v -> deleteAccount());

        loadUserSettings();
    }

    private void loadUserSettings() {
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");
        String name = prefs.getString("name", "");

        usernameEditText.setText(username);
        emailEditText.setText(email);
        nameEditText.setText(name);
    }

    private void goToProfile() {
        Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish(); // Finish SettingsActivity to go back to profile
    }

    private void clearForm() {
        usernameEditText.setText("");
        emailEditText.setText("");
        nameEditText.setText("");
        currentPasswordEditText.setText("");
        newPasswordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    private void saveChanges() {
        String newUsername = usernameEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();
        String newName = nameEditText.getText().toString();
        int userId = getUserIdSafely();

        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = prefs.getString("password", null); // Retrieve existing password
        int coins = prefs.getInt("coins", 0); // Retrieve existing coins
        int score = prefs.getInt("score", 0); // Retrieve existing score


        User updatedUser = new User(userId, newUsername, newName, newEmail, password, coins, score);

        api.updateUser(updatedUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SettingsActivity.this, "User data updated successfully!", Toast.LENGTH_SHORT).show();
                    // Update SharedPreferences with new data
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", response.body().username);
                    editor.putString("email", response.body().email);
                    editor.putString("name", response.body().name);
                    editor.apply();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Error updating user data", t);
                Toast.makeText(SettingsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is irreversible.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int userId = getUserIdSafely();
                    if (userId == -1) {
                        Toast.makeText(SettingsActivity.this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    api.deleteUser(userId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                // Clear SharedPreferences and navigate to LoginActivity
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.clear();
                                editor.apply();
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e(TAG, "Error deleting account", t);
                            Toast.makeText(SettingsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
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
}
