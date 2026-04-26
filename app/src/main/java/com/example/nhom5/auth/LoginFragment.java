package com.example.nhom5.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.auth.model.LoginRequest;
import com.example.nhom5.auth.model.LoginResponse;
import com.example.nhom5.auth.model.UserDto;
import com.example.nhom5.databinding.FragmentLoginBinding;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private boolean isPasswordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.passwordContainer.getChildAt(2).setOnClickListener(v -> togglePasswordVisibility());

        binding.tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterActivity.class);
            startActivity(intent);
        });

        // Thiết lập sự kiện click cho Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String input = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validateInput(input, password)) {
                callLoginApi(input, password);
            }
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        binding.etPassword.setSelection(binding.etPassword.getText().length());
    }

    private boolean validateInput(String input, String password) {
        boolean isValid = true;
        binding.tvErrorUsername.setVisibility(View.GONE);
        binding.tvErrorPassword.setVisibility(View.GONE);

        if (TextUtils.isEmpty(input)) {
            binding.tvErrorUsername.setText("Vui lòng nhập tên đăng nhập/email");
            binding.tvErrorUsername.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tvErrorPassword.setText("Vui lòng nhập mật khẩu");
            binding.tvErrorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        }
        return isValid;
    }

    private void callLoginApi(String input, String password) {
        setLoading(true);
        LoginRequest request = new LoginRequest(input, password);

        ApiClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse data = response.body();
                    
                    // 1. Lưu token trước
                    saveToken(data.getToken());

                    // 2. Nếu có sẵn user trong response thì lưu và vào Home luôn
                    if (data.getUser() != null) {
                        saveUserInfo(data.getUser());
                        proceedToHome();
                    } else {
                        // 3. Nếu không có user, phải gọi API lấy Profile để biết Role
                        fetchUserProfile();
                    }
                } else {
                    setLoading(false);
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserProfile() {
        ApiClient.getApiService().getProfile().enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    saveUserInfo(response.body());
                    proceedToHome();
                } else {
                    Toast.makeText(getContext(), "Không thể lấy thông tin phân quyền", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(getContext(), "Lỗi lấy profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        pref.edit().putString("token", token).commit();
    }

    private void saveUserInfo(UserDto user) {
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        
        String role = user.getRole();
        if (role != null) role = role.trim().toLowerCase();
        
        editor.putString("role", role);
        editor.putString("username", user.getUsername());
        editor.putString("fullName", user.getFullName());
        editor.commit();
        
        Log.d("LoginFragment", "User Info Saved - Role: " + role + ", User: " + user.getUsername());
    }

    private void proceedToHome() {
        Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.navigation_home);
        }
    }

    private void handleLoginError(Response<LoginResponse> response) {
        String errorMsg = "Sai thông tin đăng nhập";
        try {
            if (response.errorBody() != null) {
                String rawError = response.errorBody().string();
                JSONObject json = new JSONObject(rawError);
                if (json.has("detail")) errorMsg = json.getString("detail");
            }
        } catch (Exception e) {
            Log.e("LoginFragment", "Parse error", e);
        }
        binding.tvErrorUsername.setText(errorMsg);
        binding.tvErrorUsername.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean isLoading) {
        if (binding == null) return;
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "Đang xử lý..." : "Đăng nhập");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
