package com.example.nhom5.booking;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.nhom5.databinding.CommonDialogSuccessBinding;

public class SuccessDialogFragment extends DialogFragment {

    private CommonDialogSuccessBinding binding;
    private Runnable onExitCallback;
    private String message;

    public static SuccessDialogFragment newInstance(String message, Runnable onExitCallback) {
        SuccessDialogFragment fragment = new SuccessDialogFragment();
        fragment.message = message;
        fragment.onExitCallback = onExitCallback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CommonDialogSuccessBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Thiết lập chiều rộng popup chiếm 85% màn hình để tránh bị bóp nhỏ
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            getDialog().getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (message != null) {
            binding.tvSuccessMessage.setText(message);
        }

        binding.btnExit.setOnClickListener(v -> {
            dismiss();
            if (onExitCallback != null) {
                onExitCallback.run();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
