package com.example.nhom5.models;

public class BookingResponse {
    private int id;
    private int court;
    private String court_name;
    private String court_code;
    private String customer_name;
    private String phone;
    private String date;
    private String start_time;
    private String end_time;
    private String total_price;
    private String status;
    private String notes;
    private String created_at;
    private String updated_at;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourt() { return court; }
    public void setCourt(int court) { this.court = court; }

    public String getCourt_name() { return court_name; }
    public void setCourt_name(String court_name) { this.court_name = court_name; }

    public String getCourt_code() { return court_code; }
    public void setCourt_code(String court_code) { this.court_code = court_code; }

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

    public String getTotal_price() { return total_price; }
    public void setTotal_price(String total_price) { this.total_price = total_price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}
