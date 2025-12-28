package dsa.upc.edu.listapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.LoginRequest;
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

    // Cache the original user data to update
    private User currentUser;

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

        loadUserData();
    }

    private void loadUserData() {
        int userId = getUserIdSafely();
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String u = prefs.getString("username", null);
        String p = prefs.getString("password", null);

        if (u != null && p != null) {
            LoginRequest req = new LoginRequest(u, p);
            api.loginUser(req).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        usernameEditText.setText(currentUser.username);
                        emailEditText.setText(currentUser.email);
                        nameEditText.setText(currentUser.name);
                    } else {
                        Toast.makeText(SettingsActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Connection error", t);
                    Toast.makeText(SettingsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String newUsername = usernameEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();
        String newName = nameEditText.getText().toString();
        int userId = currentUser.id;

        // Use current password unless user wants to change it
        String password = currentUser.password; 
        
        // TODO: Handle password change logic if fields are filled (not implemented in original code properly but structure exists)
        // For now, we preserve the existing password from the fetched user object.

        User updatedUser = new User(userId, newUsername, newName, newEmail, password, currentUser.coins, currentUser.score);

        api.updateUser(updatedUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SettingsActivity.this, "User data updated successfully!", Toast.LENGTH_SHORT).show();
                    currentUser = response.body();
                    // We don't update SharedPreferences anymore for user data, EXCEPT CREDENTIALS if they changed
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("username", currentUser.username);
                    if (!currentUser.password.equals(password)) { // Simplified check, ideally check against new password field if implemented
                         editor.putString("password", currentUser.password);
                    }
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
