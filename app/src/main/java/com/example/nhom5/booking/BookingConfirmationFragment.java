package com.example.nhom5.booking;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.api.ApiService;
import com.example.nhom5.models.BookingRequest;
import com.example.nhom5.models.BookingResponse;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingConfirmationFragment extends Fragment {

    private EditText etName, etPhone;
    private Button btnConfirm;
    private int mCourtId = -1;
    private String mDate = "";
    private double mTotalPrice = 0;
    private ArrayList<String> mSelectedSlotsRaw = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirmation, container, false);

        String courtName = "Sân Cầu Lông";
        if (getArguments() != null) {
            mCourtId = getArguments().getInt("courtId", -1);
            courtName = getArguments().getString("courtName", "Sân Cầu Lông");
            mDate = getArguments().getString("date", "");
            mSelectedSlotsRaw = getArguments().getStringArrayList("selectedSlots");
        }

        String timeDisplay = "Chưa chọn giờ";
        double firstSlotPrice = 0;

        if (mSelectedSlotsRaw != null && !mSelectedSlotsRaw.isEmpty()) {
            mTotalPrice = 0;
            List<Integer> startMinutes = new ArrayList<>();
            
            for (String slot : mSelectedSlotsRaw) {
                String[] parts = slot.split("\\|"); // id|name|time|price
                if (parts.length >= 4) {
                    String startTimeRaw = parts[2];
                    String startTimeClean = startTimeRaw.length() > 5 ? startTimeRaw.substring(0, 5) : startTimeRaw;
                    
                    String[] timeParts = startTimeClean.split(":");
                    int mins = Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1]);
                    startMinutes.add(mins);
                    
                    try {
                        double price = Double.parseDouble(parts[3]);
                        mTotalPrice += price;
                        if (firstSlotPrice == 0) firstSlotPrice = price;
                    } catch (Exception e) { 
                        mTotalPrice += 180000; 
                        if (firstSlotPrice == 0) firstSlotPrice = 180000;
                    }
                }
            }
            Collections.sort(startMinutes);

            // Tạo chuỗi hiển thị từng khoảng giờ rời rạc
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < startMinutes.size(); i++) {
                int start = startMinutes.get(i);
                int end = start + 60; // Giả định mỗi slot 1 tiếng
                sb.append(String.format(Locale.getDefault(), "%02d:%02d-%02d:%02d", start/60, start%60, end/60, end%60));
                if (i < startMinutes.size() - 1) sb.append(", ");
            }
            timeDisplay = sb.toString();
        }

        DecimalFormat df = new DecimalFormat("#,###");
        
        // Cập nhật giao diện với GIÁ THẬT từ API
        ((TextView) view.findViewById(R.id.tvCourtNameHeader)).setText(courtName);
        // Hiển thị đơn giá của sân ở Header (lấy từ slot đầu tiên)
        ((TextView) view.findViewById(R.id.tvPriceHeader)).setText(getString(R.string.booking_price_per_hour, df.format(firstSlotPrice)));
        
        ((TextView) view.findViewById(R.id.tvDate)).setText("Ngày: " + mDate);
        ((TextView) view.findViewById(R.id.tvCourtField)).setText(courtName);
        ((TextView) view.findViewById(R.id.tvTimeField)).setText(timeDisplay);
        ((TextView) view.findViewById(R.id.tvHoursField)).setText((mSelectedSlotsRaw != null ? mSelectedSlotsRaw.size() : 0) + " giờ");
        ((TextView) view.findViewById(R.id.tvPriceField)).setText(df.format(mTotalPrice) + " đ");

        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnConfirm.setOnClickListener(v -> submitBooking());

        setupInputValidation();
        return view;
    }

    private void setupInputValidation() {
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(tw);
        etPhone.addTextChangedListener(tw);
    }

    private void validate() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        btnConfirm.setEnabled(!name.isEmpty() && !phone.isEmpty());
    }

    private void submitBooking() {
        if (mCourtId <= 0 || mSelectedSlotsRaw == null || mSelectedSlotsRaw.isEmpty()) {
            Toast.makeText(getContext(), "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("ĐANG XỬ LÝ...");

        List<BookingRequest.TimeSlot> apiSlots = new ArrayList<>();
        for (String slotRaw : mSelectedSlotsRaw) {
            String[] parts = slotRaw.split("\\|");
            if (parts.length >= 3) {
                String startTimeRaw = parts[2];
                String startTime = startTimeRaw.length() > 5 ? startTimeRaw.substring(0, 5) : startTimeRaw;
                
                String[] t = startTime.split(":");
                int startMins = Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]);
                int endMins = startMins + 60;
                
                String endTime = String.format(Locale.getDefault(), "%02d:%02d", endMins / 60, endMins % 60);
                apiSlots.add(new BookingRequest.TimeSlot(startTime, endTime));
            }
        }

        BookingRequest request = new BookingRequest(
                mCourtId,
                etName.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                normalizeDate(mDate),
                "", // notes
                apiSlots
        );

        ApiClient.getApiService().createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đặt sân thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack(R.id.navigation_schedule, false);
                } else {
                    String errorMsg = "Lỗi server: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("XÁC NHẬN ĐẶT SÂN");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true);
                btnConfirm.setText("XÁC NHẬN ĐẶT SÂN");
            }
        });
    }

    private String normalizeDate(String date) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return out.format(in.parse(date));
        } catch (Exception e) { return date; }
    }
}
