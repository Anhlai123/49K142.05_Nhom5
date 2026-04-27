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
                    // Sửa từ "tạo khách hàng" thành "tải danh sách khách hàng"
                    String message = buildHttpErrorMessage("tải danh sách khách hàng", response);
                    errorMessage.setValue(message);
                    return;
                }

                List<CustomerApiModel> parsedCustomers = parseCustomers(response.body());
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

                loadCustomers();
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
            id = (item.getId() != null) ? "KH" + item.getId() : "KH---";
        }

        String name = item.getName() == null ? "" : item.getName();
        String phone = item.getPhone() == null ? "" : item.getPhone();
        String email = item.getEmail() == null ? "" : item.getEmail();
        String notes = item.getNotes() == null ? "" : item.getNotes();
        
        return new CustomerFragment.Customer(name, id, phone, email, notes);
    }

    private List<CustomerApiModel> parseCustomers(JsonElement jsonElement) {
        List<CustomerApiModel> list = new ArrayList<>();
        if (jsonElement == null || jsonElement.isJsonNull()) return list;

        if (jsonElement.isJsonArray()) {
            JsonArray array = jsonElement.getAsJsonArray();
            for (JsonElement element : array) {
                CustomerApiModel item = safeParseCustomer(element);
                if (item != null) list.add(item);
            }
            return list;
        }

        if (jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            JsonArray itemsArray = null;
            
            String[] keys = {"results", "data", "customers", "items"};
            for (String key : keys) {
                if (obj.has(key) && obj.get(key).isJsonArray()) {
                    itemsArray = obj.getAsJsonArray(key);
                    break;
                }
            }

            if (itemsArray != null) {
                for (JsonElement element : itemsArray) {
                    CustomerApiModel item = safeParseCustomer(element);
                    if (item != null) list.add(item);
                }
            } else {
                CustomerApiModel item = safeParseCustomer(jsonElement);
                if (item != null && item.getId() != null) list.add(item);
            }
        }
        return list;
    }

    private CustomerApiModel safeParseCustomer(JsonElement element) {
        try {
            return gson.fromJson(element, CustomerApiModel.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildHttpErrorMessage(String action, Response<?> response) {
        return "Không thể " + action + " (HTTP " + response.code() + ")";
    }
}
