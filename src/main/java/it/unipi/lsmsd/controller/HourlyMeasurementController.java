package it.unipi.lsmsd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.unipi.lsmsd.exception.UnauthorizedException;
import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.service.CityService;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.service.HourlyMeasurementService;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/hourly")
public class HourlyMeasurementController {

    @Autowired
    private DataHarvestService dataHarvestService;
    @Autowired
    private HourlyMeasurementService hourlyMeasurementService;
    @Autowired
    private CityService cityService;

    /**
     Example Request Body for add-recent-measurements-using-hours
     {
     "name": "Pisa",
     "regions": "Tuscany",
     "latitude": 43.7085,
     "longitude": 10.4036,
     "start": "2025-01-20",
     "end": "2025-01-21"
     }

     Example Request Body for add-recent-measurements-using-hours
    {
        "name": "Pisa",
            "regions": "Tuscany",
            "latitude": 43.7085,
            "longitude": 10.4036,
            "pastHours": "12",
            "forecastHours": "12"
    }
     **/
    private boolean isValidCityName(String name) {
        return name != null && name.matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s]+$");
    }

    private boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    private boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }

    private void checkFields(CityDTO cityDTO){
        if (!isValidCityName(cityDTO.getName()) || !isValidCityName(cityDTO.getRegion())) {
            throw new IllegalArgumentException("Invalid city or region name");
        }

        if (!isValidLatitude(cityDTO.getLatitude()) || !isValidLongitude(cityDTO.getLongitude())) {
            throw new IllegalArgumentException("Invalid latitude or longitude values");
        }
    }


    private ResponseEntity<String> handleError(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            // bad request exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
        else if (ex instanceof UnauthorizedException) {
            // Unauthorized exception
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
        else {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error: " + ex.getMessage());
        }
    }

    @PostMapping("/add-historical-measurements")
    public ResponseEntity<String> addHistoricalMeasurements(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) throws JsonProcessingException {
        try{
            checkFields(cityDTO);
            APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getStart(), cityDTO.getEnd());
            hourlyMeasurementService.handleMeasurementRequest(responseDTO, cityDTO, token);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Added to the MongoDB Database: WeatherApp successfully");
        }
        catch(Exception ex) {
            return handleError(ex);
        }
    }

    @PostMapping("/add-recent-measurements-using-hours")
    public ResponseEntity<String> addRecentMeasurementsUsingHours(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) throws JsonProcessingException {
        try{
            checkFields(cityDTO);
            APIResponseDTO responseDTO = dataHarvestService.getCityRecentMeasurementUsingHours(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getPastHours(), cityDTO.getForecastHours());

            hourlyMeasurementService.handleMeasurementRequest(responseDTO, cityDTO, token);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Added to the MongoDB Database: WeatherApp successfully");
        }
        catch(Exception ex) {
            return handleError(ex);
        }
    }

    @PostMapping("/add-recent-measurements")
    public ResponseEntity<String> addRecentMeasurements(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) throws JsonProcessingException {
        try{
            checkFields(cityDTO);
            Duration duration = Duration.between(cityDTO.getLastUpdate(), LocalDateTime.now());
            APIResponseDTO responseDTO = dataHarvestService.getCityRecentMeasurementUsingHours(cityDTO.getLatitude(), cityDTO.getLongitude(), (int) duration.toHours(), 0);

            hourlyMeasurementService.handleMeasurementRequest(responseDTO, cityDTO, token);
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Added to the MongoDB Database: WeatherApp successfully");
        }
        catch(Exception ex) {
            return handleError(ex);
        }
    }
}
