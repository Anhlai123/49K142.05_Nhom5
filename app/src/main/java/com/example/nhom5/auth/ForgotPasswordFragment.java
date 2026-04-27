package com.example.nhom5.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.databinding.AuthForgotPasswordBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordFragment extends Fragment {

    private AuthForgotPasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AuthForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnReset.setOnClickListener(v -> {
            validateAndReset();
        });
    }

    private void validateAndReset() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String newPass = binding.etNewPassword.getText().toString().trim();
        String confirmPass = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo dữ liệu gửi lên server đúng với yêu cầu của API
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);
        data.put("phone", phone);
        data.put("new_password", newPass);
        data.put("confirm_password", confirmPass);

        binding.btnReset.setEnabled(false);
        binding.btnReset.setText("Đang xử lý...");

        // Gọi API reset mật khẩu
        ApiClient.getApiService().resetPassword(data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                binding.btnReset.setEnabled(true);
                binding.btnReset.setText("Đổi mật khẩu");

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.optString("detail", "Thông tin xác thực không chính xác");
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Thông tin xác thực không chính xác", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                binding.btnReset.setEnabled(true);
                binding.btnReset.setText("Đổi mật khẩu");
                Toast.makeText(getContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
