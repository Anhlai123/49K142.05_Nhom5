package com.example.nhom5.price;

import com.example.nhom5.models.PriceTableModel;
import com.example.nhom5.models.PriceTableTimeSlotModel;

import java.util.List;

public class PriceRecord {
    private String id;
    private int internalId;
    private String title;
    private String courtType;
    private String dateRange;
    private String priceRange;
    private List<String> activeDays;
    private String timeFrameCount;
    private String applyScope;
    private PriceTableModel fullModel;
    private List<PriceTableTimeSlotModel> detailedTimeSlots;

    public PriceRecord(String id, String title, String courtType, String dateRange, String priceRange, List<String> activeDays, String timeFrameCount) {
        this.id = id;
        this.title = title;
        this.courtType = courtType;
        this.dateRange = dateRange;
        this.priceRange = priceRange;
        this.activeDays = activeDays;
        this.timeFrameCount = timeFrameCount;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCourtType() { return courtType; }
    public String getDateRange() { return dateRange; }
    public String getPriceRange() { return priceRange; }
    public List<String> getActiveDays() { return activeDays; }
    public String getTimeFrameCount() { return timeFrameCount; }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public void setTimeFrameCount(String timeFrameCount) {
        this.timeFrameCount = timeFrameCount;
    }

    public String getApplyScope() {
        return applyScope;
    }

    public void setApplyScope(String applyScope) {
        this.applyScope = applyScope;
    }

    public PriceTableModel getFullModel() {
        return fullModel;
    }

    public void setFullModel(PriceTableModel fullModel) {
        this.fullModel = fullModel;
    }

    public List<PriceTableTimeSlotModel> getDetailedTimeSlots() {
        return detailedTimeSlots;
    }

    public void setDetailedTimeSlots(List<PriceTableTimeSlotModel> detailedTimeSlots) {
        this.detailedTimeSlots = detailedTimeSlots;
    }
}