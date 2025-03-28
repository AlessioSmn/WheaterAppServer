package it.unipi.lsmsd.model;

import it.unipi.lsmsd.DTO.ExtremeWeatherEventDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ExtremeWeatherEvents")
public class ExtremeWeatherEvent {

    // Attributes
    @Id
    private String id;
    private Double longitude;
    private Double latitude;
    private ExtremeWeatherEventCategory category;
    private Integer strength;
    private Integer radius;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;


    // Constructors
    public ExtremeWeatherEvent() {}

    public ExtremeWeatherEvent(String id, Double longitude, Double latitude, ExtremeWeatherEventCategory category, Integer strength, Integer radius, LocalDateTime dateStart, LocalDateTime dateEnd) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
        this.strength = strength;
        this.radius = radius;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public ExtremeWeatherEvent(ExtremeWeatherEventDTO dto) {
        this.longitude = dto.getLongitude();
        this.latitude = dto.getLatitude();
        this.category = dto.getCategory();
        this.strength = dto.getStrength();
        this.radius = dto.getRadius();
        this.dateStart = dto.getDateStart();
        this.dateEnd = dto.getDateEnd();
    }

    public ExtremeWeatherEvent(Double longitude, Double latitude, ExtremeWeatherEventCategory category, Integer strength, Integer radius, LocalDateTime dateStart, LocalDateTime dateEnd) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.category = category;
        this.strength = strength;
        this.radius = radius;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    // Getters and setters
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

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
