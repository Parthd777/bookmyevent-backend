package com.bookmyevent.model;

public class Venue extends BaseEntity {
    private String name;
    private String city;
    private int capacity;

    public Venue() {
    }

    public Venue(long id, String name, String city, int capacity) {
        super(id);
        this.name = name;
        this.city = city;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Venue{id=" + getId() + ", name='" + name + '\'' + ", city='" + city + '\'' + ", capacity=" + capacity + '}';
    }
}
