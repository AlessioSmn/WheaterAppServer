package it.unipi.lsmsd.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;

@Service
public class DataRefreshService {
    @Autowired
    private HourlyMeasurementService hourlyMeasurementService;
    @Autowired
    private DataHarvestService dataHarvestService;
    @Autowired
    private CityService cityService;
    @Autowired
    private RedisForecastService forecastRedisService;

    private final ObjectMapper objectMapper;

    public DataRefreshService(){
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /*
     * Add 
     *  Get the lis
     *  For each city get the city.endDate and compare with current date
     * Get Historical Data with the date range
     * Delete the 
     */
    public void refreshHistoricalMeasurement() throws JsonProcessingException, IOException{  
        // Use PathMatchingResourcePatternResolver to load the resource from the correct path
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:data_init/citiesId.json");

        // Get available processors to determine thread pool size
        int threadCount = Math.min(4, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        try (InputStream is = resource.getInputStream()){
            List<String> cityIdList = objectMapper.readValue(is, new TypeReference<List<String>>() {});
            
            // Submit tasks for parallel processing
            for(String cityId : cityIdList){
                // refreshCityHistoricalData(cityId);
                Callable<Void> task = () -> {
                    refreshCityHistoricalData(cityId);
                    return null;
                };
                futures.add(executor.submit(task));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get(); // Blocking call, waits for task to finish
                } catch (Exception e) {
                    e.printStackTrace(); // Handle any task exception
                }
            }
        }
        catch (IOException e) {
            // TODO: handle exception
        }
        catch (Exception e){
            System.out.println("");
        } finally {
            executor.shutdown(); // Ensure that the executor shuts down after all tasks are complete
        }
        

        // // Get the list of cities from the DB

        // // For each city Add Fresh Data
        // String cityId = "pis-tus-43.7085-10.4036";
        // refreshCityHistoricalData(cityId);

    }

    private void refreshCityHistoricalData(String cityId) throws JsonProcessingException{
        
        try {
            CityDTO cityDTO = cityService.getCityWithID(cityId);
            // Check for the Start and End Date
            if(cityDTO == null || cityDTO.getStartDate()==null || cityDTO.getEndDate()== null){ return;} 
        
            DateTimeFormatter zonedFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            ZoneId zoneId = ZoneId.of("UTC");

            // STEP 1: Save the Fresh Historical Data
            // Convert both dates to Local Timezone of the cities
            // Get the City endDate, parse to ZonedDateTime with Local timezone, add one day and fromat to (yyyy-MM-dd)
            // Add one hour
            // cityDTO.getEndDate() "Thu May 01 23:00:00 UTC 2025" --> hourly_startDate "2025-05-02"
            String hourly_startDate = ZonedDateTime.parse(cityDTO.getEndDate(), zonedFormatter)
                .plusHours(1)
                .format(isoFormatter);
            // The endDate would be yesterday so subtract a day
            // "2025-05-07"
            String hourly_endDate = LocalDate.now(zoneId)
                .minusDays(1)
                .toString();
            // Check if start date is greater than end date which means Data already updated
            if (LocalDate.parse(hourly_startDate).isAfter(LocalDate.parse(hourly_endDate))) {
                // logger
                return;
                // throw new Exception("");
            }
            // Get data from Open-Meteo API
            APIResponseDTO apiResponseDTO = dataHarvestService.getCityHistoricalMeasurement(
                cityDTO.getLatitude(),cityDTO.getLongitude(), hourly_startDate, hourly_endDate);
            HourlyMeasurementDTO hourlyMeasurementDTO = apiResponseDTO.getHourly();
            hourlyMeasurementDTO.setCityId(cityId);
            //Save to MongoDB
            hourlyMeasurementService.saveHourlyMeasurements(apiResponseDTO.getHourly());
            
            // STEP 2: Delete the Stale Historical Data
            // Calcuate the new start date for removal of the stale data
            // Parse the endDate into LocalDate
            // Convert LocalDate to ZonedDateTime at midnight UTC
            // Subtract 1 hour and 25 years
            // hourly_endDate "2025-05-02"-->city_newStartDate Date@124 "Sun May 07 00:00:00 UTC 2000"
            Date city_newStartDate = Date.from(
                LocalDate.parse(hourly_endDate)
                .atStartOfDay(zoneId)
                .minusYears(25)
                .toInstant());
            // Calcuate the  old start date
            // "2025-05-07" -->
            Date city_oldStartDate = Date.from(
                ZonedDateTime.parse(cityDTO.getStartDate(), zonedFormatter)
                .minusHours(1)
                .toInstant());
            // Delete all data before newStartDate
            hourlyMeasurementService.deleteHourlyMeasurements(cityId, city_oldStartDate, city_newStartDate);

            // STEP 3: Update the City Start and End Date
            // Convert the Date back to LocalDate
            // Format the LocalDate to the desired string format "yyyy-MM-dd"
            String formattedDate = city_newStartDate.toInstant()
                .atZone(zoneId)
                .toLocalDate()
                .format(isoFormatter);
            cityService.updateStartEndDate(formattedDate + "T00:00", hourly_endDate + "T23:00", cityId);

        } catch (NoSuchElementException e) {
            // From CityDTO cityDTO = cityService.getCityWithID(cityId);
            // TODO: log
            return;
        }
        catch( DuplicateKeyException e){

        }
    }

    public void refreshForecast() throws JsonProcessingException{

        //Make Sure no stale Forecast exists in DB
        forecastRedisService.deleteAllForecast();

        // Use PathMatchingResourcePatternResolver to load the resource from the correct path
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:data_init/citiesId.json");
        try (InputStream is = resource.getInputStream()){
            List<String> cityIdList = objectMapper.readValue(is, new TypeReference<List<String>>() {});
            

            for(String cityId : cityIdList){
                refreshCityForecast(cityId);
            }
        }
        catch (IOException e) {
            // TODO: handle exception
        }
        catch (Exception e){
            System.out.println("");
        }

    }

    private void refreshCityForecast(String cityId) throws JsonProcessingException{
        CityDTO cityDTO = cityService.getCityWithID(cityId);
        // Get 7 days forecast and save
        APIResponseDTO apiResponseDTO = dataHarvestService.getCityForecast(cityDTO.getLatitude(), cityDTO.getLongitude(), 0, 7);
        HourlyMeasurementDTO hourlyMeasurementDTO = apiResponseDTO.getHourly();
        hourlyMeasurementDTO.setCityId(cityId);
        forecastRedisService.saveForecast(hourlyMeasurementDTO);
    }
}