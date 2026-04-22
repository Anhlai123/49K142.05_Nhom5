package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PriceTableModel {
    @SerializedName("id")
    private int id;

    @SerializedName("price_table_name")
    private String name;

    @SerializedName("court_type")
    private Integer courtTypeId;

    @SerializedName("effective_date")
    private String startDate;

    @SerializedName("expiry_date")
    private String endDate;

    @SerializedName("is_all_courts")
    private boolean isAllCourts;

    @SerializedName("active_days")
    private List<Integer> activeDays;

    @SerializedName("time_slots")
    private List<PriceTableTimeSlotModel> timeSlots;

    public PriceTableModel() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCourtTypeId() { return courtTypeId; }
    public void setCourtTypeId(Integer courtTypeId) { this.courtTypeId = courtTypeId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isAllCourts() { return isAllCourts; }
    public void setAllCourts(boolean allCourts) { isAllCourts = allCourts; }

    public List<Integer> getActiveDays() { return activeDays; }
    public void setActiveDays(List<Integer> activeDays) { this.activeDays = activeDays; }

    public List<PriceTableTimeSlotModel> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<PriceTableTimeSlotModel> timeSlots) { this.timeSlots = timeSlots; }
}
