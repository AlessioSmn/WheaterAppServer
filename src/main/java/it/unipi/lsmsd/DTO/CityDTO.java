package it.unipi.lsmsd.DTO;

import it.unipi.lsmsd.model.EWEThreshold;

import java.time.LocalDateTime;

// DTO for requesting Weather Info from Open-Meteo API
public class CityDTO {
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private String start;
    private String end;
    private Integer pastHours;
    private Integer forecastHours;
    private Double elevation;
    private EWEThreshold eweThresholds;
    private LocalDateTime lastUpdate;

    // Setters and Getters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getStart() { return start; }
    public void setStart(String startDate) { this.start = startDate; }
    
    public void setEnd(String endDate) { this.end = endDate; }
    public String getEnd() { return end; }

    public Integer getPastHours() { return pastHours; }

    public void setPastHours(Integer pastHours) { this.pastHours = pastHours; }

    public Integer getForecastHours() { return forecastHours; }

    public void setForecastHours(Integer forecastHours) { this.forecastHours = forecastHours; }

    public Double getElevation() { return elevation; }
    public void setElevation(Double elevation) { this.elevation = elevation; }

    public EWEThreshold getEweThresholds() { return eweThresholds; }
    public void setEweThresholds(EWEThreshold eweThresholds) { this.eweThresholds = eweThresholds;}

    /**
     * Checks if the DTO has the necessary fields to construct the id (Name, region, latitude and longitude)
     * @return true if it has them, false otherwise
     */
    public boolean hasIdFields(){
        return getName() != null && !getName().isEmpty() &&
                getRegion() != null && !getRegion().isEmpty() &&
                getLatitude() != null &&
                getLongitude() != null;
    }
    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
