package com.flashz.mobmech;

/**
 * Created by Flash on 11-01-2017.
 */
public class Mechanic {
    public int id, distance;
    public String name, contact;
    public Double longitude, latitude;

    Mechanic(int id, String name, String contact, Double latitude, Double longitude, int distance) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }
}
