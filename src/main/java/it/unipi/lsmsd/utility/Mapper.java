package it.unipi.lsmsd.utility;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.HourlyMeasurement;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class Mapper {
    // Single reusable instance of objectMapper throughout the application's lifecycle
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Private constructor to prevent instantiation
    private Mapper() { }

    // Extracts hourly weather data from the JSON string into a list of MeasurementDTO
    public static APIResponseDTO mapAPIResponse(String json) throws JsonProcessingException{
        return objectMapper.readValue(json, APIResponseDTO.class);
    }

    // Maps HourlyMeasurementDTO to List<HourlyMeasurement>
    public static List<HourlyMeasurement> mapHourlyMeasurement(HourlyMeasurementDTO dto) {
        List<HourlyMeasurement> measurements = new ArrayList<>();
        
        List<String> times = dto.getTime();
        List<Double> temperatures = dto.getTemperature();
        List<Double> rains = dto.getRain();
        List<Double> snowfalls = dto.getSnowfall();
        List<Double> windSpeeds = dto.getWindspeed();

        int dataSize = times.size(); // Assuming all lists have the same size

        for (int i = 0; i < dataSize; i++) {
            HourlyMeasurement measurement = new HourlyMeasurement();
            
            measurement.setCityId(dto.getCityId());
            measurement.setTime(getISODate(times.get(i)));
            measurement.setTemperature(temperatures.get(i));
            measurement.setRainfall(rains.get(i));
            measurement.setSnowfall(snowfalls.get(i));
            measurement.setWindSpeed(windSpeeds.get(i));
            measurements.add(measurement);
        }

        return measurements;
    }
    
    // Maps cityDTO to city
    public static City mapCity(CityDTO cityDTO){
        
        // Get the required fields for cityID generation
        String name = cityDTO.getName();
        String region =  cityDTO.getRegion();
        Double latitude = cityDTO.getLatitude(); 
        Double longitude = cityDTO.getLongitude();
        // generate custom city Id
        String cityId = CityUtility.generateCityId(name, region , latitude, longitude);
        // Map the cityDTO to city
        return new City(cityId, name, region, latitude, longitude, cityDTO.getElevation(),0,LocalDateTime.now());
    }

    // Maps cityDTO with thresholds to city
    public static City mapCityWithThresholds(CityDTO cityDTO){

        // Get the required fields for cityID generation
        String name = cityDTO.getName();
        String region =  cityDTO.getRegion();
        Double latitude = cityDTO.getLatitude();
        Double longitude = cityDTO.getLongitude();
        // generate custom city Id
        String cityId = CityUtility.generateCityId(name, region , latitude, longitude);
        // Map the cityDTO to city
        return new City(cityId, name, region, latitude, longitude, cityDTO.getElevation(),0,LocalDateTime.now(), cityDTO.getEweThresholds());
    }
    
    // Maps city to cityDTO
    public static CityDTO mapCity(City city){
        CityDTO cityDTO = new CityDTO();
        // Map each required fields
        cityDTO.setName(city.getName());
        cityDTO.setRegion(city.getRegion());
        cityDTO.setLatitude(city.getLatitude());
        cityDTO.setLongitude(city.getLongitude());
        cityDTO.setElevation(city.getElevation());
        cityDTO.setEweThresholds(city.getEweThresholds());
        return cityDTO;
    }

    // Helper method for mapHourlyMeasurement() : returns ISO Date for given time
    private static Date getISODate(String time){
        // Parse the time string (without timezone)
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        // Parse to LocalDateTime (assuming input is UTC) since we 
        //  get "utc_offset_seconds": 0 and "timezone": "GMT" from Open-Meteo
        LocalDateTime localTime = LocalDateTime.parse(time, inputFormatter);
        // Convert LocalDateTime to Instant in UTC
        Instant instant = localTime.toInstant(ZoneOffset.UTC);
        // Convert Instant to java.util.Date
        return Date.from(instant);
    }
}
