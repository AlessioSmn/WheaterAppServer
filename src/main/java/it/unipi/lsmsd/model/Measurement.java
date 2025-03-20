package it.unipi.lsmsd.model;

import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "measurements")
public class Measurement {
    private String city;
    private Double temperature;
    private Double rainfall;
    private Double snowfall;
    private Double windSpeed;
    private LocalDateTime lastUpdate;

    public Measurement () {}

    public Measurement (String city, Double temperature, Double rainfall, Double snowfall, Double windSpeed, String lastUpdate) {
        this.city = city;
        this.temperature = temperature;
        this.rainfall = rainfall;
        this.snowfall = snowfall;
        this.windSpeed = windSpeed;
        this.lastUpdate = LocalDateTime.parse(lastUpdate);
    }
}
