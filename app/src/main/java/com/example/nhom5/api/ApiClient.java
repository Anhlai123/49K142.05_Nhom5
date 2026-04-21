package com.example.nhom5.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static Retrofit retrofit = null;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        String path = originalRequest.url().encodedPath();

                        // KHÔNG gửi token cho API login và register
                        if (path.contains("/api/login/") || path.contains("/api/register/")) {
                            return chain.proceed(originalRequest);
                        }

                        SharedPreferences prefs = mContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", "");

                        Request.Builder builder = originalRequest.newBuilder();
                        if (!token.isEmpty()) {
                            builder.addHeader("Authorization", "Token " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (mContext == null) {
            throw new RuntimeException("ApiClient must be initialized with context before use");
        }
        return getClient().create(ApiService.class);
    }
}
