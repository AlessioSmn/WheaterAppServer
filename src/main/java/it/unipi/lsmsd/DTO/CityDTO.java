package it.unipi.lsmsd.DTO;

import java.time.LocalDateTime;

public class CityDTO {
    private String id;
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private Integer elevation;
    private Integer followers;
    private LocalDateTime lastUpdate;

    // Setters and Getters
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }

    public void setRegion(String region) { this.region = region; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getElevation() { return elevation; }

    public void setElevation(Integer elevation) { this.elevation = elevation; }

    public Integer getFollowers() { return followers; }

    public void setFollowers(Integer followers) { this.followers = followers; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }

    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
