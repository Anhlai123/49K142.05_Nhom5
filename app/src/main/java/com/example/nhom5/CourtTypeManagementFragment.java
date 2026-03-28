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
import com.example.nhom5.databinding.BottomSheetAddCourtTypeBinding;
import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtTypeBinding;
import com.example.nhom5.databinding.BottomSheetUpdateCourtTypeBinding;
import com.example.nhom5.databinding.FragmentCourtTypeManagementBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

public class CourtTypeManagementFragment extends Fragment {

    private FragmentCourtTypeManagementBinding binding;
    private CourtTypeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourtTypeManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();

        binding.fabAdd.setOnClickListener(v -> showAddCourtTypeBottomSheet());
    }

    private void setupRecyclerView() {
        List<CourtType> courtTypeList = new ArrayList<>();
        courtTypeList.add(new CourtType("LOAISAN001", "Sân Bóng đá", "Đang hoạt động"));
        courtTypeList.add(new CourtType("LOAISAN002", "Sân Cầu lông", "Đang hoạt động"));
        courtTypeList.add(new CourtType("LOAISAN003", "Sân Tennis", "Đang hoạt động"));

        adapter = new CourtTypeAdapter(courtTypeList, new CourtTypeAdapter.OnCourtTypeActionListener() {
            @Override
            public void onEdit(CourtType courtType) {
                showUpdateCourtTypeBottomSheet(courtType);
            }

            @Override
            public void onDelete(CourtType courtType) {
                showDeleteConfirmBottomSheet(courtType);
            }
        });
        binding.rvCourtTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourtTypes.setAdapter(adapter);
    }

    private void showAddCourtTypeBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetAddCourtTypeBinding sheetBinding = BottomSheetAddCourtTypeBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            // Handle save logic
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showUpdateCourtTypeBottomSheet(CourtType courtType) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetUpdateCourtTypeBinding sheetBinding = BottomSheetUpdateCourtTypeBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvTypeId.setText(courtType.getId());
        sheetBinding.etTypeName.setText(courtType.getName());

        updateStatusUI(sheetBinding, courtType.getStatus());

        sheetBinding.statusActive.setOnClickListener(v -> updateStatusUI(sheetBinding, "Đang hoạt động"));
        sheetBinding.statusInactive.setOnClickListener(v -> updateStatusUI(sheetBinding, "Ngừng hoạt động"));

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmBottomSheet(CourtType courtType) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeleteCourtTypeBinding sheetBinding = BottomSheetConfirmDeleteCourtTypeBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvMessage.setText("Bạn có chắc chắn muốn xóa?");

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void updateStatusUI(BottomSheetUpdateCourtTypeBinding sheetBinding, String status) {
        sheetBinding.statusActive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusActive.setTextColor(Color.parseColor("#212121"));
        
        sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusInactive.setTextColor(Color.parseColor("#212121"));

        int activeColor = ContextCompat.getColor(requireContext(), R.color.primary);
        if ("Đang hoạt động".equals(status)) {
            sheetBinding.statusActive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusActive.setTextColor(activeColor);
        } else if ("Ngừng hoạt động".equals(status)) {
            sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusInactive.setTextColor(activeColor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}