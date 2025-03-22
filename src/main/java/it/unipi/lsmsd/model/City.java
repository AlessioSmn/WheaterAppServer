package it.unipi.lsmsd.model;

import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cities")
public class City {
    private String name;
    private String region;
    private Double latitude;
    private Double longitude;
    private Integer elevation;
    private Integer followers;
    private LocalDateTime lastUpdate;


    public City () {}

    public City (String name, String region, Double latitude, Double longitude, Integer elevation, Integer followers, String lastUpdate) {
        this.name = name;
        this.region = region;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.followers = followers;
        this.lastUpdate = LocalDateTime.parse(lastUpdate);
    }
}
