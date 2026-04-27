package com.example.nhom5.courtype;

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
import com.example.nhom5.court.ConfirmDeleteDialogFragment;
import com.example.nhom5.databinding.BottomSheetAddCourtTypeBinding;
import com.example.nhom5.databinding.BottomSheetUpdateCourtTypeBinding;
import com.example.nhom5.databinding.FragmentCourtTypeManagementBinding;
import com.example.nhom5.models.CourtTypeModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtTypeManagementFragment extends Fragment {

    private FragmentCourtTypeManagementBinding binding;
    private CourtTypeAdapter adapter;
    private List<CourtType> allCourtTypes = new ArrayList<>();

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
        setupSearch();

        binding.fabAdd.setOnClickListener(v -> showAddCourtTypeBottomSheet());
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourtTypes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCourtTypes(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allCourtTypes);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<CourtType> filteredList = new ArrayList<>();
        for (CourtType item : allCourtTypes) {
            if (item.getName().toLowerCase().contains(lowerQuery) || 
                item.getId().toLowerCase().contains(lowerQuery)) {
                filteredList.add(item);
            }
        }
        adapter.updateData(filteredList);
    }

    private void setupRecyclerView() {
        adapter = new CourtTypeAdapter(new ArrayList<>(), new CourtTypeAdapter.OnCourtTypeActionListener() {
            @Override
            public void onEdit(CourtType courtType) {
                showUpdateCourtTypeBottomSheet(courtType);
            }

            @Override
            public void onDelete(CourtType courtType) {
                showDeleteConfirmDialog(courtType);
            }
        });
        binding.rvCourtTypes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourtTypes.setAdapter(adapter);
        
        loadCourtTypesFromServer();
    }

    private void loadCourtTypesFromServer() {
        ApiClient.getApiService().getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(Call<List<CourtTypeModel>> call, Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CourtTypeModel> apiData = response.body();
                    allCourtTypes.clear();
                    
                    for (CourtTypeModel model : apiData) {
                        String displayId = model.getCode();
                        if (displayId == null || displayId.trim().isEmpty()) {
                            displayId = String.valueOf(model.getId());
                        }
                        
                        allCourtTypes.add(new CourtType(
                            displayId,
                            model.getName(),
                            "ACTIVE".equalsIgnoreCase(model.getStatus()) ? "Đang hoạt động" : "Ngừng hoạt động",
                            model.getDuration()
                        ));
                    }
                    filterCourtTypes(binding.etSearch.getText().toString());
                }
            }

            @Override
            public void onFailure(Call<List<CourtTypeModel>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi kết nối Server", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showAddCourtTypeBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetAddCourtTypeBinding sheetBinding = BottomSheetAddCourtTypeBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            String name = sheetBinding.etTypeName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên loại sân", Toast.LENGTH_SHORT).show();
                return;
            }

            CourtTypeModel newType = new CourtTypeModel();
            newType.setName(name);

            ApiClient.getApiService().createCourtType(newType).enqueue(new Callback<CourtTypeModel>() {
                @Override
                public void onResponse(Call<CourtTypeModel> call, Response<CourtTypeModel> response) {
                    if (response.isSuccessful()) {
                        bottomSheetDialog.dismiss();
                        SuccessDialogFragment successDialog = SuccessDialogFragment.newInstance(
                                "Tạo loại sân thành công",
                                () -> loadCourtTypesFromServer()
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
                public void onFailure(Call<CourtTypeModel> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.show();
    }

    private void showUpdateCourtTypeBottomSheet(CourtType courtType) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetUpdateCourtTypeBinding sheetBinding = BottomSheetUpdateCourtTypeBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvTypeId.setText(courtType.getId());
        sheetBinding.etTypeName.setText(courtType.getName());

        final String[] status = {courtType.getStatus().equals("Đang hoạt động") ? "ACTIVE" : "INACTIVE"};
        updateStatusUI(sheetBinding, courtType.getStatus());

        sheetBinding.statusActive.setOnClickListener(v -> {
            status[0] = "ACTIVE";
            updateStatusUI(sheetBinding, "Đang hoạt động");
        });
        sheetBinding.statusInactive.setOnClickListener(v -> {
            status[0] = "INACTIVE";
            updateStatusUI(sheetBinding, "Ngừng hoạt động");
        });

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnSave.setOnClickListener(v -> {
            String newName = sheetBinding.etTypeName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên loại sân", Toast.LENGTH_SHORT).show();
                return;
            }

            CourtTypeModel updatedType = new CourtTypeModel();
            updatedType.setName(newName);
            updatedType.setStatus(status[0]);

            try {
                int id = Integer.parseInt(courtType.getId().replaceAll("[^0-9]", ""));
                ApiClient.getApiService().updateCourtType(id, updatedType).enqueue(new Callback<CourtTypeModel>() {
                    @Override
                    public void onResponse(Call<CourtTypeModel> call, Response<CourtTypeModel> response) {
                        if (response.isSuccessful()) {
                            bottomSheetDialog.dismiss();
                            SuccessDialogFragment successDialog = SuccessDialogFragment.newInstance(
                                    "Cập nhật loại sân thành công",
                                    () -> loadCourtTypesFromServer()
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
                    public void onFailure(Call<CourtTypeModel> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(getContext(), "Không thể cập nhật loại sân này", Toast.LENGTH_SHORT).show();
            }
        });

        bottomSheetDialog.show();
    }

    private void showDeleteConfirmDialog(CourtType courtType) {
        ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.newInstance(
                "Xác nhận xóa loại sân",
                "Bạn có chắc chắn muốn xóa loại sân " + courtType.getName() + "?",
                "Xác nhận xóa",
                () -> {
                    try {
                        int id = Integer.parseInt(courtType.getId().replaceAll("[^0-9]", ""));
                        ApiClient.getApiService().deleteCourtType(id).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    SuccessDialogFragment successDialog = SuccessDialogFragment.newInstance(
                                            "Xóa loại sân thành công",
                                            () -> loadCourtTypesFromServer()
                                    );
                                    successDialog.show(getParentFragmentManager(), "success_dialog");
                                } else {
                                    try {
                                        String errorBody = response.errorBody().string();
                                        org.json.JSONObject jsonObject = new org.json.JSONObject(errorBody);
                                        String errorMsg = jsonObject.optString("error", "Lỗi: " + response.code());
                                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi ID không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        dialog.show(getParentFragmentManager(), "confirm_delete_court_type_dialog");
    }

    private void updateStatusUI(BottomSheetUpdateCourtTypeBinding sheetBinding, String status) {
        sheetBinding.statusActive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusActive.setTextColor(Color.parseColor("#757575"));
        
        sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_inactive);
        sheetBinding.statusInactive.setTextColor(Color.parseColor("#757575"));

        if ("Đang hoạt động".equals(status) || "ACTIVE".equals(status)) {
            sheetBinding.statusActive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusActive.setTextColor(Color.WHITE);
        } else if ("Ngừng hoạt động".equals(status) || "INACTIVE".equals(status)) {
            sheetBinding.statusInactive.setBackgroundResource(R.drawable.bg_pill_active);
            sheetBinding.statusInactive.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
