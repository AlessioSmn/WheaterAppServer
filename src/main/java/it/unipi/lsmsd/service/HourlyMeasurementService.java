package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.service.UserService;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

// Save the Weather Data to Mongo DB
@Service
public class HourlyMeasurementService {

    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;

    @Autowired
    private UserService userService;

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

    public ResponseEntity<String> handleMeasurementRequest(APIResponseDTO responseDTO, CityDTO cityDTO, String token) {
        // Check if the user is an admin
        userService.getAndCheckUserFromToken(token, Role.ADMIN);

        // NOTE: Validate the CityDTO values which can prevent unnecessary API calls to Open Meteo

        if (!isValidCityName(cityDTO.getName()) || !isValidCityName(cityDTO.getRegion())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid city name or region. Only letters are allowed.");
        }

        if (!isValidLatitude(cityDTO.getLatitude()) || !isValidLongitude(cityDTO.getLongitude())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid latitude or longitude values.");
        }

        try {
            String cityId = Mapper.mapCity(cityDTO).getId();

            HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
            hourlyMeasurementDTO.setCityId(cityId);
            this.saveHourlyMeasurements(hourlyMeasurementDTO);

            return ResponseEntity.status(HttpStatus.OK).body("Added to the MongoDB Database: WeatherApp successfully");

        } catch (HttpServerErrorException | IllegalArgumentException ex) {
            // 503 standard HTTP response when a dependent service is down
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        } catch (HttpClientErrorException ex) {
            // Error on Client side
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
        } catch (Exception ex) {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error: " + ex.getMessage());
        }
    }

    private boolean isValidCityName(String name) {
        return name != null && name.matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s]+$");
    }

    private boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    private boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
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


