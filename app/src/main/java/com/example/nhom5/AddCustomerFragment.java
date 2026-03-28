package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.nhom5.databinding.FragmentAddCustomerBinding;

public class AddCustomerFragment extends Fragment {

    private FragmentAddCustomerBinding binding;
    private CustomerViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddCustomerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CustomerViewModel.class);

        // Auto-generated ID
        binding.tvId.setText("KH00004");

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        binding.btnSave.setOnClickListener(v -> {
            boolean isValid = true;

            String name = binding.etName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String id = binding.tvId.getText().toString();

            if (name.isEmpty()) {
                binding.tvErrorName.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                binding.tvErrorName.setVisibility(View.GONE);
            }

            if (phone.isEmpty()) {
                binding.tvErrorPhone.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                binding.tvErrorPhone.setVisibility(View.GONE);
            }

            if (isValid) {
                // Add the new customer to the shared ViewModel
                viewModel.addCustomer(new CustomerFragment.Customer(name, id, phone));
                
                // Show success dialog
                SuccessDialogFragment dialog = SuccessDialogFragment.newInstance(() -> {
                    Navigation.findNavController(view).navigateUp();
                });
                dialog.show(getParentFragmentManager(), "success_dialog");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
