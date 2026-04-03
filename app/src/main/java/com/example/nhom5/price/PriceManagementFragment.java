package com.example.nhom5.price;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nhom5.R;
import com.example.nhom5.databinding.BottomSheetConfirmDeletePriceBinding;
import com.example.nhom5.databinding.BottomSheetPriceDetailsBinding;
import com.example.nhom5.databinding.FragmentPriceManagementBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    }

    private void setupRecyclerView() {
        List<PriceRecord> priceList = new ArrayList<>();
        priceList.add(new PriceRecord("BG001", "Sân Bóng đá - Tiêu chuẩn", "Sân bóng đá", "2026-01-01 ~ Vô thời hạn", "250.000đ - 300.000đ", Arrays.asList("T2", "T3", "T4", "T5", "T6", "T7", "CN"), "2 khung giờ"));
        priceList.add(new PriceRecord("BG002", "Sân bóng đá - Cuối tuần", "Sân bóng đá", "2026-01-01 ~ Vô thời hạn", "250.000đ", Arrays.asList("T7", "CN"), "1 khung giờ"));
        priceList.add(new PriceRecord("BG003", "Sân Cầu lông - Tiêu chuẩn", "Sân Cầu lông", "2026-01-01 ~ Vô thời hạn", "200.000đ - 250.000đ", Arrays.asList("T2", "T3", "T4", "T5", "T6", "T7", "CN"), "1 khung giờ"));

        adapter = new PriceAdapter(priceList, new PriceAdapter.OnPriceActionListener() {
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

    private void showPriceDetailsBottomSheet(PriceRecord price) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetPriceDetailsBinding sheetBinding = BottomSheetPriceDetailsBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvPriceId.setText(price.getId());
        sheetBinding.tvPriceName.setText(price.getTitle());
        sheetBinding.tvCourtType.setText(price.getCourtType());
        sheetBinding.tvStartDate.setText(price.getDateRange().split(" ~ ")[0]);
        sheetBinding.tvEndDate.setText(price.getDateRange().split(" ~ ")[1]);

        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmBottomSheet(PriceRecord price) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeletePriceBinding sheetBinding = BottomSheetConfirmDeletePriceBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            // Handle actual deletion here
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}