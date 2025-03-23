package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.DTO.ExtremeWeatherEventDTO;
import it.unipi.lsmsd.service.ExtremeWeatherEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ewe")
public class ExtremeWeatherEventController {

    @Autowired
    private ExtremeWeatherEventService eweService;

    @PostMapping("/insert")
    public ResponseEntity<String> insert(@RequestBody ExtremeWeatherEventDTO ewe) {
        try{
            eweService.addNewEWE(ewe);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Extreme Weather Event added successfully");
        }
        catch (IllegalArgumentException iae) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(String.format("Extreme Weather Event cannot be added, error message: %s", iae.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Extreme Weather Event cannot be added");
        }
    }
}
