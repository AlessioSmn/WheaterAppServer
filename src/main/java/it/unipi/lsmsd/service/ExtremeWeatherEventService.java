package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.ExtremeWeatherEventDTO;
import it.unipi.lsmsd.model.*;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.repository.ExtremeWeatherEventRepository;
import it.unipi.lsmsd.repository.HourlyMeasurementRepository;
import it.unipi.lsmsd.utility.QuadrupleEWEInformationHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static it.unipi.lsmsd.utility.EWEUtility.getCurrentEWEs;
import static it.unipi.lsmsd.utility.EWEUtility.getEmptyListOfEWEs;

@Service
public class ExtremeWeatherEventService {

    @Autowired
    private ExtremeWeatherEventRepository eweRepository;
    @Autowired
    private HourlyMeasurementRepository hourlyMeasurementRepository;
    @Autowired
    private CityRepository cityRepository;

    // Insert new EWE
    public String addNewEWE(ExtremeWeatherEventDTO eweDTO) throws Exception{
        try{
            if (eweDTO == null)
                throw new IllegalArgumentException("ExtremeWeatherEvent is null: Check if request parameters are correct.");

            if (eweDTO.getCategory() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'category'.");

            if (eweDTO.getDateStart() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'dateStart'.");

            if (eweDTO.getDateEnd() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'dateEnd'.");

            if (eweDTO.getLongitude() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'longitude'.");

            if (eweDTO.getLatitude() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'latitude'.");

            if (eweDTO.getStrength() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'strength'.");

            if (eweDTO.getRadius() == null)
                throw new IllegalArgumentException("Invalid ExtremeWeatherEvent: Missing required field 'radius'.");

            ExtremeWeatherEvent eweEvent = new ExtremeWeatherEvent(eweDTO);

            eweRepository.save(eweEvent);
            return "Extreme Weather Event added successfully";
        }
        catch(Exception e){
            throw e;
        }
    }

    // TODO
    //  all logic of search of ewe
    //  Decide if to return all updated EWE or maybe just a counter
    public Integer updateExtremeWeatherEvent(String cityId, LocalDateTime startTimeInterval, LocalDateTime endTimeInterval) throws Exception{

        // Gets all measurements for a given city
        Date startTime = Date.from(startTimeInterval.toInstant(ZoneOffset.UTC));
        Date endTime = Date.from(endTimeInterval.toInstant(ZoneOffset.UTC));
        List<HourlyMeasurement> hourlyMeasurements = hourlyMeasurementRepository.findByCityIdAndTimeBetween(cityId, startTime, endTime);

        // Gets the target city, in order to get the thresholds
        Optional<City> city = cityRepository.findById(cityId);
        if (city.isEmpty())
            throw new IllegalArgumentException("City not found");

        // Retrieve the city's extreme weather event thresholds
        EWEThreshold eweThresholds = city.get().getEweThresholds();

        // Array of cyclically found EWE
        List<Pair<ExtremeWeatherEventCategory, Integer>> foundEWEs = new ArrayList<>(5);

        // Array of ongoing EWE
        List<QuadrupleEWEInformationHolder> ongoingExtremeWeatherEvents = new ArrayList<>(getEmptyListOfEWEs());

        // Checks all measurements against all thresholds
        for (HourlyMeasurement measurement : hourlyMeasurements) {

            // Gets the list of current EWE
            foundEWEs = getCurrentEWEs(measurement, eweThresholds);

            // Loop over all possible EWE categories using index, thanks to the fact that the two arrays are always ordinated by category in the same way
            for (int i = 0; i < ExtremeWeatherEventCategory.values().length; i++) {

                ExtremeWeatherEventCategory eweCategory = ExtremeWeatherEventCategory.values()[i];

                int foundStrength = foundEWEs.get(i).getSecond();
                int ongoingStrength = ongoingExtremeWeatherEvents.get(i).getStrength();

                // Set new startDate if a new EWE is found
                if (foundStrength > 0 &&  ongoingStrength == 0){
                    ongoingExtremeWeatherEvents.get(i).setStrength(foundStrength);
                    ongoingExtremeWeatherEvents.get(i).setDateStart(measurement.getTime());
                }

                // Update ongoing event strength if a greater strength is detected
                if (foundStrength > ongoingStrength && ongoingStrength > 0) {
                    // Updated the strength with the max value
                    ongoingExtremeWeatherEvents.get(i).setStrength(foundStrength);
                }

                // If the found strength is 0 but the event was ongoing, it means the event has ended
                if (foundStrength == 0 && ongoingStrength > 0) {

                    // The EWE has terminated, so it can be saved into the repository
                    ongoingExtremeWeatherEvents.get(i).setDateEnd(measurement.getTime());

                    // Create new EWE,
                    ExtremeWeatherEvent newEWE = createNewEWE(city.get(), ongoingExtremeWeatherEvents.get(i));

                    // add it to repos
                    eweRepository.save(newEWE);

                    // Reset the strength to zero
                    ongoingExtremeWeatherEvents.get(i).setStrength(0);
                }
            }
        }

        // Check for still ongoing EWE, save them to DB in the case
        // TODO insert all possible still ongoing EWE

        // TODO decide if to:
        //  return nothing
        //  return count of found and inserted EWEs
        //  return list of IDs of found and inserted EWEs
        return 0;
    }

    // Utility method to be used by updateExtremeWeatherEvent
    private ExtremeWeatherEvent createNewEWE(
            City city,
            QuadrupleEWEInformationHolder eweInfo
    ){
        ExtremeWeatherEvent ewe = new ExtremeWeatherEvent();
        ewe.setCategory(eweInfo.getCategory());
        ewe.setStrength(eweInfo.getStrength());

        // Set start and end date
        ewe.setDateStart(eweInfo.getDateStart().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        ewe.setDateEnd(eweInfo.getDateEnd().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());

        // Default value for radius covering a single city
        // TODO decide on a default value
        // TODO decide on a unit of measurements, here i supposed Kilometers
        ewe.setRadius(2);

        // Set longitude and latitude from city attributes
        ewe.setLongitude(city.getLongitude());
        ewe.setLatitude(city.getLatitude());

        return ewe;
    }
}
