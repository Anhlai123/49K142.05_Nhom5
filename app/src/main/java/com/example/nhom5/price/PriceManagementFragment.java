package com.example.nhom5.price;

import android.os.Bundle;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceManagementFragment extends Fragment {

    private FragmentPriceManagementBinding binding;
    private PriceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPriceManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.fabAdd.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_priceManagementFragment_to_addPriceFragment);
        });
        
        loadPriceTables();
    }

    private void setupRecyclerView() {
        adapter = new PriceAdapter(new ArrayList<>(), new PriceAdapter.OnPriceActionListener() {
            @Override
            public void onView(PriceRecord price) {
                showPriceDetailsBottomSheet(price);
            }

            @Override
            public void onEdit(PriceRecord price) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_priceManagementFragment_to_updatePriceFragment);
            }

            @Override
            public void onDelete(PriceRecord price) {
                showDeleteConfirmBottomSheet(price);
            }
        });
        binding.rvPrices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPrices.setAdapter(adapter);
    }

    private void loadPriceTables() {
        ApiClient.getApiService().getPriceTables().enqueue(new Callback<List<PriceTableModel>>() {
            @Override
            public void onResponse(Call<List<PriceTableModel>> call, Response<List<PriceTableModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PriceRecord> records = new ArrayList<>();
                    for (PriceTableModel model : response.body()) {
                        records.add(new PriceRecord(
                                String.valueOf(model.getId()),
                                model.getPriceTableName(),
                                model.getCourtTypeName(),
                                model.getEffectiveDate() + " ~ Vô thời hạn",
                                "Đang cập nhật...",
                                Arrays.asList("T2", "T3", "T4", "T5", "T6", "T7", "CN"),
                                "1 khung giờ"
                        ));
                    }
                    adapter.updateData(records);
                }
            }

            @Override
            public void onFailure(Call<List<PriceTableModel>> call, Throwable t) {
                Log.e("API_ERROR", "Load prices failed: " + t.getMessage());
            }
        });
    }

    private void showPriceDetailsBottomSheet(PriceRecord price) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetPriceDetailsBinding sheetBinding = BottomSheetPriceDetailsBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvPriceId.setText(price.getId());
        sheetBinding.tvPriceName.setText(price.getTitle());
        sheetBinding.tvCourtType.setText(price.getCourtType());
        String[] dateParts = price.getDateRange().split(" ~ ");
        sheetBinding.tvStartDate.setText(dateParts.length > 0 ? dateParts[0] : "");
        sheetBinding.tvEndDate.setText(dateParts.length > 1 ? dateParts[1] : "Vô thời hạn");

        List<String> activeDays = price.getActiveDays();
        int dayCount = sheetBinding.layoutDays.getChildCount();
        for (int i = 0; i < dayCount; i++) {
            android.widget.TextView dayView = (android.widget.TextView) sheetBinding.layoutDays.getChildAt(i);
            String dayLabel = dayView.getText().toString();
            setDayState(dayView, activeDays.contains(dayLabel));
        }

        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void setDayState(android.widget.TextView view, boolean active) {
        if (view == null) return;
        view.setBackgroundResource(active ? R.drawable.bg_pill_active : R.drawable.bg_day_pill_inactive);
        view.setTextColor(requireContext().getColor(active ? R.color.white : R.color.inactive));
    }

    private void showDeleteConfirmBottomSheet(PriceRecord price) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeletePriceBinding sheetBinding = BottomSheetConfirmDeletePriceBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            try {
                int id = Integer.parseInt(price.getId());
                ApiClient.getApiService().deletePriceTable(id).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Xóa bảng giá thành công", Toast.LENGTH_SHORT).show();
                            loadPriceTables();
                            bottomSheetDialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "ID không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
