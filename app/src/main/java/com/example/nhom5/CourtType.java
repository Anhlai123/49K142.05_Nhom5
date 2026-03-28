package com.example.nhom5;

public class CourtType {
    private String id;
    private String name;
    private String status;

    public CourtType(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}