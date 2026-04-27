package com.example.nhom5.price;

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

import com.example.nhom5.databinding.CommonDialogExitBinding;

public class ConfirmExitDialogFragment extends DialogFragment {

    private CommonDialogExitBinding binding;
    private Runnable onConfirmCallback;

    public static ConfirmExitDialogFragment newInstance(Runnable onConfirmCallback) {
        ConfirmExitDialogFragment fragment = new ConfirmExitDialogFragment();
        fragment.onConfirmCallback = onConfirmCallback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CommonDialogExitBinding.inflate(inflater, container, false);
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
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.80);
            getDialog().getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCloseDialog.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            dismiss();
            if (onConfirmCallback != null) {
                onConfirmCallback.run();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
