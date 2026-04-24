package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PriceTableModel {
    @SerializedName("id")
    private int id;

    @SerializedName(value = "price_table_code", alternate = {"code"})
    private String priceTableCode;

    @SerializedName("price_table_name")
    private String name;

    @SerializedName("court_type")
    private Integer courtTypeId;

    @SerializedName("court_type_name")
    private String courtTypeName;

    @SerializedName("effective_date")
    private String startDate;

    @SerializedName(value = "expiry_date", alternate = {"end_date"})
    private String endDate;

    @SerializedName("apply_scope")
    private String applyScope;

    @SerializedName("is_all_courts")
    private boolean isAllCourts;

    @SerializedName("court_ids")
    private List<Integer> courtIds;

    @SerializedName("applied_courts")
    private List<AppliedCourt> appliedCourts; // Đổi sang List<AppliedCourt> thay vì List<String>

    @SerializedName(value = "applied_days", alternate = {"active_days"})
    private List<String> appliedDays;

    @SerializedName("time_slots")
    private List<PriceTableTimeSlotModel> timeSlots;

    public static class AppliedCourt {
        @SerializedName("court_name")
        public String courtName;
        @SerializedName("court_code")
        public String courtCode;
    }

    public PriceTableModel() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPriceTableCode() { return priceTableCode; }
    public void setPriceTableCode(String priceTableCode) { this.priceTableCode = priceTableCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCourtTypeId() { return courtTypeId; }
    public void setCourtTypeId(Integer courtTypeId) { this.courtTypeId = courtTypeId; }

    public String getCourtTypeName() { return courtTypeName; }
    public void setCourtTypeName(String courtTypeName) { this.courtTypeName = courtTypeName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getApplyScope() { return applyScope; }
    public void setApplyScope(String applyScope) { this.applyScope = applyScope; }

    public boolean isAllCourts() { return isAllCourts; }
    public void setAllCourts(boolean allCourts) { isAllCourts = allCourts; }

    public List<Integer> getCourtIds() { return courtIds; }
    public void setCourtIds(List<Integer> courtIds) { this.courtIds = courtIds; }

    public List<AppliedCourt> getAppliedCourts() { return appliedCourts; }
    public void setAppliedCourts(List<AppliedCourt> appliedCourts) { this.appliedCourts = appliedCourts; }

    public List<String> getAppliedDays() { return appliedDays; }
    public void setAppliedDays(List<String> appliedDays) { this.appliedDays = appliedDays; }

    public List<PriceTableTimeSlotModel> getTimeSlots() { return timeSlots; }
    public void setTimeSlots(List<PriceTableTimeSlotModel> timeSlots) { this.timeSlots = timeSlots; }
}
