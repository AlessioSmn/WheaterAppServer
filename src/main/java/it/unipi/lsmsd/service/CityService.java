package it.unipi.lsmsd.service;

import it.unipi.lsmsd.DTO.CityDTO;
import it.unipi.lsmsd.model.City;
import it.unipi.lsmsd.repository.CityRepository;
import it.unipi.lsmsd.utility.Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

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
    public String saveCity(CityDTO cityDTO) throws DuplicateKeyException{
        // Map the DTO and get the city
        City city = Mapper.mapCity(cityDTO);
        // Insert to the DB
        // NOTE: Attempt to "insert" a document with an existing id throws DuplicateKeyException
        cityRepository.insert(city);
        return city.getId();
    }

    // Saves the city to the DB and returns the cityID
    // Alert!!! : Throws DuplicateKeyException -> Need to handle it by the class that calls this method
    public String saveCityWithThresholds(CityDTO cityDTO) throws DuplicateKeyException{
        // Map the DTO and get the city
        City city = Mapper.mapCityWithThresholds(cityDTO);
        // Insert to the DB
        // NOTE: Attempt to "insert" a document with an existing id throws DuplicateKeyException
        cityRepository.insert(city);
        return city.getId();
    }

    // Return the list of cities belonging to a given region
    public List<CityDTO> getCityByRegion(String region) {
        List<City> cities = cityRepository.findByRegion(region);

        // Convert the list to List<CityDTO>
        return cities.stream()
                .map(Mapper::mapCity)
                .collect(Collectors.toList());
    }
}
