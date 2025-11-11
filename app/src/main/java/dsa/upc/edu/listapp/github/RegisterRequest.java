package dsa.upc.edu.listapp.github;

public class RegisterRequest {
    public final String username;
    public final String nom;
    public final String cognom1;
    public final String cognom2;
    public final String email;
    public final String password;
    public final String datanaixement;

    public RegisterRequest(String username, String nom, String cognom1, String cognom2,
                           String email, String password, String datanaixement) {
        this.username = username;
        this.nom = nom;
        this.cognom1 = cognom1;
        this.cognom2 = cognom2;
        this.email = email;
        this.password = password;
        this.datanaixement = datanaixement;
    }
}
