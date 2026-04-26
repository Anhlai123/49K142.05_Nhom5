package com.example.nhom5.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.court.Court;
import com.example.nhom5.databinding.FragmentFirstBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private CourtStatusAdapter adapter;
    private List<Court> courtList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        checkRoleAndSetupUI();
        updateDate();
        loadCourtsData();

        // Xử lý sự kiện nút "Thêm khách"
        binding.btnAddCustomer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addCustomerFragment);
        });

        // Xử lý sự kiện nút "Đặt lịch mới"
        binding.btnNewBooking.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_schedule);
        });
    }

    private void setupRecyclerView() {
        adapter = new CourtStatusAdapter(courtList);
        binding.rvTodaySchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTodaySchedule.setAdapter(adapter);
    }

    private void loadCourtsData() {
        ApiClient.getApiService().getCourts().enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courtList = response.body();
                    adapter.updateData(courtList);

                    // Cập nhật số lượng sân đang bận lên dashboard
                    updateDashboard(courtList);
                } else {
                    Log.e("FirstFragment", "Lỗi lấy danh sách sân: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Log.e("FirstFragment", "Lỗi kết nối: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể kết nối tới máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateDashboard(List<Court> courts) {
        long busyCount = 0;
        for (Court court : courts) {
            String status = court.getStatus();
            if (status != null && (status.equalsIgnoreCase("busy") || status.equalsIgnoreCase("bận"))) {
                busyCount++;
            }
        }
        binding.tvBusyCourtsCount.setText(busyCount + " Sân");
    }

    private void updateDate() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String dayString;
        
        if (dayOfWeek == Calendar.SUNDAY) {
            dayString = "Chủ Nhật";
        } else {
            dayString = "Thứ " + dayOfWeek;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString = dayString + ", " + sdf.format(calendar.getTime());
        binding.tvDate.setText(dateString);
    }

    private void checkRoleAndSetupUI() {
        if (getContext() == null) return;
        SharedPreferences pref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String role = pref.getString("role", "customer");
        String username = pref.getString("username", "");

        boolean isManager = "admin".equalsIgnoreCase(username.trim())
                         || "admin".equalsIgnoreCase(role.trim()) 
                         || "staff".equalsIgnoreCase(role.trim())
                         || "1".equals(role.trim());

        if (isManager) {
            binding.btnAddCustomer.setVisibility(View.VISIBLE);
        } else {
            binding.btnAddCustomer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkRoleAndSetupUI();
        loadCourtsData(); // Cập nhật lại dữ liệu khi quay lại màn hình
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
