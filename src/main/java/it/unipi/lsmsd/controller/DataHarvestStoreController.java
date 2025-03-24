package it.unipi.lsmsd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.service.DataStoreService;

@RestController
@RequestMapping("/dataDB")
public class DataHarvestStoreController {
    
    @Autowired
    private DataHarvestService dataHarvestService;
    @Autowired
    private DataStoreService dataStoreService;
    
    /**
     * Example Request Body for addCityAndMeasurements
        {
        "name": "Pisa",
        "regions": "Tuscany",
        "latitude": 43.7085,
        "longitude": 10.4036,
        "startDate": "2025-03-15",
        "endDate": "2025-03-15"
        }
     **/
    @PostMapping("/addCityMeasurements")
    public ResponseEntity<String> addCityAndMeasurements(@RequestBody CityDTO cityDTO) {
        try {
            // NOTE: Validate the CityDTO values which can prevent unnecessary API calls to Open Meteo
            // TODO: Validate and handle CityDTO.name and CityDTO.region valid inputs (no null/empty values 
            //      and only string(no spaces, numbers and special characters))
            // TODO: Validate CityDTO.latitude and CityDTO.longitude with valid inputs (only numbers)

            // Get data from Open Meteo
            APIResponseDTO responseDTO = dataHarvestService.getData(
                cityDTO.getLatitude(),
                cityDTO.getLongitude(), 
                cityDTO.getStartDate(), 
                cityDTO.getEndDate());

            // Save the city if the city doesn't exist in the DB and get the city Id
            String cityId = dataStoreService.saveCity(cityDTO.getName(), cityDTO.getRegion(), responseDTO);
           
            // Save the Measurement of the given City
            dataStoreService.saveHourlyMeasurements(responseDTO.getHourly(), cityId);

        }catch (HttpServerErrorException | IllegalArgumentException ex ){
            // 503 standard HTTP response when a dependent service is down
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ex.getMessage());
        }catch (HttpClientErrorException ex){
            // Error on Client side
            return ResponseEntity
            .status(ex.getStatusCode()) 
            .body(ex.getMessage());
        }catch(Exception ex){
            // Unexpected Error
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected Error: " + ex.getMessage());
        }
        
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("Added to the MongoDB Database:WeatherApp successfully"+ cityDTO.getName());

    }
}
