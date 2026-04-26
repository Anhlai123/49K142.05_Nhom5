package com.example.nhom5.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentFirstBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkRoleAndSetupUI();
        
        // Cập nhật ngày tháng hiện tại
        updateDate();
        
        // Xử lý sự kiện nút "Thêm khách"
        binding.btnAddCustomer.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.addCustomerFragment);
        });

        // Xử lý sự kiện nút "Đặt lịch mới"
        binding.btnNewBooking.setOnClickListener(v -> {
            // Thay vì dùng navigate trực tiếp, chúng ta thay đổi Item được chọn trên BottomNav
            // Điều này giúp Navigation Component quản lý BackStack chuẩn hơn (Tab-switching)
            if (getActivity() != null) {
                BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
                if (navView != null) {
                    navView.setSelectedItemId(R.id.navigation_schedule);
                } else {
                    // Fallback nếu không tìm thấy BottomNav
                    Navigation.findNavController(v).navigate(R.id.navigation_schedule);
                }
            }
        });
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
