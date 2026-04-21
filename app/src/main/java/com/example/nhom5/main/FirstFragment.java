package com.example.nhom5.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkRoleAndSetupUI();

        binding.btnAddCustomer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addCustomerFragment);
        });
    }

    private void checkRoleAndSetupUI() {
        if (getContext() == null) return;
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = pref.getString("role", "customer");
        String username = pref.getString("username", "");

        // Logic đồng bộ: admin hoặc staff hoặc username là admin
        boolean isManager = "admin".equalsIgnoreCase(username.trim()) 
                         || "admin".equalsIgnoreCase(role.trim()) 
                         || "staff".equalsIgnoreCase(role.trim())
                         || "1".equals(role.trim());

        if (isManager) {
            binding.btnAddCustomer.setVisibility(View.VISIBLE);
        } else {
            binding.btnAddCustomer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkRoleAndSetupUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
