package com.example.nhom5.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom5.R;
import com.example.nhom5.databinding.LayoutUpdateStatusBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UpdateStatusBottomSheet extends BottomSheetDialogFragment {

    private LayoutUpdateStatusBinding binding;
    private OnStatusSelectedListener listener;

    public interface OnStatusSelectedListener {
        void onStatusSelected(String status);
    }

    public void setOnStatusSelectedListener(OnStatusSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutUpdateStatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.statusPending.setOnClickListener(v -> selectStatus("CHỜ XÁC NHẬN"));
        binding.statusCompleted.setOnClickListener(v -> selectStatus("HOÀN TẤT"));
        binding.statusCancelled.setOnClickListener(v -> selectStatus("ĐÃ HỦY"));
    }

    private void selectStatus(String status) {
        if (listener != null) {
            listener.onStatusSelected(status);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
