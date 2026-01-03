package dsa.upc.edu.listapp.github;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EETACBROSSystemService {
    @POST("eetacbros/user/register")
    Call<User> registerUser(@Body RegisterRequest request);
    
    @POST("eetacbros/user/login")
    Call<User> loginUser(@Body LoginRequest request);
    @POST("eetacbros/shop/buy")
    Call<BuyResponse> buyItems(@Body BuyRequest request);

    @GET("eetacbros/shop/items")
    Call<List<Item>> getItems();

    @GET("eetacbros/user/items/{userId}")
    Call<List<Item>> getUserItems(@Path("userId") int userId);

    @PUT("eetacbros/user/update")
    Call<User> updateUser(@Body User user);

    @PUT("eetacbros/user/updateScore")
    Call<User> updateUserScore(@Body User user);

    @DELETE("eetacbros/user/delete/{id}")
    Call<Void> deleteUser(@Path("id") int id);

    @GET("eetacbros/ranking/{userId}")
    Call<RankingResponse> getRanking(@Path("userId") int userId);

    @GET("eetacbros/faqs")
    Call<List<FAQ>> getFAQs();
}
