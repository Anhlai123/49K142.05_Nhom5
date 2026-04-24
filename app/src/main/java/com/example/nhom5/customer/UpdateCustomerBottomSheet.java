package com.example.nhom5.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom5.booking.SuccessDialogFragment;
import com.example.nhom5.databinding.LayoutUpdateCustomerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UpdateCustomerBottomSheet extends BottomSheetDialogFragment {

    private LayoutUpdateCustomerBinding binding;
    private String customerId, customerName, customerPhone, customerEmail, customerNotes;

    public UpdateCustomerBottomSheet() {
        // Required empty public constructor
    }

    public static UpdateCustomerBottomSheet newInstance(CustomerFragment.Customer customer) {
        UpdateCustomerBottomSheet fragment = new UpdateCustomerBottomSheet();
        Bundle args = new Bundle();
        args.putString("id", customer.id);
        args.putString("name", customer.name);
        args.putString("phone", customer.phone);
        args.putString("email", customer.email);
        args.putString("notes", customer.notes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            customerId = getArguments().getString("id");
            customerName = getArguments().getString("name");
            customerPhone = getArguments().getString("phone");
            customerEmail = getArguments().getString("email");
            customerNotes = getArguments().getString("notes");
        }
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

        binding.tvId.setText(customerId);
        binding.etName.setText(customerName);
        binding.etPhone.setText(customerPhone);
        
        // Fix: Use data from customer object, not hardcoded strings
        binding.etEmail.setText(customerEmail != null ? customerEmail : "");
        binding.etNotes.setText(customerNotes != null ? customerNotes : "");

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> {
            // Logic to update customer via ViewModel should go here
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
