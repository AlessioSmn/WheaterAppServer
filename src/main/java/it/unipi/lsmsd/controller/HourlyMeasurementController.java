package it.unipi.lsmsd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.service.CityService;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.service.ForecastRedisService;
import it.unipi.lsmsd.service.HourlyMeasurementService;
import it.unipi.lsmsd.utility.CityUtility;
import it.unipi.lsmsd.utility.Mapper;

@RestController
@RequestMapping("/hourly")
public class HourlyMeasurementController {

    @Autowired
    private DataHarvestService dataHarvestService;
    @Autowired
    private HourlyMeasurementService hourlyMeasurementService;
    @Autowired
    private CityService cityService;
    @Autowired
    private ForecastRedisService forecastRedisService;

    /**
    Example Request Body :
    {
    "name": "Pisa",
    "regions": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036,
    "start": "2025-01-20",
    "end": "2025-01-21"
    }
    **/

    // Gets the historical data of a city for given time-frame from the Open-Meteo API and saves in the MongoDB
    @PostMapping("/add-historical-measurements")
    public ResponseEntity<String> addHistoricalMeasurements(@RequestBody CityDTO cityDTO) throws JsonProcessingException {

         // NOTE: Validate the CityDTO values which can prevent unnecessary API calls to Open Meteo
        // TODO: Validate and handle CityDTO.name and CityDTO.region valid inputs (no null/empty values
        //      and only string(no spaces, numbers and special characters))
        // TODO: Validate CityDTO.latitude and CityDTO.longitude with valid inputs (only numbers)
        APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getStartDate(), cityDTO.getEndDate());
        
        try {
            // Update elevation in cityDTO
            cityDTO.setElevation(responseDTO.getElevation());
            String cityId = saveOrRetrieveCityId(cityDTO);

            HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
            hourlyMeasurementDTO.setCityId(cityId);
            hourlyMeasurementService.saveHourlyMeasurements(hourlyMeasurementDTO);

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
    
    /** 
    Example Request Body :
    {
    "name": "Pisa",
    "regions": "Tuscany",
    "latitude": 43.7085,
    "longitude": 10.4036,
    "pastDays": "1", // Optional
    "forecastDays": "8" // Optional
    }
    **/
    // Gets the forecast(7 days by default) of a city from the Open-Meteo API and saves in the Redis Server
    @PostMapping("/add-forecast")
    public ResponseEntity<String> addForecast(@RequestBody CityDTO cityDTO) throws JsonProcessingException {
        
        // NOTE: Validate the CityDTO values which can prevent unnecessary API calls to Open Meteo
        // TODO: Validate and handle CityDTO.name and CityDTO.region valid inputs (no null/empty values
        //      and only string(no spaces, numbers and special characters))
        // TODO: Validate CityDTO.latitude and CityDTO.longitude with valid inputs (only numbers)
        APIResponseDTO responseDTO = dataHarvestService.getCityForecast(cityDTO.getLatitude(), cityDTO.getLongitude(), cityDTO.getPastDays(), cityDTO.getForecastDays());
        
        try {
            HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
            // Assumes cityDTO contains non-empty values for attributes: Name, Region, Latitude and Longitude 
            String cityId = CityUtility.generateCityId(cityDTO.getName(), cityDTO.getRegion() , cityDTO.getLatitude(), cityDTO.getLongitude());
            hourlyMeasurementDTO.setCityId(cityId);
            forecastRedisService.saveForecast(hourlyMeasurementDTO);

            return ResponseEntity.status(HttpStatus.OK).body(String.format("Added %s to the Redis successfully", cityDTO.getName()));

        } catch (HttpServerErrorException | IllegalArgumentException ex) {
            // 503 standard HTTP response when a dependent service is down
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        } catch (HttpClientErrorException ex) {
            // Error on Client side
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
        } catch(JsonProcessingException ex){
            //Error when creating JSON from HourlyMeasurementDTO before saving in Redis
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("JsonProcessing Error before saving in Redis: " + ex.getMessage());
        } catch (Exception ex) {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error: " + ex.getMessage());
        }
    }

    private String saveOrRetrieveCityId(CityDTO cityDTO) {
        // Save the city if the city doesn't exist in the DB and get the city Id
        try {
            return cityService.saveCity(cityDTO);
        } catch (Exception e) {
            return Mapper.mapCity(cityDTO).getId();
        }
    }
}
