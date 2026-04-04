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
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingConfirmationFragment extends Fragment {

    private EditText etName, etPhone;
    private Button btnConfirm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_confirmation, container, false);

        // 3. Auto-Fill Logic: Retrieve arguments
        String courtName = getArguments() != null ? getArguments().getString("courtName", "Sân Cầu Lông 2") : "Sân Cầu Lông 2";
        String startTime = getArguments() != null ? getArguments().getString("startTime", "07:00") : "07:00";
        String date = getArguments() != null ? getArguments().getString("date", "13/01/2026") : "13/01/2026";
        int courtId = getArguments() != null ? getArguments().getInt("courtId", -1) : -1;

        // pricePerHour in nav_graph is integer, but original code used double. Checking both.
        double pricePerHour = 60000.0;
        if (getArguments() != null) {
            Object price = getArguments().get("pricePerHour");
            if (price instanceof Integer) {
                pricePerHour = (Integer) price;
            } else if (price instanceof Double) {
                pricePerHour = (Double) price;
            }
        }

        // Calculate End Time
        int startHour = 7;
        try {
            startHour = Integer.parseInt(startTime.split(":")[0]);
        } catch (Exception e) {
            Log.w("BOOKING_CONFIRM", "Invalid start time, using default", e);
        }
        String endTime = String.format(Locale.getDefault(), "%02d:00", startHour + 1);
        double totalHours = 1.0;
        double totalPrice = totalHours * pricePerHour;

        DecimalFormat priceFormatter = new DecimalFormat("#,###");
        String formattedPrice = priceFormatter.format(totalPrice) + " đ";

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
        Button btnCancel = view.findViewById(R.id.btnCancel);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Set data
        tvCourtNameHeader.setText(courtName);
        tvPriceHeader.setText(getString(R.string.booking_price_per_hour, priceFormatter.format(pricePerHour)));
        tvDate.setText(getString(R.string.booking_date_label, date));
        tvCourtField.setText(courtName);
        tvTimeField.setText(getString(R.string.booking_time_range, startTime, endTime));
        tvHoursField.setText(String.format(Locale.getDefault(), "%dh00", (int) totalHours));
        tvPriceField.setText(formattedPrice);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        btnConfirm.setOnClickListener(v -> submitBooking(courtId, date, startTime, endTime));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInput();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);

        validateInput();

        return view;
    }

    private void submitBooking(int courtId, String date, String startTime, String endTime) {
        if (!isAdded() || getContext() == null) return;

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.booking_missing_contact), Toast.LENGTH_SHORT).show();
            return;
        }
        if (courtId <= 0) {
            Toast.makeText(requireContext(), getString(R.string.booking_missing_court), Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText(R.string.booking_creating_button);

        BookingRequest request = new BookingRequest(
                courtId,
                name,
                phone,
                normalizeDateForRequest(date),
                startTime,
                endTime,
                ""
        );

        ApiService apiService = ApiClient.getApiService();
        apiService.createBooking(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                if (!isAdded() || getContext() == null) return;

                btnConfirm.setEnabled(true);
                btnConfirm.setText(R.string.booking_confirm_button);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), getString(R.string.booking_success_message, name), Toast.LENGTH_SHORT).show();
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigate(R.id.navigation_home);
                    }
                } else {
                    Toast.makeText(requireContext(), "Đặt sân thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e("BOOKING_API", "createBooking failed with code " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;

                btnConfirm.setEnabled(true);
                btnConfirm.setText(R.string.booking_confirm_button);
                Toast.makeText(requireContext(), getString(R.string.booking_connect_error), Toast.LENGTH_SHORT).show();
                Log.e("BOOKING_API", "createBooking error", t);
            }
        });
    }

    private String normalizeDateForRequest(String date) {
        if (date == null) return "";
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return date;
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            inputFormat.setLenient(false);
            Date parsedDate = inputFormat.parse(date);
            if (parsedDate == null) return date;
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            return date;
        }
    }

    private void validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        btnConfirm.setEnabled(!name.isEmpty() && !phone.isEmpty());
        if (btnConfirm.isEnabled()) {
            btnConfirm.setText(R.string.booking_confirm_button);
        }
    }
}
