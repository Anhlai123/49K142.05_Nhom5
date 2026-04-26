package com.example.nhom5.price;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.databinding.BottomSheetConfirmDeletePriceBinding;
import com.example.nhom5.databinding.BottomSheetPriceDetailsBinding;
import com.example.nhom5.databinding.FragmentPriceManagementBinding;
import com.example.nhom5.models.PriceTableModel;
import com.example.nhom5.models.PriceTableTimeSlotModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceManagementFragment extends Fragment {

    private FragmentPriceManagementBinding binding;
    private PriceAdapter adapter;
    private List<PriceRecord> allPriceRecords = new ArrayList<>();
    private final DecimalFormat currencyFormatter = new DecimalFormat("#,###");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPriceManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        setupRecyclerView();
        setupSearch();
        loadPriceData();

        binding.fabAdd.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_priceManagementFragment_to_addPriceFragment);
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPrices(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterPrices(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allPriceRecords);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<PriceRecord> filteredList = new ArrayList<>();
        for (PriceRecord record : allPriceRecords) {
            if (record.getTitle().toLowerCase().contains(lowerQuery) || 
                record.getId().toLowerCase().contains(lowerQuery)) {
                filteredList.add(record);
            }
        }
        adapter.updateData(filteredList);
    }

    private void setupRecyclerView() {
        adapter = new PriceAdapter(new ArrayList<>(), new PriceAdapter.OnPriceActionListener() {
            @Override
            public void onView(PriceRecord price) {
                showPriceDetailsBottomSheet(price);
            }

            @Override
            public void onEdit(PriceRecord price) {
                Bundle args = new Bundle();
                args.putInt("priceId", price.getInternalId());
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_priceManagementFragment_to_updatePriceFragment, args);
            }

            @Override
            public void onDelete(PriceRecord price) {
                showDeleteConfirmBottomSheet(price);
            }
        });
        binding.rvPrices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPrices.setAdapter(adapter);
    }

    private void loadPriceData() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        
        ApiClient.getApiService().getPriceTables().enqueue(new Callback<List<PriceTableModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<PriceTableModel>> call, @NonNull Response<List<PriceTableModel>> response) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    mapApiDataToDisplay(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PriceTableModel>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Load prices failed: " + t.getMessage());
            }
        });
    }

    private void mapApiDataToDisplay(List<PriceTableModel> apiData) {
        allPriceRecords.clear();
        for (PriceTableModel item : apiData) {
            String code = item.getPriceTableCode() != null ? item.getPriceTableCode() : "BG" + item.getId();
            String courtType = item.getCourtTypeName() != null ? item.getCourtTypeName() : "Chưa xác định";
            String dateRange = (item.getStartDate() != null ? item.getStartDate() : "N/A") + " ~ " + (item.getEndDate() != null ? item.getEndDate() : "Vô thời hạn");

            List<String> appliedDays = item.getAppliedDays() != null ? item.getAppliedDays() : new ArrayList<>();

            PriceRecord record = new PriceRecord(
                    code,
                    item.getName(),
                    courtType,
                    dateRange,
                    "Đang tải giá...",
                    appliedDays,
                    "..."
            );
            record.setInternalId(item.getId());
            record.setApplyScope(item.getApplyScope());
            record.setFullModel(item);
            allPriceRecords.add(record);
        }
        filterPrices(binding.etSearch.getText().toString());
        fetchTimeSlotsAndRefreshPrices();
    }

    private void fetchTimeSlotsAndRefreshPrices() {
        ApiClient.getApiService().getPriceTableTimeSlots().enqueue(new Callback<List<PriceTableTimeSlotModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<PriceTableTimeSlotModel>> call, @NonNull Response<List<PriceTableTimeSlotModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updatePriceRanges(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PriceTableTimeSlotModel>> call, @NonNull Throwable t) {}
        });
    }

    private void updatePriceRanges(List<PriceTableTimeSlotModel> slots) {
        for (PriceRecord record : allPriceRecords) {
            List<PriceTableTimeSlotModel> mySlots = new ArrayList<>();
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;

            for (PriceTableTimeSlotModel slot : slots) {
                if (slot.getPriceTable() != null && slot.getPriceTable().equals(record.getInternalId())) {
                    mySlots.add(slot);
                    try {
                        long p = (long) Double.parseDouble(slot.getUnitPrice().replaceAll("[^0-9.]", ""));
                        if (p < min) min = p;
                        if (p > max) max = p;
                    } catch (Exception ignored) {}
                }
            }

            record.setDetailedTimeSlots(mySlots);
            if (!mySlots.isEmpty()) {
                String priceStr = (min == max) ? currencyFormatter.format(min) + "đ" 
                                              : currencyFormatter.format(min) + "đ - " + currencyFormatter.format(max) + "đ";
                record.setPriceRange(priceStr);
                
                // Hiển thị danh sách khung giờ thay vì số lượng
                List<String> timeStrings = new ArrayList<>();
                for (PriceTableTimeSlotModel slot : mySlots) {
                    String start = slot.getStartTime().substring(0, 5);
                    String end = slot.getEndTime().substring(0, 5);
                    timeStrings.add(start + "-" + end);
                }
                record.setTimeFrameCount(TextUtils.join(", ", timeStrings));
            } else {
                record.setPriceRange("Chưa có giá");
                record.setTimeFrameCount("0 khung giờ");
            }
        }
        filterPrices(binding.etSearch.getText().toString());
    }

    private void showPriceDetailsBottomSheet(PriceRecord price) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetPriceDetailsBinding sheetBinding = BottomSheetPriceDetailsBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvPriceId.setText(price.getId());
        sheetBinding.tvPriceName.setText(price.getTitle());
        sheetBinding.tvCourtType.setText(price.getCourtType());
        
        String scopeText = "Tất cả sân thuộc loại";
        if (!"ALL".equalsIgnoreCase(price.getApplyScope())) {
            if (price.getFullModel() != null && price.getFullModel().getAppliedCourts() != null && !price.getFullModel().getAppliedCourts().isEmpty()) {
                List<String> names = new ArrayList<>();
                for (PriceTableModel.AppliedCourt ac : price.getFullModel().getAppliedCourts()) {
                    names.add(ac.courtName);
                }
                scopeText = "Sân cụ thể: " + TextUtils.join(", ", names);
            } else {
                scopeText = "Sân cụ thể: Đã chọn riêng biệt";
            }
        }
        sheetBinding.tvScope.setText(scopeText);

        if (price.getFullModel() != null) {
            sheetBinding.tvStartDate.setText(price.getFullModel().getStartDate() != null ? price.getFullModel().getStartDate() : "N/A");
            sheetBinding.tvEndDate.setText(price.getFullModel().getEndDate() != null ? price.getFullModel().getEndDate() : "Vô thời hạn");
        }

        List<String> activeDays = price.getActiveDays();
        for (int i = 0; i < sheetBinding.layoutDays.getChildCount(); i++) {
            if (sheetBinding.layoutDays.getChildAt(i) instanceof android.widget.TextView) {
                android.widget.TextView dayView = (android.widget.TextView) sheetBinding.layoutDays.getChildAt(i);
                String dayLabel = dayView.getText().toString();
                setDayState(dayView, activeDays != null && activeDays.contains(dayLabel));
            }
        }

        sheetBinding.layoutTimeFrames.removeAllViews();
        List<PriceTableTimeSlotModel> slots = price.getDetailedTimeSlots();
        if (slots == null || slots.isEmpty()) {
            View emptyView = getLayoutInflater().inflate(R.layout.item_no_data, sheetBinding.layoutTimeFrames, false);
            sheetBinding.layoutTimeFrames.addView(emptyView);
        } else {
            for (PriceTableTimeSlotModel slot : slots) {
                View slotView = getLayoutInflater().inflate(R.layout.item_price_detail_slot, sheetBinding.layoutTimeFrames, false);
                renderSlotView(slotView, slot);
                sheetBinding.layoutTimeFrames.addView(slotView);
            }
        }

        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private void renderSlotView(View view, PriceTableTimeSlotModel slot) {
        try {
            android.widget.TextView tvTime = view.findViewById(R.id.tv_time_range);
            android.widget.TextView tvPrice = view.findViewById(R.id.tv_price_value);
            
            String startTime = slot.getStartTime();
            if (startTime.length() >= 5) startTime = startTime.substring(0, 5);
            
            String endTime = slot.getEndTime();
            if (endTime.length() >= 5) endTime = endTime.substring(0, 5);

            tvTime.setText(startTime + " - " + endTime);
            
            double p = Double.parseDouble(slot.getUnitPrice().replaceAll("[^0-9.]", ""));
            tvPrice.setText(currencyFormatter.format(p) + "đ");
        } catch (Exception e) {
            Log.e("UI_ERROR", "Error rendering slot", e);
        }
    }

    private void setDayState(android.widget.TextView view, boolean active) {
        view.setBackgroundResource(active ? R.drawable.bg_pill_active : R.drawable.bg_day_pill_inactive);
        view.setTextColor(requireContext().getColor(active ? R.color.white : R.color.inactive));
    }

    private void showDeleteConfirmBottomSheet(PriceRecord price) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeletePriceBinding sheetBinding = BottomSheetConfirmDeletePriceBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            ApiClient.getApiService().deletePriceTable(price.getInternalId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                        loadPriceData();
                    }
                    bottomSheetDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    bottomSheetDialog.dismiss();
                }
            });
        });
        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
