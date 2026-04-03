package com.example.nhom5.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom5.R;
import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtBinding;
import com.example.nhom5.databinding.LayoutOrderDetailsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
        
        // Initial status simulation
        String initialStatus = "CHỜ XÁC NHẬN";
        if (orderId.equals("DDS001")) initialStatus = "ĐÃ XÁC NHẬN";
        if (orderId.equals("DDS003")) initialStatus = "HOÀN TẤT";
        
        updateStatusUI(initialStatus);

        // Close buttons
        binding.btnCloseHeader.setOnClickListener(v -> dismiss());
        binding.btnCloseBottom.setOnClickListener(v -> dismiss());

        // Nút xác nhận đơn
        binding.btnConfirm.setOnClickListener(v -> {
            updateStatusUI("ĐÃ XÁC NHẬN");
        });

        // Nút cập nhật trạng thái
        binding.btnUpdateStatus.setOnClickListener(v -> {
            UpdateStatusBottomSheet updateStatusBottomSheet = new UpdateStatusBottomSheet();
            updateStatusBottomSheet.setOnStatusSelectedListener(status -> {
                updateStatusUI(status);
            });
            updateStatusBottomSheet.show(getParentFragmentManager(), "update_status");
        });

        // Nút xóa đơn
        binding.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmBottomSheet();
        });
    }

    private void showDeleteConfirmBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeleteCourtBinding sheetBinding = BottomSheetConfirmDeleteCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvTitle.setText("Xác nhận xóa đơn");
        sheetBinding.tvMessage.setText("Bạn có muốn xóa đơn đặt này?");

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            dismiss(); // Sau khi đồng ý xóa thì đóng bottom sheet chi tiết
        });

        bottomSheetDialog.show();
    }

    private void updateStatusUI(String status) {
        binding.tvStatus.setText(status);
        
        if (status.equalsIgnoreCase("ĐÃ XÁC NHẬN") || 
            status.equalsIgnoreCase("HOÀN TẤT") || 
            status.equalsIgnoreCase("ĐÃ HỦY")) {
            binding.btnConfirm.setVisibility(View.GONE);
        } else {
            binding.btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
