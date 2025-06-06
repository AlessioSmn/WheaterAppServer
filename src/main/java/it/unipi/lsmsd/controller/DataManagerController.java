package it.unipi.lsmsd.controller;

import java.io.IOException;

import it.unipi.lsmsd.exception.UnauthorizedException;
import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.service.AutomatingService;
import it.unipi.lsmsd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.unipi.lsmsd.service.DataInitializeService;
import it.unipi.lsmsd.service.DataRefreshService;

@RestController
@RequestMapping("/data-manager")
public class DataManagerController {

    @Autowired
    DataInitializeService dataInitializeService;

    @Autowired
    DataRefreshService dataRefreshService;

    @Autowired
    private UserService userService;

    @Autowired
    private AutomatingService automatingService;


    @PutMapping("/update/forecasts")
    public ResponseEntity<Object> updateForecasts(@RequestHeader("Authorization") String token) {
        try{
            automatingService.updateForecasts(token);
            return ResponseEntity
                    .status(HttpStatus.OK).build();
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }
    @PutMapping("/update/measurements")
    public ResponseEntity<Object> updateMeasurements(@RequestHeader("Authorization") String token) {
        try{
            automatingService.updateMeasurements(token);
            return ResponseEntity
                    .status(HttpStatus.OK).build();
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }
    @PutMapping("/update/ewe")
    public ResponseEntity<Object> updateEWEs(@RequestHeader("Authorization") String token) {
        try{
            automatingService.updateExtremeWeatherEvents(token);
            return ResponseEntity
                    .status(HttpStatus.OK).build();
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch(Exception ex){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + ex.getMessage());
        }
    }

    @PostMapping("initialize-all")
    public ResponseEntity<String> initializeAll(
            @RequestHeader("Authorization") String token) throws IOException{
        try {
            userService.getAndCheckUserFromToken(token, Role.ADMIN);

            // Step 1
            dataInitializeService.initializeCitiesMongo();
            dataInitializeService.initializeCitiesRedis();
            // Step 2
            dataInitializeService.initializeMeasurements();
            // Step 3
            dataRefreshService.refreshHistoricalMeasurement();
            // Step 4
            dataRefreshService.initializeExtremeWeatherEvents();
            // Step 5
            dataRefreshService.refreshForecast();
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body("Initialization complete. Check Log to Verify");
    }

    /* Methods for Initialize Data*/
    @PostMapping("initialize-1-cities")
    public ResponseEntity<String> initializeCityData(
            @RequestHeader("Authorization") String token) throws IOException{
        try {
            userService.getAndCheckUserFromToken(token, Role.ADMIN);
            dataInitializeService.initializeCitiesMongo();
            dataInitializeService.initializeCitiesRedis();
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body("Citites Data Initialized successfully. Check Log to Verify");
    }

    @PostMapping("initialize-2-hourly-measurements")
    public ResponseEntity<String> initializeMeasurementData(
            @RequestHeader("Authorization") String token) throws IOException{
        try{
            userService.getAndCheckUserFromToken(token, Role.ADMIN);
            dataInitializeService.initializeMeasurements();
            return ResponseEntity.status(HttpStatus.OK).body("Measurements Data Initialized successfully. Check Log to Verify");
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("initialize-3-refresh-historical")
    public ResponseEntity<String> refreshHistoricalMeasurement(
            @RequestHeader("Authorization") String token) throws JsonProcessingException, IOException{
        try{
            userService.getAndCheckUserFromToken(token, Role.ADMIN);
            dataRefreshService.refreshHistoricalMeasurement();
            return ResponseEntity.status(HttpStatus.OK).body("Historical Measurements Data Refreshed successfully. Check Log to Verify");
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("initialize-4-extremeWeatherEvents")
    public ResponseEntity<String> initializeExtremeWeatherEvents(
            @RequestHeader("Authorization") String token){
        try{
            userService.getAndCheckUserFromToken(token, Role.ADMIN);
            dataRefreshService.initializeExtremeWeatherEvents();
            return ResponseEntity.status(HttpStatus.OK).body("EWE initialized successfully. Check Log to Verify");
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("initialize-5-refresh-forecast")
    public ResponseEntity<String> refreshforecast(
            @RequestHeader("Authorization") String token) throws JsonProcessingException{
        try{
        userService.getAndCheckUserFromToken(token, Role.ADMIN);
        dataRefreshService.refreshForecast();
        return ResponseEntity.status(HttpStatus.OK).body("Forecast Data Refreshed successfully. Check Log to Verify");
        }
        catch(UnauthorizedException Ue){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + Ue.getMessage());
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }
}
