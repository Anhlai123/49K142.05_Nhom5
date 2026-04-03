package com.example.nhom5.models;

public class BookingRequest {
    private int court_id;
    private String customer_name;
    private String phone;
    private String date;
    private String start_time;
    private String end_time;
    private String notes;

    public BookingRequest(int court_id, String customer_name, String phone,
                         String date, String start_time, String end_time, String notes) {
        this.court_id = court_id;
        this.customer_name = customer_name;
        this.phone = phone;
        this.date = date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.notes = notes;
    }

    public int getCourt_id() { return court_id; }
    public void setCourt_id(int court_id) { this.court_id = court_id; }

    public String getCustomer_name() { return customer_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStart_time() { return start_time; }
    public void setStart_time(String start_time) { this.start_time = start_time; }

    public String getEnd_time() { return end_time; }
    public void setEnd_time(String end_time) { this.end_time = end_time; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
