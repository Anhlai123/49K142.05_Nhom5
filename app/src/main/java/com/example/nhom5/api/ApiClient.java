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
    // Chạy trên Android Emulator: sử dụng http://10.0.2.2:8000/ (mặc định cho Django)
//    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static final String BASE_URL = "http://192.168.1.21:8000/";
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
            // Logging Interceptor - để debug request/response
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                Log.d(TAG, "HTTP: " + message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Retry Interceptor - để retry khi lỗi kết nối
            Interceptor retryInterceptor = chain -> {
                IOException lastException = null;
                
                for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                    try {
                        Log.d(TAG, "Request attempt: " + attempt + " - " + chain.request().url());
                        okhttp3.Response response = chain.proceed(chain.request());
                        Log.d(TAG, "Request successful on attempt: " + attempt);
                        return response;
                    } catch (IOException e) {
                        lastException = e;
                        Log.w(TAG, "Request failed on attempt " + attempt + ": " + e.getMessage());
                        
                        if (attempt < MAX_RETRIES) {
                            try {
                                // Delay trước khi retry
                                Thread.sleep(RETRY_DELAY_MS);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }
                
                throw lastException;
            };

            // OkHttpClient với timeout, retry, và logging
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
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(retryInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)      // Timeout kết nối: 30 giây
                    .readTimeout(30, TimeUnit.SECONDS)         // Timeout đọc: 30 giây
                    .writeTimeout(30, TimeUnit.SECONDS)        // Timeout ghi: 30 giây
                    .retryOnConnectionFailure(true)            // Retry khi kết nối thất bại
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            Log.d(TAG, "Retrofit client created. Base URL: " + BASE_URL);
            Log.d(TAG, "Max retries: " + MAX_RETRIES + ", Retry delay: " + RETRY_DELAY_MS + "ms");
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (mContext == null) {
            throw new RuntimeException("ApiClient must be initialized with context before use");
        }
        return getClient().create(ApiService.class);
    }

    /**
     * Reset Retrofit client (để thay đổi BASE_URL nếu cần)
     */
    public static void resetClient() {
        retrofit = null;
    }
}
