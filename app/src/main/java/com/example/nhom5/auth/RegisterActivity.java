package com.example.nhom5.auth;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.auth.model.RegisterRequest;
import com.example.nhom5.auth.model.RegisterResponse;
import com.example.nhom5.models.CreateCustomerRequest;
import com.example.nhom5.models.CustomerApiModel;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etFullName;
    private EditText etPhone;
    private EditText etAddress;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_register_activity);

        bindViews();
        setupActions();
    }

    private void bindViews() {
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnRegister = findViewById(R.id.btn_register);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
    }

    private void setupActions() {
        tvBackToLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String fullName = etFullName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (!validateInput(username, email, password, fullName, phone, address)) {
                return;
            }

            RegisterRequest request = new RegisterRequest(
                username,
                email,
                password,
                fullName,
                phone,
                address
            );
            callRegisterApi(request);
        });
    }

    private boolean validateInput(
        String username,
        String email,
        String password,
        String fullName,
        String phone,
        String address
    ) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()
            || fullName.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void callRegisterApi(RegisterRequest request) {
        setLoading(true);

        ApiClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    // Sau khi đăng ký User thành công, tự động tạo thông tin Khách hàng
                    createCustomerRecord(request);
                } else {
                    setLoading(false);
                    String errorMessage = parseErrorMessage(response);
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createCustomerRecord(RegisterRequest request) {
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(
                request.getFullName(),
                request.getPhone(),
                request.getEmail(),
                request.getAddress() // Ghi địa chỉ vào phần ghi chú (notes) của khách hàng
        );

        ApiClient.getApiService().createCustomer(customerRequest).enqueue(new Callback<CustomerApiModel>() {
            @Override
            public void onResponse(@NonNull Call<CustomerApiModel> call, @NonNull Response<CustomerApiModel> response) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(@NonNull Call<CustomerApiModel> call, @NonNull Throwable t) {
                setLoading(false);
                // Vẫn cho phép thành công vì User đã được tạo
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công (Lỗi đồng bộ khách hàng)", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private String parseErrorMessage(Response<RegisterResponse> response) {
        try {
            if (response.errorBody() == null) return "Đăng ký thất bại";
            String raw = response.errorBody().string();
            JSONObject json = new JSONObject(raw);
            if (json.has("username")) return "Tên đăng nhập đã tồn tại";
            if (json.has("email")) return "Email đã tồn tại";
            if (json.has("detail")) return json.optString("detail");
            return "Lỗi đăng ký: " + raw;
        } catch (Exception e) {
            return "Đăng ký thất bại";
        }
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? "Đang xử lý..." : "Đăng ký");
    }
}
