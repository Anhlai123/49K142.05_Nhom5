package com.example.nhom5.price;

import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.nhom5.databinding.FragmentAddPriceBinding;
import com.example.nhom5.models.CourtTypeModel;
import com.example.nhom5.models.PriceTableModel;
import com.example.nhom5.models.PriceTableTimeSlotModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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

    private FragmentAddPriceBinding binding;
    private final Set<String> selectedDays = new LinkedHashSet<>();
    private final Set<String> selectedCourts = new LinkedHashSet<>();

    private List<CourtTypeModel> courtTypeList = new ArrayList<>();
    private CourtTypeModel selectedCourtType = null;
    private boolean allCourtsSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPriceBinding.inflate(inflater, container, false);
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
        setupTimePickers();
        updateScopeUi(true);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnSave.setOnClickListener(v -> validateAndSave());

        binding.btnAddFrame.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng thêm nhiều khung giờ đang được phát triển", Toast.LENGTH_SHORT).show();
        });
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
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<CourtTypeModel>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load court types", t);
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
        if (!allCourts) populateCourtChipsPlaceholder();
    }

    private void styleScopePill(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.bg_pill_active : R.drawable.bg_pill_inactive);
        view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
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
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void updateCourtTypeLabel(String courtType) {
        binding.tvSelectedCourtType.setText(courtType);
    }

    private void populateCourtChipsPlaceholder() {
        binding.layoutCourtChips.removeAllViews();
        for (String court : Arrays.asList("Sân 1", "Sân 2", "Sân 3", "Sân 4")) {
            binding.layoutCourtChips.addView(createChip(court));
        }
        updateCourtSummary();
    }

    private MaterialButton createChip(String text) {
        MaterialButton chip = new MaterialButton(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);
        chip.setText(text);
        chip.setAllCaps(false);
        chip.setCheckable(true);
        chip.setCornerRadius(dpToPx(20));
        chip.setStrokeWidth(dpToPx(1));
        chip.setTextSize(13f);
        chip.setOnClickListener(v -> {
            if (selectedCourts.contains(text)) selectedCourts.remove(text);
            else selectedCourts.add(text);
            setCourtChipState(chip, selectedCourts.contains(text));
            updateCourtSummary();
        });
        setCourtChipState(chip, selectedCourts.contains(text));
        return chip;
    }

    private void setCourtChipState(MaterialButton chip, boolean selected) {
        chip.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.white)));
        chip.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), selected ? R.color.primary : R.color.inactive)));
        chip.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.inactive));
    }

    private void updateCourtSummary() {
        binding.tvSelectedCourtsSummary.setText(selectedCourts.isEmpty() ? "Chưa chọn sân nào" : "Đã chọn: " + TextUtils.join(", ", selectedCourts));
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

    private void setupTimePickers() {
        binding.etStartTime.setOnClickListener(v -> showTimePicker(binding.etStartTime));
        binding.etEndTime.setOnClickListener(v -> showTimePicker(binding.etEndTime));
    }

    private void showTimePicker(android.widget.EditText target) {
        new TimePickerDialog(requireContext(), (view, hour, min) -> target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, min)), 7, 0, true).show();
    }

    private void validateAndSave() {
        String name = binding.etPriceName.getText().toString().trim();
        String startDate = binding.etStartDate.getText().toString().trim();
        String endDate = binding.etEndDate.getText().toString().trim();
        String startTime = binding.etStartTime.getText().toString().trim();
        String endTime = binding.etEndTime.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();

        if (name.isEmpty() || selectedCourtType == null || priceStr.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        PriceTableModel model = new PriceTableModel();
        model.setName(name);
        model.setCourtTypeId(selectedCourtType.getId());
        model.setStartDate(convertToApiDate(startDate));
        model.setEndDate(convertToApiDate(endDate));
        model.setAllCourts(allCourtsSelected);

        // Chỉnh Day Mapping: T2=1, ..., CN=7 (Chuẩn ISO/Django)
        List<Integer> daysInt = new ArrayList<>();
        for (int i = 0; i < DAY_KEYS.length; i++) {
            if (selectedDays.contains(DAY_KEYS[i])) daysInt.add(i + 1);
        }
        model.setActiveDays(daysInt);

        // Chuẩn hóa Time Slot HH:mm:ss
        PriceTableTimeSlotModel slot = new PriceTableTimeSlotModel();
        slot.setStartTime(startTime.length() == 5 ? startTime + ":00" : startTime);
        slot.setEndTime(endTime.length() == 5 ? endTime + ":00" : endTime);
        slot.setPrice(Double.parseDouble(priceStr));
        model.setTimeSlots(Arrays.asList(slot));

        savePriceTable(model);
    }

    private String convertToApiDate(String uiDate) {
        if (TextUtils.isEmpty(uiDate) || uiDate.contains("d")) return null;
        try {
            Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(uiDate);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        } catch (Exception e) { return null; }
    }

    private void savePriceTable(PriceTableModel model) {
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Đang lưu...");

        ApiClient.getApiService().createPriceTable(model).enqueue(new Callback<PriceTableModel>() {
            @Override
            public void onResponse(@NonNull Call<PriceTableModel> call, @NonNull Response<PriceTableModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                } else {
                    handleErrorResponse(response);
                }
            }
            @Override
            public void onFailure(@NonNull Call<PriceTableModel> call, @NonNull Throwable t) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("Lưu Bảng giá");
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(Response<PriceTableModel> response) {
        binding.btnSave.setEnabled(true);
        binding.btnSave.setText("Lưu Bảng giá");
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject json = new JSONObject(errorJson);
                StringBuilder sb = new StringBuilder("Lỗi: ");
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    sb.append(key).append(": ").append(json.get(key)).append("\n");
                }
                Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi: 400 Bad Request", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }
    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
