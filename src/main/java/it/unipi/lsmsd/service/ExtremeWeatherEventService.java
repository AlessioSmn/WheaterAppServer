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
import java.util.stream.Collectors;

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

    private static final Integer DEFAULT_LOCAL_EWE_RANGE = 0;

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


    public List<String> updateExtremeWeatherEvent(
            String cityId,
            LocalDateTime startTimeInterval,
            LocalDateTime endTimeInterval
    ) throws Exception{

        // Gets all measurements for a given city
        Date startTime = Date.from(startTimeInterval.toInstant(ZoneOffset.UTC));
        Date endTime = Date.from(endTimeInterval.toInstant(ZoneOffset.UTC));
        List<HourlyMeasurement> hourlyMeasurements = hourlyMeasurementRepository.findByCityIdAndTimeBetweenOrderByTimeTimeAsc(cityId, startTime, endTime);

        // TODO log time interval of search and found measurements, at least the number

        // Gets the target city, in order to get the thresholds
        Optional<City> city = cityRepository.findById(cityId);
        if (city.isEmpty())
            throw new IllegalArgumentException("City not found");

        // Retrieve the city's extreme weather event thresholds
        EWEThreshold eweThresholds = city.get().getEweThresholds();

        // Array of cyclically found EWE
        List<Pair<ExtremeWeatherEventCategory, Integer>> foundEWEs;

        // Working array of found and ongoing EWEs
        List<QuadrupleEWEInformationHolder> ongoingExtremeWeatherEvents = new ArrayList<>(getEmptyListOfEWEs());

        // TODO decide if to switch to the updating of ongoing EWE instead of replacing them
        //  We'd need a flag in the quadruple (thus changing the class to a Quintuple) to store
        //  the information on the original and already present record, like eweId = [id|null]
        //  Then the save method has to check if that field is set to a value or to null:
        //      - null: insert like is being done now
        //      - id: update the EWE fields, no new inserts
        //  Obviously the new field eweId has to be set to null after the ewe has been updated,
        //  in case a new ewe of same type is found and later needs to be inserted
        // Ongoing EWE already present in the db
        List<QuadrupleEWEInformationHolder> currentLocalEWEs = getListOfCurrentLocalEWEs(city.get());
        // Putting the currentLocalEWEs into the ongoingExtremeWeatherEvents array
        for (QuadrupleEWEInformationHolder dbEWE : currentLocalEWEs) {
            int index = dbEWE.getCategory().ordinal();
            ongoingExtremeWeatherEvents.set(index, dbEWE);
        }

        // List of completed EWEs, to be returned
        List<String> compltedEWEs = new ArrayList<>();

        // Checks all measurements against all thresholds
        for (HourlyMeasurement measurement : hourlyMeasurements) {

            // Gets the list of current EWE
            foundEWEs = getCurrentEWEs(measurement, eweThresholds);

            // Loop over all possible EWE categories using index,
            // thanks to the fact that the two arrays are always ordinated by category in the same way
            for (int i = 0; i < ExtremeWeatherEventCategory.values().length; i++) {

                // Find the ongoing ewe strength and found ewe strength
                int foundStrength = foundEWEs.get(i).getSecond();
                int ongoingStrength = ongoingExtremeWeatherEvents.get(i).getStrength();

                // Note: strength == 0 means no ewe is found, the measurement value is under the threshold limit

                // Set new startDate if a new EWE is found
                if (foundStrength > 0 &&  ongoingStrength == 0){
                    ongoingExtremeWeatherEvents.get(i).setStrength(foundStrength);
                    ongoingExtremeWeatherEvents.get(i).setDateStart(measurement.getTime());
                    // TODO log the fact that a new EWE is found, so log:
                    //  type
                    //  start date
                    //  strength
                }

                // Update ongoing event strength if a greater strength is detected
                if (foundStrength > ongoingStrength && ongoingStrength > 0) {
                    // Updated the strength with the max value
                    ongoingExtremeWeatherEvents.get(i).setStrength(foundStrength);
                    System.out.println("Updating EWE");
                }

                // If the found strength is 0 but the event was ongoing, it means the event has ended
                if (foundStrength == 0 && ongoingStrength > 0) {

                    // The EWE has terminated, so it can be saved into the repository
                    ongoingExtremeWeatherEvents.get(i).setDateEnd(measurement.getTime());

                    // Create new EWE,
                    ExtremeWeatherEvent newEWE = createNewEWE(city.get(), ongoingExtremeWeatherEvents.get(i), true);

                    // Add it to repos
                    ExtremeWeatherEvent insertedEwe = eweRepository.save(newEWE);

                    // Add its id to the list of completed EWEs
                    compltedEWEs.add(insertedEwe.getId());

                    // Reset the strength to zero and both dates to null
                    ongoingExtremeWeatherEvents.get(i).resetData();

                    // TODO log the fact that an EWE is concluded, so log:
                    //  type
                    //  start date
                    //  end date
                    //  strength
                }
            }
        }

        // Check for still ongoing EWE, save them to DB in the case
        for(QuadrupleEWEInformationHolder ongoingEwe : ongoingExtremeWeatherEvents){

            // The ongoing ewe have a non-null Start Date and a null End Date
            if(ongoingEwe.getDateStart() != null && ongoingEwe.getDateEnd() == null){

                // Create new EWE, without setting the end date field
                ExtremeWeatherEvent newEWE = createNewEWE(city.get(), ongoingEwe, false);

                // Add it to repos
                ExtremeWeatherEvent insertedEwe = eweRepository.save(newEWE);

                // Add its id to the list of completed EWEs
                compltedEWEs.add(insertedEwe.getId());
            }
        }

        // TODO decide if to:
        //  return nothing
        //  return count of found and inserted EWEs
        //  return list of IDs of found and inserted EWEs
        return compltedEWEs;
    }

    /**
     * Utility method to be used by updateExtremeWeatherEvent
     * @param city City model
     * @param eweInfo Information on the EWE to be inserted
     * @param terminated True if the EWE has terminated, thus it has an End Date
     * @return ExtremeWeatherEvent instance
     */
    private ExtremeWeatherEvent createNewEWE(
            City city,
            QuadrupleEWEInformationHolder eweInfo,
            Boolean terminated
    ){
        ExtremeWeatherEvent ewe = new ExtremeWeatherEvent();
        ewe.setCategory(eweInfo.getCategory());
        ewe.setStrength(eweInfo.getStrength());

        // Set start date
        ewe.setDateStart(eweInfo.getDateStart().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());

        // Set end date only on terminated ewe
        if(terminated){
            ewe.setDateEnd(eweInfo.getDateEnd().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        }

        // Default value for radius covering a single city
        // TODO decide on a default value
        // TODO decide on a unit of measurements, here i supposed Kilometers
        ewe.setRadius(DEFAULT_LOCAL_EWE_RANGE);

        // Set longitude and latitude from city attributes
        ewe.setLongitude(city.getLongitude());
        ewe.setLatitude(city.getLatitude());

        return ewe;
    }

    /**
     * Searches for ongoing local Extreme Weather Events (EWEs) in a given city.
     * The locality is assured by searching only for the local radius,
     * The ongoing part is assured by search for record with a null dateEnd
     * @param city City model, the target city
     * @return List<QuadrupleEWEInformationHolder> list of current local EWEs
     */
    public List<QuadrupleEWEInformationHolder> getListOfCurrentLocalEWEs(City city){
        List<QuadrupleEWEInformationHolder> currentLocalEWEs = new ArrayList<>();

        // Searches the ongoing EWE in the db
        List<ExtremeWeatherEvent> ongoingLocalEWEs = eweRepository.findByLongitudeAndLatitudeAndRadiusAndDateEndIsNull(
                city.getLongitude(),
                city.getLatitude(),
                DEFAULT_LOCAL_EWE_RANGE // searches only for EWE in a single city
        );

        // TODO decide if to switch to the updating of ongoing EWE instead of replacing them
        eweRepository.deleteAll(ongoingLocalEWEs);

        // Map each EWE of ongoingLocalEWEs into a QuadrupleEWEInformationHolder
        return ongoingLocalEWEs.stream()
                .map(ewe -> new QuadrupleEWEInformationHolder(
                        ewe.getCategory(),
                        ewe.getStrength(),
                        Date.from(ewe.getDateStart().atZone(ZoneId.systemDefault()).toInstant()),
                        null
                ))
                .collect(Collectors.toList());
    }
}
