package com.example.nhom5;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.nhom5.databinding.DialogSuccessBinding;

public class SuccessDialogFragment extends DialogFragment {

    private DialogSuccessBinding binding;
    private Runnable onExitCallback;

    public static SuccessDialogFragment newInstance(Runnable onExitCallback) {
        SuccessDialogFragment fragment = new SuccessDialogFragment();
        fragment.onExitCallback = onExitCallback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSuccessBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
