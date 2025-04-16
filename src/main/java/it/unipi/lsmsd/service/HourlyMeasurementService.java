package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// Save the Weather Data to Mongo DB
@Service
public class HourlyMeasurementService {

    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;

    @Autowired
    private UserService userService;

    // Saves the list of hourlyMeasurement of the given city to the DB in Time-Series Collection "hourly_measurements" 
    public void saveHourlyMeasurements( HourlyMeasurementDTO hourlyMeasurementDTO) throws Exception{
        
        // Extract list of hourly data
        List<HourlyMeasurement> measurements = Mapper.mapHourlyMeasurement(hourlyMeasurementDTO);
        // TODO: Save in Batch to prevent overloading DB
        // Insert the list
        hourlyMeasurementRepository.insert(measurements);
        
        // Suggestion: Use Parallel/ Multitask
    }

    public void handleMeasurementRequest(APIResponseDTO responseDTO, CityDTO cityDTO, String token)  throws Exception {
        // Check if the user is an admin
        userService.getAndCheckUserFromToken(token, Role.ADMIN);

        // NOTE: Validate the CityDTO values which can prevent unnecessary API calls to Open Meteo
        String cityId = Mapper.mapCity(cityDTO).getId();

        HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
        hourlyMeasurementDTO.setCityId(cityId);
        this.saveHourlyMeasurements(hourlyMeasurementDTO);
    }
/*
    // Get all the measurements with city name
    public List<HourlyMeasurement> getMeasurementsByCity(String cityName) {
        // TODO: Why cityName? Need Lat and Long as index?????
        // TODO: if cityName need to get cityID(eventually need the lat long)
        return hourlyMeasurementRepository.findByCityId(cityName);
    }
*/
}


