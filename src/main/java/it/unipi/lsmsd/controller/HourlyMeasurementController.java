package it.unipi.lsmsd.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Handles HTTP GET requests to retrieve the 24-hour weather forecast for a specified city.
     *
     * @param cityId the unique identifier of the city for which the forecast is requested
     * @return a {@link ResponseEntity} containing the 24-hour forecast as a JSON-formatted string and an HTTP status code 200 (OK)
     */
    @GetMapping("/forecast/today")
    public ResponseEntity<Object> get24HrForecast(@RequestParam String cityId) {
        LocalDate today = LocalDate.now();
        String jsonForecast = forecastRedisService.getForecastTargetDay(cityId, today);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

    /**
     * Handles HTTP GET requests to retrieve the 24-hour weather forecast for a specified city and date.
     *
     * @param cityId the unique identifier of the city for which the forecast is requested
     * @param targetDate the {@link LocalDate} representing the day (in UTC) for which the forecast is desired
     * @return a {@link ResponseEntity} containing the forecast as a JSON-formatted string and an HTTP status code 200 (OK)
     */
    @GetMapping("/forecast/day")
    public ResponseEntity<Object> get24HrForecast(@RequestParam String cityId, @RequestParam LocalDate targetDate) {
        String jsonForecast = forecastRedisService.getForecastTargetDay(cityId, targetDate);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

    /**
     * Handles HTTP GET requests to retrieve the 7-day weather forecast for a specified city.
     *
     * @param cityId the unique identifier of the city for which the forecast is requested
     * @return a {@link ResponseEntity} containing the 7-day forecast as a JSON-formatted string and an HTTP status code 200 (OK)
     * @throws IOException if an I/O error occurs during the retrieval of the forecast data
     */
    @GetMapping("/forecast/week")
    public ResponseEntity<Object> get7DayForecast(@RequestParam String cityId) throws IOException {
        String jsonForecast = forecastRedisService.get7DayForecast(cityId);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

    /**
     * Handles HTTP GET requests to retrieve the 24-hour weather forecast for an arbitrary city
     * identified by region, latitude, and longitude.
     * <p>
     * The forecast corresponds to the current UTC day and is retrieved based on geolocation parameters.
     *
     * @param region the name of the region in which the city is located
     * @param latitude the geographic latitude of the city
     * @param longitude the geographic longitude of the city
     * @return a {@link ResponseEntity} containing the forecast as a JSON-formatted string and an HTTP status code 200 (OK)
     */
    @GetMapping("/forecast/today/arbitrary-city")
    public ResponseEntity<Object> get24HrForecastArbCity(
            @RequestParam String region,
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {

        LocalDate today = LocalDate.now();

        String jsonForecast = forecastRedisService.getForecastArbitraryCityTargetDay(region, latitude, longitude, today);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

    @GetMapping("/forecast/day/arbitrary-city")
    public ResponseEntity<Object> get24HrForecastArbCity(
            @RequestParam String region,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam LocalDate targetDate
    ) {

        String jsonForecast = forecastRedisService.getForecastArbitraryCityTargetDay(region, latitude, longitude, targetDate);
        return ResponseEntity.status(HttpStatus.OK).body(jsonForecast);
    }

}
