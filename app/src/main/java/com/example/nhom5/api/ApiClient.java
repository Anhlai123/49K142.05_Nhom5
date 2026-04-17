package com.example.nhom5.api;

import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Chạy trên Android Emulator: sử dụng http://10.0.2.2:8000/ (mặc định cho Django)
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static final String TAG = "ApiClient";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
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
        return getClient().create(ApiService.class);
    }

    /**
     * Reset Retrofit client (để thay đổi BASE_URL nếu cần)
     */
    public static void resetClient() {
        retrofit = null;
    }
}
