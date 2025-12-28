package dsa.upc.edu.listapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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
import dsa.upc.edu.listapp.github.LoginRequest;
import dsa.upc.edu.listapp.github.RankingResponse;
import dsa.upc.edu.listapp.github.RankingUser;
import dsa.upc.edu.listapp.github.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private EETACBROSSystemService api;
    private SharedPreferences prefs;

    private Button gamesBtn, settingsBtn, logoutBtn, shopBtn, faqBtn;

    private TextView usernameTextView, coinsTextView, scoreTextView;
    private RecyclerView inventoryRecycler;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    // Ranking Views
    private LinearLayout rankingListContainer;
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
        faqBtn = findViewById(R.id.faqBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        inventoryRecycler = findViewById(R.id.inventoryRecycler);
        usernameTextView = findViewById(R.id.username);
        coinsTextView = findViewById(R.id.totalCoins);
        scoreTextView = findViewById(R.id.totalScore);


        // Ranking Bindings
        rankingListContainer = findViewById(R.id.rankingListContainer);

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
        if (faqBtn != null) {
            faqBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, FAQActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void updateProfileDisplay(User user) {
        if (user == null) return;
        if (usernameTextView != null) {
            usernameTextView.setText(user.username);
        }
        if (coinsTextView != null) {
            coinsTextView.setText("Coins: " + user.coins);
        }
        if (scoreTextView != null) {
            scoreTextView.setText("Score: " + user.score);
        }
    }

    private void renderUserItems() {
        if (mAdapter != null) {
            mAdapter.setData(userItems);
        }
    }

    private void logOut() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
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
        if (userId == -1) return;

        // Use stored credentials to re-login and get fresh user data
        String u = prefs.getString("username", null);
        String p = prefs.getString("password", null);

        if (u != null && p != null) {
            LoginRequest req = new LoginRequest(u, p);
            api.loginUser(req).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        updateProfileDisplay(user);
                        loadRanking(userId, user.username);
                    } else {
                        Log.e(TAG, "Failed to refresh user data via login");
                        // Optionally handle logout if credentials invalid
                    }
                }
                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, "Connection error refreshing user data", t);
                }
            });
        }

        // 2. Load Items
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
            }
        });
    }

    private void loadRanking(int userId, String currentUsername) {
        api.getRanking(userId).enqueue(new Callback<RankingResponse>() {
            @Override
            public void onResponse(Call<RankingResponse> call, Response<RankingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RankingResponse ranking = response.body();
                    List<RankingUser> podium = ranking.podium;
                    RankingUser userEntry = ranking.userEntry;

                    if (rankingListContainer != null) {
                        rankingListContainer.removeAllViews();
                    }

                    if (podium != null) {
                        for (RankingUser user : podium) {
                            addRankRow(user, currentUsername);
                        }
                    }

                    if (userEntry != null) {
                        rankDivider.setVisibility(View.VISIBLE);
                        rankUserContainer.setVisibility(View.VISIBLE);
                        rankUserPos.setText(String.valueOf(userEntry.position));
                        rankUserUser.setText(userEntry.username);
                        rankUserScore.setText(String.valueOf(userEntry.score));
                        rankUserContainer.setBackgroundColor(Color.parseColor("#E0F7FA"));
                    } else {
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

    private void addRankRow(RankingUser user, String currentUsername) {
        if (rankingListContainer == null) return;
        
        View row = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.item_ranking_row, rankingListContainer, false);
        
        TextView pos = row.findViewById(R.id.rowPos);
        TextView name = row.findViewById(R.id.rowUser);
        TextView score = row.findViewById(R.id.rowScore);
        LinearLayout container = row.findViewById(R.id.rowContainer);

        pos.setText(String.valueOf(user.position));
        name.setText(user.username);
        score.setText(String.valueOf(user.score));

        if (user.username.equals(currentUsername)) {
            container.setBackgroundColor(Color.parseColor("#E0F7FA")); 
        } else {
            container.setBackgroundColor(Color.TRANSPARENT);
        }
        
        rankingListContainer.addView(row);
    }
}
