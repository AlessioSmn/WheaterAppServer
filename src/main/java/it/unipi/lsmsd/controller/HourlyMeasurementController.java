package it.unipi.lsmsd.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
    @PostMapping("/add-historical-measurements__THIS_MUST_BE_DELETED")
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
    @GetMapping("/get-historical-measurements__THIS_MUST_BE_DELETED_or_moved_to_analytics")
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
    @PostMapping("/add-forecast__THIS_MUST_BE_DELETED")
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
