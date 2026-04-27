package com.example.nhom5.booking;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.models.BookingRequest;
import com.example.nhom5.models.BookingResponse;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingConfirmationFragment extends Fragment {

    private EditText etName, etPhone;
    private TextView tvErrorName, tvErrorPhone;
    private Button btnConfirm;
    private String mDate = "";
    private double mTotalPrice = 0;
    private ArrayList<String> mSelectedSlotsRaw = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirmation, container, false);

        // 1. Nhận dữ liệu
        String courtTypeName = "Sân Cầu lông";
        if (getArguments() != null) {
            mDate = getArguments().getString("date", "");
            mSelectedSlotsRaw = getArguments().getStringArrayList("selectedSlots");
            courtTypeName = getArguments().getString("courtTypeName", "Sân Cầu lông");
        }

        LinearLayout llMultipleCourtsContainer = view.findViewById(R.id.llMultipleCourtsContainer);
        RelativeLayout rlSingleCourtHeader = view.findViewById(R.id.rlSingleCourtHeader);
        TextView tvCourtNameHeader = view.findViewById(R.id.tvCourtNameHeader);
        TextView tvPriceHeader = view.findViewById(R.id.tvPriceHeader);
        DecimalFormat df = new DecimalFormat("#,###");

        if (mSelectedSlotsRaw != null && !mSelectedSlotsRaw.isEmpty()) {
            mTotalPrice = 0;
            TreeMap<String, List<Integer>> groupedTimes = new TreeMap<>();
            List<SlotDetail> allDetails = new ArrayList<>();

            for (String slot : mSelectedSlotsRaw) {
                String[] parts = slot.split("\\|"); 
                if (parts.length >= 4) {
                    String courtName = parts[1];
                    String startTimeStr = parts[2].length() > 5 ? parts[2].substring(0, 5) : parts[2];
                    String[] t = startTimeStr.split(":");
                    int mins = Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]);
                    
                    double price = 0;
                    try { price = Double.parseDouble(parts[3]); } catch (Exception e) { price = 180000; }
                    
                    if (!groupedTimes.containsKey(courtName)) groupedTimes.put(courtName, new ArrayList<>());
                    groupedTimes.get(courtName).add(mins);
                    
                    allDetails.add(new SlotDetail(courtName, startTimeStr, price));
                    mTotalPrice += price;
                }
            }

            // Hiển thị thông tin sân
            if (allDetails.size() >= 2) {
                rlSingleCourtHeader.setVisibility(View.GONE);
                llMultipleCourtsContainer.setVisibility(View.VISIBLE);
                llMultipleCourtsContainer.removeAllViews();
                
                Map<String, Double> courtPriceSummary = new HashMap<>();
                for(SlotDetail d : allDetails) {
                    courtPriceSummary.put(d.courtName, courtPriceSummary.getOrDefault(d.courtName, 0.0) + d.price);
                }
                
                for (Map.Entry<String, Double> entry : courtPriceSummary.entrySet()) {
                    addCourtPriceRow(llMultipleCourtsContainer, entry.getKey(), df.format(entry.getValue()) + "đ", tvCourtNameHeader, tvPriceHeader);
                }
            } else if (allDetails.size() == 1) {
                rlSingleCourtHeader.setVisibility(View.VISIBLE);
                llMultipleCourtsContainer.setVisibility(View.GONE);
                SlotDetail detail = allDetails.get(0);
                tvCourtNameHeader.setText(detail.courtName);
                tvCourtNameHeader.setTypeface(null, Typeface.BOLD);
                tvPriceHeader.setText(df.format(detail.price) + "đ/h");
                tvPriceHeader.setTypeface(null, Typeface.BOLD);
            }

            // Hiển thị chi tiết thời gian
            StringBuilder sbDetail = new StringBuilder();
            int count = 0;
            for (String court : groupedTimes.keySet()) {
                List<Integer> times = groupedTimes.get(court);
                Collections.sort(times);
                sbDetail.append(court).append(": ");
                for (int i = 0; i < times.size(); i++) {
                    int start = times.get(i);
                    sbDetail.append(String.format(Locale.getDefault(), "%02d:%02d-%02d:%02d", start/60, start%60, (start+60)/60, (start+60)%60));
                    if (i < times.size() - 1) sbDetail.append(", ");
                }
                if (++count < groupedTimes.size()) sbDetail.append("\n");
            }
            ((TextView) view.findViewById(R.id.tvTimeField)).setText(sbDetail.toString());
        }

        // Hiển thị các thông tin khác
        ((TextView) view.findViewById(R.id.tvCourtType)).setText("Loại sân: " + courtTypeName);
        ((TextView) view.findViewById(R.id.tvDate)).setText("Ngày đặt: " + mDate);
        ((TextView) view.findViewById(R.id.tvHoursField)).setText((mSelectedSlotsRaw != null ? mSelectedSlotsRaw.size() : 0) + " giờ");
        ((TextView) view.findViewById(R.id.tvPriceField)).setText(df.format(mTotalPrice) + " đ");

        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        tvErrorName = view.findViewById(R.id.tvErrorName);
        tvErrorPhone = view.findViewById(R.id.tvErrorPhone);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnConfirm.setOnClickListener(v -> submitBooking());

        setupInputValidation();
        return view;
    }

    private void addCourtPriceRow(LinearLayout container, String name, String price, TextView templateName, TextView templatePrice) {
        RelativeLayout row = new RelativeLayout(getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int paddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        row.setPadding(0, 0, 0, paddingBottom);

        TextView tvName = new TextView(getContext());
        tvName.setText(name);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, templateName.getTextSize());
        tvName.setTypeface(null, Typeface.BOLD);
        tvName.setTextColor(templateName.getTextColors());
        RelativeLayout.LayoutParams lpName = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpName.addRule(RelativeLayout.ALIGN_PARENT_START);
        tvName.setLayoutParams(lpName);

        TextView tvPrice = new TextView(getContext());
        tvPrice.setText(price);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, templatePrice.getTextSize());
        tvPrice.setTypeface(null, Typeface.BOLD);
        tvPrice.setTextColor(templatePrice.getTextColors());
        RelativeLayout.LayoutParams lpPrice = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpPrice.addRule(RelativeLayout.ALIGN_PARENT_END);
        tvPrice.setLayoutParams(lpPrice);

        row.addView(tvName);
        row.addView(tvPrice);
        container.addView(row);
    }

    private static class SlotDetail {
        String courtName;
        String time;
        double price;
        SlotDetail(String courtName, String time, double price) {
            this.courtName = courtName;
            this.time = time;
            this.price = price;
        }
    }

    private void setupInputValidation() {
        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tvErrorName.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tvErrorPhone.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void submitBooking() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        boolean hasError = false;
        if (name.isEmpty()) {
            tvErrorName.setVisibility(View.VISIBLE);
            hasError = true;
        }
        if (phone.isEmpty()) {
            tvErrorPhone.setVisibility(View.VISIBLE);
            hasError = true;
        }

        if (hasError) return;

        btnConfirm.setEnabled(false);
        btnConfirm.setText("ĐANG XỬ LÝ...");

        Map<Integer, List<BookingRequest.TimeSlot>> groupedByCourt = new HashMap<>();
        for (String slotRaw : mSelectedSlotsRaw) {
            String[] parts = slotRaw.split("\\|");
            if (parts.length >= 4) {
                int courtId = Integer.parseInt(parts[0]);
                String start = parts[2].length() > 5 ? parts[2].substring(0, 5) : parts[2];
                String[] t = start.split(":");
                int endMins = Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]) + 60;
                String end = String.format(Locale.getDefault(), "%02d:%02d", endMins/60, endMins%60);
                
                if (!groupedByCourt.containsKey(courtId)) groupedByCourt.put(courtId, new ArrayList<>());
                groupedByCourt.get(courtId).add(new BookingRequest.TimeSlot(start, end));
            }
        }

        final int totalRequests = groupedByCourt.size();
        final int[] finishedCount = {0};
        final int[] successCount = {0};
        final List<String> errorMessages = Collections.synchronizedList(new ArrayList<>());
        String date = normalizeDate(mDate);

        for (Map.Entry<Integer, List<BookingRequest.TimeSlot>> entry : groupedByCourt.entrySet()) {
            BookingRequest request = new BookingRequest(entry.getKey(), name, phone, date, "", entry.getValue());
            ApiClient.getApiService().createBooking(request).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                    synchronized (finishedCount) {
                        finishedCount[0]++;
                        if (response.isSuccessful()) {
                            successCount[0]++;
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String errorMsg = jsonObject.optString("error", "Lỗi không xác định");
                                errorMessages.add(errorMsg);
                            } catch (Exception e) {
                                errorMessages.add("Lỗi hệ thống: " + response.code());
                            }
                        }
                        checkAllFinished(finishedCount[0], successCount[0], totalRequests, errorMessages);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                    synchronized (finishedCount) {
                        finishedCount[0]++;
                        errorMessages.add("Lỗi kết nối Server");
                        checkAllFinished(finishedCount[0], successCount[0], totalRequests, errorMessages);
                    }
                }
            });
        }
    }

    private void checkAllFinished(int finished, int success, int total, List<String> errorMessages) {
        if (finished == total) {
            if (success == total) {
                SuccessDialogFragment dialog = SuccessDialogFragment.newInstance("Đặt sân thành công", () -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).popBackStack(R.id.navigation_schedule, false);
                    }
                });
                dialog.show(getParentFragmentManager(), "success_dialog");
            } else {
                if (!errorMessages.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String msg : errorMessages) {
                        sb.append(msg).append("\n");
                    }
                    Toast.makeText(getContext(), sb.toString().trim(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Đặt sân thất bại! Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
                btnConfirm.setEnabled(true);
                btnConfirm.setText("XÁC NHẬN");
            }
        }
    }

    private String normalizeDate(String date) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return out.format(in.parse(date));
        } catch (Exception e) { return date; }
    }
}
