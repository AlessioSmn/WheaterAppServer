package it.unipi.lsmsd.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cities")
public class City {
    private String id;
    private String name;
    private String country;

    public City() {}

    public City(String name, String country) {
        this.name = name;
        this.country = country;
    }
}
