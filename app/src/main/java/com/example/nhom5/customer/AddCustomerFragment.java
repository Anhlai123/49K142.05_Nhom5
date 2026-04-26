package com.example.nhom5.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.nhom5.booking.SuccessDialogFragment;
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

        binding.tvId.setText("Tự động tạo từ hệ thống");

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.btnSave.setEnabled(isLoading == null || !isLoading)
        );

        binding.btnSave.setOnClickListener(v -> {
            boolean isValid = true;

            String name = binding.etName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String notes = binding.etNotes.getText().toString().trim();

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
                viewModel.createCustomer(name, phone, email, notes, new CustomerViewModel.CreateCustomerCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) {
                            return;
                        }
                        // Khôi phục popup thông báo thành công
                        SuccessDialogFragment dialog = SuccessDialogFragment.newInstance("Lưu thông tin thành công", () -> {
                            Navigation.findNavController(view).navigateUp();
                        });
                        dialog.show(getParentFragmentManager(), "success_dialog");
                    }

                    @Override
                    public void onError(String message) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
