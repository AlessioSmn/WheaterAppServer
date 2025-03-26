package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.service.HourlyMeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ewe/update")
public class UpdateEWEController {

    @Autowired
    private HourlyMeasurementService measurementService;

    @GetMapping("/count-by-city")
    public ResponseEntity<String> countOutOfThresholdMeasurementsByCity(@RequestParam String city) {
        int count = measurementService.getMeasurementsByCity(city).size();
        String jsonResponse = String.format(
                "{\"Total measurements found\": %d, \"Measurements out of boundaries\": \"TODO\"}", count);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);

    }
}
