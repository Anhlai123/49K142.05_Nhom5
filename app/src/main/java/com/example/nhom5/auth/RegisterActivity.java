package com.example.nhom5.auth;

import android.os.Bundle;
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
        setContentView(R.layout.activity_register);

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

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu tối thiểu 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void callRegisterApi(RegisterRequest request) {
        setLoading(true);

        ApiClient.getApiService().register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    String message = "Đăng ký thành công";
                    RegisterResponse body = response.body();
                    if (body != null && body.getMessage() != null && !body.getMessage().trim().isEmpty()) {
                        message = body.getMessage();
                    }
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                String errorMessage = parseErrorMessage(response);
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(
                    RegisterActivity.this,
                    "Không thể kết nối server: " + t.getLocalizedMessage(),
                    Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String parseErrorMessage(Response<RegisterResponse> response) {
        try {
            if (response.errorBody() == null) {
                return "Đăng ký thất bại. Vui lòng thử lại";
            }

            String raw = response.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) {
                return "Đăng ký thất bại. Vui lòng thử lại";
            }

            JSONObject json = new JSONObject(raw);
            if (json.has("username")) return json.getJSONArray("username").optString(0);
            if (json.has("email")) return json.getJSONArray("email").optString(0);
            if (json.has("phone")) return json.getJSONArray("phone").optString(0);
            if (json.has("password")) return json.getJSONArray("password").optString(0);
            if (json.has("detail")) return json.optString("detail");

            return "Đăng ký thất bại. Vui lòng kiểm tra thông tin";
        } catch (Exception e) {
            return "Đăng ký thất bại. Vui lòng thử lại";
        }
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? "Đang xử lý..." : "Đăng ký");
        tvBackToLogin.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }
}


