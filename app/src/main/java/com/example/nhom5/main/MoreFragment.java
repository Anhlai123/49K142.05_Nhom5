package com.example.nhom5.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentMoreBinding;

public class MoreFragment extends Fragment {

    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayUserInfo();
        setupMenuVisibility();
        setupActions();
    }

    private void displayUserInfo() {
        if (getContext() == null) return;
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String fullName = pref.getString("fullName", "");
        String role = pref.getString("role", "customer");
        String username = pref.getString("username", "");

        boolean isAdmin = "admin".equalsIgnoreCase(username) || "admin".equalsIgnoreCase(role);

        if (binding.tvUserName != null) {
            if (isAdmin) {
                binding.tvUserName.setText("QUẢN TRỊ VIÊN");
            } else {
                binding.tvUserName.setText(fullName.isEmpty() ? "Người dùng" : fullName);
            }
        }

        if (binding.tvUserRole != null) {
            if (isAdmin) {
                binding.tvUserRole.setText("Hệ thống quản trị");
            } else {
                String roleDisplay = "Khách hàng";
                if ("staff".equalsIgnoreCase(role)) roleDisplay = "Nhân viên";
                binding.tvUserRole.setText(roleDisplay);
            }
        }
    }

    private void setupMenuVisibility() {
        if (getContext() == null) return;
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = pref.getString("role", "customer");
        String username = pref.getString("username", "");

        // ĐỒNG BỘ LOGIC: Nếu username là admin HOẶC role là admin/staff thì là Manager
        boolean isManager = "admin".equalsIgnoreCase(username.trim()) 
                         || "admin".equalsIgnoreCase(role.trim()) 
                         || "staff".equalsIgnoreCase(role.trim())
                         || "1".equals(role.trim());

        if (isManager) {
            // Khi là Admin/Manager, ẩn phần "Tài khoản" và hiện phần "Quản lý"
            binding.tvLabelAccount.setVisibility(View.GONE);
            binding.cardAccount.setVisibility(View.GONE);

            binding.tvLabelManage.setVisibility(View.VISIBLE);
            binding.cardAdminActions.setVisibility(View.VISIBLE);
        } else {
            // Khi là Khách hàng, hiện phần "Tài khoản" và ẩn phần "Quản lý"
            binding.tvLabelAccount.setVisibility(View.VISIBLE);
            binding.cardAccount.setVisibility(View.VISIBLE);

            binding.tvLabelManage.setVisibility(View.GONE);
            binding.cardAdminActions.setVisibility(View.GONE);
        }
    }

    private void setupActions() {
        binding.btnMyProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_profileFragment);
        });

        binding.btnManageCourts.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_courtManagementFragment);
        });

        binding.btnManageCourtTypes.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_courtTypeManagementFragment);
        });

        binding.btnManagePrices.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_priceManagementFragment);
        });

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        pref.edit().clear().commit();
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.loginFragment);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayUserInfo();
        setupMenuVisibility();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
