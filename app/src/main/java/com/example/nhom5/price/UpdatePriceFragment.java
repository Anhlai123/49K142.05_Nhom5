package com.example.nhom5.price;

import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentUpdatePriceBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UpdatePriceFragment extends Fragment {

    private static final String[] DAY_KEYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private FragmentUpdatePriceBinding binding;
    private final Set<String> selectedDays = new LinkedHashSet<>();
    private final Set<String> selectedCourts = new LinkedHashSet<>();
    private final LinkedHashMap<String, List<String>> courtOptions = new LinkedHashMap<>();
    private String currentCourtType = "Sân Cầu lông";
    private boolean allCourtsSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUpdatePriceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCourtOptions();
        setupScopeSelection();
        setupCourtTypePicker();
        setupDaySelection();
        setupDatePickers();
        setupTimePickers();
        initializeFormValues();
        updateScopeUi(true);
        updateCourtTypeLabel(currentCourtType);

        binding.btnClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnSave.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnAddFrame.setOnClickListener(v -> {
            // Giữ nguyên nút thêm khung giờ.
        });
    }

    private void initCourtOptions() {
        courtOptions.put("Sân Cầu lông", Arrays.asList("Sân 1", "Sân 2", "Sân 3", "Sân 4"));
        courtOptions.put("Sân bóng đá", Arrays.asList("Sân 1", "Sân 2", "Sân 3"));
        courtOptions.put("Sân Tennis", Arrays.asList("Sân 1", "Sân 2"));
    }

    private void initializeFormValues() {
        binding.etStartDate.setText(getString(R.string.default_start_date));
        binding.etEndDate.setText(getString(R.string.default_end_date));
        binding.etStartTime.setText(getString(R.string.default_start_time));
        binding.etEndTime.setText(getString(R.string.default_end_time));
        Collections.addAll(selectedDays, DAY_KEYS);
        refreshDayStates();
    }

    private void setupScopeSelection() {
        binding.btnScopeAll.setOnClickListener(v -> updateScopeUi(true));
        binding.btnScopeSpecific.setOnClickListener(v -> updateScopeUi(false));
    }

    private void updateScopeUi(boolean allCourts) {
        allCourtsSelected = allCourts;
        styleScopePill(binding.btnScopeAll, allCourts);
        styleScopePill(binding.btnScopeSpecific, !allCourts);
        binding.layoutSpecificCourts.setVisibility(allCourts ? View.GONE : View.VISIBLE);
        if (!allCourts) {
            populateCourtChips(currentCourtType);
        }
    }

    private void styleScopePill(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.inactive));
    }

    private void setupCourtTypePicker() {
        binding.btnSelectType.setOnClickListener(v -> {
            List<String> types = new ArrayList<>(courtOptions.keySet());
            int checkedItem = Math.max(0, types.indexOf(currentCourtType));
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn loại sân")
                    .setSingleChoiceItems(types.toArray(new String[0]), checkedItem, (dialog, which) -> {
                        currentCourtType = types.get(which);
                        updateCourtTypeLabel(currentCourtType);
                        selectedCourts.clear();
                        if (!allCourtsSelected) {
                            populateCourtChips(currentCourtType);
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton("Huỷ", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void updateCourtTypeLabel(String courtType) {
        binding.tvSelectedCourtType.setText(courtType);
    }

    private void populateCourtChips(String courtType) {
        binding.layoutCourtChips.removeAllViews();
        List<String> courts = courtOptions.get(courtType);
        if (courts == null) courts = new ArrayList<>();
        selectedCourts.retainAll(courts);
        if (courts == null || courts.isEmpty()) {
            binding.tvSelectedCourtsSummary.setText(getString(R.string.no_courts_to_choose));
            return;
        }

        for (String court : courts) {
            MaterialButton chip = new MaterialButton(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(dpToPx(8));
            chip.setLayoutParams(params);
            chip.setText(court);
            chip.setAllCaps(false);
            chip.setCheckable(true);
            chip.setCornerRadius(dpToPx(20));
            chip.setStrokeWidth(dpToPx(1));
            chip.setTextSize(13f);
            chip.setMinHeight(dpToPx(40));
            chip.setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6));
            chip.setOnClickListener(v -> {
                if (selectedCourts.contains(court)) {
                    selectedCourts.remove(court);
                    setCourtChipState(chip, false);
                } else {
                    selectedCourts.add(court);
                    setCourtChipState(chip, true);
                }
                updateCourtSummary();
            });
            setCourtChipState(chip, selectedCourts.contains(court));
            binding.layoutCourtChips.addView(chip);
        }
        updateCourtSummary();
    }

    private void setCourtChipState(MaterialButton chip, boolean selected) {
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.white)));
        chip.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.inactive)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
    }

    private void updateCourtSummary() {
        if (selectedCourts.isEmpty()) {
            binding.tvSelectedCourtsSummary.setText(getString(R.string.no_courts_selected));
        } else {
            binding.tvSelectedCourtsSummary.setText(getString(R.string.selected_courts_summary, TextUtils.join(", ", selectedCourts)));
        }
    }

    private void setupDaySelection() {
        List<TextView> dayViews = Arrays.asList(
                binding.dayT2, binding.dayT3, binding.dayT4,
                binding.dayT5, binding.dayT6, binding.dayT7, binding.dayCn
        );
        for (int i = 0; i < dayViews.size(); i++) {
            final String day = DAY_KEYS[i];
            TextView view = dayViews.get(i);
            view.setOnClickListener(v -> {
                if (selectedDays.contains(day)) {
                    selectedDays.remove(day);
                } else {
                    selectedDays.add(day);
                }
                setDayState(view, selectedDays.contains(day));
            });
            setDayState(view, selectedDays.contains(day));
        }
    }

    private void refreshDayStates() {
        setDayState(binding.dayT2, selectedDays.contains("T2"));
        setDayState(binding.dayT3, selectedDays.contains("T3"));
        setDayState(binding.dayT4, selectedDays.contains("T4"));
        setDayState(binding.dayT5, selectedDays.contains("T5"));
        setDayState(binding.dayT6, selectedDays.contains("T6"));
        setDayState(binding.dayT7, selectedDays.contains("T7"));
        setDayState(binding.dayCn, selectedDays.contains("CN"));
    }

    private void setDayState(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_pill_active : R.drawable.bg_day_pill_inactive);
        view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
    }

    private void setupDatePickers() {
        binding.etStartDate.setOnClickListener(v -> showDatePicker(binding.etStartDate, "Chọn ngày hiệu lực"));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(binding.etEndDate, "Chọn ngày kết thúc"));
    }

    private void showDatePicker(android.widget.EditText target, String title) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> target.setText(formatDate(selection)));
        picker.show(getParentFragmentManager(), title.replace(" ", "_"));
    }

    private String formatDate(long selection) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(selection);
    }

    private void setupTimePickers() {
        binding.etStartTime.setOnClickListener(v -> showTimePicker(binding.etStartTime, 7));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(binding.etEndTime, 8));
    }

    private void showTimePicker(android.widget.EditText target, int defaultHour) {
        int[] time = parseTime(target.getText() == null ? null : target.getText().toString(), defaultHour, 0);
        new TimePickerDialog(requireContext(), (TimePicker view, int hourOfDay, int minute) ->
                target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                time[0], time[1], true).show();
    }

    private int[] parseTime(String value, int defaultHour, int defaultMinute) {
        if (value == null || !value.matches("\\d{2}:\\d{2}")) {
            return new int[]{defaultHour, defaultMinute};
        }
        try {
            String[] parts = value.split(":");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception ignored) {
            return new int[]{defaultHour, defaultMinute};
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}