package com.example.nhom5;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.text.DecimalFormat;
import java.util.Locale;

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
            e.printStackTrace();
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
        tvPriceHeader.setText(priceFormatter.format(pricePerHour) + "/h");
        tvDate.setText("Ngày đặt: " + date);
        tvCourtField.setText(courtName);
        tvTimeField.setText(startTime + " - " + endTime);
        tvHoursField.setText(String.format(Locale.getDefault(), "%dh00", (int)totalHours));
        tvPriceField.setText(formattedPrice);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString();
            Toast.makeText(requireContext(), "Đặt sân thành công cho " + name, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.navigation_home);
        });

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

    private void validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        btnConfirm.setEnabled(!name.isEmpty() && !phone.isEmpty());
    }
}
