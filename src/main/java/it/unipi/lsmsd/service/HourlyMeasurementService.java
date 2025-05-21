package it.unipi.lsmsd.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.unipi.lsmsd.DTO.APIResponseDTO;
import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.utility.CityUtility;
import it.unipi.lsmsd.utility.ISODateUtil;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public void refreshHourlyMeasurementsAutomaticFromOpenMeteo(String cityId) throws JsonProcessingException {
        Optional<City> optionalCity = cityRepository.findById(cityId);
        if(optionalCity.isEmpty()){
            return;
        }
        City city = optionalCity.get();

        LocalDateTime lastMeasurementUpdate = city.getLastMeasurementUpdate();
        LocalDateTime now = LocalDateTime.now();

        // Validate the CityDTO values
        APIResponseDTO responseDTO = dataHarvestService.getCityHistoricalMeasurement(
                city.getLatitude(),
                city.getLongitude(),
                lastMeasurementUpdate.toLocalDate().toString(),
                now.toLocalDate().toString()
        );

        HourlyMeasurementDTO hourlyMeasurementDTO = responseDTO.getHourly();

        hourlyMeasurementDTO.setCityId(cityId);
        // Save the data in MongoDB
        saveHourlyMeasurements(hourlyMeasurementDTO);

        // Update the last updated date
        city.setLastMeasurementUpdate(now.minusDays(1));
    }

    // TODO : Throw specific error type for every exception
    // Error Type	            HTTP Status Code	        Action
    // Duplicate Key Error	    409 Conflict	            Inform user of data conflict
    // Validation Error	        400 Bad                     Request	User corrects input
    // Network/Timeout Error	504 Gateway                 Timeout	Retry logic or inform user
    // Write Concern Failure	503 Service                 Unavailable	Retry or alert admin
    // Unexpected Error	        500 Internal Server Error	Log & alert for investigation

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

    // Get all the measurements with city name
    public HourlyMeasurementDTO getHourlyMeasurements(CityDTO cityDTO) {
        String cityId = CityUtility.generateCityId(cityDTO.getName(), cityDTO.getRegion() , cityDTO.getLatitude(), cityDTO.getLongitude());
        // // Convert date from String to ISO Date
        // LocalDate localDate = LocalDate.parse(cityDTO.getStartDate());
        // LocalDateTime localDateTime = localDate.atStartOfDay();
        // ZonedDateTime localZonedDateTime = localDateTime.atZone(localZone);
        // Instant utcInstant = localZonedDateTime.toInstant();

        Date startDate = ISODateUtil.getISODate(cityDTO.getStartDate()+"T00:00");
        Date endDate = ISODateUtil.getISODate(cityDTO.getEndDate()+"T23:00");
        
        List<HourlyMeasurement> measurements = hourlyMeasurementRepository.findByCityIdAndTimeBetweenOrderByTimeTimeAsc(cityId, startDate, endDate);
        // Map List<HourlyMeasurement> to HourlyMeasurementDTO
        HourlyMeasurementDTO hourlyMeasurementDTO = Mapper.mapHourlyMeasurementDTO(measurements);
        return hourlyMeasurementDTO;
    }

    public void deleteHourlyMeasurements(String cityId, Date startDate, Date endDate){
        hourlyMeasurementRepository.deleteByCityIdAndTimeBetween(cityId, startDate, endDate);
    }
}


