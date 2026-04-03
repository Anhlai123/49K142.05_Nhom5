package com.example.nhom5.models;

import java.util.List;

public class CourtData {
    private int court_id;
    private String court_code;
    private String court_name;
    private String court_status;
    private List<Slot> slots;

    // Getters and setters
    public int getCourtId() { return court_id; }
    public void setCourtId(int court_id) { this.court_id = court_id; }

    public String getCourtCode() { return court_code; }
    public void setCourtCode(String court_code) { this.court_code = court_code; }

    public String getCourtName() { return court_name; }
    public void setCourtName(String court_name) { this.court_name = court_name; }

    public String getCourtStatus() { return court_status; }
    public void setCourtStatus(String court_status) { this.court_status = court_status; }

    public List<Slot> getSlots() { return slots; }
    public void setSlots(List<Slot> slots) { this.slots = slots; }
}
