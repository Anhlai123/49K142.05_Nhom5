package com.example.nhom5.schedule;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nhom5.R;
import com.example.nhom5.api.ApiClient;
import com.example.nhom5.api.ApiService;
import com.example.nhom5.models.BookingRequest;
import com.example.nhom5.models.BookingResponse;
import com.example.nhom5.models.CourtData;
import com.example.nhom5.models.CourtScheduleResponse;
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
    private int selectedCourtId = 2; // Default to Sân 2 as in XML
    private int currentCourtTypeId = 1; // 1: Badminton, 2: Soccer

    // State management for By Court view
    private enum SlotStatus { AVAILABLE, BOOKED, SELECTED, MAINTENANCE }
    private Set<String> selectedSlots = new HashSet<>();

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

        // Initialize date to today
        selectedCalendar = Calendar.getInstance();
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        selectedDateApi = apiFormat.format(selectedCalendar.getTime());
        tvSelectedDate.setText(displayFormat.format(selectedCalendar.getTime()));

        // Load data from API
        // Moving this to onViewCreated for better layout stability
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCourtSchedule(selectedDateApi, currentCourtTypeId);
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
                Log.e("API_ERROR", t.getMessage());
                setupMockGrid();
            }
        });
    }

    private void displayTableSchedule(CourtScheduleResponse schedule) {
        if (!isAdded() || getContext() == null) return;
        tableLayout.removeAllViews();
        
        // 1. Header Row
        TableRow headerRow = new TableRow(getContext());
        
        TextView thuHeader = createCell("THỨ", true, 80);
        if (thuHeader != null) {
            thuHeader.setBackgroundResource(R.drawable.bg_grid_cell);
            thuHeader.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.primary)));
            thuHeader.setTextColor(Color.WHITE);
            headerRow.addView(thuHeader);
        }

        TextView sanHeader = createCell("SÂN", true, 80);
        if (sanHeader != null) {
            sanHeader.setBackgroundResource(R.drawable.bg_grid_cell);
            sanHeader.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green_text));
            headerRow.addView(sanHeader);
        }
        
        if (!schedule.getData().isEmpty()) {
            for (Slot slot : schedule.getData().get(0).getSlots()) {
                TextView timeCell = createCell(slot.getStartTime().substring(0, 5), true, 60);
                if (timeCell != null) {
                    timeCell.setBackgroundResource(R.drawable.bg_grid_cell);
                    timeCell.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F1F8F6")));
                    timeCell.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green_text));
                    headerRow.addView(timeCell);
                }
            }
        }
        tableLayout.addView(headerRow);

        // 2. Data Rows
        List<CourtData> courts = schedule.getData();
        int numCourts = courts.size();
        for (int i = 0; i < numCourts; i++) {
            CourtData court = courts.get(i);
            TableRow row = new TableRow(getContext());
            
            // Date cell
            TextView dayTv = createCell(i == numCourts / 2 ? "Thứ 2" : "", false, 80);
            if (dayTv != null) {
                if (i == 0) dayTv.setBackgroundResource(R.drawable.bg_day_top);
                else if (i == numCourts - 1) dayTv.setBackgroundResource(R.drawable.bg_day_bottom);
                else dayTv.setBackgroundResource(R.drawable.bg_day_middle);
                row.addView(dayTv);
            }

            // Court name cell
            TextView courtTv = createCell(court.getCourtName(), false, 80);
            if (courtTv != null) {
                courtTv.setBackgroundResource(R.drawable.bg_grid_cell);
                courtTv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_grey));
                row.addView(courtTv);
            }

            List<Slot> slots = court.getSlots();
            for (int k = 0; k < slots.size(); k++) {
                Slot slot = slots.get(k);
                String status = slot.getStatus();
                
                if ("booked".equalsIgnoreCase(status) || "maintenance".equalsIgnoreCase(status)) {
                    // Start spanning
                    int span = 1;
                    int next = k + 1;
                    while (next < slots.size() && status.equalsIgnoreCase(slots.get(next).getStatus())) {
                        span++;
                        next++;
                    }
                    
                    TextView spanCell = createCell(status.equalsIgnoreCase("booked") ? "Đã đặt" : "Bảo trì", false, 60 * span);
                    if (spanCell != null) {
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
                    }
                    
                    k = next - 1; // Skip the spanned slots
                } else {
                    // Normal available slot
                    View cell = new View(getContext());
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
                    );
                    cell.setLayoutParams(params);
                    cell.setBackgroundResource(R.drawable.bg_grid_cell);
                    cell.setOnClickListener(v -> showBookingConfirmation(court.getCourtName(), court.getCourtId(), schedule.getDate(), slot.getStartTime()));
                    row.addView(cell);
                }
            }
            tableLayout.addView(row);
        }
    }

    private void setupMockGrid() {
        if (tableLayout == null || getContext() == null) return;
        tableLayout.removeAllViews();
        String[] times = {
                "THỨ", "SÂN", 
                "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", 
                "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30",
                "19:00", "19:30", "20:00", "20:30", "21:00", "21:30"
        };
        
        TableRow headerRow = new TableRow(getContext());
        for (int i = 0; i < times.length; i++) {
            TextView tv = createCell(times[i], true, (i == 0 || i == 1) ? 80 : 60);
            if (tv != null) {
                if (i == 0) {
                    tv.setBackgroundResource(R.drawable.bg_grid_cell);
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.primary)));
                    tv.setTextColor(Color.WHITE);
                } else if (i == 1) {
                    tv.setBackgroundResource(R.drawable.bg_grid_cell);
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green_text));
                } else {
                    tv.setBackgroundResource(R.drawable.bg_grid_cell);
                    tv.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F1F8F6")));
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_green_text));
                }
                headerRow.addView(tv);
            }
        }
        tableLayout.addView(headerRow);

        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (String day : days) {
            addDayScheduleMock(day, 4);
        }
        setupTimeGrid();
    }

    private void addDayScheduleMock(String day, int numCourts) {
        if (!isAdded() || getContext() == null) return;
        int numTimeSlots = 30;
        for (int i = 0; i < numCourts; i++) {
            TableRow row = new TableRow(getContext());
            TextView dayTv = createCell(i == 1 ? day : "", false, 80);
            if (dayTv != null) {
                if (i == 0) dayTv.setBackgroundResource(R.drawable.bg_day_top);
                else if (i == numCourts - 1) dayTv.setBackgroundResource(R.drawable.bg_day_bottom);
                else dayTv.setBackgroundResource(R.drawable.bg_day_middle);
                row.addView(dayTv);
            }

            TextView courtTv = createCell("Sân " + (i + 1), false, 80);
            if (courtTv != null) {
                courtTv.setBackgroundResource(R.drawable.bg_grid_cell);
                courtTv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_grey));
                row.addView(courtTv);
            }

            for (int j = 0; j < numTimeSlots; j++) {
                final int courtIndex = i;
                final int timeIndex = j;
                
                // Randomly decide if this slot starts a booked or maintenance block
                double rand = Math.random();
                if ((rand < 0.1 || rand > 0.93) && j < numTimeSlots - 1) { // Don't start at the very last slot
                    String status = rand < 0.1 ? "booked" : "maintenance";
                    int span = (int) (Math.random() * 2) + 2; // span 2-3 cells
                    if (j + span > numTimeSlots) span = numTimeSlots - j;
                    if (span < 2) span = 2; // Force minimum 1 hour
                    
                    TextView spanCell = createCell(status.equalsIgnoreCase("booked") ? "Đã đặt" : "Bảo trì", false, 60 * span);
                    if (spanCell != null) {
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
                    }
                    j += (span - 1);
                } else {
                    View cell = new View(getContext());
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
                    );
                    cell.setLayoutParams(params);
                    cell.setBackgroundResource(R.drawable.bg_grid_cell);
                    cell.setOnClickListener(v -> showBookingConfirmation("Sân Cầu Lông " + (courtIndex + 1), courtIndex + 1, "13/01/2026", getTimeForSlot(timeIndex)));
                    row.addView(cell);
                }
            }
            tableLayout.addView(row);
        }
    }

    private void showDatePickerDialog() {
        if (getContext() == null) return;
        
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int day = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            String displayStr = displayFormat.format(selectedCalendar.getTime());
            selectedDateApi = apiFormat.format(selectedCalendar.getTime());
            
            tvSelectedDate.setText(displayStr);
            
            // Reload data
            loadCourtSchedule(selectedDateApi, currentCourtTypeId);
            
        }, year, month, day);
        
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
            setupTimeGrid(); // Refresh grid for the selected court
        };

        court1.setOnClickListener(listener);
        court2.setOnClickListener(listener);
        court3.setOnClickListener(listener);
        court4.setOnClickListener(listener);
    }

    private void updateCourtSelectionUI() {
        if (!isAdded()) return;
        
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
        if (getContext() == null) return;
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenu().add(0, 1, 0, "Sân Cầu Lông");
        popupMenu.getMenu().add(0, 2, 1, "Sân Bóng Đá");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            tvSelectedCourtType.setText(title);
            currentCourtTypeId = item.getItemId();
            
            // Reload data for the selected court type
            loadCourtSchedule(selectedDateApi, currentCourtTypeId);
            return true;
        });
        popupMenu.show();
    }

    private void showFilterPopupMenu(View view) {
        if (getContext() == null) return;
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_schedule_filter, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            tvScheduleTypeText.setText(title);
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
            
            updateSlotUI(tv, getSlotStatusMock(slot), false);
            tv.setOnClickListener(v -> handleSlotClick(tv));
            timeGrid.addView(tv);
        }
    }

    private SlotStatus getSlotStatusMock(String slot) {
        if (slot.equals("15:00") || slot.equals("16:00")) return SlotStatus.BOOKED;
        return SlotStatus.AVAILABLE;
    }

    private void handleSlotClick(TextView tv) {
        String slot = (String) tv.getTag();
        SlotStatus currentStatus = getSlotStatusMock(slot);

        if (currentStatus == SlotStatus.BOOKED) {
            showBookedBottomSheet();
            return;
        }

        if (currentStatus == SlotStatus.SELECTED) {
            selectedSlots.remove(slot);
            updateSlotUI(tv, SlotStatus.AVAILABLE, true);
        } else {
            selectedSlots.clear();
            for (int i = 0; i < timeGrid.getChildCount(); i++) {
                TextView child = (TextView) timeGrid.getChildAt(i);
                updateSlotUI(child, getSlotStatusMock((String)child.getTag()), false);
            }
            selectedSlots.add(slot);
            updateSlotUI(tv, SlotStatus.SELECTED, true);
            showBookingConfirmation("Sân Cầu Lông 2", 2, "13/01/2026", slot);
        }
    }

    private void showBookedBottomSheet() {
        if (getContext() == null) return;
        currentDialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_booked_bottom_sheet, null);
        view.findViewById(R.id.btn_close_booked).setOnClickListener(v -> currentDialog.dismiss());
        currentDialog.setContentView(view);
        currentDialog.show();
    }

    private void updateSlotUI(TextView tv, SlotStatus status, boolean animate) {
        if (!isAdded()) return;
        int bgColor, textColor;
        switch (status) {
            case BOOKED:
                bgColor = ContextCompat.getColor(getContext(), R.color.booked_cell_bg);
                textColor = Color.parseColor("#FF5252"); // Red
                break;
            case SELECTED:
                bgColor = ContextCompat.getColor(getContext(), R.color.selected_slot_bg);
                textColor = ContextCompat.getColor(getContext(), R.color.selected_slot_text); // Yellow
                break;
            default:
                bgColor = Color.WHITE;
                textColor = Color.parseColor("#808080");
                break;
        }
        applySlotStyles(tv, bgColor, textColor);
    }

    private void applySlotStyles(TextView tv, int bgColor, int textColor) {
        if (!isAdded()) return;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        drawable.setColor(bgColor);
        drawable.setStroke((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), Color.parseColor("#F1F3F4"));
        tv.setBackground(drawable);
        tv.setTextColor(textColor);
    }

    private void showBookingConfirmation(String court, int courtId, String day, String time) {
        if (getContext() == null) return;
        currentDialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_confirm_booking, null);
        
        TextView tvTitle = bottomSheetView.findViewById(R.id.tvCourtTitle);
        tvTitle.setText(court + " - " + day + " " + time);
        
        bottomSheetView.findViewById(R.id.btnConfirmBooking).setOnClickListener(v -> {
            currentDialog.dismiss();
            if (isAdded()) {
                Bundle args = new Bundle();
                args.putInt("courtId", courtId);
                args.putString("courtName", court);
                args.putString("date", day);
                args.putString("startTime", time);
                NavHostFragment.findNavController(this).navigate(R.id.action_navigation_schedule_to_bookingConfirmationFragment, args);
            }
        });

        currentDialog.setContentView(bottomSheetView);
        currentDialog.show();
    }

    private TextView createCell(String text, boolean isHeader, int widthDp) {
        if (getContext() == null) return null;
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
        );
        tv.setLayoutParams(params);
        tv.setTextSize(11);
        if (isHeader) {
            tv.setTypeface(null, Typeface.BOLD);
        }
        return tv;
    }

    private String getTimeForSlot(int j) {
        String[] times = {
                "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", 
                "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30",
                "19:00", "19:30", "20:00", "20:30", "21:00", "21:30"
        };
        return times[j];
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
        currentDialog = null;
        tableLayout = null;
        tvScheduleTypeText = null;
        layoutBySchedule = null;
        layoutByCourt = null;
        timeGrid = null;
    }
}
