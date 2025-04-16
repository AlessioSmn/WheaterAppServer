package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.exception.CityNotFoundException;
import it.unipi.lsmsd.service.CityService;
import it.unipi.lsmsd.service.ExtremeWeatherEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ewe")
public class ExtremeWeatherEventController {

    @Autowired
    private ExtremeWeatherEventService extremeWeatherEventService;

    @Autowired
    private CityService cityService;


    @PutMapping("/update/automatic")
    public ResponseEntity<String> updateExtremeWeatherEventAutomatic(
            @RequestHeader("Authorization") String token,
            @RequestParam String cityId
    ) {
        try {
            // Get city's last Ewe update
            LocalDateTime lastEweUpdate = cityService.getLastEweUpdateById(cityId);

            // Calls service updateExtremeWeatherEvent over time interval (lastEweUpdate; Now)
            List<String> createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, lastEweUpdate, LocalDateTime.now(), token);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "Created EWEs count", createdEWEs.size(),
                            "Created EWEs IDs", createdEWEs
                    ).toString());
        }
        catch (CityNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
        catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server error: " + ex.getMessage());
        }
        finally {
            // Update lastEweUpdate to now
            try {
                // Note: even if it throws IllegalArgumentException it only does so on city not found,
                // but if here it should have already found the city calling getLastEweUpdateById()
                cityService.setLastEweUpdateById(cityId, LocalDateTime.now());
            }
            catch (Exception e) {
                // TODO log error
            }
        }
    }


    @PutMapping("/update/recent")
    public ResponseEntity<String> updateExtremeWeatherEventRecent(
            @RequestHeader("Authorization") String token,
            @RequestParam String cityId,
            @RequestParam Integer hours
    ) {

        try {
            // Calls service updateExtremeWeatherEvent over time interval (NOW - hours; NOW)
            List<String> createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, LocalDateTime.now().minusHours(hours), LocalDateTime.now(), token);

            // TODO return information properly formatted
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "Created EWEs count", createdEWEs.size(),
                            "Created EWEs IDs", createdEWEs
                    ).toString());

        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server error: " + e.getMessage());
        }
    }

    @PutMapping("/update/range")
    public ResponseEntity<String> updateExtremeWeatherEventRange(
            @RequestHeader("Authorization") String token,
            @RequestParam String cityId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime
    ) {

        try {
            // Call service updateExtremeWeatherEvent over time interval (startTime; endTime)
            List<String> createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, startTime, endTime, token);

            // TODO return information properly formatted
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "Created EWEs count", createdEWEs.size(),
                            "Created EWEs IDs", createdEWEs
                    ).toString());

        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server error: " + e.getMessage());
        }
    }


    @PutMapping("/clean/range")
    public ResponseEntity<String> cleanUpExtremeWeatherEventRange(
            @RequestHeader("Authorization") String token,
            @RequestParam String cityId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime
    ) {
        // Call the relative service
        Map<String, Integer> cleanupResult = extremeWeatherEventService.cleanExtremeWeatherEventDuplicates(cityId, startTime, endTime, token);

        String TEMP_STRING = String.format("{\n\t\"removed\": %d,\n\t\"inserted\": %d\n}",
                cleanupResult.get("EWEs Removed"), cleanupResult.get("EWEs Inserted"));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(TEMP_STRING);
    }


    @PutMapping("/clean/all")
    public ResponseEntity<String> cleanUpExtremeWeatherEventRange(
            @RequestParam String cityId
    ) {
        // Call the relative service
        extremeWeatherEventService.cleanExtremeWeatherEventDuplicatesAll(cityId);

        // TODO return information properly formatted
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("ok");

    }


}
