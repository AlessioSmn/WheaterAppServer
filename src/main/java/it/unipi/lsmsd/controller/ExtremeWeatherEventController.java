package it.unipi.lsmsd.controller;

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

    // TODO



    // TODO using ONLY the updateExtremeWeatherEventAutomatic method will be safe from duplicate insertions
    //  Recent and Range risk the fact that an admin could put a wrong time interval and recalculate already present EWE
    //  We have to find a solution:
    //      1) Always suppose to have the DB in a consistent state where ALL EWEs up to date X are calculated:
    //          then we just have to find the most recent one and check that the time interval request is not overlapping with this
    //      2) All other solutions may be too complex to implement, they must resort to just look into every single EWE for check for duplicates
    //      3) Maybe just create a new Controller/Service that 'cleans up' every possible duplicate by looking into every EWEs, category by category.
    //          This way it can be called way less frequently and thus it's not a performance problem


    // TODO decide if to put a 'LastEweUpdate' attribute in city
    //  then you can call updateExtremeWeatherEvent just specifying the city,
    //  and it will automatically select the time interval from last update up to now
    //      It will be like this:
    // @PutMapping("/update/automatic")
    // public ResponseEntity<String> updateExtremeWeatherEventAutomatic(@RequestParam String cityId){


    @PutMapping("/update/recent")
    public ResponseEntity<String> updateExtremeWeatherEventRecent(
            @RequestParam String cityId,
            @RequestParam Integer hours
    ){

        try {
            // Calls service updateExtremeWeatherEvent over time interval (NOW - hours; NOW)
            List<String> createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, LocalDateTime.now().minusHours(hours), LocalDateTime.now());

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
            @RequestParam String cityId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime
    ){

        try {
            // Call service updateExtremeWeatherEvent over time interval (startTime; endTime)
            List<String> createdEWEs = extremeWeatherEventService.updateExtremeWeatherEvent(cityId, startTime, endTime);

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

}
