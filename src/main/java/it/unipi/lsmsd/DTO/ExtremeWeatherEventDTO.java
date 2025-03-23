package it.unipi.lsmsd.DTO;

import it.unipi.lsmsd.model.ExtremeWeatherEventCategory;
import java.time.LocalDateTime;

public class ExtremeWeatherEventDTO {

    // Attributes
    private Double longitude;
    private Double latitude;
    private ExtremeWeatherEventCategory category;
    private Integer strength;
    private Integer radius;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;

    // Constructors
    public ExtremeWeatherEventDTO(Double longitude, Double latitude, ExtremeWeatherEventCategory category, Integer strength, Integer radius, LocalDateTime dateStart, LocalDateTime dateEnd) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
        this.strength = strength;
        this.radius = radius;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    // getters and setters
    public Double getLongitude() { return longitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getLatitude() { return latitude; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public ExtremeWeatherEventCategory getCategory() { return category; }

    public void setCategory(ExtremeWeatherEventCategory category) { this.category = category; }

    public Integer getStrength() { return strength; }

    public void setStrength(Integer strength) { this.strength = strength; }

    public Integer getRadius() { return radius; }

    public void setRadius(Integer radius) { this.radius = radius; }

    public LocalDateTime getDateStart() { return dateStart; }

    public void setDateStart(LocalDateTime dateStart) { this.dateStart = dateStart; }

    public LocalDateTime getDateEnd() { return dateEnd; }

    public void setDateEnd(LocalDateTime dateEnd) { this.dateEnd = dateEnd; }
}
