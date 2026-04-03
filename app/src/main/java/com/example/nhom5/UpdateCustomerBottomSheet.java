package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nhom5.databinding.LayoutUpdateCustomerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UpdateCustomerBottomSheet extends BottomSheetDialogFragment {

    private LayoutUpdateCustomerBinding binding;
    private final CustomerFragment.Customer customer;

    public UpdateCustomerBottomSheet(CustomerFragment.Customer customer) {
        this.customer = customer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutUpdateCustomerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (customer != null) {
            binding.tvId.setText(customer.id);
            binding.etName.setText(customer.name);
            binding.etPhone.setText(customer.phone);
        }

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> {
            // Show success dialog with message
            SuccessDialogFragment dialog = SuccessDialogFragment.newInstance("Cập nhật thông tin khách hàng thành công", () -> {
                dismiss();
            });
            dialog.show(getParentFragmentManager(), "success_dialog");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
