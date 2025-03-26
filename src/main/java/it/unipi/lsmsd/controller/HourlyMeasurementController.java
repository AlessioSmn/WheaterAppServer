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

import com.mongodb.MongoWriteException;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.service.CityService;
import it.unipi.lsmsd.service.DataHarvestService;
import it.unipi.lsmsd.service.HourlyMeasurementService;
import it.unipi.lsmsd.utility.Mapper;

@RestController
@RequestMapping("/hourly")
public class HourlyMeasurementController {
    
    @Autowired
    private DataHarvestService dataHarvestService;
    @Autowired
    private HourlyMeasurementService hourlyMeasurementService;
    @Autowired
    private CityService cityService;
    
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
    // Adds the Hourly data given City within given Timeframe after harvesting the data from the Open-Meteo API 
    @PostMapping("/addMeasurements")
    public ResponseEntity<String> addMeasurements(@RequestBody CityDTO cityDTO) {
        try {
            // NOTE: Validate the CityDTO values which can prevent unnecessary API calls to Open Meteo
            // TODO: Validate and handle CityDTO.name and CityDTO.region valid inputs (no null/empty values 
            //      and only string(no spaces, numbers and special characters))
            // TODO: Validate CityDTO.latitude and CityDTO.longitude with valid inputs (only numbers)

            // Get data from Open Meteo
            APIResponseDTO responseDTO = dataHarvestService.getCityData(
                cityDTO.getLatitude(),
                cityDTO.getLongitude(), 
                cityDTO.getStartDate(), 
                cityDTO.getEndDate());

            // Upadte elevation in cityDTO
            cityDTO.setElevation(responseDTO.getElevation()); 

            // Save the city if the city doesn't exist in the DB and get the city Id
            String cityId = "";
            try {
                // Save and get the id
                cityId = cityService.saveCity(cityDTO);
            } catch (Exception e) {
                // Since the city already exists simply get the Id from the City mapped from cityDTO
                // Alternate would be to call CityUtility.generateCityId() which is called internally by the Mapper.mapCity()
                cityId = Mapper.mapCity(cityDTO).getId();
            }
                       
            //Save the Measurement of the given City
            HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();
            hourlyMeasurementDTO.setCityId(cityId);
            hourlyMeasurementService.saveHourlyMeasurements(hourlyMeasurementDTO);

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
