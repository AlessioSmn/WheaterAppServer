package it.unipi.lsmsd.model;

import java.time.LocalDateTime;
import java.lang.Math;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cities")
public class City {
    @Id
    private String id; // MongoDB Id which is a custom id 
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private Integer followers;
    private LocalDateTime lastUpdate;
    private EWEThreshold eweThresholds;
    private LocalDateTime lastEweUpdate;

    // Constructors   
    public City(){}
    public City(String id, String name, String region, Double latitude, Double longitude, Double elevation,
            Integer followers, LocalDateTime lastUpdate) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.followers = followers;
        this.lastUpdate = lastUpdate;
    }
    public City(String id, String name, String region, Double latitude, Double longitude, Double elevation,
                Integer followers, LocalDateTime lastUpdate, EWEThreshold eweThresholds) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.followers = followers;
        this.lastUpdate = lastUpdate;
        this.eweThresholds = eweThresholds;
    }

    // Method that calculates the distance between 2 cities
    public static Double distance(City a, City b) {
        // Mean Earth radius in km
        final double R = 6371.0;

        // Latitude and Longitude in radians
        double latA = Math.toRadians(a.getLatitude());
        double lonA = Math.toRadians(a.getLongitude());
        double latB = Math.toRadians(b.getLatitude());
        double lonB = Math.toRadians(b.getLongitude());

        // Difference between latitudes e longitudes
        double dLat = latB - latA;
        double dLon = lonB - lonA;

        // Haversine's formula
        double a1 = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(latA) * Math.cos(latB) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a1), Math.sqrt(1 - a1));

        // Distance in km
        double distance = R * c;

        return (Double) distance;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getRegion() { return region; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Double getElevation() { return elevation; }
    public Integer getFollowers() { return followers; }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public EWEThreshold getEweThresholds() { return eweThresholds; }
    public LocalDateTime getLastEweUpdate() { return lastEweUpdate; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRegion(String country) { this.region = country; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setElevation(Double elevation) { this.elevation = elevation; }
    public void setFollowers(Integer followers) { this.followers = followers; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    public void setEweThresholds(EWEThreshold eweThreshold) { this.eweThresholds = eweThreshold; }
    public void setLastEweUpdate(LocalDateTime lastEweUpdate) { this.lastEweUpdate = lastEweUpdate; }

}
