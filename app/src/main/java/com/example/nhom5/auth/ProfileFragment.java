package com.example.nhom5.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.api.ApiClient;
import com.example.nhom5.auth.model.UserDto;
import com.example.nhom5.databinding.FragmentProfileBinding;
import com.example.nhom5.models.CreateCustomerRequest;
import com.example.nhom5.models.CustomerApiModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private static final String TAG = "ProfileFragment";
    private int customerId = -1;
    private String userEmail = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        // 1. Hiển thị thông tin từ máy ngay lập tức để người dùng thấy dữ liệu mới nhất
        loadFromPrefs();
        
        // 2. Đồng bộ dữ liệu từ Server sau
        loadInitialData();
        
        binding.btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadFromPrefs() {
        if (getContext() == null) return;
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        
        String fullName = pref.getString("fullName", "");
        String phone = pref.getString("phone", "");
        String address = pref.getString("address", "");
        String username = pref.getString("username", "");
        String email = pref.getString("email", "");

        if (!fullName.isEmpty()) binding.etFullName.setText(fullName);
        if (!phone.isEmpty()) binding.etPhone.setText(phone);
        if (!address.isEmpty()) binding.etAddress.setText(address);
        if (!email.isEmpty()) binding.etEmail.setText(email);
        if (!username.isEmpty()) binding.tvUsername.setText("@" + username);
    }

    private void loadInitialData() {
        setLoading(true);
        ApiClient.getApiService().getProfile().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    userEmail = user.getEmail();
                    
                    // Chỉ cập nhật UI nếu dữ liệu từ server khác với những gì đang hiển thị (tránh bị reset ô nhập)
                    if (binding.etFullName.getText().toString().isEmpty()) {
                        updateUI(user);
                    }
                    
                    saveUserToPrefs(user);
                    findCustomerIdByEmail(userEmail);
                } else {
                    setLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                setLoading(false);
            }
        });
    }

    private void findCustomerIdByEmail(String email) {
        ApiClient.getApiService().getCustomers().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<CustomerApiModel> list = parseCustomers(response.body());
                    for (CustomerApiModel kh : list) {
                        if (email.equalsIgnoreCase(kh.getName()) || email.equalsIgnoreCase(kh.getCode()) 
                            || (kh.getPhone() != null && kh.getPhone().equals(binding.etPhone.getText().toString()))) {
                            customerId = kh.getId();
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) { setLoading(false); }
        });
    }

    private void saveChanges() {
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        
        // 3. LƯU VÀO MÁY NGAY LẬP TỨC (Optimistic Update)
        UserDto tempUpdate = new UserDto();
        tempUpdate.setFullName(fullName);
        tempUpdate.setPhone(phone);
        tempUpdate.setAddress(address);
        tempUpdate.setEmail(email);
        saveUserToPrefs(tempUpdate);

        CreateCustomerRequest customerReq = new CreateCustomerRequest(fullName, phone, email, address);
        
        if (customerId != -1) {
            ApiClient.getApiService().updateCustomer(customerId, customerReq).enqueue(new Callback<CustomerApiModel>() {
                @Override
                public void onResponse(@NonNull Call<CustomerApiModel> call, @NonNull Response<CustomerApiModel> response) {
                    updateUserProfileDiligently(fullName, phone, address);
                }
                @Override
                public void onFailure(@NonNull Call<CustomerApiModel> call, @NonNull Throwable t) {
                    updateUserProfileDiligently(fullName, phone, address);
                }
            });
        } else {
            ApiClient.getApiService().createCustomer(customerReq).enqueue(new Callback<CustomerApiModel>() {
                @Override
                public void onResponse(@NonNull Call<CustomerApiModel> call, @NonNull Response<CustomerApiModel> response) {
                    updateUserProfileDiligently(fullName, phone, address);
                }
                @Override
                public void onFailure(@NonNull Call<CustomerApiModel> call, @NonNull Throwable t) {
                    updateUserProfileDiligently(fullName, phone, address);
                }
            });
        }
    }

    private void updateUserProfileDiligently(String name, String phone, String address) {
        UserDto userUpdate = new UserDto();
        userUpdate.setFullName(name);
        userUpdate.setPhone(phone);
        userUpdate.setAddress(address);

        ApiClient.getApiService().updateProfile(userUpdate).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> response) {
                setLoading(false);
                UserDto finalUser = response.isSuccessful() && response.body() != null ? response.body() : userUpdate;
                saveUserToPrefs(finalUser);
                Toast.makeText(getContext(), "Đã lưu thay đổi thành công!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                setLoading(false);
                saveUserToPrefs(userUpdate);
                Toast.makeText(getContext(), "Đã cập nhật bảng khách hàng", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void saveUserToPrefs(UserDto user) {
        if (getContext() == null || user == null) return;
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (user.getFullName() != null) editor.putString("fullName", user.getFullName());
        if (user.getPhone() != null) editor.putString("phone", user.getPhone());
        if (user.getAddress() != null) editor.putString("address", user.getAddress());
        if (user.getEmail() != null) editor.putString("email", user.getEmail());
        editor.commit(); // Ép buộc lưu ngay lập tức
    }

    private List<CustomerApiModel> parseCustomers(JsonElement root) {
        List<CustomerApiModel> list = new ArrayList<>();
        Gson gson = new Gson();
        if (root.isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray()) list.add(gson.fromJson(e, CustomerApiModel.class));
        } else if (root.isJsonObject() && root.getAsJsonObject().has("results")) {
            for (JsonElement e : root.getAsJsonObject().getAsJsonArray("results")) list.add(gson.fromJson(e, CustomerApiModel.class));
        }
        return list;
    }

    private void updateUI(UserDto user) {
        if (binding == null) return;
        binding.etFullName.setText(user.getFullName());
        binding.etEmail.setText(user.getEmail());
        binding.etPhone.setText(user.getPhone());
        binding.etAddress.setText(user.getAddress());
        binding.tvUsername.setText("@" + user.getUsername());
    }

    private void setLoading(boolean isLoading) {
        if (binding != null) {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!isLoading);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
