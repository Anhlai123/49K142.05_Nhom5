package com.example.nhom5.schedule;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
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
    private LinearLayout layoutDayColumnScrollable;
    private GridLayout timeGrid;
    private LinearLayout layoutCourtSelector;
    private ApiService apiService;
    private BottomSheetDialog currentDialog;
    
    private String selectedDateApi;
    private Calendar selectedCalendar;
    private int currentCourtTypeId = -1; 
    private int selectedCourtId = -1; 
    private List<CourtTypeModel> courtTypeList = new ArrayList<>();
    private List<CourtData> currentCourtsData = new ArrayList<>();

    private enum SlotStatus { AVAILABLE, BOOKED, SELECTED, MAINTENANCE }
    private Set<String> selectedSlotKeys = new HashSet<>(); 

    private final int CELL_HEIGHT_DP = 65;

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
        layoutDayColumnScrollable = view.findViewById(R.id.layoutDayColumnScrollable);
        timeGrid = view.findViewById(R.id.timeGrid);
        layoutCourtSelector = view.findViewById(R.id.layoutCourtSelector);
        
        apiService = ApiClient.getApiService();
        
        if (tvScheduleTypeText != null) tvScheduleTypeText.setTextSize(15);
        if (tvSelectedCourtType != null) tvSelectedCourtType.setTextSize(15);
        if (tvSelectedDate != null) tvSelectedDate.setTextSize(15);

        view.findViewById(R.id.boxScheduleType).setOnClickListener(this::showFilterPopupMenu);
        
        View datePickerBox = view.findViewById(R.id.boxDatePicker);
        if (datePickerBox != null) {
            datePickerBox.setOnClickListener(v -> showDatePickerDialog());
        }
        
        view.findViewById(R.id.boxCourtType).setOnClickListener(this::showCourtTypePopupMenu);

        selectedCalendar = Calendar.getInstance();
        updateDateDisplay();

        return view;
    }

    private void updateDateDisplay() {
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDateApi = apiFormat.format(selectedCalendar.getTime());
        if (tvSelectedDate != null) {
            tvSelectedDate.setText(displayFormat.format(selectedCalendar.getTime()));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCourtTypes();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentCourtTypeId != -1) {
            loadCourtSchedule(selectedDateApi, currentCourtTypeId);
        }
    }

    private void loadCourtTypes() {
        apiService.getCourtTypes().enqueue(new Callback<List<CourtTypeModel>>() {
            @Override
            public void onResponse(Call<List<CourtTypeModel>> call, Response<List<CourtTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    courtTypeList = response.body();
                    if (!courtTypeList.isEmpty()) {
                        CourtTypeModel firstType = courtTypeList.get(0);
                        currentCourtTypeId = firstType.getId();
                        tvSelectedCourtType.setText(firstType.getName());
                        loadCourtSchedule(selectedDateApi, currentCourtTypeId);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<CourtTypeModel>> call, Throwable t) {}
        });
    }

    private void loadCourtSchedule(String date, int courtTypeId) {
        if (courtTypeId == -1) return;
        apiService.getCourtSchedule(date, courtTypeId).enqueue(new Callback<CourtScheduleResponse>() {
            @Override
            public void onResponse(Call<CourtScheduleResponse> call, Response<CourtScheduleResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    currentCourtsData = response.body().getData();
                    selectedSlotKeys.clear();
                    
                    if (selectedCourtId == -1 && !currentCourtsData.isEmpty()) {
                        selectedCourtId = currentCourtsData.get(0).getCourtId();
                    }
                    
                    displayTableSchedule(response.body());
                    updateCourtSelectorUI();
                    setupTimeGrid(); 
                }
            }
            @Override
            public void onFailure(Call<CourtScheduleResponse> call, Throwable t) {}
        });
    }

    private boolean isBooked(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.equals("booked") || s.equals("pending") || s.equals("confirmed") || s.equals("completed");
    }

    private void displayTableSchedule(CourtScheduleResponse schedule) {
        if (!isAdded() || getContext() == null) return;
        tableLayout.removeAllViews();
        layoutDayColumnScrollable.removeAllViews();
        
        String dayName = new SimpleDateFormat("EEEE", new Locale("vi", "VN")).format(selectedCalendar.getTime());
        if (dayName.equalsIgnoreCase("Thứ Hai")) dayName = "Thứ 2";
        else if (dayName.equalsIgnoreCase("Thứ Ba")) dayName = "Thứ 3";
        else if (dayName.equalsIgnoreCase("Thứ Tư")) dayName = "Thứ 4";
        else if (dayName.equalsIgnoreCase("Thứ Năm")) dayName = "Thứ 5";
        else if (dayName.equalsIgnoreCase("Thứ Sáu")) dayName = "Thứ 6";
        else if (dayName.equalsIgnoreCase("Thứ Bảy")) dayName = "Thứ 7";
        else if (dayName.equalsIgnoreCase("Chủ Nhật")) dayName = "CN";

        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CELL_HEIGHT_DP, getResources().getDisplayMetrics());

        // 1. Cột THỨ
        TextView headerThu = new TextView(getContext());
        headerThu.setText("THỨ");
        headerThu.setGravity(Gravity.CENTER);
        headerThu.setTypeface(null, Typeface.BOLD);
        headerThu.setTextColor(Color.WHITE);
        headerThu.setLayoutParams(new LinearLayout.LayoutParams(widthPx, heightPx));
        headerThu.setBackgroundResource(R.drawable.bg_grid_cell);
        headerThu.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#5BA260")));
        layoutDayColumnScrollable.addView(headerThu);

        if (!currentCourtsData.isEmpty()) {
            TextView dayContent = new TextView(getContext());
            dayContent.setText(dayName);
            dayContent.setGravity(Gravity.CENTER);
            dayContent.setTypeface(null, Typeface.BOLD);
            dayContent.setTextColor(Color.parseColor("#2D4F31"));
            dayContent.setTextSize(16);
            int totalHeightPx = currentCourtsData.size() * heightPx;
            dayContent.setLayoutParams(new LinearLayout.LayoutParams(widthPx, totalHeightPx));
            dayContent.setBackgroundResource(R.drawable.bg_day_content);
            layoutDayColumnScrollable.addView(dayContent);
        }

        // 2. TableLayout
        TableRow headerRow = new TableRow(getContext());
        headerRow.addView(createStyledHeaderCell("SÂN", Color.WHITE, Color.parseColor("#1A202C"), 80));
        
        if (!currentCourtsData.isEmpty()) {
            for (Slot slot : currentCourtsData.get(0).getSlots()) {
                headerRow.addView(createStyledHeaderCell(slot.getStartTime().substring(0, 5), Color.parseColor("#F8F9FA"), Color.parseColor("#5BA260"), 80));
            }
        }
        tableLayout.addView(headerRow);

        for (CourtData court : currentCourtsData) {
            TableRow row = new TableRow(getContext());
            
            TextView courtTv = createCell(court.getCourtName(), false, 80);
            courtTv.setBackgroundResource(R.drawable.bg_grid_cell);
            courtTv.setTypeface(null, Typeface.BOLD);
            courtTv.setTextColor(Color.parseColor("#1A202C"));
            row.addView(courtTv);

            List<Slot> slots = court.getSlots();
            for (int k = 0; k < slots.size(); k++) {
                Slot slot = slots.get(k);
                String status = slot.getStatus();
                if (isBooked(status) || "maintenance".equalsIgnoreCase(status)) {
                    boolean typeIsBooked = isBooked(status);
                    int span = 1;
                    int next = k + 1;
                    while (next < slots.size() && 
                          ((typeIsBooked && isBooked(slots.get(next).getStatus())) || 
                           (!typeIsBooked && "maintenance".equalsIgnoreCase(slots.get(next).getStatus())))) {
                        span++;
                        next++;
                    }
                    TextView spanCell = createCell(typeIsBooked ? "Đã đặt" : "BẢO TRÌ", false, 80 * span);
                    TableRow.LayoutParams params = (TableRow.LayoutParams) spanCell.getLayoutParams();
                    params.span = span;
                    spanCell.setLayoutParams(params);
                    spanCell.setPadding(0, 0, 0, 0);
                    if (typeIsBooked) {
                        spanCell.setBackgroundResource(R.drawable.bg_booked_status);
                        spanCell.setTextColor(Color.parseColor("#FF5252"));
                        spanCell.setOnClickListener(v -> showBookedBottomSheet());
                    } else {
                        spanCell.setBackgroundResource(R.drawable.bg_maintenance_status);
                        spanCell.setTextColor(Color.parseColor("#757575"));
                        spanCell.setOnClickListener(v -> showMaintenanceBottomSheet());
                    }
                    spanCell.setTypeface(null, Typeface.BOLD);
                    row.addView(spanCell);
                    k = next - 1;
                } else {
                    String slotKey = court.getCourtId() + "|" + court.getCourtName() + "|" + slot.getStartTime() + "|" + slot.getPrice();
                    TextView cell = new TextView(getContext());
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CELL_HEIGHT_DP, getResources().getDisplayMetrics())
                    );
                    cell.setLayoutParams(params);
                    cell.setBackgroundResource(R.drawable.bg_slot_available); 
                    cell.setGravity(Gravity.CENTER);
                    updateTableCellUI(cell, selectedSlotKeys.contains(slotKey));
                    cell.setOnClickListener(v -> toggleSlotSelection(slotKey, cell));
                    row.addView(cell);
                }
            }
            tableLayout.addView(row);
        }
    }

    private void updateCourtSelectorUI() {
        if (layoutCourtSelector == null || getContext() == null) return;
        layoutCourtSelector.removeAllViews();
        for (CourtData court : currentCourtsData) {
            TextView tv = new TextView(getContext());
            tv.setText(court.getCourtName());
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(32, 0, 32, 0);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(13);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())
            );
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);
            if (court.getCourtId() == selectedCourtId) {
                tv.setBackgroundResource(R.drawable.bg_court_card_selected);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.primary));
            } else {
                tv.setBackgroundResource(R.drawable.bg_court_card);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green_text));
            }
            tv.setOnClickListener(v -> {
                selectedCourtId = court.getCourtId();
                updateCourtSelectorUI();
                setupTimeGrid();
            });
            layoutCourtSelector.addView(tv);
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
        TreeMap<String, List<Integer>> grouped = new TreeMap<>();
        for (String key : selectedSlotKeys) {
            String[] parts = key.split("\\|");
            String courtName = parts[1];
            String[] t = parts[2].split(":");
            int mins = Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]);
            if (!grouped.containsKey(courtName)) grouped.put(courtName, new ArrayList<>());
            grouped.get(courtName).add(mins);
        }
        StringBuilder sb = new StringBuilder();
        for (String court : grouped.keySet()) {
            List<Integer> times = grouped.get(court);
            Collections.sort(times);
            sb.append(court).append(": ");
            for (int i = 0; i < times.size(); i++) {
                int start = times.get(i);
                sb.append(String.format(Locale.getDefault(), "%02d:%02d-%02d:%02d", start/60, start%60, (start+60)/60, (start+60)%60));
                if (i < times.size() - 1) sb.append(", ");
            }
            if (grouped.size() > 1) sb.append(" | ");
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
            args.putInt("courtId", Integer.parseInt(parts[0])); 
            args.putString("courtName", parts[1]); 
            args.putString("courtTypeName", tvSelectedCourtType.getText().toString());
            args.putString("date", tvSelectedDate.getText().toString());
            args.putStringArrayList("selectedSlots", slotList); 
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_schedule_to_bookingConfirmationFragment, args);
        }
    }

    private void updateTableCellUI(TextView cell, boolean isSelected) {
        if (isSelected) {
            cell.setBackgroundResource(R.drawable.bg_grid_cell);
            cell.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.selected_slot_bg)));
        } else {
            cell.setBackgroundResource(R.drawable.bg_slot_available); 
            cell.setBackgroundTintList(null);
        }
    }

    private void setupTimeGrid() {
        if (!isAdded() || getContext() == null || currentCourtsData.isEmpty()) return;
        timeGrid.removeAllViews();
        CourtData court = null;
        for (CourtData c : currentCourtsData) {
            if (c.getCourtId() == selectedCourtId) {
                court = c;
                break;
            }
        }
        if (court == null) court = currentCourtsData.get(0);

        for (Slot slot : court.getSlots()) {
            TextView tv = new TextView(getContext());
            tv.setText(slot.getStartTime().substring(0, 5));
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 24, 0, 24); 
            tv.setTextSize(14);
            tv.setTypeface(null, Typeface.BOLD);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); 
            params.setMargins(8, 8, 8, 8);
            tv.setLayoutParams(params);
            String slotKey = court.getCourtId() + "|" + court.getCourtName() + "|" + slot.getStartTime() + "|" + slot.getPrice();
            if (isBooked(slot.getStatus())) updateSlotUI(tv, SlotStatus.BOOKED);
            else if ("maintenance".equalsIgnoreCase(slot.getStatus())) updateSlotUI(tv, SlotStatus.MAINTENANCE);
            else if (selectedSlotKeys.contains(slotKey)) updateSlotUI(tv, SlotStatus.SELECTED);
            else updateSlotUI(tv, SlotStatus.AVAILABLE);
            tv.setOnClickListener(v -> {
                if (isBooked(slot.getStatus())) showBookedBottomSheet();
                else if ("maintenance".equalsIgnoreCase(slot.getStatus())) showMaintenanceBottomSheet();
                else toggleSlotSelection(slotKey, tv);
            });
            timeGrid.addView(tv);
        }
    }

    private void updateSlotUI(TextView tv, SlotStatus status) {
        int bgColor = Color.WHITE, textColor = Color.parseColor("#808080");
        if (status == SlotStatus.BOOKED) { bgColor = ContextCompat.getColor(getContext(), R.color.booked_cell_bg); textColor = Color.RED; }
        else if (status == SlotStatus.MAINTENANCE) { bgColor = Color.parseColor("#F5F5F5"); textColor = Color.parseColor("#757575"); }
        else if (status == SlotStatus.SELECTED) { bgColor = ContextCompat.getColor(getContext(), R.color.selected_slot_bg); textColor = ContextCompat.getColor(getContext(), R.color.selected_slot_text); }
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        drawable.setColor(bgColor);
        drawable.setStroke(1, Color.parseColor("#F1F3F4"));
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
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CELL_HEIGHT_DP, getResources().getDisplayMetrics())
        );
        tv.setLayoutParams(params);
        tv.setPadding(0, 0, 0, 0); 
        tv.setTextSize(14); 
        if (isHeader) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private void showDatePickerDialog() {
        if (getContext() == null) return;
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, yearSelected, monthOfYear, dayOfMonth) -> {
            selectedCalendar.set(yearSelected, monthOfYear, dayOfMonth);
            updateDateDisplay();
            selectedSlotKeys.clear();
            if (currentDialog != null) currentDialog.dismiss();
            loadCourtSchedule(selectedDateApi, currentCourtTypeId);
        }, year, month, day);
        dialog.show();
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
            selectedCourtId = -1; 
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
                updateCourtSelectorUI();
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

    private void showMaintenanceBottomSheet() {
        currentDialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_maintenance_bottom_sheet, null);
        view.findViewById(R.id.btn_close_maintenance).setOnClickListener(v -> currentDialog.dismiss());
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
