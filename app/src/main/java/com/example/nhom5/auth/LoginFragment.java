package com.example.nhom5.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Pre-filled values as requested initially
        binding.etUsername.setText("nhom5");
        binding.etPassword.setText("12345");

        binding.tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterActivity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            boolean isValid = true;

            // Reset errors
            binding.tvErrorUsername.setVisibility(View.GONE);
            binding.tvErrorPassword.setVisibility(View.GONE);

            // Check if empty
            if (TextUtils.isEmpty(username)) {
                binding.tvErrorUsername.setText("Vui lòng nhập tên đăng nhập");
                binding.tvErrorUsername.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (TextUtils.isEmpty(password)) {
                binding.tvErrorPassword.setText("Vui lòng nhập mật khẩu");
                binding.tvErrorPassword.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (!isValid) return;

            // Check credentials
            if (username.equals("nhom5") && password.equals("12345")) {
                // Success - navigate to Home
                Navigation.findNavController(v).navigate(R.id.navigation_home);
            } else {
                // Failed - show error on one of the fields or both
                binding.tvErrorUsername.setText("Sai tên đăng nhập/ mật khẩu");
                binding.tvErrorUsername.setVisibility(View.VISIBLE);
                binding.tvErrorPassword.setText("Sai tên đăng nhập/ mật khẩu");
                binding.tvErrorPassword.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
