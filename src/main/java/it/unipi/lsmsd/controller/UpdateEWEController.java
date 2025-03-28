package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.service.ExtremeWeatherEventService;
import it.unipi.lsmsd.service.HourlyMeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ewe/update")
public class UpdateEWEController {

    @Autowired
    private ExtremeWeatherEventService extremeWeatherEventService;

    // TODO decide the HTTP method (put or post)
    public ResponseEntity<String> updateRecentExtremeWeatherEvent(@RequestParam String cityId, @RequestParam Integer hours){

        try {
            // Calls service updateExtremeWeatherEvent over time interval (NOW - hours; NOW)
            Integer createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, LocalDateTime.now().minusHours(hours), LocalDateTime.now());

            // TODO return information properly formatted
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("0");
        }
        catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server error: " + e.getMessage());
        }
    }

    // To check past EWE
    // TODO decide the HTTP method (put or post)
    public ResponseEntity<String> updatePastExtremeWeatherEvent(@RequestParam String cityId, @RequestParam LocalDateTime startTime, @RequestParam LocalDateTime endTime){

        try {
            // Call service updateExtremeWeatherEvent over time interval (startTime; endTime)
            Integer createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, startTime, endTime);

            // TODO return information properly formatted
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("0");
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server error: " + e.getMessage());
        }
    }
}
