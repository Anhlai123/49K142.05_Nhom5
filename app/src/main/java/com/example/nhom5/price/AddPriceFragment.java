package com.example.nhom5.price;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nhom5.databinding.FragmentAddPriceBinding;

public class AddPriceFragment extends Fragment {

    private FragmentAddPriceBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPriceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnSave.setOnClickListener(v -> {
            // Handle save logic
            Navigation.findNavController(v).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}