package com.example.nhom5.courtype;

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
import com.example.nhom5.databinding.BottomSheetAddCourtTypeBinding;
import com.example.nhom5.databinding.BottomSheetConfirmDeleteCourtTypeBinding;
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
        // Khởi tạo với danh sách trống
        adapter = new CourtTypeAdapter(new ArrayList<>(), new CourtTypeAdapter.OnCourtTypeActionListener() {
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
        
        // Gọi API lấy dữ liệu thật
        loadCourtTypesFromServer();
    }

    private void loadCourtTypesFromServer() {
        ApiClient.getApiService().getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(Call<List<CourtTypeModel>> call, Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CourtTypeModel> apiData = response.body();
                    List<CourtType> displayList = new ArrayList<>();
                    
                    // Chuyển đổi từ CourtTypeModel (API) sang CourtType (Adapter)
                    for (CourtTypeModel model : apiData) {
                        displayList.add(new CourtType(
                            String.valueOf(model.getId()),
                            model.getName(),
                            "ACTIVE".equalsIgnoreCase(model.getStatus()) ? "Đang hoạt động" : "Ngừng hoạt động",
                            model.getDuration()
                        ));
                    }
                    adapter.updateData(displayList);
                } else {
                    Log.e("API_ERROR", "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CourtTypeModel>> call, Throwable t) {
                Log.e("API_ERROR", "Failed to fetch court types: " + t.getMessage());
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
                        Toast.makeText(getContext(), "Thêm loại sân thành công!", Toast.LENGTH_SHORT).show();
                        loadCourtTypesFromServer();
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
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

            ApiClient.getApiService().updateCourtType(Integer.parseInt(courtType.getId()), updatedType).enqueue(new Callback<CourtTypeModel>() {
                @Override
                public void onResponse(Call<CourtTypeModel> call, Response<CourtTypeModel> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        loadCourtTypesFromServer();
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
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

    private void showDeleteConfirmBottomSheet(CourtType courtType) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme);
        BottomSheetConfirmDeleteCourtTypeBinding sheetBinding = BottomSheetConfirmDeleteCourtTypeBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(sheetBinding.getRoot());

        sheetBinding.tvMessage.setText("Bạn có chắc chắn muốn xóa loại sân này?");

        sheetBinding.btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetBinding.btnConfirm.setOnClickListener(v -> {
            ApiClient.getApiService().deleteCourtType(Integer.parseInt(courtType.getId())).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
                        loadCourtTypesFromServer();
                        bottomSheetDialog.dismiss();
                    } else if (response.code() == 500) {
                        Toast.makeText(getContext(), "Không thể xóa: Loại sân này đang có dữ liệu liên quan (Sân hoặc Bảng giá). Hãy xóa chúng trước!", Toast.LENGTH_LONG).show();
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheetDialog.show();
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
