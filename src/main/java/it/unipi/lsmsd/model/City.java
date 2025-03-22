package it.unipi.lsmsd.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cities")
public class City {
<<<<<<< HEAD
    @Id
    private String id; // MongoDB Id which is a custom id 
=======
>>>>>>> 0fec31d (add favorite-cities functionalities)
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private Integer followers;
    private LocalDateTime lastUpdate;
    private EWEThreshold eweThresholds;

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

}
