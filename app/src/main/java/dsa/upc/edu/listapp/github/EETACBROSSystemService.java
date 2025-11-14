package dsa.upc.edu.listapp.github;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EETACBROSSystemService {
    @POST("eetacbros/user/register")
    Call<User> registerUser(@Body RegisterRequest request);

    @POST("eetacbros/user/login")
    Call<User> loginUser(@Body LoginRequest request);
}

