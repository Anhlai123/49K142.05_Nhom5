package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nhom5.databinding.LayoutOrderDetailsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class OrderDetailsBottomSheet extends BottomSheetDialogFragment {

    private LayoutOrderDetailsBinding binding;
    private final String orderId;

    public OrderDetailsBottomSheet(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutOrderDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvTitle.setText("Chi tiết đơn đặt: " + orderId);
        
        // Close buttons
        binding.btnCloseHeader.setOnClickListener(v -> dismiss());
        binding.btnCloseBottom.setOnClickListener(v -> dismiss());

        // Action stubs
        binding.btnConfirm.setOnClickListener(v -> {
            // Logic for confirming order
            dismiss();
        });

        binding.btnUpdateStatus.setOnClickListener(v -> {
            UpdateStatusBottomSheet updateStatusBottomSheet = new UpdateStatusBottomSheet();
            updateStatusBottomSheet.show(getParentFragmentManager(), "update_status");
        });

        binding.btnDelete.setOnClickListener(v -> {
            // Logic for deleting order
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
