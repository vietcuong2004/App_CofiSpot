package com.example.myapplication.Model;

public class DailyTask {
    private String id;
    private String name;
    private int points;
    private boolean completed;
    private String lastUpdated;

    public DailyTask(String id, String name, int points, boolean completed, String lastUpdated) {
        this.id = id;
        this.name = name;
        this.points = points;
        this.completed = completed;
        this.lastUpdated = lastUpdated;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPoints() { return points; }
    public boolean isCompleted() { return completed; }
    public String getLastUpdated() { return lastUpdated; }
}