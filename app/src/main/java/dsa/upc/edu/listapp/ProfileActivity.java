package dsa.upc.edu.listapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.Item;
import dsa.upc.edu.listapp.github.RankingResponse;
import dsa.upc.edu.listapp.github.RankingUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private EETACBROSSystemService api;
    private SharedPreferences prefs;

    private Button gamesBtn, settingsBtn, logoutBtn, shopBtn;

    private TextView usernameTextView, coinsTextView, scoreTextView;
    private RecyclerView inventoryRecycler;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    // Ranking Views
    private TextView rank1Pos, rank1User, rank1Score;
    private LinearLayout rank1Container;
    private TextView rank2Pos, rank2User, rank2Score;
    private LinearLayout rank2Container;
    private TextView rank3Pos, rank3User, rank3Score;
    private LinearLayout rank3Container;
    private View rankDivider;
    private TextView rankUserPos, rankUserUser, rankUserScore;
    private LinearLayout rankUserContainer;

    private List<Item> userItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("EETACBROSPreferences", MODE_PRIVATE);

        // Bind Views
        gamesBtn = findViewById(R.id.gamesBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        shopBtn = findViewById(R.id.shopBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        inventoryRecycler = findViewById(R.id.inventoryRecycler);
        usernameTextView = findViewById(R.id.username);
        coinsTextView = findViewById(R.id.totalCoins);
        scoreTextView = findViewById(R.id.totalScore);


        // Ranking Bindings
        rank1Pos = findViewById(R.id.rank1Pos);
        rank1User = findViewById(R.id.rank1User);
        rank1Score = findViewById(R.id.rank1Score);
        rank1Container = findViewById(R.id.rank1Container);

        rank2Pos = findViewById(R.id.rank2Pos);
        rank2User = findViewById(R.id.rank2User);
        rank2Score = findViewById(R.id.rank2Score);
        rank2Container = findViewById(R.id.rank2Container);

        rank3Pos = findViewById(R.id.rank3Pos);
        rank3User = findViewById(R.id.rank3User);
        rank3Score = findViewById(R.id.rank3Score);
        rank3Container = findViewById(R.id.rank3Container);

        rankDivider = findViewById(R.id.rankDivider);

        rankUserPos = findViewById(R.id.rankUserPos);
        rankUserUser = findViewById(R.id.rankUserUser);
        rankUserScore = findViewById(R.id.rankUserScore);
        rankUserContainer = findViewById(R.id.rankUserContainer);


        if (inventoryRecycler != null) {
            inventoryRecycler.setHasFixedSize(true);
            layoutManager = new GridLayoutManager(this, 3);
            inventoryRecycler.setLayoutManager(layoutManager);
            mAdapter = new MyAdapter();
            inventoryRecycler.setAdapter(mAdapter);
        }

        api = API.getGithub();

        loadUserData();

        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> logOut());
        }
        if (shopBtn != null) {
            shopBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ShopActivity.class);
                startActivity(intent);
            });
        }
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileDisplay();
    }

    private void updateProfileDisplay() {
        String username = prefs.getString("username", "Unknown");
        int coins = prefs.getInt("coins", 0);
        int score = prefs.getInt("score", 0);
        if (usernameTextView != null) {
            usernameTextView.setText(username);
        }
        if (coinsTextView != null) {
            coinsTextView.setText("Coins: " + coins);
        }
        if (scoreTextView != null) {
            scoreTextView.setText("Score: " + score);
        }
    }

    private void renderUserItems() {
        if (mAdapter != null) {
            mAdapter.setData(userItems);
        }
    }

    private void logOut() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("username");
        editor.remove("password");
        editor.remove("coins");
        editor.putInt("userId", -1);
        editor.putBoolean("isLoggedIn", false);
        editor.commit();
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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

    private void loadUserData() {
        int userId = getUserIdSafely();
        if (userId == -1) {
            Toast.makeText(ProfileActivity.this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load Items
        api.getUserItems(userId).enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userItems.clear();
                    userItems.addAll(response.body());
                    renderUserItems();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load inventory", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Log.e(TAG, "Connection error", t);
                Toast.makeText(ProfileActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load Ranking
        loadRanking(userId);
    }

    private void loadRanking(int userId) {
        String currentUsername = prefs.getString("username", "");

        api.getRanking(userId).enqueue(new Callback<RankingResponse>() {
            @Override
            public void onResponse(Call<RankingResponse> call, Response<RankingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RankingResponse ranking = response.body();
                    List<RankingUser> podium = ranking.podium;
                    RankingUser userEntry = ranking.userEntry;

                    // Clear previous states
                    rank1User.setText("-"); rank1Score.setText("-"); rank1Container.setBackgroundColor(Color.TRANSPARENT);
                    rank2User.setText("-"); rank2Score.setText("-"); rank2Container.setBackgroundColor(Color.TRANSPARENT);
                    rank3User.setText("-"); rank3Score.setText("-"); rank3Container.setBackgroundColor(Color.TRANSPARENT);

                    // Fill Podium
                    if (podium != null) {
                        if (podium.size() > 0) {
                            setupRankRow(podium.get(0), rank1User, rank1Score, rank1Container, currentUsername);
                        }
                        if (podium.size() > 1) {
                            setupRankRow(podium.get(1), rank2User, rank2Score, rank2Container, currentUsername);
                        }
                        if (podium.size() > 2) {
                            setupRankRow(podium.get(2), rank3User, rank3Score, rank3Container, currentUsername);
                        }
                    }

                    // Handle User Entry
                    if (userEntry != null) {
                        rankDivider.setVisibility(View.VISIBLE);
                        rankUserContainer.setVisibility(View.VISIBLE);
                        rankUserPos.setText(String.valueOf(userEntry.position));
                        rankUserUser.setText(userEntry.username);
                        rankUserScore.setText(String.valueOf(userEntry.score));
                        // Highlight user entry container just to be sure
                        rankUserContainer.setBackgroundColor(Color.parseColor("#E0F7FA"));
                    } else {
                        // User is in podium or no data
                        rankDivider.setVisibility(View.GONE);
                        rankUserContainer.setVisibility(View.GONE);
                    }

                } else {
                    Log.e(TAG, "Failed to load ranking");
                }
            }

            @Override
            public void onFailure(Call<RankingResponse> call, Throwable t) {
                Log.e(TAG, "Error loading ranking", t);
            }
        });
    }

    private void setupRankRow(RankingUser user, TextView nameView, TextView scoreView, LinearLayout container, String currentUsername) {
        nameView.setText(user.username);
        scoreView.setText(String.valueOf(user.score));
        if (user.username.equals(currentUsername)) {
            container.setBackgroundColor(Color.parseColor("#E0F7FA")); // Light Cyan for highlight
        } else {
            container.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
