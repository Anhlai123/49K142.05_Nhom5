package com.example.nhom5.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Cập nhật ngày tháng hiện tại
        updateDate();
        
        // Xử lý sự kiện nút "Thêm khách"
        binding.btnAddCustomer.setOnClickListener(v -> {
            // Điều hướng tới màn hình thêm khách hàng mới (AddCustomerFragment)
            Navigation.findNavController(v).navigate(R.id.addCustomerFragment);
        });

        // Xử lý sự kiện nút "Đặt lịch mới"
        binding.btnNewBooking.setOnClickListener(v -> {
            // Điều hướng tới màn hình Lịch sân (ScheduleFragment)
            Navigation.findNavController(v).navigate(R.id.navigation_schedule);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
