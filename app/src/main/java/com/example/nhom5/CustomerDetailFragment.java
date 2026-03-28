package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nhom5.databinding.FragmentCustomerDetailsBinding;

public class CustomerDetailFragment extends Fragment {

    private FragmentCustomerDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCustomerDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        binding.btnEdit.setOnClickListener(v -> {
            // Creating a dummy customer based on the hardcoded data in the layout
            // In a real app, this should be passed as an argument to this fragment
            CustomerFragment.Customer customer = new CustomerFragment.Customer("Nguyễn Văn A", "KH00001", "0901 234 567");
            UpdateCustomerBottomSheet bottomSheet = new UpdateCustomerBottomSheet(customer);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
