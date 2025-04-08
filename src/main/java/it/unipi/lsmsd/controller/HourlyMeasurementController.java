package it.unipi.lsmsd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.service.CityService;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.service.HourlyMeasurementService;
import it.unipi.lsmsd.utility.Mapper;

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
     "startDate": "2025-01-20",
     "endDate": "2025-01-21"
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


    @PostMapping("/add-historical-measurements")
    public ResponseEntity<String> addHistoricalMeasurements(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) throws JsonProcessingException {
        APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getStart(), cityDTO.getEnd());
        return hourlyMeasurementService.handleMeasurementRequest(responseDTO, cityDTO, token);
    }

    @PostMapping("/add-recent-measurements-using-hours")
    public ResponseEntity<String> addRecentMeasurementsUsingHours(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) throws JsonProcessingException {
        APIResponseDTO responseDTO = dataHarvestService.getCityRecentMeasurementUsingHours(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getPastHours(), cityDTO.getForecastHours());
        return hourlyMeasurementService.handleMeasurementRequest(responseDTO, cityDTO, token);
    }

    @PostMapping("/add-recent-measurements")
    public ResponseEntity<String> addRecentMeasurements(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) throws JsonProcessingException {
        Duration duration = Duration.between(cityDTO.getLastUpdate(), LocalDateTime.now());
        APIResponseDTO responseDTO = dataHarvestService.getCityRecentMeasurementUsingHours(cityDTO.getLatitude(), cityDTO.getLongitude(), (int) duration.toHours(), 0);
        return hourlyMeasurementService.handleMeasurementRequest(responseDTO, cityDTO, token);
    }
}
