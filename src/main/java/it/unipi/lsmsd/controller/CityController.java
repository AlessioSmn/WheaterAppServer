package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.service.CityService;

import org.springframework.dao.DuplicateKeyException;
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

    @PostMapping("/add")
    public ResponseEntity<String> addCity(@RequestBody CityDTO cityDTO) {
        try{
            cityService.saveCity(cityDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)  // 201 for creation
                    .body("City added successfully");
        }
        catch(DuplicateKeyException ex){
            return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body("City already exits: " + ex.getMessage());
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

    @PostMapping("/add-with-thresholds")
    public ResponseEntity<String> addCityWithThresholds(@RequestBody CityDTO cityDTO) {
        try{
            cityService.saveCityWithThresholds(cityDTO);

            return ResponseEntity
                    .status(HttpStatus.CREATED)  // 201 for creation
                    .body("City added successfully");
        }
        catch(DuplicateKeyException ex){
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("City already exits: " + ex.getMessage());
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
    public ResponseEntity<Object> getCityByName(@RequestParam String cityName){
        try{
            // Retrieves the city
            CityDTO cityDto = cityService.getCity(cityName);

            // Returns all city's information into the body
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(cityDto); // Spring automatically converts CityDTO to JSON
        }
        catch(NoSuchElementException NSEe){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("City not found: " + NSEe.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
