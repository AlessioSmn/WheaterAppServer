package it.unipi.lsmsd.DTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.EWEThreshold;

import java.time.LocalDateTime;

public class CityTEMPORARY_NAME_DTO {

    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private Integer followers;
    private LocalDateTime lastUpdate;
    private EWEThreshold eweThresholds;

    // Constructors
    public CityTEMPORARY_NAME_DTO() {
    }

    public CityTEMPORARY_NAME_DTO(City cityModel) {
        this.name = cityModel.getName();
        this.region = cityModel.getRegion();
        this.latitude = cityModel.getLatitude();
        this.longitude = cityModel.getLongitude();
        this.elevation = cityModel.getElevation();
        this.followers = cityModel.getFollowers();
        this.lastUpdate = cityModel.getLastUpdate();
        this.eweThresholds = cityModel.getEweThresholds();
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getElevation() {
        return elevation;
    }

    public Integer getFollowers() {
        return followers;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public EWEThreshold getEweThresholds() {
        return eweThresholds;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setEweThresholds(EWEThreshold eweThresholds) {
        this.eweThresholds = eweThresholds;
    }

    // Json conversion
    public String toJson() throws Exception {
        // Create an ObjectMapper for serialization
        ObjectMapper objectMapper = new ObjectMapper();

        // Register the JavaTimeModule to handle LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // Serialize the object into JSON format
        return objectMapper.writeValueAsString(this);
    }
}