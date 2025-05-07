package it.unipi.lsmsd.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.repository.CityRepository;

@Service
public class DataRefreshService {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CityService cityService;

    /*

     * Add 
     *  Get the lis
     *  For each city get the city.endDate and compare with current date
     * Get Historical Data with the date range
     * Delete the 
     */
    public void refreshHistoricalMeasurement(){

        // Get the list of cities from the DB

        // For each city Add Fresh Data
        String cityId = "pis-tus-43.7085-10.4036";
        // Get the city
        CityDTO cityDTO = cityService.getCityWithID(cityId);



    }
}
