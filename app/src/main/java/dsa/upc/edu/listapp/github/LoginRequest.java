package dsa.upc.edu.listapp.github;

public class LoginRequest {
    public final String username;
    public final String password;
    public final String name;
    public final String email;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.name = null;
        this.email = null;
    }
}
