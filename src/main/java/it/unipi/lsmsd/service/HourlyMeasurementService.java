package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
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

    // TODO : Throw specific error type for every exception
    // Error Type	            HTTP Status Code	        Action
    // Duplicate Key Error	    409 Conflict	            Inform user of data conflict
    // Validation Error	        400 Bad                     Request	User corrects input
    // Network/Timeout Error	504 Gateway                 Timeout	Retry logic or inform user
    // Write Concern Failure	503 Service                 Unavailable	Retry or alert admin
    // Unexpected Error	        500 Internal Server Error	Log & alert for investigation

    // Saves the list of hourlyMeasurement of the given city to the DB in Time-Series Collection "hourly_measurements" 
    public void saveHourlyMeasurements( HourlyMeasurementDTO hourlyMeasurementDTO) {
        
        // Extract list of hourly data
        List<HourlyMeasurement> measurements = Mapper.mapHourlyMeasurement(hourlyMeasurementDTO);
        // TODO: Save in Batch to prevent overloading DB
        // Insert the list
        hourlyMeasurementRepository.insert(measurements);
        
        // Suggestion: Use Parallel/ Multitask
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


