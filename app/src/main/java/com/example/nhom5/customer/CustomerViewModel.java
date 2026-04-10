package com.example.nhom5.customer;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhom5.api.ApiClient;
import com.example.nhom5.api.ApiService;
import com.example.nhom5.models.CreateCustomerRequest;
import com.example.nhom5.models.CustomerApiModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerViewModel extends ViewModel {
    private final MutableLiveData<List<CustomerFragment.Customer>> customers = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ApiService apiService = ApiClient.getApiService();
    private final Gson gson = new Gson();

    public interface CreateCustomerCallback {
        void onSuccess();
        void onError(String message);
    }

    public CustomerViewModel() {
        customers.setValue(new ArrayList<>());
        loadCustomers();
    }

    public LiveData<List<CustomerFragment.Customer>> getCustomers() {
        return customers;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadCustomers() {
        loading.setValue(true);
        apiService.getCustomers().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                loading.setValue(false);
                if (!response.isSuccessful() || response.body() == null) {
                    errorMessage.setValue(buildHttpErrorMessage("tải danh sách khách hàng", response));
                    return;
                }

                List<CustomerApiModel> parsedCustomers = parseCustomers(response.body());
                if (parsedCustomers.isEmpty()) {
                    errorMessage.setValue("API trả về dữ liệu không đúng định dạng danh sách khách hàng");
                }

                List<CustomerFragment.Customer> mappedList = new ArrayList<>();
                for (CustomerApiModel item : parsedCustomers) {
                    mappedList.add(mapToUi(item));
                }
                customers.setValue(mappedList);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                loading.setValue(false);
                errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void createCustomer(String name, String phone, String email, String notes, CreateCustomerCallback callback) {
        loading.setValue(true);
        CreateCustomerRequest request = new CreateCustomerRequest(name, phone, email, notes);

        apiService.createCustomer(request).enqueue(new Callback<CustomerApiModel>() {
            @Override
            public void onResponse(@NonNull Call<CustomerApiModel> call, @NonNull Response<CustomerApiModel> response) {
                loading.setValue(false);
                if (!response.isSuccessful() || response.body() == null) {
                    String message = buildHttpErrorMessage("tạo khách hàng", response);
                    errorMessage.setValue(message);
                    callback.onError(message);
                    return;
                }

                List<CustomerFragment.Customer> currentList = customers.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                List<CustomerFragment.Customer> newList = new ArrayList<>(currentList);
                newList.add(mapToUi(response.body()));
                customers.setValue(newList);
                callback.onSuccess();
            }

            @Override
            public void onFailure(@NonNull Call<CustomerApiModel> call, @NonNull Throwable t) {
                loading.setValue(false);
                String message = "Lỗi mạng: " + t.getMessage();
                errorMessage.setValue(message);
                callback.onError(message);
            }
        });
    }

    private CustomerFragment.Customer mapToUi(CustomerApiModel item) {
        String id = item.getCode();
        if (id == null || id.trim().isEmpty()) {
            id = item.getId() != null ? "KH" + item.getId() : "KH---";
        }

        String name = item.getName() == null ? "" : item.getName();
        String phone = item.getPhone() == null ? "" : item.getPhone();
        return new CustomerFragment.Customer(name, id, phone);
    }

    private List<CustomerApiModel> parseCustomers(JsonElement jsonElement) {
        List<CustomerApiModel> list = new ArrayList<>();
        JsonArray itemsArray = extractCustomerArray(jsonElement);

        if (itemsArray == null && jsonElement != null && jsonElement.isJsonObject()) {
            // Fallback for APIs returning a single customer object.
            CustomerApiModel single = safeParseCustomer(jsonElement);
            if (single != null) {
                list.add(single);
            }
            return list;
        }

        if (itemsArray == null) {
            return list;
        }

        for (JsonElement element : itemsArray) {
            CustomerApiModel parsed = safeParseCustomer(element);
            if (parsed != null) {
                list.add(parsed);
            }
        }
        return list;
    }

    private JsonArray extractCustomerArray(JsonElement root) {
        if (root == null) {
            return null;
        }

        if (root.isJsonArray()) {
            return root.getAsJsonArray();
        }

        if (!root.isJsonObject()) {
            return null;
        }

        JsonObject object = root.getAsJsonObject();
        for (String key : Arrays.asList("results", "data", "customers", "items", "rows")) {
            JsonElement value = object.get(key);
            if (value == null || value.isJsonNull()) {
                continue;
            }
            if (value.isJsonArray()) {
                return value.getAsJsonArray();
            }
            if (value.isJsonObject()) {
                JsonArray nested = extractCustomerArray(value);
                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }

    private CustomerApiModel safeParseCustomer(JsonElement element) {
        try {
            return gson.fromJson(element, CustomerApiModel.class);
        } catch (Exception ignored) {
            // Skip malformed item to keep list rendering robust.
            return null;
        }
    }

    private String buildHttpErrorMessage(String action, Response<?> response) {
        String detail = "";
        try {
            if (response.errorBody() != null) {
                detail = response.errorBody().string();
            }
        } catch (Exception ignored) {
            detail = "";
        }

        String normalized = detail == null ? "" : detail.toLowerCase(Locale.US);
        if (response.code() == 400 && normalized.contains("invalid http_host header")) {
            return "HTTP 400: Django đang chặn host 10.0.2.2. Thêm 10.0.2.2 vào ALLOWED_HOSTS trong settings.py";
        }

        if (detail != null && !detail.trim().isEmpty()) {
            String compact = detail.replaceAll("\\s+", " ").trim();
            if (compact.length() > 160) {
                compact = compact.substring(0, 160) + "...";
            }
            return "Không thể " + action + " (HTTP " + response.code() + "): " + compact;
        }
        return "Không thể " + action + " (HTTP " + response.code() + ")";
    }
}
