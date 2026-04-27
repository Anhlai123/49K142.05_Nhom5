package com.example.nhom5.court;

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

import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtBinding;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    private BottomSheetConfirmDeleteCourtBinding binding;
    private String title;
    private String message;
    private String confirmButtonText;
    private Runnable onConfirmCallback;

    public static ConfirmDeleteDialogFragment newInstance(String message, Runnable onConfirmCallback) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        fragment.message = message;
        fragment.onConfirmCallback = onConfirmCallback;
        return fragment;
    }

    public static ConfirmDeleteDialogFragment newInstance(String title, String message, String confirmButtonText, Runnable onConfirmCallback) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        fragment.title = title;
        fragment.message = message;
        fragment.confirmButtonText = confirmButtonText;
        fragment.onConfirmCallback = onConfirmCallback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetConfirmDeleteCourtBinding.inflate(inflater, container, false);
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
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (title != null) {
            binding.tvTitle.setText(title);
        }
        if (message != null) {
            binding.tvMessage.setText(message);
        }
        if (confirmButtonText != null) {
            binding.btnConfirm.setText(confirmButtonText);
        }

        binding.btnCancel.setOnClickListener(v -> dismiss());
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
