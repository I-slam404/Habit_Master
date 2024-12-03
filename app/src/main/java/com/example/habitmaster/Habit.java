package com.example.habitmaster;

public class Habit {
    private int id;
    private String name;
    private String frequency;
    private String dateTime; // Field for storing date and time

    // Full constructor
    public Habit(int id, String name, String frequency, String dateTime) {
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.dateTime = dateTime;
    }

    // Default constructor
    public Habit() {}

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getFrequency() { return frequency; }
    public String getDateTime() { return dateTime; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    // Override toString for easy printing
    @Override
    public String toString() {
        return "Habit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", frequency='" + frequency + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
