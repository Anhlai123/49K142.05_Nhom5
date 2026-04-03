package com.example.nhom5.models;

public class Slot {
    private String start_time;
    private String end_time;
    private String price;
    private String status; // "available", "booked", "maintenance"

    // Getters and setters
    public String getStartTime() { return start_time; }
    public void setStartTime(String start_time) { this.start_time = start_time; }

    public String getEndTime() { return end_time; }
    public void setEndTime(String end_time) { this.end_time = end_time; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
