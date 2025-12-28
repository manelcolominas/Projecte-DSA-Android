package dsa.upc.edu.listapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.BuyRequest;
import dsa.upc.edu.listapp.github.BuyResponse;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.Item;
import dsa.upc.edu.listapp.github.LoginRequest;
import dsa.upc.edu.listapp.github.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {

    private View cartIcon;
    private Button checkoutBtn, logoutBtn, closeCart, clearCart, profileBtn;
    private RelativeLayout cartModal;
    private TextView cartCount, coinsTextView;

    private EETACBROSSystemService api;
    private SharedPreferences prefs;

    // Local cart: itemId -> quantity
    private Map<Integer, Integer> cart = new HashMap<>();

    // List of items fetched from API
    private List<Item> shopItems = new ArrayList<>();

    private int coins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        prefs = getSharedPreferences("EETACBROSPreferences", MODE_PRIVATE);
        // coins = prefs.getInt("coins", 0); // No longer trusted source

        cartIcon = findViewById(R.id.cartIcon);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileBtn = findViewById(R.id.profileBtn);
        cartModal = findViewById(R.id.cartModal);
        cartCount = findViewById(R.id.cartCount);
        coinsTextView = findViewById(R.id.coins);

        api = API.getGithub();

        loadUserData(); // To get coins
        loadShopItems();

        // Set button listeners
        cartIcon.setOnClickListener(view -> seeCartItems());

        logoutBtn.setOnClickListener(v -> logOut());
        profileBtn.setOnClickListener(v -> gotToProfile());
    }

    private void loadUserData() {
        int userId = getUserIdSafely();
        if (userId == -1) {
            Toast.makeText(this, "User not logged in properly", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use stored credentials to re-login and get fresh user data
        String u = prefs.getString("username", null);
        String p = prefs.getString("password", null);

        if (u != null && p != null) {
            LoginRequest req = new LoginRequest(u, p);
            api.loginUser(req).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        coins = response.body().coins;
                        updateCoinsDisplay();
                    } else {
                        // Fallback to 0 or error
                        coins = 0;
                        updateCoinsDisplay();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // Fallback to 0 or error
                    coins = 0;
                    updateCoinsDisplay();
                }
            });
        }
    }

    private void logOut() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("userId", -1);
        editor.putBoolean("isLoggedIn", false);
        editor.commit();

        Intent intent = new Intent(ShopActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void gotToProfile() {

        Intent intent = new Intent(ShopActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    // -----------------------------
    // Load items from GET /items
    // -----------------------------
    private void loadShopItems() {
        api.getItems().enqueue(new Callback<List<Item>>(){ 
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    shopItems.clear();
                    shopItems.addAll(response.body());
                    renderShopItems();
                    Toast.makeText(ShopActivity.this,
                            "Items loaded: " + shopItems.size(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopActivity.this,
                            "Failed to load items",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                Toast.makeText(ShopActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderShopItems() {
        LinearLayout productsGrid = findViewById(R.id.productsGrid);
        productsGrid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Item item : shopItems) {
            View productCard = inflater.inflate(R.layout.product_card, productsGrid, false);

            TextView itemEmoji = productCard.findViewById(R.id.itemEmoji);
            TextView itemName = productCard.findViewById(R.id.itemName);
            TextView itemDescription = productCard.findViewById(R.id.itemDescription);
            TextView itemPrice = productCard.findViewById(R.id.itemPrice);
            Button addToCartBtn = productCard.findViewById(R.id.addToCartBtn);

            itemEmoji.setText(item.getEmoji());
            itemName.setText(item.getName());
            itemDescription.setText(item.getDescription());
            itemPrice.setText("Price: " + item.getPrice());

            addToCartBtn.setOnClickListener(v -> addToCart(item));

            productsGrid.addView(productCard);
        }
    }

    private void updateCoinsDisplay() {
        coinsTextView.setText(String.valueOf(coins));
    }

    // -----------------------------
    // Add item to cart
    // -----------------------------

    private void addToCart(Item item) {
        int id = item.getId();
        Integer quantity = cart.get(id);
        if (quantity == null) {
            quantity = 0;
        }
        cart.put(id, quantity + 1);
        updateCartCount();
        Toast.makeText(this,
                "Added " + item.getName() + " x" + cart.get(id),
                Toast.LENGTH_SHORT).show();
    }

    private void updateCartCount() {
        int totalItems = 0;
        for (int quantity : cart.values()) {
            totalItems += quantity;
        }
        cartCount.setText(String.valueOf(totalItems));
    }

    // -----------------------------
    // View cart items
    // -----------------------------
    private void seeCartItems() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show the modal
        cartModal.setVisibility(View.VISIBLE);

        // Initialize modal views and listeners
        closeCart = findViewById(R.id.closeCart);
        clearCart = findViewById(R.id.clearCart);
        checkoutBtn = findViewById(R.id.checkoutBtn);
        LinearLayout cartItemsLayout = findViewById(R.id.cartItems);
        TextView cartTotal = findViewById(R.id.cartTotal);

        // Clear previous items
        cartItemsLayout.removeAllViews();

        int total = 0;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int id = entry.getKey();
            int qty = entry.getValue();
            Item item = findItemById(id);

            if (item != null) {
                View cartItemView = inflater.inflate(R.layout.cart_item, cartItemsLayout, false);
                TextView cartItemName = cartItemView.findViewById(R.id.cartItemName);
                TextView cartItemQuantity = cartItemView.findViewById(R.id.cartItemQuantity);

                cartItemName.setText(item.getName());
                cartItemQuantity.setText("x" + qty);
                cartItemsLayout.addView(cartItemView);

                total += item.getPrice() * qty;
            }
        }

        cartTotal.setText(String.valueOf(total));


        closeCart.setOnClickListener(v -> cartModal.setVisibility(View.GONE));
        clearCart.setOnClickListener(v -> {
            cart.clear();
            updateCartCount();
            Toast.makeText(ShopActivity.this, "Cart cleared", Toast.LENGTH_SHORT).show();
            cartModal.setVisibility(View.GONE);
        });
        checkoutBtn.setOnClickListener(v -> buyCartItems());
    }

    private Item findItemById(int id) {
        for (Item i : shopItems) {
            if (i.getId() == id) return i;
        }
        return null;
    }

    // -----------------------------
    // Buy items
    // -----------------------------

    private void buyCartItems() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Item> itemsToBuy = new ArrayList<>();
        int totalCost = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Item item = findItemById(entry.getKey());
            if (item != null) {
                item.setQuantity(entry.getValue());
                itemsToBuy.add(item);
                totalCost += item.getPrice() * entry.getValue();
            }
        }

        if (coins < totalCost) {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentUserId = getUserIdSafely();
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        BuyRequest purchaseRequest = new BuyRequest(currentUserId, itemsToBuy);

        int finalTotalCost = totalCost;
        api.buyItems(purchaseRequest).enqueue(new Callback<BuyResponse>() {
            @Override
            public void onResponse(Call<BuyResponse> call, Response<BuyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BuyResponse buyResponse = response.body();

                    //Deduct coins
                    coins -= finalTotalCost;
                    updateCoinsDisplay();

                    // No longer update SharedPreferences for coins
                    // SharedPreferences.Editor editor = prefs.edit();
                    // editor.putInt("coins", coins);
                    // editor.commit();

                    // Clear cart
                    cart.clear();
                    updateCartCount();
                    
                    cartModal.setVisibility(View.GONE);

                    // Show purchased items
                    StringBuilder purchased = new StringBuilder("Purchased items:");
                    for (Item item : buyResponse.getItems()) {
                        purchased.append(item.getName()).append(" x1"); // backend returns each item separately
                    }

                    Toast.makeText(ShopActivity.this, purchased.toString(), Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(ShopActivity.this, "Error purchasing items.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BuyResponse> call, Throwable t) {
                Toast.makeText(ShopActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}
