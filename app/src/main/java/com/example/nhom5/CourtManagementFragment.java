package com.example.nhom5;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.nhom5.databinding.BottomSheetAddCourtBinding;
import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtBinding;
import com.example.nhom5.databinding.BottomSheetUpdateCourtBinding;
import com.example.nhom5.databinding.FragmentCourtManagementBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

public class CourtManagementFragment extends Fragment {

    private FragmentCourtManagementBinding binding;
    private CourtAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourtManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.fabAdd.setOnClickListener(v -> showAddCourtBottomSheet());
    }

    private void showAddCourtBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetAddCourtBinding sheetBinding = BottomSheetAddCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            // Handle save logic
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showUpdateCourtBottomSheet(Court court) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetUpdateCourtBinding sheetBinding = BottomSheetUpdateCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        // Fill data
        sheetBinding.tvCourtId.setText(court.getId());
        sheetBinding.etCourtName.setText(court.getName());
        sheetBinding.tvSelectedType.setText(court.getType());

        // Initialize status selection
        updateStatusUI(sheetBinding, court.getStatus());

        sheetBinding.statusAvailable.setOnClickListener(v -> updateStatusUI(sheetBinding, "Sẵn sàng"));
        sheetBinding.statusMaintenance.setOnClickListener(v -> updateStatusUI(sheetBinding, "Đang bảo trì"));
        sheetBinding.statusInactive.setOnClickListener(v -> updateStatusUI(sheetBinding, "Ngừng sử dụng"));

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            // Handle update logic
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmBottomSheet(Court court) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeleteCourtBinding sheetBinding = BottomSheetConfirmDeleteCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvMessage.setText("Bạn có chắc chắn muốn xóa sân " + court.getName() + "?");

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            // Handle delete logic
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateStatusUI(BottomSheetUpdateCourtBinding sheetBinding, String status) {
        // Reset all to inactive style
        sheetBinding.statusAvailable.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusAvailable.setTextColor(Color.parseColor("#212121"));
        
        sheetBinding.statusMaintenance.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusMaintenance.setTextColor(Color.parseColor("#212121"));
        
        sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusInactive.setTextColor(Color.parseColor("#212121"));

        // Set active style for selected status
        int activeColor = ContextCompat.getColor(requireContext(), R.color.primary);
        if ("Sẵn sàng".equals(status)) {
            sheetBinding.statusAvailable.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusAvailable.setTextColor(activeColor);
        } else if ("Đang bảo trì".equals(status)) {
            sheetBinding.statusMaintenance.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusMaintenance.setTextColor(activeColor);
        } else if ("Ngừng sử dụng".equals(status) || "Ngừng hoạt động".equals(status)) {
            sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusInactive.setTextColor(activeColor);
        }
    }

    private void setupRecyclerView() {
        List<Court> courtList = new ArrayList<>();
        courtList.add(new Court("SAN001", "Sân B1", "Sân bóng đá", "Sẵn sàng"));
        courtList.add(new Court("SAN002", "Sân B2", "Sân bóng đá", "Sẵn sàng"));
        courtList.add(new Court("SAN003", "Sân C1", "Sân cầu lông", "Đang bảo trì"));
        courtList.add(new Court("SAN004", "Sân C2", "Sân cầu lông", "Sẵn sàng"));
        courtList.add(new Court("SAN005", "Sân C3", "Sân cầu lông", "Ngừng sử dụng"));

        adapter = new CourtAdapter(courtList, new CourtAdapter.OnCourtActionListener() {
            @Override
            public void onEdit(Court court) {
                showUpdateCourtBottomSheet(court);
            }

            @Override
            public void onDelete(Court court) {
                showDeleteConfirmBottomSheet(court);
            }
        });
        binding.rvCourts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourts.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}