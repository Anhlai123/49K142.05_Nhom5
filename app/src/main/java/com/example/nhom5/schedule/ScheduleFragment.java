package com.example.nhom5.schedule;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.app.DatePickerDialog;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Collections;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.api.ApiService;
import com.example.nhom5.models.CourtData;
import com.example.nhom5.models.CourtScheduleResponse;
import com.example.nhom5.models.CourtTypeModel;
import com.example.nhom5.models.Slot;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleFragment extends Fragment {

    private TableLayout tableLayout;
    private TextView tvScheduleTypeText;
    private TextView tvSelectedDate;
    private TextView tvSelectedCourtType;
    private View layoutBySchedule, layoutByCourt;
    private GridLayout timeGrid;
    private TextView court1, court2, court3, court4;
    private ApiService apiService;
    private BottomSheetDialog currentDialog;
    
    private String selectedDateApi;
    private Calendar selectedCalendar;
    private int selectedCourtId = 2; 
    private int currentCourtTypeId = 1; 
    private List<CourtTypeModel> courtTypeList = new ArrayList<>();

    private enum SlotStatus { AVAILABLE, BOOKED, SELECTED, MAINTENANCE }
    private Set<String> selectedSlotKeys = new HashSet<>(); // Format: "courtName|startTime"
    private final int PRICE_PER_SLOT = 180000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        
        tableLayout = view.findViewById(R.id.tableLayout);
        tvScheduleTypeText = view.findViewById(R.id.tvScheduleTypeText);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvSelectedCourtType = view.findViewById(R.id.tvSelectedCourtType);
        layoutBySchedule = view.findViewById(R.id.layoutBySchedule);
        layoutByCourt = view.findViewById(R.id.layoutByCourt);
        timeGrid = view.findViewById(R.id.timeGrid);
        
        apiService = ApiClient.getApiService();
        
        view.findViewById(R.id.boxScheduleType).setOnClickListener(this::showFilterPopupMenu);
        view.findViewById(R.id.boxDatePicker).setOnClickListener(v -> showDatePickerDialog());
        view.findViewById(R.id.boxCourtType).setOnClickListener(this::showCourtTypePopupMenu);

        court1 = view.findViewById(R.id.court1);
        court2 = view.findViewById(R.id.court2);
        court3 = view.findViewById(R.id.court3);
        court4 = view.findViewById(R.id.court4);
        setupCourtSelectionListeners();

        selectedCalendar = Calendar.getInstance();
        updateDateDisplay();

        return view;
    }

    private void updateDateDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDateApi = apiFormat.format(selectedCalendar.getTime());
        tvSelectedDate.setText(displayFormat.format(selectedCalendar.getTime()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCourtTypes();
        loadCourtSchedule(selectedDateApi, currentCourtTypeId);
    }

    private void loadCourtTypes() {
        apiService.getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(Call<List<CourtTypeModel>> call, Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courtTypeList = response.body();
                    for (CourtTypeModel type : courtTypeList) {
                        if (type.getId() == currentCourtTypeId) {
                            tvSelectedCourtType.setText(type.getName());
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CourtTypeModel>> call, Throwable t) {}
        });
    }

    private void loadCourtSchedule(String date, int courtTypeId) {
        apiService.getCourtSchedule(date, courtTypeId).enqueue(new Callback<CourtScheduleResponse>() {
            @Override
            public void onResponse(Call<CourtScheduleResponse> call, Response<CourtScheduleResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    displayTableSchedule(response.body());
                } else {
                    setupMockGrid();
                }
            }
            @Override
            public void onFailure(Call<CourtScheduleResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                setupMockGrid();
            }
        });
    }

    private void displayTableSchedule(CourtScheduleResponse schedule) {
        if (!isAdded() || getContext() == null) return;
        tableLayout.removeAllViews();
        
        String dayOfWeekLabel = "Thứ";
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date date = fmt.parse(schedule.getDate());
            dayOfWeekLabel = new SimpleDateFormat("EEEE", new Locale("vi", "VN")).format(date);
        } catch (Exception e) { dayOfWeekLabel = "Ngày"; }

        TableRow headerRow = new TableRow(getContext());
        headerRow.addView(createStyledHeaderCell("THỨ", ContextCompat.getColor(getContext(), R.color.primary), Color.WHITE, 80));
        headerRow.addView(createStyledHeaderCell("SÂN", Color.parseColor("#F1F8F6"), ContextCompat.getColor(getContext(), R.color.dark_green_text), 80));
        
        if (!schedule.getData().isEmpty()) {
            for (Slot slot : schedule.getData().get(0).getSlots()) {
                headerRow.addView(createStyledHeaderCell(slot.getStartTime().substring(0, 5), Color.parseColor("#F1F8F6"), ContextCompat.getColor(getContext(), R.color.dark_green_text), 60));
            }
        }
        tableLayout.addView(headerRow);

        List<CourtData> courts = schedule.getData();
        for (int i = 0; i < courts.size(); i++) {
            CourtData court = courts.get(i);
            TableRow row = new TableRow(getContext());
            
            TextView dayTv = createCell(i == courts.size() / 2 ? dayOfWeekLabel : "", false, 80);
            if (i == 0) dayTv.setBackgroundResource(R.drawable.bg_day_top);
            else if (i == courts.size() - 1) dayTv.setBackgroundResource(R.drawable.bg_day_bottom);
            else dayTv.setBackgroundResource(R.drawable.bg_day_middle);
            row.addView(dayTv);

            TextView courtTv = createCell(court.getCourtName(), false, 80);
            courtTv.setBackgroundResource(R.drawable.bg_grid_cell);
            courtTv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_grey));
            row.addView(courtTv);

            List<Slot> slots = court.getSlots();
            for (int k = 0; k < slots.size(); k++) {
                Slot slot = slots.get(k);
                String status = slot.getStatus();
                String slotKey = court.getCourtName() + "|" + slot.getStartTime();
                
                if ("booked".equalsIgnoreCase(status) || "maintenance".equalsIgnoreCase(status)) {
                    int span = 1;
                    int next = k + 1;
                    while (next < slots.size() && status.equalsIgnoreCase(slots.get(next).getStatus())) {
                        span++;
                        next++;
                    }
                    TextView spanCell = createCell(status.equalsIgnoreCase("booked") ? "Đã đặt" : "Bảo trì", false, 60 * span);
                    TableRow.LayoutParams params = (TableRow.LayoutParams) spanCell.getLayoutParams();
                    params.span = span;
                    spanCell.setLayoutParams(params);
                    if (status.equalsIgnoreCase("booked")) {
                        spanCell.setBackgroundResource(R.drawable.bg_booked_status);
                        spanCell.setTextColor(Color.parseColor("#FF5252"));
                        spanCell.setOnClickListener(v -> showBookedBottomSheet());
                    } else {
                        spanCell.setBackgroundResource(R.drawable.bg_maintenance_status);
                        spanCell.setTextColor(Color.parseColor("#9E9E9E"));
                    }
                    spanCell.setTypeface(null, Typeface.BOLD);
                    row.addView(spanCell);
                    k = next - 1;
                } else {
                    TextView cell = new TextView(getContext());
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
                    );
                    cell.setLayoutParams(params);
                    cell.setBackgroundResource(R.drawable.bg_grid_cell);
                    updateTableCellUI(cell, selectedSlotKeys.contains(slotKey));
                    cell.setOnClickListener(v -> toggleSlotSelection(slotKey, cell));
                    row.addView(cell);
                }
            }
            tableLayout.addView(row);
        }
    }

    private void toggleSlotSelection(String slotKey, View cell) {
        if (selectedSlotKeys.contains(slotKey)) {
            selectedSlotKeys.remove(slotKey);
            if (cell instanceof TextView) updateTableCellUI((TextView) cell, false);
        } else {
            selectedSlotKeys.add(slotKey);
            if (cell instanceof TextView) updateTableCellUI((TextView) cell, true);
        }

        if (selectedSlotKeys.isEmpty()) {
            if (currentDialog != null) currentDialog.dismiss();
        } else {
            showBookingConfirmation(); 
        }
    }

    private void showBookingConfirmation() {
        if (getContext() == null) return;
        
        if (currentDialog != null && currentDialog.isShowing()) {
            updatePopupContent();
            return;
        }

        currentDialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_confirm_booking, null);
        currentDialog.setContentView(bottomSheetView);
        updatePopupContent();

        bottomSheetView.findViewById(R.id.btnConfirmBooking).setOnClickListener(v -> navigateToBooking());

        Window window = currentDialog.getWindow();
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }
        currentDialog.show();
    }

    private void updatePopupContent() {
        if (currentDialog == null) return;
        TextView tvTitle = currentDialog.findViewById(R.id.tvCourtTitle);
        if (tvTitle != null) {
            tvTitle.setText(getFormattedSelectionSummary());
        }
    }

    private String getFormattedSelectionSummary() {
        if (selectedSlotKeys.isEmpty()) return "";
        
        TreeMap<String, List<Integer>> groupedByCourt = new TreeMap<>();
        for (String key : selectedSlotKeys) {
            String[] parts = key.split("\\|");
            String court = parts[0];
            String[] timeParts = parts[1].split(":");
            int minutes = Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1]);
            
            if (!groupedByCourt.containsKey(court)) groupedByCourt.put(court, new ArrayList<>());
            groupedByCourt.get(court).add(minutes);
        }

        StringBuilder sb = new StringBuilder();
        for (String court : groupedByCourt.keySet()) {
            List<Integer> times = groupedByCourt.get(court);
            Collections.sort(times);
            
            sb.append(court).append(": ");
            for (int i = 0; i < times.size(); i++) {
                int start = times.get(i);
                int end = start + 60; // Giả sử mỗi slot 1 tiếng
                sb.append(String.format("%02d:%02d - %02d:%02d", start/60, start%60, end/60, end%60));
                if (i < times.size() - 1) sb.append(", ");
            }
            if (groupedByCourt.size() > 1) sb.append(" | ");
        }
        return sb.toString();
    }

    private void navigateToBooking() {
        if (currentDialog != null) currentDialog.dismiss();
        if (isAdded() && !selectedSlotKeys.isEmpty()) {
            ArrayList<String> slotList = new ArrayList<>(selectedSlotKeys);
            String firstKey = slotList.get(0);
            String[] parts = firstKey.split("\\|");
            
            Bundle args = new Bundle();
            args.putInt("courtId", 1); // Mock ID
            args.putString("courtName", parts[0]);
            args.putString("date", tvSelectedDate.getText().toString());
            args.putStringArrayList("selectedSlots", slotList); // Gửi toàn bộ danh sách
            args.putInt("totalSlots", slotList.size());

            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_schedule_to_bookingConfirmationFragment, args);
        }
    }

    private void updateTableCellUI(TextView cell, boolean isSelected) {
        if (isSelected) {
            cell.setBackgroundResource(R.drawable.bg_grid_cell);
            cell.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.selected_slot_bg)));
        } else {
            cell.setBackgroundResource(R.drawable.bg_grid_cell);
            cell.setBackgroundTintList(null);
        }
    }

    private void setupMockGrid() {
        if (tableLayout == null || getContext() == null) return;
        tableLayout.removeAllViews();
        setupTimeGrid();
    }

    private void setupTimeGrid() {
        if (!isAdded() || getContext() == null) return;
        timeGrid.removeAllViews();
        String[] slots = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
        for (String slot : slots) {
            TextView tv = new TextView(getContext());
            tv.setText(slot);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 32, 0, 32);
            tv.setTextSize(14);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTag(slot);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            tv.setLayoutParams(params);
            
            String slotKey = "Sân " + selectedCourtId + "|" + slot;
            SlotStatus status = getSlotStatusMock(slot);
            if (selectedSlotKeys.contains(slotKey)) status = SlotStatus.SELECTED;
            updateSlotUI(tv, status);
            tv.setOnClickListener(v -> handleSlotClick(tv, slotKey));
            timeGrid.addView(tv);
        }
    }

    private SlotStatus getSlotStatusMock(String slot) {
        if (slot.equals("15:00") || slot.equals("16:00")) return SlotStatus.BOOKED;
        return SlotStatus.AVAILABLE;
    }

    private void handleSlotClick(TextView tv, String slotKey) {
        SlotStatus currentStatus = getSlotStatusMock((String)tv.getTag());
        if (currentStatus == SlotStatus.BOOKED) {
            showBookedBottomSheet();
            return;
        }
        toggleSlotSelection(slotKey, tv);
    }

    private void updateSlotUI(TextView tv, SlotStatus status) {
        if (!isAdded()) return;
        int bgColor, textColor;
        switch (status) {
            case BOOKED:
                bgColor = ContextCompat.getColor(getContext(), R.color.booked_cell_bg);
                textColor = Color.parseColor("#FF5252");
                break;
            case SELECTED:
                bgColor = ContextCompat.getColor(getContext(), R.color.selected_slot_bg);
                textColor = ContextCompat.getColor(getContext(), R.color.selected_slot_text);
                break;
            default:
                bgColor = Color.WHITE;
                textColor = Color.parseColor("#808080");
                break;
        }
        applySlotStyles(tv, bgColor, textColor);
    }

    private void applySlotStyles(TextView tv, int bgColor, int textColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        drawable.setColor(bgColor);
        drawable.setStroke((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), Color.parseColor("#F1F3F4"));
        tv.setBackground(drawable);
        tv.setTextColor(textColor);
    }

    private TextView createStyledHeaderCell(String text, int bgColor, int textColor, int widthDp) {
        TextView tv = createCell(text, true, widthDp);
        tv.setBackgroundResource(R.drawable.bg_grid_cell);
        tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));
        tv.setTextColor(textColor);
        return tv;
    }

    private TextView createCell(String text, boolean isHeader, int widthDp) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
        );
        tv.setLayoutParams(params);
        tv.setTextSize(11);
        if (isHeader) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
            updateDateDisplay();
            selectedSlotKeys.clear();
            if (currentDialog != null) currentDialog.dismiss();
            loadCourtSchedule(selectedDateApi, currentCourtTypeId);
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), selectedCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupCourtSelectionListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.court1) selectedCourtId = 1;
            else if (id == R.id.court2) selectedCourtId = 2;
            else if (id == R.id.court3) selectedCourtId = 3;
            else if (id == R.id.court4) selectedCourtId = 4;
            updateCourtSelectionUI();
            setupTimeGrid();
        };
        court1.setOnClickListener(listener);
        court2.setOnClickListener(listener);
        court3.setOnClickListener(listener);
        court4.setOnClickListener(listener);
    }

    private void updateCourtSelectionUI() {
        TextView[] courts = {court1, court2, court3, court4};
        for (int i = 0; i < courts.length; i++) {
            if (i + 1 == selectedCourtId) {
                courts[i].setBackgroundResource(R.drawable.bg_court_card_selected);
                courts[i].setTextColor(ContextCompat.getColor(getContext(), R.color.primary));
            } else {
                courts[i].setBackgroundResource(R.drawable.bg_court_card);
                courts[i].setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green_text));
            }
        }
    }

    private void showCourtTypePopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        if (courtTypeList.isEmpty()) popupMenu.getMenu().add(0, 0, 0, "Đang tải...");
        else {
            for (CourtTypeModel type : courtTypeList) popupMenu.getMenu().add(0, type.getId(), 0, type.getName());
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) return false;
            tvSelectedCourtType.setText(item.getTitle());
            currentCourtTypeId = item.getItemId();
            selectedSlotKeys.clear();
            if (currentDialog != null) currentDialog.dismiss();
            loadCourtSchedule(selectedDateApi, currentCourtTypeId);
            return true;
        });
        popupMenu.show();
    }

    private void showFilterPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_schedule_filter, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            tvScheduleTypeText.setText(item.getTitle());
            if (item.getItemId() == R.id.menu_by_court) {
                layoutBySchedule.setVisibility(View.GONE);
                layoutByCourt.setVisibility(View.VISIBLE);
            } else {
                layoutBySchedule.setVisibility(View.VISIBLE);
                layoutByCourt.setVisibility(View.GONE);
            }
            return true;
        });
        popupMenu.show();
    }

    private void showBookedBottomSheet() {
        currentDialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_booked_bottom_sheet, null);
        view.findViewById(R.id.btn_close_booked).setOnClickListener(v -> currentDialog.dismiss());
        currentDialog.setContentView(view);
        currentDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();
        currentDialog = null;
    }
}
