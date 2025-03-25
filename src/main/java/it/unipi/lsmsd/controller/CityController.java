package it.unipi.lsmsd.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.unipi.lsmsd.DTO.CityTEMPORARY_NAME_DTO;
import it.unipi.lsmsd.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/city")
public class CityController {

    @Autowired
    private CityService cityService;

    @PutMapping("/add")
    public ResponseEntity<String> addCity(@RequestBody CityTEMPORARY_NAME_DTO cityName) {
        try{
            cityService.insertNewCity(cityName);

            return ResponseEntity
                    .status(HttpStatus.CREATED)  // 201 for creation
                    .body("City added successfully");
        }
        catch(IllegalArgumentException IAe){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Illegal argument: " + IAe.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/info")
    public ResponseEntity<String> getCityByName(@RequestParam String cityName){
        try{
            // Retrieves the city
            CityTEMPORARY_NAME_DTO cityDto = cityService.getCity(cityName);

            // Returns all city's information into the body
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(cityDto.toJson());
        }
        catch(NoSuchElementException NSEe){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("City not found: " + NSEe.getMessage());
        }
        catch (JsonProcessingException JPe) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("JsonProcessingException: " + JPe.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
