package it.unipi.lsmsd.DTO;

import it.unipi.lsmsd.model.EWEThreshold;

// DTO for requesting Weather Info from Open-Meteo API
public class CityDTO {
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    //NOTE: The Dates should be in format: 2025-03-15
    private String startDate;
    private String endDate;
    private Double elevation;
    private EWEThreshold eweThresholds;

    // Setters and Getters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getEndDate() { return endDate; }

    public Double getElevation() { return elevation; }
    public void setElevation(Double elevation) { this.elevation = elevation; }

    public EWEThreshold getEweThresholds() { return eweThresholds; }
    public void setEweThresholds(EWEThreshold eweThresholds) { this.eweThresholds = eweThresholds;}

}
