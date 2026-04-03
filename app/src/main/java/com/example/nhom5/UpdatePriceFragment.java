package com.example.nhom5;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nhom5.databinding.FragmentUpdatePriceBinding;

public class UpdatePriceFragment extends Fragment {

    private FragmentUpdatePriceBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdatePriceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.pillAllScope.setOnClickListener(v -> setScopeSelected(true));
        binding.pillSpecificScope.setOnClickListener(v -> setScopeSelected(false));

        TextView[] dayPills = new TextView[] {
                binding.dayT2, binding.dayT3, binding.dayT4,
                binding.dayT5, binding.dayT6, binding.dayT7, binding.dayCn
        };
        for (TextView dayPill : dayPills) {
            dayPill.setOnClickListener(v -> toggleDayPill((TextView) v));
        }

        setScopeSelected(true);
        setDayPillState(binding.dayT2, true);
        setDayPillState(binding.dayT3, true);
        setDayPillState(binding.dayT4, true);
        setDayPillState(binding.dayT5, true);
        setDayPillState(binding.dayT6, true);
        setDayPillState(binding.dayT7, true);
        setDayPillState(binding.dayCn, true);

        binding.btnClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnSave.setOnClickListener(v -> {
            // Handle update logic
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void setScopeSelected(boolean allScope) {
        if (allScope) {
            binding.pillAllScope.setBackgroundResource(R.drawable.bg_pill_active);
            binding.pillAllScope.setTextColor(requireContext().getColor(R.color.primary));
            binding.pillSpecificScope.setBackgroundResource(R.drawable.bg_pill_inactive);
            binding.pillSpecificScope.setTextColor(requireContext().getColor(R.color.text_grey));
        } else {
            binding.pillAllScope.setBackgroundResource(R.drawable.bg_pill_inactive);
            binding.pillAllScope.setTextColor(requireContext().getColor(R.color.text_grey));
            binding.pillSpecificScope.setBackgroundResource(R.drawable.bg_pill_active);
            binding.pillSpecificScope.setTextColor(requireContext().getColor(R.color.primary));
        }
    }

    private void toggleDayPill(TextView textView) {
        setDayPillState(textView, !textView.isSelected());
    }

    private void setDayPillState(TextView textView, boolean selected) {
        textView.setSelected(selected);
        textView.setBackgroundResource(selected ? R.drawable.bg_day_pill_active : R.drawable.bg_day_pill_inactive);
        textView.setTextColor(requireContext().getColor(selected ? R.color.white : R.color.inactive));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}