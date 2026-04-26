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

        // Giữ nguyên tiêu đề tĩnh: "Tính năng khác" và "Hệ thống đặt sân thể thao"
        // Không gọi displayUserInfo() để tránh ghi đè nội dung header

        setupMenuVisibility();
        setupActions();
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
            binding.tvLabelManage.setVisibility(View.VISIBLE);
            binding.cardAdminActions.setVisibility(View.VISIBLE);
        } else {
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
        setupMenuVisibility();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
