package it.unipi.lsmsd.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /* Methods for Initialize Data*/
    @PostMapping("initialize-cities")
    public ResponseEntity<String> initializeCityData() throws IOException{
        dataInitializeService.initializeCities();
        return ResponseEntity.status(HttpStatus.OK).body("Citites Data Initialized successfully. Check Log to Verify");
    }

    @PostMapping("initialize-hourly-measurements")
    public ResponseEntity<String> initializeMeasurementData() throws IOException{
        dataInitializeService.initializeMeasurements();
        return ResponseEntity.status(HttpStatus.OK).body("Measurements Data Initialized successfully. Check Log to Verify");
    }

    @PostMapping("refresh-historical")
    public ResponseEntity<String> refreshHistoricalMeasurement() throws JsonProcessingException, IOException{
        dataRefreshService.refreshHistoricalMeasurement();
        return ResponseEntity.status(HttpStatus.OK).body("Historical Measurements Data Refreshed successfully. Check Log to Verify");
    }

    @PostMapping("refresh-forecast")
    public ResponseEntity<String> refreshforecast() throws JsonProcessingException{
        dataRefreshService.refreshForecast();
        return ResponseEntity.status(HttpStatus.OK).body("Forecast Data Refreshed successfully. Check Log to Verify");
    }
}
