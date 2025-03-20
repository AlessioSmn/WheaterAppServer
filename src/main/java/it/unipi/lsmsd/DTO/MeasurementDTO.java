package it.unipi.lsmsd.DTO;

import java.time.LocalDateTime;

public class MeasurementDTO {
    private String city;
    private Double temperature;
    private Double rainfall;
    private Double snowfall;
    private Double windSpeed;
    private LocalDateTime lastUpdate;

    // Setters and Getters
    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public Double getTemperature() { return temperature; }

    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getRainfall() { return rainfall; }

    public void setRainfall(Double rainfall) { this.rainfall = rainfall; }

    public Double getSnowfall() { return snowfall; }

    public void setSnowfall(Double snowfall) { this.snowfall = snowfall; }

    public Double getWindSpeed() { return windSpeed; }

    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }

    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
}
