package it.unipi.lsmsd.model;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

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

    // Custom cityId generation logic
    // Pisa,Tuscany, (43.690685, 10.452489) --> pis-tus-43.6907-10.4525 
    // 23 characters long
    // Static function for reusuabilty
    public static String generateCityId(String name, String region, Double latitude, Double longitude) {
        DecimalFormat df = new DecimalFormat("#.####");  // Format coordinates to 4 decimal places
        // Generate code based on each inputs
        String latCode = df.format(latitude);
        String lonCode = df.format(longitude);
        String nameCode = name.substring(0, 3);
        String regionCode = (region.length() >= 3) ? region.substring(0, 3):region;
        return (nameCode + "-" + regionCode + "-" + latCode + "-" + lonCode).toLowerCase();
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
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setRegion(String country) { this.region = country; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setElevation(Double elevation) { this.elevation = elevation; }
    public void setFollowers(Integer followers) { this.followers = followers; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    
}
