package it.unipi.lsmsd.DTO;

import it.unipi.lsmsd.model.EWEThreshold;

// DTO for requesting Weather Info from Open-Meteo API
public class CityDTO {
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private String startDate; 
    private String endDate;
    private Integer pastDays = 0; // Default Open-Meteo provides 0 past day forecast
    private Integer forecastDays = 7; // Default Open-Meteo provides 7 day forecast
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

    public Integer getPastDays() { return pastDays; }

    public void setPastDays(Integer pastHours) { this.pastDays = pastHours; }

    public Integer getForecastDays() { return forecastDays; }

    public void setForecastDays(Integer forecastHours) { this.forecastDays = forecastHours; }

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
}
