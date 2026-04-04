package com.example.nhom5.price;

import java.util.List;

public class PriceRecord {
    private String id;
    private String title;
    private String courtType;
    private String dateRange;
    private String priceRange;
    private List<String> activeDays;
    private String timeFrameCount;

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
}