package com.example.nhom5.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Đảm bảo không có khoảng trắng dư thừa trong chuỗi URL
    private static final String BASE_URL = "http://192.168.1.14:8000/";
    private static final String TAG = "ApiClient";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private static Retrofit retrofit = null;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d(TAG, "HTTP: " + message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor retryInterceptor = chain -> {
                IOException lastException = null;
                for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                    try {
                        return chain.proceed(chain.request());
                    } catch (IOException e) {
                        lastException = e;
                        if (attempt < MAX_RETRIES) {
                            try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { break; }
                        }
                    }
                }
                throw lastException;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        String path = originalRequest.url().encodedPath();

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
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(retryInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
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

    public static void resetClient() {
        retrofit = null;
    }
}
