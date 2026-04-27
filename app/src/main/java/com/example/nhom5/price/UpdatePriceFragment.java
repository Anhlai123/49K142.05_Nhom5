package com.example.nhom5.price;

import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.nhom5.databinding.PriceMgmtUpdateBinding;
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

public class UpdatePriceFragment extends Fragment {

    private static final String TAG = "UpdatePriceFragment";
    private static final String[] DAY_KEYS = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

    private PriceMgmtUpdateBinding binding;
    private final Set<String> selectedDays = new LinkedHashSet<>();
    private final Set<Integer> selectedCourtIds = new LinkedHashSet<>();
    private final List<String> selectedCourtNamesDisplay = new ArrayList<>();
    private final List<View> timeSlotViews = new ArrayList<>();

    private List<CourtTypeModel> courtTypeList = new ArrayList<>();
    private CourtTypeModel selectedCourtType = null;
    private List<Court> availableCourtsForType = new ArrayList<>();
    private boolean allCourtsSelected = true;
    private int priceTableId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = PriceMgmtUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            priceTableId = getArguments().getInt("priceId", -1);
        }

        loadCourtTypes();
        setupScopeSelection();
        setupDaySelection();
        setupDatePickers();

        binding.btnAddFrame.setOnClickListener(v -> addTimeSlotView(null));
        
        binding.btnClose.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnSave.setOnClickListener(v -> validateAndSave());

        if (priceTableId != -1) {
            loadPriceTableDetails();
        }
    }

    private void loadPriceTableDetails() {
        ApiClient.getApiService().getPriceTableDetail(priceTableId).enqueue(new Callback<PriceTableModel>() {
            @Override
            public void onResponse(@NonNull Call<PriceTableModel> call, @NonNull Response<PriceTableModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateData(response.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<PriceTableModel> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Không thể tải chi tiết bảng giá", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateData(PriceTableModel model) {
        binding.tvPriceId.setText(model.getPriceTableCode());
        binding.etPriceName.setText(model.getName());
        binding.etStartDate.setText(formatApiToUiDate(model.getStartDate()));
        binding.etEndDate.setText(formatApiToUiDate(model.getEndDate()));
        
        allCourtsSelected = model.isAllCourts();
        updateScopeUi(allCourtsSelected);
        
        if (model.getAppliedDays() != null) {
            selectedDays.addAll(model.getAppliedDays());
            refreshDayStates();
        }

        if (model.getTimeSlots() != null) {
            binding.layoutTimeSlotsContainer.removeAllViews();
            timeSlotViews.clear();
            for (PriceTableTimeSlotModel slot : model.getTimeSlots()) {
                addTimeSlotView(slot);
            }
        }
    }

    private void loadCourtTypes() {
        ApiClient.getApiService().getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<CourtTypeModel>> call, @NonNull Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courtTypeList = response.body();
                    // Setup picker after loading types
                    binding.btnSelectType.setOnClickListener(v -> showCourtTypePicker());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<CourtTypeModel>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load court types", t);
            }
        });
    }

    private void showCourtTypePicker() {
        if (courtTypeList.isEmpty()) return;
        String[] names = new String[courtTypeList.size()];
        int checkedItem = -1;
        for (int i = 0; i < courtTypeList.size(); i++) {
            names[i] = courtTypeList.get(i).getName();
            if (selectedCourtType != null && courtTypeList.get(i).getId().equals(selectedCourtType.getId())) checkedItem = i;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn loại sân")
                .setSingleChoiceItems(names, checkedItem, (dialog, which) -> {
                    selectedCourtType = courtTypeList.get(which);
                    binding.tvSelectedCourtType.setText(selectedCourtType.getName());
                    loadCourtsForSelectedType(selectedCourtType.getId());
                    dialog.dismiss();
                })
                .show();
    }

    private void loadCourtsForSelectedType(int courtTypeId) {
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
        if (!allCourts && selectedCourtType != null) loadCourtsForSelectedType(selectedCourtType.getId());
    }

    private void styleScopePill(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
    }

    private void populateCourtChips() {
        binding.layoutCourtChips.removeAllViews();
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

    private void refreshDayStates() {
        List<TextView> dayViews = Arrays.asList(binding.dayT2, binding.dayT3, binding.dayT4, binding.dayT5, binding.dayT6, binding.dayT7, binding.dayCn);
        for (int i = 0; i < dayViews.size(); i++) {
            setDayState(dayViews.get(i), selectedDays.contains(DAY_KEYS[i]));
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

    private void showDatePicker(EditText target) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
        picker.addOnPositiveButtonClickListener(selection -> target.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selection)));
        picker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void addTimeSlotView(PriceTableTimeSlotModel data) {
        View view = getLayoutInflater().inflate(R.layout.price_mgmt_timeslot_item, binding.layoutTimeSlotsContainer, false);
        
        TextView tvTitle = view.findViewById(R.id.tv_frame_title);
        tvTitle.setText("Khung giờ " + (timeSlotViews.size() + 1));
        
        EditText etStart = view.findViewById(R.id.et_start_time);
        EditText etEnd = view.findViewById(R.id.et_end_time);
        EditText etPrice = view.findViewById(R.id.et_price);
        View btnRemove = view.findViewById(R.id.btn_remove_frame);
        
        if (data != null) {
            etStart.setText(data.getStartTime().substring(0, 5));
            etEnd.setText(data.getEndTime().substring(0, 5));
            etPrice.setText(data.getUnitPrice());
        }

        etStart.setOnClickListener(v -> showTimePicker(etStart));
        etEnd.setOnClickListener(v -> showTimePicker(etEnd));
        
        btnRemove.setOnClickListener(v -> {
            if (timeSlotViews.size() > 1) {
                binding.layoutTimeSlotsContainer.removeView(view);
                timeSlotViews.remove(view);
                updateTimeSlotTitles();
            } else {
                Toast.makeText(getContext(), "Phải có ít nhất một khung giờ", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.layoutTimeSlotsContainer.addView(view);
        timeSlotViews.add(view);
    }

    private void updateTimeSlotTitles() {
        for (int i = 0; i < timeSlotViews.size(); i++) {
            TextView tvTitle = timeSlotViews.get(i).findViewById(R.id.tv_frame_title);
            tvTitle.setText("Khung giờ " + (i + 1));
        }
    }

    private void showTimePicker(EditText target) {
        new TimePickerDialog(requireContext(), (view, hour, min) -> target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, min)), 7, 0, true).show();
    }

    private void validateAndSave() {
        String name = binding.etPriceName.getText().toString().trim();
        String startDateStr = binding.etStartDate.getText().toString().trim();
        String endDateStr = binding.etEndDate.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        List<PriceTableTimeSlotModel> slots = new ArrayList<>();
        for (int i = 0; i < timeSlotViews.size(); i++) {
            View slotView = timeSlotViews.get(i);
            EditText etStart = slotView.findViewById(R.id.et_start_time);
            EditText etEnd = slotView.findViewById(R.id.et_end_time);
            EditText etPrice = slotView.findViewById(R.id.et_price);
            
            String startTime = etStart.getText().toString().trim();
            String endTime = etEnd.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            
            if (startTime.isEmpty() || endTime.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin cho các khung giờ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Kiểm tra trùng giờ với các khung giờ trước đó
            for (int j = 0; j < slots.size(); j++) {
                PriceTableTimeSlotModel existing = slots.get(j);
                if (isTimeOverlapping(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                    Toast.makeText(getContext(), "Khung giờ " + (i + 1) + " bị trùng với khung giờ " + (j + 1), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            PriceTableTimeSlotModel slot = new PriceTableTimeSlotModel();
            slot.setStartTime(startTime.length() == 5 ? startTime + ":00" : startTime);
            slot.setEndTime(endTime.length() == 5 ? endTime + ":00" : endTime);
            slot.setUnitPrice(priceStr);
            slots.add(slot);
        }

        PriceTableModel model = new PriceTableModel();
        model.setName(name);
        if (selectedCourtType != null) model.setCourtTypeId(selectedCourtType.getId());
        model.setStartDate(convertToApiDate(startDateStr));
        model.setEndDate(convertToApiDate(endDateStr));
        model.setAllCourts(allCourtsSelected);
        
        if (!allCourtsSelected) {
            model.setCourtIds(new ArrayList<>(selectedCourtIds));
        }

        model.setAppliedDays(new ArrayList<>(selectedDays));
        model.setTimeSlots(slots);
        
        updatePriceTableOnServer(model);
    }

    private boolean isTimeOverlapping(String start1, String end1, String start2, String end2) {
        // Chỉ lấy HH:mm để so sánh
        String s1 = start1.substring(0, 5);
        String e1 = end1.substring(0, 5);
        String s2 = start2.substring(0, 5);
        String e2 = end2.substring(0, 5);

        // Chuyển về phút để so sánh dễ hơn
        int startMin1 = timeToMinutes(s1);
        int endMin1 = timeToMinutes(e1);
        int startMin2 = timeToMinutes(s2);
        int endMin2 = timeToMinutes(e2);

        // Logic kiểm tra chồng lấn: (StartA < EndB) AND (EndA > StartB)
        return startMin1 < endMin2 && endMin1 > startMin2;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String convertToApiDate(String uiDate) {
        if (TextUtils.isEmpty(uiDate) || uiDate.equals("dd/MM/yyyy")) return null;
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(uiDate);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        } catch (Exception e) { return null; }
    }

    private String formatApiToUiDate(String apiDate) {
        if (TextUtils.isEmpty(apiDate)) return "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(apiDate);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) { return apiDate; }
    }

    private void updatePriceTableOnServer(PriceTableModel model) {
        binding.btnSave.setEnabled(false);
        ApiClient.getApiService().updatePriceTable(priceTableId, model).enqueue(new Callback<PriceTableModel>() {
            @Override
            public void onResponse(@NonNull Call<PriceTableModel> call, @NonNull Response<PriceTableModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                } else {
                    binding.btnSave.setEnabled(true);
                    try {
                        String errorBody = response.errorBody().string();
                        org.json.JSONObject jsonObject = new org.json.JSONObject(errorBody);
                        String errorMsg = jsonObject.optString("error", "Lỗi khi cập nhật");
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi khi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<PriceTableModel> call, @NonNull Throwable t) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
