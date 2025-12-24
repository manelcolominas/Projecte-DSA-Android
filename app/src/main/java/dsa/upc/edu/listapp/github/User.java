package dsa.upc.edu.listapp.github;

public class User {
    public final int id;
    public final String username;
    public final String name;
    public final String email;
    public final String password;
    public final int coins;
    public final int score;


    public User(int id, String username, String name, String email, String password, int coins, int score) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.coins = coins;
        this.score = score;
    }
}
