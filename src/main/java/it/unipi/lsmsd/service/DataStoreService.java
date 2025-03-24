package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

// Save the Weather Data to Mongo DB
@Service
public class DataStoreService {

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;

    // Saves the city to the DB and returns the cityID
    public String saveCity(String name, String region, APIResponseDTO apiResponseDTO){
        //Extract longitude and latitude from APIResponseDTO
        Double latitude = apiResponseDTO.getLatitude();
        Double longitude = apiResponseDTO.getLongitude();
        // generate custom city Id
        String cityId = City.generateCityId(name, region , latitude, longitude);

        // Check if the city already exits 
        if (!cityRepository.existsById(cityId)) {
            // Create city instance
            City city = new City();
            city.setId(cityId);
            city.setName(name);
            city.setRegion(region);
            city.setLatitude(latitude);
            city.setLongitude(longitude);
            city.setElevation(apiResponseDTO.getElevation());
            city.setFollowers(0);
            city.setLastUpdate(LocalDateTime.now());
            // Save
            cityRepository.save(city);
        }
        return cityId;
    }
        
    // Saves the list of hourlyMeasurement of the given city to the DB in Time-Series Collection "hourly_measurements" 
    public void saveHourlyMeasurements( HourlyMeasurementDTO hourlyMeasurementDTO, String cityId) {
        // 1. Extract each hourly data based on the time
        
        // 2. Create the Model Class with 1Day-1Document pattern based on MongoDB Time-Series Collection Requirements
        // 3. Save each document
        // Suggestion: Use Parallel/ Multitask
        hourlyMeasurementRepository.save(null);
    }

}
