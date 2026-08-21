package com.bookmyevent.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue extends BaseEntity {
    private String name;
    private String city;
    private int capacity;

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

    public Venue() {}
    public Venue(String name, String city, int capacity) {
        this.name = name;
        this.city = city;
        this.capacity = capacity;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<Event> getEvents() { return events; }
}