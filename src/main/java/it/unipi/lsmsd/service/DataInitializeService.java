package it.unipi.lsmsd.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.utility.MongoInitializer;

@Service
public class DataInitializeService {

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CityService cityService;
    @Autowired
    private HourlyMeasurementService hourlyMeasurementService;

    private static final Logger logger = LoggerFactory.getLogger(MongoInitializer.class);
    private final ObjectMapper objectMapper;

    public DataInitializeService(){
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
  
    public void initializeCities() throws IOException{
        try (InputStream is = getClass().getResourceAsStream("classpath:/data_init/cities.json")) {
            List<City> cities = objectMapper.readValue(is, new TypeReference<List<City>>() {});
            // Save to the MondoDB
            cityRepository.saveAll(cities);
        }        
    }

    // public void initializeHourlyHistoricalMeasurement() throws IOException{
    //     String cityId = "flo-tus-43.7792-11.2463";
    //     InputStream is = getClass().getResourceAsStream("/data_init/measurements/mil-lom-45.4643-9.1895.json");
    //     if (is == null) {
    //         throw new FileNotFoundException("Resource file not found");
    //     }
    //     ObjectMapper mapper = new ObjectMapper();
    //     mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignore unknown fields from JSON
    //     APIResponseDTO responseDTO = mapper.readValue(is, new TypeReference<APIResponseDTO>() {});
    //     HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();

    //     hourlyMeasurementDTO.setCityId(cityId);
    //     hourlyMeasurementService.saveHourlyMeasurements(hourlyMeasurementDTO);

    //     // NOTE: Assumption that the last element of the time list is the latest date
    //     List<String> timeList = hourlyMeasurementDTO.getTime();
    //     String lastDate = timeList.get(timeList.size() - 1);
    //     cityService.updateLastUpdate(lastDate, cityId);

    //     logger.info("Added HourlyMeasurement for "+"cityId" + " and updated city.lastUpdate = "+ lastDate);

    //     return;
    // }

    public void initializeMeasurements() throws IOException {
        // Finds all JSON files under resources/data_init/measurements/
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/data_init/measurements/*.json");

        // Use a thread pool with max 4 threads or number of available CPUs (whichever is lower)
        int threadCount = Math.min(4, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // Submit a separate task for each file to process in parallel
        for (Resource resource : resources) {
            futures.add(executor.submit(() -> processFile(resource)));
        }

        // Wait for all threads to finish execution
        for (Future<?> future : futures) {
            try {
                future.get(); // Will block until each task is done
            } catch (InterruptedException | ExecutionException e) {
                // Log and continue if any task fails
                logger.error("Error in file processing task: " + e.getMessage());
            }
        }

        // Cleanly shut down the executor
        executor.shutdown();
    }

    
    // Processes a single JSON file: parses, extracts cityId, and saves data to MongoDB.
    private void processFile(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            // Deserialize JSON into DTO
            APIResponseDTO responseDTO = objectMapper.readValue(is, new TypeReference<APIResponseDTO>() {});
            HourlyMeasurementDTO dto = responseDTO.getHourly();
            
            // Use the filename (e.g. pis-tus-43.7085-10.4036.json) as the cityId
            String cityId = Optional.ofNullable(resource.getFilename())
                    .map(name -> name.replace(".json", ""))
                    .orElseThrow(() -> new IllegalArgumentException("City ID cannot be determined from filename: "));

            dto.setCityId(cityId);
            // Save the parsed measurement to MongoDB
            hourlyMeasurementService.saveHourlyMeasurements(dto);
            
            // Save the first and last element of the time as timeframe of the historical data
            List<String> timeList = dto.getTime(); 
            String endDate = timeList.get(timeList.size() - 1);
            String startDate = timeList.get(0);
            // Update the city Last Update date
            cityService.updateStartEndDate(startDate, endDate, cityId);
            logger.info("Added HourlyMeasurement for "+cityId+" and updated city.lastUpdate = "+ endDate);

        } catch (IOException e) {
            // Log the error but don't stop the process
            logger.error("Failed to process " + resource.getFilename() + ": " + e.getMessage());
        }
    }
}
