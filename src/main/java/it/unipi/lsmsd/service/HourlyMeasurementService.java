package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.DTO.HourlyMeasurementDTO;
import it.unipi.lsmsd.model.HourlyMeasurement;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.utility.CityUtility;
import it.unipi.lsmsd.utility.ISODateUtil;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

// Save the Weather Data to Mongo DB
@Service
public class HourlyMeasurementService {

    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;

    // TODO : Throw specific error type for every exception
    // Error Type	            HTTP Status Code	        Action
    // Duplicate Key Error	    409 Conflict	            Inform user of data conflict
    // Validation Error	        400 Bad                     Request	User corrects input
    // Network/Timeout Error	504 Gateway                 Timeout	Retry logic or inform user
    // Write Concern Failure	503 Service                 Unavailable	Retry or alert admin
    // Unexpected Error	        500 Internal Server Error	Log & alert for investigation

    // Saves the list of hourlyMeasurement of the given city to the DB in Time-Series Collection "hourly_measurements" 
    public void saveHourlyMeasurements( HourlyMeasurementDTO hourlyMeasurementDTO) {
        
        // Extract list of hourly data
        List<HourlyMeasurement> measurements = Mapper.mapHourlyMeasurement(hourlyMeasurementDTO);
        // Insert the list in DB
        hourlyMeasurementRepository.insert(measurements);
    }

    // Get all the measurements with city name
    public HourlyMeasurementDTO getHourlyMeasurements(CityDTO cityDTO) {
        String cityId = CityUtility.generateCityId(cityDTO.getName(), cityDTO.getRegion() , cityDTO.getLatitude(), cityDTO.getLongitude());
        // Convert date from String to ISO Date
        Date startDate = ISODateUtil.getISODate(cityDTO.getStartDate()+"T00:00");
        Date endDate = ISODateUtil.getISODate(cityDTO.getEndDate()+"T23:00");
        
        List<HourlyMeasurement> measurements = hourlyMeasurementRepository.findByCityIdAndTimeBetweenOrderByTimeTimeAsc(cityId, startDate, endDate);
        // Map List<HourlyMeasurement> to HourlyMeasurementDTO
        HourlyMeasurementDTO hourlyMeasurementDTO = Mapper.mapHourlyMeasurementDTO(measurements);
        return hourlyMeasurementDTO;
    }

}


