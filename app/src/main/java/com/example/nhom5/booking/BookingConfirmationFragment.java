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
    private String mStartTime = "";
    private String mEndTime = "";
    private double mTotalPrice = 0;
    private ArrayList<String> mSelectedSlotsRaw = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirmation, container, false);

        // 1. Nhận dữ liệu
        String courtName = "Sân Đang Tải...";
        String courtTypeName = "Loại sân";
        if (getArguments() != null) {
            mCourtId = getArguments().getInt("courtId", -1);
            courtName = getArguments().getString("courtName", "Sân");
            mDate = getArguments().getString("date", "");
            mSelectedSlotsRaw = getArguments().getStringArrayList("selectedSlots");
            courtTypeName = getArguments().getString("courtTypeName", "Loại sân");
        }

        String timeDisplay = "Chưa chọn giờ";
        double firstSlotPrice = 0;

        if (mSelectedSlotsRaw != null && !mSelectedSlotsRaw.isEmpty()) {
            mTotalPrice = 0;
            List<Integer> startMinutes = new ArrayList<>();
            for (String slot : mSelectedSlotsRaw) {
                String[] parts = slot.split("\\|"); 
                if (parts.length >= 4) {
                    String startTimeClean = parts[2].length() > 5 ? parts[2].substring(0, 5) : parts[2];
                    String[] t = startTimeClean.split(":");
                    startMinutes.add(Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]));
                    try {
                        double price = Double.parseDouble(parts[3]);
                        mTotalPrice += price;
                        if (firstSlotPrice == 0) firstSlotPrice = price;
                    } catch (Exception e) { mTotalPrice += 180000; }
                }
            }
            Collections.sort(startMinutes);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < startMinutes.size(); i++) {
                int start = startMinutes.get(i);
                sb.append(String.format(Locale.getDefault(), "%02d:%02d-%02d:%02d", start/60, start%60, (start+60)/60, (start+60)%60));
                if (i < startMinutes.size() - 1) sb.append(", ");
            }
            timeDisplay = sb.toString();
            mStartTime = String.format(Locale.getDefault(), "%02d:%02d", startMinutes.get(0)/60, startMinutes.get(0)%60);
            int lastMins = startMinutes.get(startMinutes.size()-1) + 60;
            mEndTime = String.format(Locale.getDefault(), "%02d:%02d", lastMins/60, lastMins%60);
        }

        // 2. Hiển thị dữ liệu
        DecimalFormat df = new DecimalFormat("#,###");
        ((TextView) view.findViewById(R.id.tvCourtNameHeader)).setText(courtName);
        ((TextView) view.findViewById(R.id.tvPriceHeader)).setText(df.format(firstSlotPrice) + "đ/h");
        ((TextView) view.findViewById(R.id.tvCourtType)).setText(courtTypeName);
        ((TextView) view.findViewById(R.id.tvDate)).setText("Ngày đặt: " + mDate);
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
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                btnConfirm.setEnabled(!name.isEmpty() && !phone.isEmpty());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        etName.addTextChangedListener(tw);
        etPhone.addTextChangedListener(tw);
    }

    private void submitBooking() {
        btnConfirm.setEnabled(false);
        btnConfirm.setText("ĐANG XỬ LÝ...");
        List<BookingRequest.TimeSlot> apiSlots = new ArrayList<>();
        for (String slotRaw : mSelectedSlotsRaw) {
            String[] parts = slotRaw.split("\\|");
            if (parts.length >= 3) {
                String start = parts[2].length() > 5 ? parts[2].substring(0, 5) : parts[2];
                String[] t = start.split(":");
                int endMins = Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]) + 60;
                String end = String.format(Locale.getDefault(), "%02d:%02d", endMins/60, endMins%60);
                apiSlots.add(new BookingRequest.TimeSlot(start, end));
            }
        }
        BookingRequest request = new BookingRequest(mCourtId, etName.getText().toString().trim(), etPhone.getText().toString().trim(), normalizeDate(mDate), "", apiSlots);
        ApiClient.getApiService().createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đặt sân thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack(R.id.navigation_schedule, false);
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnConfirm.setEnabled(true);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true);
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
