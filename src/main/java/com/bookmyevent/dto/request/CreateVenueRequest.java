package com.bookmyevent.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateVenueRequest {

    @NotBlank(message = "venue name must not be blank")
    private String name;

    @NotBlank(message = "city must not be blank")
    private String city;

    @Min(value = 1, message = "capacity must be at least 1")
    private int capacity;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
