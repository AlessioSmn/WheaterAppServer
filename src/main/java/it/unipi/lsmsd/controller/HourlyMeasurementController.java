package it.unipi.lsmsd.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.service.RedisForecastService;
import it.unipi.lsmsd.service.HourlyMeasurementService;
import it.unipi.lsmsd.utility.CityUtility;

import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/hourly")
public class HourlyMeasurementController {

    @Autowired
    private DataHarvestService dataHarvestService;
    @Autowired
    private HourlyMeasurementService hourlyMeasurementService;
    @Autowired
    private RedisForecastService forecastRedisService;

    /**
    Example Request Body :
    {
    "name": "Pisa",
    "region": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036,
    "startDate": "2025-01-20",
    "endDate": "2025-01-21"
    }
    **/
    // Gets the historical data of a city for given time-frame from the Open-Meteo API and saves in the MongoDB
    @PostMapping("/add-historical-measurements")
    public ResponseEntity<String> addHistoricalMeasurements(@RequestBody CityDTO cityDTO) throws JsonProcessingException {
        // Validate the CityDTO values
        APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getStartDate(), cityDTO.getEndDate());

        HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
        String cityId = CityUtility.generateCityId(cityDTO.getName(), cityDTO.getRegion() , cityDTO.getLatitude(), cityDTO.getLongitude());
        hourlyMeasurementDTO.setCityId(cityId);
        // Save the data in MongoDB
        hourlyMeasurementService.saveHourlyMeasurements(hourlyMeasurementDTO);

        return ResponseEntity.status(HttpStatus.OK).body("Added to the MongoDB Database: WeatherApp successfully");
    }

    /**
    Example Request Body :
    {
    "name": "Pisa",
    "region": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036,
    "startDate": "2025-01-20",
    "endDate": "2025-01-21"
    }
    **/
    @GetMapping("/get-historical-measurements")
    public ResponseEntity<HourlyMeasurementDTO> getHistoricalMeasurements(@RequestBody CityDTO cityDTO){
        HourlyMeasurementDTO responseBody  = hourlyMeasurementService.getHourlyMeasurements(cityDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }


    /** 
    Example Request Body :
    {
    "name": "Pisa",
    "region": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036,
    "pastDays": "1", // Optional
    "forecastDays": "8" // Optional
    }
    **/
    // Gets the forecast (7 days by default) of a city from the Open-Meteo API and saves in the Redis Server
    @PostMapping("/add-forecast")
    public ResponseEntity<String> addForecast(@RequestBody CityDTO cityDTO) throws JsonProcessingException {
        
        // Get Forecast from Open-Meteo
        APIResponseDTO responseDTO = dataHarvestService.getCityForecast(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getPastDays(), cityDTO.getForecastDays());

        HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
        String cityId = CityUtility.generateCityId(cityDTO.getName(), cityDTO.getRegion() , cityDTO.getLatitude(), cityDTO.getLongitude());
        hourlyMeasurementDTO.setCityId(cityId);
        // Save the forecast in Redis
        forecastRedisService.saveForecast(hourlyMeasurementDTO);

        return ResponseEntity.status(HttpStatus.OK).body(String.format("Added %s to the Redis successfully", cityDTO.getName()));
    }

    /** 
    Example Request Body :
    {
    "name": "Pisa",
    "region": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036,
    "startDate": "2025-04-24" //Optional
    }
    **/
    // Gets the 24hr forecast for given city with given date
    @GetMapping("/get-24Hr-forecast")
    public ResponseEntity<String> get24HrForecast(@RequestBody CityDTO cityDTO) {
        String jsonForecast = forecastRedisService.get24HrForecast(cityDTO);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

    /** 
    Example Request Body :
    {
    "name": "Pisa",
    "region": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036
    }
    **/
    // Gets the 7 days forecast for given city starting from the current local date
    @GetMapping("/get-7Day-forecast")
    public ResponseEntity<String> get7DayForecast(@RequestBody CityDTO cityDTO) throws IOException {
        String jsonForecast = forecastRedisService.get7DayForecast(cityDTO);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }
    /**
     Example Request Body :
     {
     "region": "Tuscany",
     "latitude": 43.7085,
     "longitude": 10.4036
     }
     **/

    @GetMapping("24Hr-arbitrary-city")
    public ResponseEntity<String> get24HrForecastArbCity(@RequestBody CityDTO cityDTO) throws IOException {
        String jsonForecast = forecastRedisService.get24HrForecastArbCity(cityDTO);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

}
