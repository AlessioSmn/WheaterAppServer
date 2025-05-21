package it.unipi.lsmsd.service;

import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomatingService {
    /**
     * @Title cron Explanation:
     * @brief A cron expression is a string composed of six or seven fields (depending on the system),
     * which define a schedule for executing tasks in a time-based manner.
     * In Spring (and many Unix-like systems), the standard format includes six fields:
     * @Format {@code second minute hour day-of-month month day-of-week}
     * @Fields
     * <ul>
     *   <li><b>Second</b> (0–59): Defines the second of execution.</li>
     *   <li><b>Minute</b> (0–59): Defines the minute of execution.</li>
     *   <li><b>Hour</b> (0–23): Defines the hour of execution (24-hour format).</li>
     *   <li><b>Day of Month</b> (1–31): Defines the day of the month.</li>
     *   <li><b>Month</b> (1–12 or JAN–DEC): Defines the month.</li>
     *   <li><b>Day of Week</b> (0–7 or SUN–SAT): Defines the day of the week (0 and 7 both represent Sunday).</li>
     * </ul>
     * @SpecialCharacters
     * <ul>
     *   <li><b>*</b>: Every possible value for the field.</li>
     *   <li><b>?</b>: No specific value (used when either day-of-month or day-of-week is not specified).</li>
     *   <li><b>-</b>: Specifies a range (e.g., 1-5).</li>
     *   <li><b>,</b>: Specifies a list of values (e.g., MON,WED,FRI).</li>
     *   <li><b>/</b>: Specifies increments (e.g., [asterisc]/15 for every 15 units).</li>
     *   <li><b>L</b>: Refers to the "last" applicable value (e.g., last day of month or last Friday).</li>
     *   <li><b>#</b>: Specifies the nth occurrence of a weekday in a month (e.g., 2#1 is the first Monday).</li>
     * </ul>
     * @Example
     * <pre>
     * "0 0 2 * * *" → Executes daily at 2:00 AM.
     * </pre>
     */



    @Autowired
    ExtremeWeatherEventService extremeWeatherEventService;

    @Autowired
    RedisForecastService redisForecastService;

    @Autowired
    HourlyMeasurementService hourlyMeasurementService;

    @Autowired
    CityRepository cityRepository;

    private List<City> cities;

    // Every day at 03:00 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void minuteUpdate(){

        getListOfCities();

        updateMeasurements();

        updateExtremeWeatherEvents();

        update24HForecasts();
    }

    private void getListOfCities(){
        cities = cityRepository.findAll();
    }

    private void updateMeasurements(){
        // Loop over all cities
        for(City city : cities){
            try {
                hourlyMeasurementService.refreshHourlyMeasurementsAutomaticFromOpenMeteo(city.getId());
            }
            catch (Exception ignored){

            }
        }
    }

    private void updateExtremeWeatherEvents(){
        // Loop over all cities
        for(City city : cities){
            // Automatic update of the extreme weather event
            extremeWeatherEventService.updateExtremeWeatherEventAutomatic(city.getId());
        }
    }

    private void update24HForecasts(){
        // Empty redis
        redisForecastService.deleteAllForecast();

        // Loop over all cities
        for(City city : cities){
            try{
                redisForecastService.refreshForecastAutomaticFromOpenMeteo(city.getId());
            }
            catch (Exception ignored){

            }
        }
    }
}
