package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.CityTEMPORARY_NAME_DTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    public CityTEMPORARY_NAME_DTO getCity(String cityName) throws Exception {
        Optional<City> city = cityRepository.findByName(cityName);
        return new CityTEMPORARY_NAME_DTO(city.get()); // throws NoSuchElementException is no city is found
    }

    // Inserts a new city only if is not already present. If it finds a city with the same name throws a IllegalArgumentException
    public void insertNewCity(CityTEMPORARY_NAME_DTO city) throws Exception {
        Optional<City> existingCity = cityRepository.findByName(city.getName());

        // If city already present then throw IllegalArgumentException
        if (existingCity.isPresent()) {
            throw new IllegalArgumentException("City already exists");
        }

        // Convert DTO to model
        City cityModel = new City(city);

        // Save the new city in the database
        cityRepository.save(cityModel);
    }
}
