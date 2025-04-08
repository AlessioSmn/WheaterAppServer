package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.exception.CityException;
import it.unipi.lsmsd.exception.CityNotFoundException;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.model.Role;
import it.unipi.lsmsd.service.UserService;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserService userService;

    // Get City info with City Name
    public CityDTO getCity(String cityName) throws NoSuchElementException {
        Optional<City> city = cityRepository.findByName(cityName);
        // throws NoSuchElementException is no city is found
        if (!city.isPresent()) { throw new NoSuchElementException("City not found with name: " + cityName ); }
        return  Mapper.mapCity(city.get()); 
    }

    // Get City info with City Id
    public CityDTO getCityWithID(String cityID) throws NoSuchElementException {
        Optional<City> city = cityRepository.findById(cityID);
        // throws NoSuchElementException is no city is found
        if (!city.isPresent()) { throw new NoSuchElementException("City not found with id: " + cityID); }
        return  Mapper.mapCity(city.get());
    }

    // Saves the city to the DB and returns the cityID
    // Alert!!! : Throws DuplicateKeyException -> Need to handle it by the class that calls this method
    public String saveCity(CityDTO cityDTO, String token) throws DuplicateKeyException{
        // Check if the user's role is ADMIN
        userService.getAndCheckUserFromToken(token, Role.ADMIN);
        // Map the DTO and get the city
        City city = Mapper.mapCity(cityDTO);
        // Insert to the DB
        // NOTE: Attempt to "insert" a document with an existing id throws DuplicateKeyException
        cityRepository.insert(city);
        return city.getId();
    }

    // Saves the city to the DB and returns the cityID
    // Alert!!! : Throws DuplicateKeyException -> Need to handle it by the class that calls this method
    public String saveCityWithThresholds(CityDTO cityDTO, String token) throws DuplicateKeyException{
        // Check if the user's role is ADMIN
        userService.getAndCheckUserFromToken(token, Role.ADMIN);
        // Map the DTO and get the city
        City city = Mapper.mapCityWithThresholds(cityDTO);
        // Insert to the DB
        // NOTE: Attempt to "insert" a document with an existing id throws DuplicateKeyException
        cityRepository.insert(city);
        return city.getId();
    }


    /**
     * Updates the city thresholds
     * @param cityDTO CityDTO with the new thresholds included
     * @throws CityNotFoundException on city not found
     * @throws CityException on city not identifiable
     */
    public void updateCityThresholds(CityDTO cityDTO, String token) throws CityException {
        // Check if the user's role is ADMIN
        userService.getAndCheckUserFromToken(token, Role.ADMIN);

        // Check if the DTO has the necessary fields for city identification
        if(!cityDTO.hasIdFields()){
            throw new CityException("City id fields not provided: name, region, longitude, latitude");
        }

        // Maps to the city model
        City city = Mapper.mapCityWithThresholds(cityDTO);

        // Gets the city
        Optional<City> cityOpt = cityRepository.findById(city.getId());

        if(cityOpt.isEmpty()){
            throw new CityNotFoundException("City not found, add the city before updating");
        }

        // If the city is already present it updates the EweThresholds field
        City existingCity = cityOpt.get();
        existingCity.setEweThresholds(cityDTO.getEweThresholds());
        cityRepository.save(existingCity);
    }

    // Return the list of cities belonging to a given region
    public List<CityDTO> getCityByRegion(String region) {
        List<City> cities = cityRepository.findByRegion(region);

        // Convert the list to List<CityDTO>
        return cities.stream()
                .map(Mapper::mapCity)
                .collect(Collectors.toList());
    }

    public LocalDateTime getLastEweUpdateById(String cityId) throws IllegalArgumentException{
        Optional<City> city = cityRepository.findById(cityId);

        if(city.isEmpty()){
            throw new IllegalArgumentException("City not found");
        }

        return city.get().getLastEweUpdate();
    }

    public void setLastEweUpdateById(String cityId, LocalDateTime newLastEweUpdate) throws IllegalArgumentException{
        Optional<City> city = cityRepository.findById(cityId);

        if(city.isEmpty()){
            throw new IllegalArgumentException("City not found");
        }

        city.get().setLastEweUpdate(newLastEweUpdate);
    }
}
