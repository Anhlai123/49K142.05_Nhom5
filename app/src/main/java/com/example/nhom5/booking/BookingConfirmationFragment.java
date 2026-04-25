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
    private final int MOCK_PRICE_PER_HOUR = 180000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirmation, container, false);

        // Retrieve arguments
        String courtName = "Sân Cầu Lông 2";
        String date = "13/01/2026";
        int courtId = -1;
        ArrayList<String> selectedSlots = new ArrayList<>();

        if (getArguments() != null) {
            courtName = getArguments().getString("courtName", courtName);
            date = getArguments().getString("date", date);
            courtId = getArguments().getInt("courtId", -1);
            selectedSlots = getArguments().getStringArrayList("selectedSlots");
        }

        // Logic for multiple slots
        int numSlots = (selectedSlots != null) ? selectedSlots.size() : 1;
        double totalPrice = (double) numSlots * MOCK_PRICE_PER_HOUR;

        // Formatting display string
        String timeSummary = "Chưa chọn giờ";
        String firstStartTime = "07:00";
        String lastEndTime = "08:00";

        if (selectedSlots != null && !selectedSlots.isEmpty()) {
            List<String> timesOnly = new ArrayList<>();
            for (String key : selectedSlots) {
                String[] parts = key.split("\\|");
                timesOnly.add(parts[1]);
            }
            Collections.sort(timesOnly);
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < timesOnly.size(); i++) {
                String start = timesOnly.get(i);
                int hour = Integer.parseInt(start.split(":")[0]);
                String end = String.format(Locale.getDefault(), "%02d:00", hour + 1);
                sb.append(start).append("-").append(end);
                if (i < timesOnly.size() - 1) sb.append(", ");
            }
            timeSummary = sb.toString();
            firstStartTime = timesOnly.get(0);
            
            // Calculate last end time for API
            int lastHour = Integer.parseInt(timesOnly.get(timesOnly.size()-1).split(":")[0]);
            lastEndTime = String.format(Locale.getDefault(), "%02d:00", lastHour + 1);
        }

        DecimalFormat priceFormatter = new DecimalFormat("#,###");
        String formattedTotalPrice = priceFormatter.format(totalPrice) + " đ";

        // Bind views
        TextView tvCourtNameHeader = view.findViewById(R.id.tvCourtNameHeader);
        TextView tvPriceHeader = view.findViewById(R.id.tvPriceHeader);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvCourtField = view.findViewById(R.id.tvCourtField);
        TextView tvTimeField = view.findViewById(R.id.tvTimeField);
        TextView tvHoursField = view.findViewById(R.id.tvHoursField);
        TextView tvPriceField = view.findViewById(R.id.tvPriceField);
        etName = view.findViewById(R.id.etName);
        etPhone = view.findViewById(R.id.etPhone);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        
        // Set data
        tvCourtNameHeader.setText(courtName);
        tvPriceHeader.setText(getString(R.string.booking_price_per_hour, priceFormatter.format(MOCK_PRICE_PER_HOUR)));
        tvDate.setText(getString(R.string.booking_date_label, date));
        tvCourtField.setText(courtName);
        tvTimeField.setText(timeSummary); // Show ALL selected ranges
        tvHoursField.setText(numSlots + " giờ");
        tvPriceField.setText(formattedTotalPrice);

        final String finalStartTime = firstStartTime;
        final String finalEndTime = lastEndTime;
        final String finalDate = date;
        final int finalCourtId = courtId;

        btnConfirm.setOnClickListener(v -> submitBooking(finalCourtId, finalDate, finalStartTime, finalEndTime));

        // Navigation
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        
        Button btnCancel = view.findViewById(R.id.btnCancel);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        validateInput();
        etName.addTextChangedListener(new SimpleTextWatcher());
        etPhone.addTextChangedListener(new SimpleTextWatcher());

        return view;
    }

    private void submitBooking(int courtId, String date, String startTime, String endTime) {
        if (!isAdded()) return;

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        
        btnConfirm.setEnabled(false);
        btnConfirm.setText(R.string.booking_creating_button);

        BookingRequest request = new BookingRequest(
                courtId, name, phone, normalizeDateForRequest(date),
                startTime, endTime, ""
        );

        ApiService apiService = ApiClient.getApiService();
        apiService.createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                if (!isAdded()) return;
                btnConfirm.setEnabled(true);
                btnConfirm.setText(R.string.booking_confirm_button);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đặt sân thành công!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack(R.id.navigation_schedule, false);
                } else {
                    Toast.makeText(requireContext(), "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnConfirm.setEnabled(true);
                btnConfirm.setText(R.string.booking_confirm_button);
                Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String normalizeDateForRequest(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(parsedDate);
        } catch (Exception e) { return date; }
    }

    private void validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        btnConfirm.setEnabled(!name.isEmpty() && !phone.isEmpty());
    }

    private class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { validateInput(); }
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
