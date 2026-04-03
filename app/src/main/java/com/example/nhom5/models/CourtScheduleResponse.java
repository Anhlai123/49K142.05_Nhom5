package com.example.nhom5.models;

import java.util.List;

public class CourtScheduleResponse {
    private String date;
    private int court_type_id;
    private String court_type_name;
    private List<CourtData> data;

    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getCourtTypeId() { return court_type_id; }
    public void setCourtTypeId(int court_type_id) { this.court_type_id = court_type_id; }

    public String getCourtTypeName() { return court_type_name; }
    public void setCourtTypeName(String court_type_name) { this.court_type_name = court_type_name; }

    public List<CourtData> getData() { return data; }
    public void setData(List<CourtData> data) { this.data = data; }
}
