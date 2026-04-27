package com.example.nhom5.court;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
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
import com.example.nhom5.booking.SuccessDialogFragment;
import com.example.nhom5.databinding.BottomSheetAddCourtBinding;
import com.example.nhom5.databinding.BottomSheetFilterCourtBinding;
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
    private String selectedStatus;
    private List<Court> allCourts = new ArrayList<>();
    private String currentFilterStatus = "ALL"; // ALL, READY, MAINTENANCE, INACTIVE

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
        setupSearch();

        binding.fabAdd.setOnClickListener(v -> showAddCourtBottomSheet());
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog filterDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetFilterCourtBinding filterBinding = BottomSheetFilterCourtBinding.inflate(getLayoutInflater());
        filterDialog.setContentView(filterBinding.getRoot());

        // Set initial state
        switch (currentFilterStatus) {
            case "READY": filterBinding.rbReady.setChecked(true); break;
            case "MAINTENANCE": filterBinding.rbMaintenance.setChecked(true); break;
            case "INACTIVE": filterBinding.rbInactive.setChecked(true); break;
            default: filterBinding.rbAll.setChecked(true); break;
        }

        filterBinding.btnApplyFilter.setOnClickListener(v -> {
            int checkedId = filterBinding.rgStatusFilter.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_ready) currentFilterStatus = "READY";
            else if (checkedId == R.id.rb_maintenance) currentFilterStatus = "MAINTENANCE";
            else if (checkedId == R.id.rb_inactive) currentFilterStatus = "INACTIVE";
            else currentFilterStatus = "ALL";

            applyFilters();
            filterDialog.dismiss();
        });

        filterDialog.show();
    }

    private void applyFilters() {
        String query = binding.etSearch.getText().toString().toLowerCase().trim();
        List<Court> filteredList = new ArrayList<>();

        for (Court court : allCourts) {
            boolean matchesQuery = true;
            if (!query.isEmpty()) {
                String courtCode = court.getCode() != null ? court.getCode() : "";
                matchesQuery = court.getName().toLowerCase().contains(query) || courtCode.toLowerCase().contains(query);
            }

            boolean matchesStatus = true;
            if (!"ALL".equals(currentFilterStatus)) {
                matchesStatus = currentFilterStatus.equalsIgnoreCase(court.getStatus());
            }

            if (matchesQuery && matchesStatus) {
                filteredList.add(court);
            }
        }
        adapter.updateData(filteredList);
    }

    private void showAddCourtBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetAddCourtBinding sheetBinding = BottomSheetAddCourtBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        selectedCourtType = null;

        sheetBinding.btnSelectType.setOnClickListener(v -> {
            showSelectCourtTypeBottomSheet(new OnTypeSelectedListener() {
                @Override
                public void onSelected(CourtTypeModel item) {
                    selectedCourtType = item;
                    sheetBinding.tvSelectedType.setText(item.getName());
                    sheetBinding.tvSelectedType.setTextColor(Color.BLACK);
                }
            });
        });

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

            Court newCourt = new Court(courtName, selectedCourtType.getId(), "READY");
            
            ApiClient.getApiService().createCourt(newCourt).enqueue(new Callback<Court>() {
                @Override
                public void onResponse(Call<Court> call, Response<Court> response) {
                    if (response.isSuccessful()) {
                        bottomSheetDialog.dismiss();
                        SuccessDialogFragment successDialog = SuccessDialogFragment.newInstance(
                                "Tạo sân thành công",
                                () -> loadCourtsFromServer()
                        );
                        successDialog.show(getParentFragmentManager(), "success_dialog");
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
                public void onFailure(Call<Court> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", t.getMessage());
                }
            });
        });

        bottomSheetDialog.show();
    }

    private interface OnTypeSelectedListener {
        void onSelected(CourtTypeModel item);
    }

    private void showSelectCourtTypeBottomSheet(OnTypeSelectedListener listener) {
        BottomSheetDialog selectDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetSelectCourtTypeBinding selectBinding = BottomSheetSelectCourtTypeBinding.inflate(getLayoutInflater());
        selectDialog.setContentView(selectBinding.getRoot());

        selectBinding.btnClose.setOnClickListener(v -> selectDialog.dismiss());

        ApiClient.getApiService().getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(Call<List<CourtTypeModel>> call, Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourtTypeSelectionAdapter selectionAdapter = new CourtTypeSelectionAdapter(response.body(), item -> {
                        listener.onSelected(item);
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

        sheetBinding.tvCourtId.setText(court.getCode() != null ? court.getCode() : String.valueOf(court.getId()));
        sheetBinding.etCourtName.setText(court.getName());
        sheetBinding.tvSelectedType.setText(court.getType());
        sheetBinding.tvSelectedType.setTextColor(Color.BLACK);

        selectedCourtType = null;
        selectedStatus = court.getStatus();
        
        sheetBinding.statusAvailable.setText("Sẵn sàng");
        sheetBinding.statusMaintenance.setText("Đang bảo trì");
        sheetBinding.statusInactive.setText("Ngừng sử dụng");

        updateStatusUI(sheetBinding, selectedStatus);

        sheetBinding.statusAvailable.setOnClickListener(v -> {
            selectedStatus = "READY";
            updateStatusUI(sheetBinding, "READY");
        });
        sheetBinding.statusMaintenance.setOnClickListener(v -> {
            selectedStatus = "MAINTENANCE";
            updateStatusUI(sheetBinding, "MAINTENANCE");
        });
        sheetBinding.statusInactive.setOnClickListener(v -> {
            selectedStatus = "INACTIVE";
            updateStatusUI(sheetBinding, "INACTIVE");
        });

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            String newName = sheetBinding.etCourtName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên sân", Toast.LENGTH_SHORT).show();
                return;
            }

            Court updatedCourt = new Court();
            updatedCourt.setName(newName);
            updatedCourt.setStatus(selectedStatus);
            if (selectedCourtType != null) {
                updatedCourt.setCourtTypeId(selectedCourtType.getId());
            }

            ApiClient.getApiService().updateCourt(court.getId(), updatedCourt).enqueue(new Callback<Court>() {
                @Override
                public void onResponse(Call<Court> call, Response<Court> response) {
                    if (response.isSuccessful()) {
                        bottomSheetDialog.dismiss();
                        SuccessDialogFragment successDialog = SuccessDialogFragment.newInstance(
                                "Cập nhật sân thành công",
                                () -> loadCourtsFromServer()
                        );
                        successDialog.show(getParentFragmentManager(), "success_dialog");
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            org.json.JSONObject jsonObject = new org.json.JSONObject(errorBody);
                            String errorMsg = jsonObject.optString("error", "Lỗi cập nhật: " + response.code());
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Court> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmDialog(Court court) {
        ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.newInstance(
                "Bạn có chắc chắn muốn xóa sân " + court.getName() + "?",
                () -> {
                    ApiClient.getApiService().deleteCourt(court.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                SuccessDialogFragment successDialog = SuccessDialogFragment.newInstance(
                                        "Xóa sân thành công",
                                        () -> loadCourtsFromServer()
                                );
                                successDialog.show(getParentFragmentManager(), "success_dialog");
                            } else {
                                try {
                                    String errorBody = response.errorBody().string();
                                    org.json.JSONObject jsonObject = new org.json.JSONObject(errorBody);
                                    String errorMsg = jsonObject.optString("error", "Không thể xóa sân");
                                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(getContext(), "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );
        dialog.show(getParentFragmentManager(), "confirm_delete_dialog");
    }

    private void updateStatusUI(BottomSheetUpdateCourtBinding sheetBinding, String status) {
        sheetBinding.statusAvailable.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusAvailable.setTextColor(Color.parseColor("#757575"));
        
        sheetBinding.statusMaintenance.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusMaintenance.setTextColor(Color.parseColor("#757575"));
        
        sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusInactive.setTextColor(Color.parseColor("#757575"));

        if ("READY".equalsIgnoreCase(status) || "Sẵn sàng".equalsIgnoreCase(status)) {
            sheetBinding.statusAvailable.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusAvailable.setTextColor(Color.WHITE);
        } else if ("MAINTENANCE".equalsIgnoreCase(status) || "Đang bảo trì".equalsIgnoreCase(status)) {
            sheetBinding.statusMaintenance.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusMaintenance.setTextColor(Color.WHITE);
        } else if ("INACTIVE".equalsIgnoreCase(status) || "Ngừng sử dụng".equalsIgnoreCase(status)) {
            sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusInactive.setTextColor(Color.WHITE);
        }
    }

    private void setupRecyclerView() {
        adapter = new CourtAdapter(new ArrayList<>(), new CourtAdapter.OnCourtActionListener() {
            @Override
            public void onEdit(Court court) { showUpdateCourtBottomSheet(court); }
            @Override
            public void onDelete(Court court) { showDeleteConfirmDialog(court); }
        });
        binding.rvCourts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourts.setAdapter(adapter);
        loadCourtsFromServer();
    }

    private void loadCourtsFromServer() {
        ApiClient.getApiService().getCourts().enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCourts = response.body();
                    applyFilters();
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
