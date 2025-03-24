package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// Save the Weather Data to Mongo DB
@Service
public class DataStoreService {

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;

    // TODO : Throw specific error type for every exception
    // Error Type	            HTTP Status Code	        Action
    // Duplicate Key Error	    409 Conflict	            Inform user of data conflict
    // Validation Error	        400 Bad                     Request	User corrects input
    // Network/Timeout Error	504 Gateway                 Timeout	Retry logic or inform user
    // Write Concern Failure	503 Service                 Unavailable	Retry or alert admin
    // Unexpected Error	        500 Internal Server Error	Log & alert for investigation

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
    public void saveHourlyMeasurements( HourlyMeasurementDTO hourlyMeasurementDTO) {
        
        // Extract list of hourly data
        List<HourlyMeasurement> measurements = Mapper.mapHourlyMeasurement(hourlyMeasurementDTO);
        // TODO: Save in Batch to prevent overloading DB
        // Insert the list
        hourlyMeasurementRepository.insert(measurements);
        
        // Suggestion: Use Parallel/ Multitask
    }

}


