package com.example.nhom5.schedule;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.nhom5.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScheduleFragment extends Fragment {

    private TableLayout tableLayout;
    private TextView tvScheduleTypeText;
    private View layoutBySchedule, layoutByCourt;
    private GridLayout timeGrid;

    // State management for By Court view
    private enum SlotStatus { AVAILABLE, BOOKED, SELECTED }
    private Set<String> selectedSlots = new HashSet<>();
    private List<String> bookedSlots = new ArrayList<>();
    private boolean isMultiSelection = false; // Single selection logic (Option A)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        tableLayout = view.findViewById(R.id.tableLayout);
        tvScheduleTypeText = view.findViewById(R.id.tvScheduleTypeText);
        layoutBySchedule = view.findViewById(R.id.layoutBySchedule);
        layoutByCourt = view.findViewById(R.id.layoutByCourt);
        timeGrid = view.findViewById(R.id.timeGrid);
        
        view.findViewById(R.id.boxScheduleType).setOnClickListener(this::showFilterPopupMenu);

        // Mock booked slots from "database"
        bookedSlots.add("15:00");
        bookedSlots.add("16:00");
        bookedSlots.add("18:00");

        setupGrid();
        setupTimeGrid();
        return view;
    }

    private void showFilterPopupMenu(View view) {
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
            
            updateSlotUI(tv, getSlotStatus(slot), false);
            
            tv.setOnClickListener(v -> handleSlotClick(tv));

            timeGrid.addView(tv);
        }
    }

    private SlotStatus getSlotStatus(String slot) {
        if (bookedSlots.contains(slot)) return SlotStatus.BOOKED;
        if (selectedSlots.contains(slot)) return SlotStatus.SELECTED;
        return SlotStatus.AVAILABLE;
    }

    private void handleSlotClick(TextView tv) {
        String slot = (String) tv.getTag();
        SlotStatus currentStatus = getSlotStatus(slot);

        if (currentStatus == SlotStatus.BOOKED) return;

        if (currentStatus == SlotStatus.SELECTED) {
            selectedSlots.remove(slot);
            updateSlotUI(tv, SlotStatus.AVAILABLE, true);
        } else {
            // Option A: Single selection logic
            for (int i = 0; i < timeGrid.getChildCount(); i++) {
                TextView otherTv = (TextView) timeGrid.getChildAt(i);
                String otherSlot = (String) otherTv.getTag();
                if (selectedSlots.contains(otherSlot)) {
                    selectedSlots.remove(otherSlot);
                    updateSlotUI(otherTv, SlotStatus.AVAILABLE, true);
                }
            }
            
            selectedSlots.add(slot);
            updateSlotUI(tv, SlotStatus.SELECTED, true);
            showBookingBottomSheet("Sân Cầu Lông 2", tv, slot);
        }
    }

    private void updateSlotUI(TextView tv, SlotStatus status, boolean animate) {
        int bgColor, textColor;
        
        switch (status) {
            case BOOKED:
                bgColor = Color.parseColor("#FFEBEE");
                textColor = Color.parseColor("#FF0000");
                tv.setEnabled(false);
                break;
            case SELECTED:
                bgColor = Color.parseColor("#FFF9C4");
                textColor = Color.parseColor("#FBC02D");
                tv.setEnabled(true);
                break;
            case AVAILABLE:
            default:
                bgColor = Color.WHITE;
                textColor = Color.parseColor("#808080");
                tv.setEnabled(true);
                break;
        }

        if (animate) {
            animateColorChange(tv, bgColor, textColor);
        } else {
            applySlotStyles(tv, bgColor, textColor);
        }
    }

    private void applySlotStyles(TextView tv, int bgColor, int textColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        drawable.setColor(bgColor);
        drawable.setStroke((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), 
                Color.parseColor("#F1F3F4"));
        
        tv.setBackground(drawable);
        tv.setTextColor(textColor);
    }

    private void animateColorChange(TextView tv, int targetBgColor, int targetTextColor) {
        int startBgColor = Color.WHITE;
        if (tv.getBackground() instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) tv.getBackground();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                startBgColor = gd.getColor() != null ? gd.getColor().getDefaultColor() : Color.WHITE;
            }
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startBgColor, targetBgColor);
        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
            drawable.setColor(color);
            drawable.setStroke((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), 
                    Color.parseColor("#F1F3F4"));
            tv.setBackground(drawable);
        });
        
        ValueAnimator textColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), tv.getCurrentTextColor(), targetTextColor);
        textColorAnimation.setDuration(300);
        textColorAnimation.addUpdateListener(animator -> tv.setTextColor((int) animator.getAnimatedValue()));

        colorAnimation.start();
        textColorAnimation.start();
    }

    private void showBookingBottomSheet(String courtName, TextView selectedTv, String slot) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.CustomBottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_confirm_booking, null);
        
        TextView tvTitle = bottomSheetView.findViewById(R.id.tvCourtTitle);
        tvTitle.setText(courtName);
        
        bottomSheetView.findViewById(R.id.btnConfirmBooking).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Navigation.findNavController(getView()).navigate(R.id.action_navigation_schedule_to_bookingConfirmationFragment);
        });

        bottomSheetDialog.setOnDismissListener(dialog -> {
            if (selectedSlots.contains(slot)) {
                selectedSlots.remove(slot);
                updateSlotUI(selectedTv, SlotStatus.AVAILABLE, true);
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void setupGrid() {
        String[] times = {
                "THỨ", "SÂN", 
                "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", 
                "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30",
                "19:00", "19:30", "20:00", "20:30", "21:00", "21:30"
        };
        
        TableRow headerRow = new TableRow(getContext());
        for (int i = 0; i < times.length; i++) {
            TextView tv = createCell(times[i], true);
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
        tableLayout.addView(headerRow);

        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (String day : days) {
            addDaySchedule(day, 4);
        }
    }

    private void addDaySchedule(String day, int numCourts) {
        int numTimeSlots = 30;
        for (int i = 0; i < numCourts; i++) {
            TableRow row = new TableRow(getContext());
            TextView dayTv = createCell(i == 1 ? day : "", false);
            if (i == 0) dayTv.setBackgroundResource(R.drawable.bg_day_top);
            else if (i == numCourts - 1) dayTv.setBackgroundResource(R.drawable.bg_day_bottom);
            else dayTv.setBackgroundResource(R.drawable.bg_day_middle);
            row.addView(dayTv);

            TextView courtTv = createCell("Sân " + (i + 1), false);
            courtTv.setBackgroundResource(R.drawable.bg_grid_cell);
            courtTv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_grey));
            row.addView(courtTv);

            for (int j = 0; j < numTimeSlots; j++) {
                View cell = new View(getContext());
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()),
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
                );
                cell.setLayoutParams(params);
                cell.setBackgroundResource(R.drawable.bg_grid_cell);
                
                // Random mock data
                if (Math.random() < 0.1) {
                    cell.setBackgroundResource(R.drawable.bg_booked_status);
                } else if (Math.random() < 0.05) {
                    cell.setBackgroundResource(R.drawable.bg_maintenance_status);
                }
                
                row.addView(cell);
            }
            tableLayout.addView(row);
        }
    }

    private TextView createCell(String text, boolean isHeader) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        int width = isHeader ? 60 : 60;
        if (text.equals("THỨ") || text.equals("SÂN") || text.startsWith("Thứ") || text.startsWith("Sân")) {
            width = 80;
        }
        
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics())
        );
        tv.setLayoutParams(params);
        tv.setTextSize(11);
        if (isHeader) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }
}
