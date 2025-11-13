package dsa.upc.edu.listapp.github;

public class RegisterRequest {
    public final String username;
    public final String nom;
    public final String email;
    public final String password;

    public RegisterRequest(String username, String nom,
                           String email, String password) {
        this.username = username;
        this.nom = nom;
        this.email = email;
        this.password = password;
    }
}
