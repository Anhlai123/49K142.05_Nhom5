package com.example.nhom5.court;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.databinding.BottomSheetAddCourtBinding;
import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtBinding;
import com.example.nhom5.databinding.BottomSheetSelectCourtTypeBinding;
import com.example.nhom5.databinding.BottomSheetUpdateCourtBinding;
import com.example.nhom5.databinding.FragmentCourtManagementBinding;
import com.example.nhom5.models.CourtTypeModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtManagementFragment extends Fragment {

    private FragmentCourtManagementBinding binding;
    private CourtAdapter adapter;
    private CourtTypeModel selectedCourtType;

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

        selectedCourtType = null;

        sheetBinding.btnSelectType.setOnClickListener(v -> showSelectCourtTypeBottomSheet(sheetBinding));

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            String courtName = sheetBinding.etCourtName.getText().toString().trim();
            if (courtName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên sân", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCourtType == null) {
                Toast.makeText(getContext(), "Vui lòng chọn loại sân", Toast.LENGTH_SHORT).show();
                return;
            }

            Court newCourt = new Court(courtName, selectedCourtType.getId());
            
            ApiClient.getApiService().createCourt(newCourt).enqueue(new Callback<Court>() {
                @Override
                public void onResponse(Call<Court> call, Response<Court> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Thêm sân thành công!", Toast.LENGTH_SHORT).show();
                        loadCourtsFromServer();
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Court> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", t.getMessage());
                }
            });
        });

        bottomSheetDialog.show();
    }

    private void showSelectCourtTypeBottomSheet(BottomSheetAddCourtBinding addCourtBinding) {
        BottomSheetDialog selectDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetSelectCourtTypeBinding selectBinding = BottomSheetSelectCourtTypeBinding.inflate(getLayoutInflater());
        selectDialog.setContentView(selectBinding.getRoot());

        selectBinding.btnClose.setOnClickListener(v -> selectDialog.dismiss());

        ApiClient.getApiService().getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(Call<List<CourtTypeModel>> call, Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourtTypeSelectionAdapter selectionAdapter = new CourtTypeSelectionAdapter(response.body(), item -> {
                        selectedCourtType = item;
                        addCourtBinding.tvSelectedType.setText(item.getName());
                        addCourtBinding.tvSelectedType.setTextColor(Color.BLACK);
                        selectDialog.dismiss();
                    });
                    selectBinding.rvCourtTypes.setLayoutManager(new LinearLayoutManager(getContext()));
                    selectBinding.rvCourtTypes.setAdapter(selectionAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<CourtTypeModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể tải danh sách loại sân", Toast.LENGTH_SHORT).show();
            }
        });

        selectDialog.show();
    }

    private void showUpdateCourtBottomSheet(Court court) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetUpdateCourtBinding sheetBinding = BottomSheetUpdateCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        // Fill data
        sheetBinding.tvCourtId.setText(String.valueOf(court.getId()));
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
        sheetBinding.statusAvailable.setTextColor(Color.parseColor("#757575"));
        
        sheetBinding.statusMaintenance.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusMaintenance.setTextColor(Color.parseColor("#757575"));
        
        sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusInactive.setTextColor(Color.parseColor("#757575"));

        // Set active style for selected status
        if ("Sẵn sàng".equals(status)) {
            sheetBinding.statusAvailable.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusAvailable.setTextColor(Color.WHITE);
        } else if ("Đang bảo trì".equals(status)) {
            sheetBinding.statusMaintenance.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusMaintenance.setTextColor(Color.WHITE);
        } else if ("Ngừng sử dụng".equals(status) || "Ngừng hoạt động".equals(status)) {
            sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusInactive.setTextColor(Color.WHITE);
        }
    }

    private void setupRecyclerView() {
        // 1. Khởi tạo adapter với danh sách trống trước
        adapter = new CourtAdapter(new ArrayList<>(), new CourtAdapter.OnCourtActionListener() {
            @Override
            public void onEdit(Court court) { showUpdateCourtBottomSheet(court); }
            @Override
            public void onDelete(Court court) { showDeleteConfirmBottomSheet(court); }
        });
        binding.rvCourts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourts.setAdapter(adapter);
        // 2. Gọi API để lấy dữ liệu thật
        loadCourtsFromServer();
    }

    private void loadCourtsFromServer() {
        ApiClient.getApiService().getCourts().enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật dữ liệu từ Server vào RecyclerView
                    adapter.updateData(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Log.e("API_ERROR", "Không thể lấy danh sách sân: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
