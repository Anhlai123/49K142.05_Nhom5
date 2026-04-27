package com.example.nhom5.price;

import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.court.Court;
import com.example.nhom5.databinding.PriceMgmtAddBinding;
import com.example.nhom5.databinding.PriceMgmtTimeslotItemBinding;
import com.example.nhom5.models.CourtTypeModel;
import com.example.nhom5.models.PriceTableModel;
import com.example.nhom5.models.PriceTableTimeSlotModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPriceFragment extends Fragment {

    private static final String TAG = "AddPriceFragment";
    private static final String[] DAY_KEYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private PriceMgmtAddBinding binding;
    private final Set<String> selectedDays = new LinkedHashSet<>();
    private final Set<Integer> selectedCourtIds = new LinkedHashSet<>();
    private final List<String> selectedCourtNamesDisplay = new ArrayList<>();
    private final List<PriceMgmtTimeslotItemBinding> timeSlotBindings = new ArrayList<>();

    private List<CourtTypeModel> courtTypeList = new ArrayList<>();
    private CourtTypeModel selectedCourtType = null;
    private List<Court> availableCourtsForType = new ArrayList<>();
    private boolean allCourtsSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PriceMgmtAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCourtTypes();
        setupScopeSelection();
        setupCourtTypePicker();
        setupDaySelection();
        setupDatePickers();
        setupTimeSlotActions();
        updateScopeUi(true);

        // Add initial time slot
        addNewTimeSlot();

        binding.btnBack.setOnClickListener(v -> showExitConfirmDialog());
        binding.btnCancel.setOnClickListener(v -> showExitConfirmDialog());
        binding.btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void showExitConfirmDialog() {
        ConfirmExitDialogFragment dialog = ConfirmExitDialogFragment.newInstance(() -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
        dialog.show(getParentFragmentManager(), "confirm_exit_price_dialog");
    }

    private void loadCourtTypes() {
        ApiClient.getApiService().getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<CourtTypeModel>> call, @NonNull Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courtTypeList = response.body();
                    if (!courtTypeList.isEmpty()) {
                        selectedCourtType = courtTypeList.get(0);
                        updateCourtTypeLabel(selectedCourtType.getName());
                        loadCourtsForSelectedType(selectedCourtType.getId());
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<CourtTypeModel>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load court types", t);
            }
        });
    }

    private void loadCourtsForSelectedType(int courtTypeId) {
        selectedCourtIds.clear();
        selectedCourtNamesDisplay.clear();
        updateCourtSummary();

        ApiClient.getApiService().getCourts().enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(@NonNull Call<List<Court>> call, @NonNull Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableCourtsForType.clear();
                    for (Court court : response.body()) {
                        if (court.getCourtTypeId() != null && court.getCourtTypeId() == courtTypeId) {
                            availableCourtsForType.add(court);
                        }
                    }
                    if (!allCourtsSelected) populateCourtChips();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Court>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load courts", t);
            }
        });
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
        if (!allCourts) populateCourtChips();
    }

    private void styleScopePill(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_pill_active_light : R.drawable.bg_pill_inactive);
        view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.inactive));
    }

    private void setupCourtTypePicker() {
        binding.btnSelectCourtType.setOnClickListener(v -> {
            if (courtTypeList.isEmpty()) return;
            String[] names = new String[courtTypeList.size()];
            int checkedItem = 0;
            for (int i = 0; i < courtTypeList.size(); i++) {
                names[i] = courtTypeList.get(i).getName();
                if (selectedCourtType != null && courtTypeList.get(i).getId().equals(selectedCourtType.getId())) checkedItem = i;
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Chọn loại sân")
                    .setSingleChoiceItems(names, checkedItem, (dialog, which) -> {
                        selectedCourtType = courtTypeList.get(which);
                        updateCourtTypeLabel(selectedCourtType.getName());
                        loadCourtsForSelectedType(selectedCourtType.getId());
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void updateCourtTypeLabel(String courtType) {
        binding.tvSelectedCourtType.setText(courtType);
    }

    private void populateCourtChips() {
        binding.layoutCourtChips.removeAllViews();
        if (availableCourtsForType.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Không có sân nào thuộc loại này");
            tv.setTextSize(12);
            binding.layoutCourtChips.addView(tv);
            return;
        }
        for (Court court : availableCourtsForType) {
            binding.layoutCourtChips.addView(createCourtChip(court));
        }
        updateCourtSummary();
    }

    private MaterialButton createCourtChip(Court court) {
        MaterialButton chip = new MaterialButton(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);
        chip.setText(court.getName());
        chip.setAllCaps(false);
        chip.setCheckable(true);
        chip.setCornerRadius(dpToPx(20));
        chip.setStrokeWidth(dpToPx(1));
        chip.setTextSize(13f);
        
        chip.setOnClickListener(v -> {
            if (selectedCourtIds.contains(court.getId())) {
                selectedCourtIds.remove(court.getId());
                selectedCourtNamesDisplay.remove(court.getName());
            } else {
                selectedCourtIds.add(court.getId());
                selectedCourtNamesDisplay.add(court.getName());
            }
            setCourtChipState(chip, selectedCourtIds.contains(court.getId()));
            updateCourtSummary();
        });
        setCourtChipState(chip, selectedCourtIds.contains(court.getId()));
        return chip;
    }

    private void setCourtChipState(MaterialButton chip, boolean selected) {
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.white)));
        chip.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.inactive)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
    }

    private void updateCourtSummary() {
        binding.tvSelectedCourtsSummary.setText(selectedCourtNamesDisplay.isEmpty() ? "Chưa chọn sân nào" : "Đã chọn: " + TextUtils.join(", ", selectedCourtNamesDisplay));
    }

    private void setupDaySelection() {
        List<TextView> dayViews = Arrays.asList(binding.dayT2, binding.dayT3, binding.dayT4, binding.dayT5, binding.dayT6, binding.dayT7, binding.dayCn);
        for (int i = 0; i < dayViews.size(); i++) {
            final String day = DAY_KEYS[i];
            TextView view = dayViews.get(i);
            view.setOnClickListener(v -> {
                if (selectedDays.contains(day)) selectedDays.remove(day);
                else selectedDays.add(day);
                setDayState(view, selectedDays.contains(day));
            });
        }
    }

    private void setDayState(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_pill_active : R.drawable.bg_day_pill_inactive);
        view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
    }

    private void setupDatePickers() {
        binding.etStartDate.setOnClickListener(v -> showDatePicker(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(binding.etEndDate));
    }

    private void showDatePicker(android.widget.EditText target) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(selection -> target.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selection)));
        picker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void setupTimeSlotActions() {
        binding.btnAddFrame.setOnClickListener(v -> addNewTimeSlot());
    }

    private void addNewTimeSlot() {
        PriceMgmtTimeslotItemBinding slotBinding = PriceMgmtTimeslotItemBinding.inflate(getLayoutInflater(), binding.layoutTimeSlotsContainer, false);
        
        slotBinding.etStartTime.setOnClickListener(v -> showTimePicker(slotBinding.etStartTime));
        slotBinding.etEndTime.setOnClickListener(v -> showTimePicker(slotBinding.etEndTime));
        
        slotBinding.btnRemoveFrame.setOnClickListener(v -> {
            if (timeSlotBindings.size() > 1) {
                binding.layoutTimeSlotsContainer.removeView(slotBinding.getRoot());
                timeSlotBindings.remove(slotBinding);
                updateSlotTitles();
            } else {
                Toast.makeText(getContext(), "Phải có ít nhất một khung giờ", Toast.LENGTH_SHORT).show();
            }
        });

        binding.layoutTimeSlotsContainer.addView(slotBinding.getRoot());
        timeSlotBindings.add(slotBinding);
        updateSlotTitles();
    }

    private void updateSlotTitles() {
        for (int i = 0; i < timeSlotBindings.size(); i++) {
            timeSlotBindings.get(i).tvFrameTitle.setText("Khung giờ " + (i + 1));
        }
    }

    private void showTimePicker(android.widget.EditText target) {
        new TimePickerDialog(requireContext(), (view, hour, min) -> target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, min)), 7, 0, true).show();
    }

    private void validateAndSave() {
        String name = binding.etPriceName.getText().toString().trim();
        String startDateStr = binding.etStartDate.getText().toString().trim();
        String endDateStr = binding.etEndDate.getText().toString().trim();

        if (name.isEmpty() || selectedCourtType == null) {
            Toast.makeText(getContext(), "Vui lòng nhập tên bảng giá và chọn loại sân", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PriceTableTimeSlotModel> slots = new ArrayList<>();
        int index = 1;
        for (PriceMgmtTimeslotItemBinding slotBinding : timeSlotBindings) {
            String startTime = slotBinding.etStartTime.getText().toString().trim();
            String endTime = slotBinding.etEndTime.getText().toString().trim();
            String priceStr = slotBinding.etPrice.getText().toString().trim();

            if (startTime.isEmpty() || endTime.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Khung giờ " + index + ": Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (convertTimeToMinutes(endTime) <= convertTimeToMinutes(startTime)) {
                Toast.makeText(getContext(), "Khung giờ " + index + ": Giờ kết thúc phải lớn hơn giờ bắt đầu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra trùng lặp hoặc giao thoa khung giờ (Overlap check)
            if (isTimeOverlap(startTime, endTime, slots)) {
                Toast.makeText(getContext(), "Khung giờ " + index + " bị trùng lặp hoặc giao thoa với các khung giờ trước", Toast.LENGTH_SHORT).show();
                return;
            }

            PriceTableTimeSlotModel slot = new PriceTableTimeSlotModel();
            slot.setStartTime(startTime.length() == 5 ? startTime + ":00" : startTime);
            slot.setEndTime(endTime.length() == 5 ? endTime + ":00" : endTime);
            slot.setUnitPrice(priceStr);
            try {
                slot.setPrice(Double.parseDouble(priceStr));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Khung giờ " + index + ": Giá tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            slots.add(slot);
            index++;
        }

        PriceTableModel priceTable = new PriceTableModel();
        priceTable.setName(name);
        priceTable.setCourtTypeId(selectedCourtType.getId());
        priceTable.setAllCourts(allCourtsSelected);
        priceTable.setCourtIds(new ArrayList<>(selectedCourtIds));
        priceTable.setAppliedDays(new ArrayList<>(selectedDays));
        priceTable.setStartDate(startDateStr.isEmpty() ? null : convertDateFormat(startDateStr));
        priceTable.setEndDate(endDateStr.isEmpty() ? null : convertDateFormat(endDateStr));
        priceTable.setTimeSlots(slots);

        ApiClient.getApiService().createPriceTable(priceTable).enqueue(new Callback<PriceTableModel>() {
            @Override
            public void onResponse(@NonNull Call<PriceTableModel> call, @NonNull Response<PriceTableModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Thêm bảng giá thành công", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(errorBody);
                        String errorMsg = jsonObject.optString("error", "Lỗi: " + response.code());
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PriceTableModel> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private boolean isTimeOverlap(String start, String end, List<PriceTableTimeSlotModel> existingSlots) {
        int s1 = convertTimeToMinutes(start);
        int e1 = convertTimeToMinutes(end);
        for (PriceTableTimeSlotModel slot : existingSlots) {
            int s2 = convertTimeToMinutes(slot.getStartTime().substring(0, 5));
            int e2 = convertTimeToMinutes(slot.getEndTime().substring(0, 5));
            if (s1 < e2 && e1 > s2) return true;
        }
        return false;
    }

    private String convertDateFormat(String date) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return out.format(in.parse(date));
        } catch (Exception e) {
            return date;
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
