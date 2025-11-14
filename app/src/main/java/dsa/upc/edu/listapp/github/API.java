package dsa.upc.edu.listapp.github;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API {

    //final static String URL = "http://localhost:8080/swagger/";
    //final static String URL = "http://10.0.2.2:8080/dsaApp/";
    //final static String URL = "http://10.0.2.2:8080/swagger/";
    final static String URL = "http://10.0.2.2:8080/example/";

    private static Retrofit retrofit;
    private static EETACBROSSystemService github;

    public static Retrofit getRetrofit() {
        if(retrofit==null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static EETACBROSSystemService getGithub() {
        if(github==null) {
            github = getRetrofit().create(EETACBROSSystemService.class);
        }
        return github;
    }

}
