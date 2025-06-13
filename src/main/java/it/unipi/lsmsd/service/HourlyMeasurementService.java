package it.unipi.lsmsd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.exception.CityNotFoundException;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

// Save the Weather Data to Mongo DB
@Service
public class HourlyMeasurementService {
    @Autowired
    private DataHarvestService dataHarvestService;

    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CityService cityService;

    /**
     * Retrieves and stores all available hourly weather measurements for the specified city
     * starting from the day after its last recorded update until yesterday, using the Open-Meteo API.
     * <p>
     * If the city does not exist in the database, the operation is aborted silently.
     * Upon successful retrieval, the new measurements are saved in MongoDB and the city's
     * {@code lastMeasurementUpdate} field is updated to the last date retrieved.
     *
     * @param cityId the unique identifier of the city for which measurements are to be refreshed
     * @throws JsonProcessingException if an error occurs while parsing the Open-Meteo API response
     */
    public void refreshHourlyMeasurementsAutomaticFromOpenMeteo(String cityId) throws JsonProcessingException {
        Optional<City> optionalCity = cityRepository.findById(cityId);
        if(optionalCity.isEmpty()){
            return;
        }
        City city = optionalCity.get();

        LocalDateTime lastMeasurementUpdate = city.getLastMeasurementUpdate();
        // The day after
        LocalDate lastMeasurementUpdate_date = lastMeasurementUpdate.toLocalDate().plusDays(1);
        // Up until yesterday
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Validate the CityDTO values
        APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(
                city.getLatitude(),
                city.getLongitude(),
                lastMeasurementUpdate_date.toString(),
                yesterday.toString()
        );

        HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();

        hourlyMeasurementDTO.setCityId(cityId);
        // Save the data in MongoDB
        saveHourlyMeasurements(hourlyMeasurementDTO);

        cityService.setLastMeasurementUpdateById(cityId, yesterday.atTime(23, 0));
    }

    /**
     * Retrieves and stores the last seven full days (from eight days ago to yesterday)
     * of hourly weather measurements for the specified city using the Open-Meteo API.
     * <p>
     * If the city is not found, a {@link CityNotFoundException} is thrown.
     * The retrieved measurements are saved in MongoDB, and the method returns the date
     * of the latest measurement stored, intended to be used as {@code lastMeasurementUpdate}.
     *
     * @param cityId the unique identifier of the city for which measurements are to be retrieved
     * @return the {@link LocalDate} corresponding to yesterday, i.e., the last day included in the update
     * @throws JsonProcessingException if an error occurs while parsing the Open-Meteo API response
     * @throws CityNotFoundException if the city with the given ID does not exist
     */
    public void refreshHourlyMeasurementsLastweekFromOpenMeteo(String cityId) throws JsonProcessingException {
        Optional<City> optionalCity = cityRepository.findById(cityId);
        if(optionalCity.isEmpty()){
            throw new CityNotFoundException("City with id=" + cityId +" not found");
        }
        City city = optionalCity.get();

        // The day after
        LocalDate eightDaysAgo = LocalDate.now().minusDays(8);
        // Up until yesterday
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Validate the CityDTO values
        APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(
                city.getLatitude(),
                city.getLongitude(),
                eightDaysAgo.toString(),
                yesterday.toString()
        );

        HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();

        hourlyMeasurementDTO.setCityId(cityId);
        // Save the data in MongoDB
        saveHourlyMeasurements(hourlyMeasurementDTO);

        cityService.setLastMeasurementUpdateById(cityId, yesterday.atTime(23, 0));
    }

    // Saves the list of hourlyMeasurement of the given city to the DB in Time-Series Collection "hourly_measurements" 
    public void saveHourlyMeasurements( HourlyMeasurementDTO hourlyMeasurementDTO) {
        List<HourlyMeasurement> measurements = Mapper.mapHourlyMeasurement(hourlyMeasurementDTO);
        int batchSize = 10000;

        if(measurements.size()<= batchSize){         
            hourlyMeasurementRepository.insert(measurements);
            return;
        }
        
        for (int i = 0; i < measurements.size(); i += batchSize) {
            int end = Math.min(i + batchSize, measurements.size());
            List<HourlyMeasurement> batch = measurements.subList(i, end);
            hourlyMeasurementRepository.insert(batch);
        }
    }

    public void deleteHourlyMeasurements(String cityId, Date startDate, Date endDate){
        hourlyMeasurementRepository.deleteByCityIdAndTimeBetween(cityId, startDate, endDate);
    }
}


