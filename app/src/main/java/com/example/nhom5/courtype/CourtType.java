package com.example.nhom5.courtype;

public class CourtType {
    private String id;
    private String name;
    private String status;
    private Integer duration;

    public CourtType(String id, String name, String status, Integer duration) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public Integer getDuration() { return duration; }
}