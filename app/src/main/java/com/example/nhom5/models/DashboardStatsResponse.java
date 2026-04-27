package com.example.nhom5.models;

import com.google.gson.annotations.SerializedName;

public class DashboardStatsResponse {
    @SerializedName("revenue_today")
    private double revenueToday;

    @SerializedName("revenue_yesterday")
    private double revenueYesterday;

    @SerializedName("revenue_month")
    private double revenueMonth;

    public double getRevenueToday() { return revenueToday; }
    public double getRevenueYesterday() { return revenueYesterday; }
    public double getRevenueMonth() { return revenueMonth; }
}
