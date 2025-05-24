package it.unipi.lsmsd.controller;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.exception.CityException;
import it.unipi.lsmsd.exception.CityNotFoundException;
import it.unipi.lsmsd.service.CityService;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.utility.Mapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/city")
public class CityController {

    @Autowired
    private CityService cityService;

    @PostMapping("/add")
    public ResponseEntity<String> addCity(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) {
        try{
            cityService.saveCity(cityDTO, token);

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

    @PostMapping("/add-cities__THIS_MUST_BE_DELETED")
    public ResponseEntity<String> addCities() throws IOException{
        String response = cityService.saveCitiesFromList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/add-with-thresholds")
    public ResponseEntity<String> addCityWithThresholds(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) {
        try{
            cityService.saveCityWithThresholds(cityDTO, token);

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

    // TODO have new cityDTO which maps directly the model to return that one, it the easier way.
    //  I don't want to return a city with start and end fields, it makes no sense.
    //  And i believe that its way too complex to manually exclude some field or do a on-the-fly conversion to json
    @PostMapping("/update-thresholds")
    public ResponseEntity<Object> updateCityThresholds(@RequestHeader("Authorization") String token, @RequestBody CityDTO cityDTO) {
        try{
            // Update the city threshold
            cityService.updateCityThresholds(cityDTO, token);

            String cityId = Mapper.mapCity(cityDTO).getId();
            CityDTO cityDtoAfter = cityService.getCityWithID(cityId);

            // Returns all city's information into the body
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(cityDtoAfter);
        }
        catch (CityNotFoundException CNFe) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("City not found: " + CNFe.getMessage());
        }
        catch (CityException Ce) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("City not identifiable: " + Ce.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // TODO have new cityDTO which maps directly the model to return that one, it the easier way.
    //  I don't want to return a city with start and end fields, it makes no sense.
    //  And i believe that its way too complex to manually exclude some field or do a on-the-fly conversion to json
    @GetMapping("/info")
    public ResponseEntity<Object> getCityByName(@RequestParam String cityName){
        try{
            // Retrieves the city
            List<CityDTO> cityDto = cityService.getCity(cityName);

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
