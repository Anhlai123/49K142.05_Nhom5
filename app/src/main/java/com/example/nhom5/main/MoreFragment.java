package com.example.nhom5.main;

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

        // Setup click listeners for menu items
        binding.btnManageCourts.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_courtManagementFragment);
        });

        binding.btnManageCourtTypes.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_courtTypeManagementFragment);
        });

        binding.btnManagePrices.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_priceManagementFragment);
        });

        // Logout listener
        binding.btnLogout.setOnClickListener(v -> {
            // Navigate back to login screen and clear backstack
            Navigation.findNavController(v).navigate(R.id.loginFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
