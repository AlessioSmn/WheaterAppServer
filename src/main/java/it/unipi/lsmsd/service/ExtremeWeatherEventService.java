package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.ExtremeWeatherEventDTO;
import it.unipi.lsmsd.exception.CityNotFoundException;
import it.unipi.lsmsd.exception.ThresholdsNotPresentException;
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
import java.util.*;
import java.util.stream.Collectors;

import static it.unipi.lsmsd.utility.CityUtility.hasCityAllThresholdsFields;
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

    /**
     * Scans the measurements stored in the database to identify values that exceed predefined thresholds.
     * If such values are found, a new Extreme Weather Event is created for the corresponding category.
     *
     * @param cityId The ID of the city for which measurements should be analyzed.
     * @param startTimeInterval The start time of the interval from which to retrieve measurements.
     * @param endTimeInterval The end time of the interval up to which measurements should be retrieved.
     * @return A list of newly inserted Extreme Weather Events.
     * @throws CityNotFoundException If the specified city is not found.
     * @throws ThresholdsNotPresentException If the specified city doesn't have the threshold specified.
     */
    public List<ExtremeWeatherEvent> updateExtremeWeatherEvent(
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
            throw new CityNotFoundException("Specified city " + cityId + " was not found");


        if(!hasCityAllThresholdsFields(city.get()))
            throw new ThresholdsNotPresentException("City doesn't have all threshold fields correctly specified");

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
        List<ExtremeWeatherEvent> compltedEWEs = new ArrayList<>();

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
                    compltedEWEs.add(insertedEwe);

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
                compltedEWEs.add(insertedEwe);
            }
        }

        // TODO decide if to:
        //  return nothing
        //  return count of found and inserted EWEs
        //  return list of IDs of found and inserted EWEs
        return compltedEWEs;
    }

    // TODO
    //  1) Get the first and last ewe (in terms of the first startTime and the last endTime)
    //      1.1) Attention to endDate not set
    //  2) Call cleanExtremeWeatherEventDuplicates over that interval or more
    public void cleanExtremeWeatherEventDuplicatesAll(
            String cityId
    ) {
        // TODO Get the first and last ewe (in terms of the first startTime and the last endTime), attention to endDate not set
        // TODO call cleanExtremeWeatherEventDuplicates over that interval or more
    }

    public Map<String, Integer> cleanExtremeWeatherEventDuplicates(
            String cityId,
            LocalDateTime startTimeInterval,
            LocalDateTime endTimeInterval
    ) {
        // Counters for removed and inserted EWEs
        int removedCount = 0;
        int insertedCount = 0;

        // loop over all EWE categories
        for (ExtremeWeatherEventCategory eweCategory : ExtremeWeatherEventCategory.values()) {

            // Get all EWE for that category over given time interval, ordered by startTime
            List<ExtremeWeatherEvent> eweList = eweRepository.findByCityIdAndCategoryAndDateStartBetweenOrderByDateStart(
                    cityId, eweCategory, startTimeInterval, endTimeInterval);

            if (eweList.size() <= 1) continue;

            List<ExtremeWeatherEvent> eweToRemove = new ArrayList<>();
            List<ExtremeWeatherEvent> eweToInsert = new ArrayList<>();

            ExtremeWeatherEvent mergeEwe = eweList.get(0);
            int overlappingCounter = 1;

            // Loop over all events
            for (int i = 1; i < eweList.size(); i++) {

                ExtremeWeatherEvent currentEwe = eweList.get(i);

                // If dateStart is null then we have no way to check if overlapping
                if(currentEwe.getDateStart() == null) continue;

                // Check if the current ewe is overlapping with the previous / merge ewe
                // If they are overlapping
                if(areOrderedEwesOverlapping(mergeEwe, currentEwe)) {
                    // Increase the counter
                    overlappingCounter++;

                    // Set the strength to the max of margeEwe's and currentEwe's
                    mergeEwe.setStrength(Math.max(mergeEwe.getStrength(), currentEwe.getStrength()));

                    // If either is set to null then null is set
                    if (mergeEwe.getDateEnd() == null || currentEwe.getDateEnd() == null) {
                        mergeEwe.setDateEnd(null);
                    }
                    // Set the dateEnd to the max of margeEwe's and currentEwe's.
                    else {
                        mergeEwe.setDateEnd(mergeEwe.getDateEnd().isAfter(currentEwe.getDateEnd())
                                ? mergeEwe.getDateEnd()
                                : currentEwe.getDateEnd()
                        );
                    }

                }

                // If they are not overlapping
                else {
                    // We passed some overlapping ewe, they are to be marked as toRemove and the mergeEwe to be inserted
                    if(overlappingCounter != 1) {

                        // Insert all overlapping EWEs into the eweToRemove array
                        for (int j = i - overlappingCounter; j < i; j++)
                            eweToRemove.add(eweList.get(j));

                        // insert mergeEwe into eweToInsert array
                        eweToInsert.add(mergeEwe);

                        // Update counters
                        removedCount += overlappingCounter;
                        insertedCount++;
                    }

                    // Overwrite the mergeEwe
                    mergeEwe = currentEwe;

                    // Reset the counter
                    overlappingCounter = 1;
                }
            }

            // Handle eventual remaining overlapping EWEs
            if(overlappingCounter != 1) {

                // Insert all overlapping EWEs into the eweToRemove array
                for (int j = eweList.size() - overlappingCounter; j < eweList.size(); j++)
                    eweToRemove.add(eweList.get(j));

                // insert mergeEwe into eweToInsert array
                eweToInsert.add(mergeEwe);

                // Update counters
                removedCount += overlappingCounter;
                insertedCount++;
            }

            // Delete all marked EWEs
            eweRepository.deleteAll(eweToRemove);

            // Insert all new merged EWEs
            eweRepository.saveAll(eweToInsert);
        }

        // Return the counts of removed and inserted EWEs in a Map
        Map<String, Integer> result = new HashMap<>();
        result.put("EWEs Removed", removedCount);
        result.put("EWEs Inserted", insertedCount);
        return result;
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

System.out.println("NEW EWE Date start:" + ewe.getDateStart());

        // Set end date only on terminated ewe
        if(terminated){
            ewe.setDateEnd(eweInfo.getDateEnd().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
System.out.println("NEW EWE Date end__:" + ewe.getDateEnd());
        }
        System.out.println("NEW EWE Date end__: null");

        // Set city id
        ewe.setCityId(city.getId());

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

        // Searches the ongoing EWEs in the db
        List<ExtremeWeatherEvent> ongoingLocalEWEs = eweRepository.findByCityIdAndDateEndIsNull(city.getId());

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

    /**
     * Checks if two ExtremeWeatherEvent already ordered by startDate are overlapping in time, by checking EweB endDate against EweA dates.
     * dateEnd set to null is intended as ongoing ExtremeWeatherEvent and treated as such
     *
     * @param EweA First ExtremeWeatherEvent
     * @param EweB Second ExtremeWeatherEvent
     * @return Returns true if the two EWEs are overlapping, false otherwise
     */
    private Boolean areOrderedEwesOverlapping(ExtremeWeatherEvent EweA, ExtremeWeatherEvent EweB) {

        // If EweA is still ongoing then the two EweB are necessarily overlapping
        if(EweA.getDateEnd() == null){
            return true;
        }

        // Given that they're already order by startDate we can check if they are overlapping by
        // checking if EweB startDate is before EweA endDate
        return !EweB.getDateStart().isAfter(EweA.getDateEnd());
    }
}
