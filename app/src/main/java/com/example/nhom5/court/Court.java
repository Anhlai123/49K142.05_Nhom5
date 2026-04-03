package com.example.nhom5.court;

public class Court {
    private String id;
    private String name;
    private String type;
    private String status; // Ready, Maintenance, Inactive

    public Court(String id, String name, String type, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getStatus() { return status; }
}