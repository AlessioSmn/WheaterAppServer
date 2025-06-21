package it.unipi.lsmsd.service;

import it.unipi.lsmsd.exception.UnauthorizedException;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.repository.CityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomatingService {

    @Autowired
    ExtremeWeatherEventService extremeWeatherEventService;

    @Autowired
    RedisForecastService redisForecastService;

    @Autowired
    HourlyMeasurementService hourlyMeasurementService;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    UserService userService;


    public void updateMeasurements(String token) throws RuntimeException {
        userService.getAndCheckUserFromToken(token, Role.ADMIN);

        List<City> cities =  cityRepository.findAll();

        // Loop over all cities
        for(City city : cities){
            try {
                hourlyMeasurementService.refreshHourlyMeasurementsAutomaticFromOpenMeteo(city.getId());
            }
            catch (Exception ignored){

            }
        }
    }
    @Async
    public void updateMeasurementsAsync(String token) {
        updateMeasurements(token);
    }

    public void updateExtremeWeatherEvents(String token) throws RuntimeException{
        userService.getAndCheckUserFromToken(token, Role.ADMIN);

        List<City> cities =  cityRepository.findAll();

        // Loop over all cities
        for(City city : cities){
            // Automatic update of the extreme weather event
            extremeWeatherEventService.updateExtremeWeatherEventAutomatic(city.getId());
            System.out.println("EWE Updated: " + city.getId());
        }
    }
    @Async
    public void updateExtremeWeatherEventsAsync(String token) {
        updateExtremeWeatherEvents(token);
    }

    public void updateForecasts(String token) throws RuntimeException{
        userService.getAndCheckUserFromToken(token, Role.ADMIN);

        List<City> cities =  cityRepository.findAll();

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
    @Async
    public void updateForecastsAsync(String token) {
        updateForecasts(token);
    }

}
